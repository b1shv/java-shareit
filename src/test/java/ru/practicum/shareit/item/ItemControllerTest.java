package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@Import({ItemMapper.class, BookingMapper.class, UserMapper.class, CommentMapper.class})
class ItemControllerTest {
    @MockBean
    ItemService itemService;

    @MockBean
    BookingService bookingService;

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private final Item item1 = Item.builder()
            .id(1L)
            .ownerId(1)
            .name("item 1")
            .description("text")
            .available(true)
            .build();
    private final Item item2 = Item.builder()
            .id(2L)
            .ownerId(1)
            .name("item 1")
            .description("text")
            .available(false)
            .build();
    private final User user = User.builder()
            .id(2)
            .name("User")
            .email("user@email.com")
            .build();
    private final Booking lastBooking = Booking.builder()
            .id(4L)
            .booker(user)
            .item(item1)
            .start(LocalDateTime.of(2023, 3, 1, 12, 0))
            .end(LocalDateTime.of(2023, 3, 5, 12, 0))
            .status(BookingStatus.APPROVED)
            .build();
    private final Booking nextBooking = Booking.builder()
            .id(12L)
            .booker(user)
            .item(item1)
            .start(LocalDateTime.of(2023, 10, 1, 12, 0))
            .end(LocalDateTime.of(2023, 10, 5, 12, 0))
            .status(BookingStatus.APPROVED)
            .build();
    private final Comment comment1 = Comment.builder()
            .id(111)
            .author(user)
            .item(item1)
            .text("text")
            .created(LocalDateTime.of(2023, 3, 5, 12, 0))
            .build();
    private final Comment comment2 = Comment.builder()
            .id(222)
            .author(user)
            .item(item1)
            .text("text")
            .created(LocalDateTime.of(2023, 3, 5, 12, 0))
            .build();
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;

    @Test
    void getAll_shouldReturnOwnersItemsWithLastAndNextBookingsAndComments() throws Exception {
        when(itemService.getItemsByOwnerId(1, DEFAULT_FROM, DEFAULT_SIZE)).thenReturn(List.of(item1, item2));
        when(bookingService.getLastItemBooking(item1.getId())).thenReturn(lastBooking);
        when(bookingService.getNextItemBooking(item1.getId())).thenReturn(nextBooking);
        when(itemService.getComments(item1.getId())).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(item1.getId()))
                .andExpect(jsonPath("$.[0].lastBooking.id").value(lastBooking.getId()))
                .andExpect(jsonPath("$.[0].nextBooking.id").value(nextBooking.getId()))
                .andExpect(jsonPath("$.[0].comments[0].id").value(comment1.getId()))
                .andExpect(jsonPath("$.[0].comments[1].id").value(comment2.getId()))
                .andExpect(jsonPath("$.[1].id").value(item2.getId()));
    }

    @Test
    void getAll_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-4";
        String wrongSize = "0";

        mockMvc.perform(get("/items")
                        .param("from", wrongFrom)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .param("size", wrongSize)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getItemsByOwnerId(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAll_shouldReturnNotFound_ifUserNotFound() throws Exception {
        when(itemService.getItemsByOwnerId(anyLong(), anyInt(), anyInt())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_ShouldReturnItemWithBookings_ifUserIsOwner() throws Exception {
        when(itemService.getItemById(item1.getId())).thenReturn(item1);
        when(bookingService.getLastItemBooking(item1.getId())).thenReturn(lastBooking);
        when(bookingService.getNextItemBooking(item1.getId())).thenReturn(nextBooking);
        when(itemService.getComments(item1.getId())).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/items/{id}", item1.getId())
                        .header(USER_ID_HEADER, item1.getOwnerId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()))
                .andExpect(jsonPath("$.lastBooking.id").value(lastBooking.getId()))
                .andExpect(jsonPath("$.nextBooking.id").value(nextBooking.getId()))
                .andExpect(jsonPath("$.comments").isNotEmpty());
    }

    @Test
    void getById_ShouldReturnItemWithoutBookings_ifUserIsNotOwner() throws Exception {
        int randomUserId = 113;
        when(itemService.getItemById(item1.getId())).thenReturn(item1);
        when(bookingService.getLastItemBooking(item1.getId())).thenReturn(lastBooking);
        when(bookingService.getNextItemBooking(item1.getId())).thenReturn(nextBooking);
        when(itemService.getComments(item1.getId())).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/items/{id}", item1.getId())
                        .header(USER_ID_HEADER, randomUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()))
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isNotEmpty());

        verify(bookingService, never()).getLastItemBooking(anyLong());
        verify(bookingService, never()).getNextItemBooking(anyLong());
    }

    @Test
    void getById_ShouldReturnNotFound_ifItemNotFound() throws Exception {
        when(itemService.getItemById(anyLong())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/item{id}", 1)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnItems() throws Exception {
        when(itemService.searchText(anyString(), anyInt(), anyInt())).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .param("text", "random text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(item1.getId()))
                .andExpect(jsonPath("$.[1].id").value(item2.getId()));
    }

    @Test
    void search_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-1";
        String wrongSize = "-5";

        mockMvc.perform(get("/items/search")
                        .param("from", wrongFrom)
                        .param("text", "random text"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/search")
                        .param("size", wrongSize)
                        .param("text", "random text"))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).searchText(anyString(), anyInt(), anyInt());
    }

    @Test
    void add_shouldReturnOk_ifValidationPassedAndUserFound() throws Exception {
        ItemDto item1Dto = ItemDto.builder()
                .name(item1.getName())
                .description(item1.getDescription())
                .available(item1.getAvailable())
                .build();
        when(itemService.addItem(any(Item.class))).thenReturn(item1);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, item1.getOwnerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1Dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()))
                .andExpect(jsonPath("$.name").value(item1.getName()))
                .andExpect(jsonPath("$.description").value(item1.getDescription()))
                .andExpect(jsonPath("$.available").value(item1.getAvailable()));
    }

    @Test
    void add_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        ItemDto itemDtoNoName = ItemDto.builder()
                .description("text")
                .available(true)
                .build();
        ItemDto itemDtoNoDescription = ItemDto.builder()
                .name("name")
                .available(true)
                .build();
        ItemDto itemDtoNoAvailable = ItemDto.builder()
                .name("name")
                .description("text")
                .build();

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoNoName)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoNoDescription)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDtoNoAvailable)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addItem(any(Item.class));
    }

    @Test
    void add_shouldReturnNotFound_ifUserNotFound() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("text")
                .available(true)
                .build();
        when(itemService.addItem(any(Item.class))).thenThrow(new NotFoundException());

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnOk_ifItemFound() throws Exception {
        long randomItemId = 14;
        ItemDto itemDto = ItemDto.builder().name("name").build();
        when(itemService.updateItem(any(Item.class))).thenReturn(item1);

        mockMvc.perform(patch("/items/{id}", randomItemId)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()));
    }

    @Test
    void update_shouldReturnNotFound_ifItemNotFound() throws Exception {
        long randomItemId = 14;
        ItemDto itemDto = ItemDto.builder().name("name").build();
        when(itemService.updateItem(any(Item.class))).thenThrow(new NotFoundException());

        mockMvc.perform(patch("/items/{id}", randomItemId)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_shouldReturnOk_ifAuthorAndItemFoundAndValidationPassed() throws Exception {
        int randomCommentId = 26;
        CommentDto commentDto = CommentDto.builder().text("text text text").build();
        when(itemService.getItemById(item1.getId())).thenReturn(item1);
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.addComment(any(Comment.class))).thenAnswer(InvocationOnMock -> {
            Comment comment = InvocationOnMock.getArgument(0, Comment.class);
            comment.setId(randomCommentId);
            return comment;
        });

        mockMvc.perform(post("/items/{itemId}/comment", item1.getId())
                        .header(USER_ID_HEADER, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomCommentId))
                .andExpect(jsonPath("$.authorName").value(user.getName()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }

    @Test
    void addComment_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        CommentDto commentDto = CommentDto.builder().build();

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).getItemById(anyLong());
        verify(userService, never()).getUserById(anyLong());
        verify(itemService, never()).addComment(any(Comment.class));
    }

    @Test
    void addComment_shouldReturnNotFound_ifAuthorOrItemNotFound() throws Exception {
        int wrongId = 1;
        int correctId = 2;

        CommentDto commentDto = CommentDto.builder().text("text").build();
        when(itemService.getItemById(wrongId)).thenThrow(new NotFoundException());
        when(userService.getUserById(wrongId)).thenThrow(new NotFoundException());

        mockMvc.perform(post("/items/{itemId}/comment", wrongId)
                        .header(USER_ID_HEADER, correctId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/items/{itemId}/comment", correctId)
                        .header(USER_ID_HEADER, wrongId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound());

        verify(itemService, never()).addComment(any(Comment.class));
    }
}