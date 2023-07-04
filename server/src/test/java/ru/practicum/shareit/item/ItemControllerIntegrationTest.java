package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@Import({ItemMapper.class, BookingMapper.class, UserMapper.class, CommentMapper.class})
class ItemControllerIntegrationTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

    @MockBean
    private ItemService itemService;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_shouldReturnOwnersItemsWithLastAndNextBookingsAndComments() throws Exception {
        Item item1 = item1();
        Item item2 = item2();
        User user = user();
        Booking lastBooking = lastBooking(user, item1);
        Booking nextBooking = nextBooking(user, item1);
        Comment comment1 = comment1(user, item1);
        Comment comment2 = comment2(user, item1);

        when(itemService.getItemsByOwnerId(1, DEFAULT_PAGEABLE)).thenReturn(List.of(item1, item2));
        when(bookingService.getLastItemBooking(item1.getId())).thenReturn(lastBooking);
        when(bookingService.getNextItemBooking(item1.getId())).thenReturn(nextBooking);
        when(itemService.getComments(item1.getId())).thenReturn(List.of(comment1, comment2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(item1.getId()))
                .andExpect(jsonPath("$.[0].lastBooking.id").value(lastBooking.getId()))
                .andExpect(jsonPath("$.[0].nextBooking.id").value(nextBooking.getId()))
                .andExpect(jsonPath("$.[0].comments[0].id").value(comment1.getId()))
                .andExpect(jsonPath("$.[0].comments[1].id").value(comment2.getId()))
                .andExpect(jsonPath("$.[1].id").value(item2.getId()));

        verify(itemService, times(1)).getItemsByOwnerId(1, DEFAULT_PAGEABLE);
        verify(itemService, times(1)).getComments(item1.getId());
        verify(bookingService, times(1)).getLastItemBooking(item1.getId());
        verify(bookingService, times(1)).getNextItemBooking(item1.getId());
    }

    @Test
    void getAll_shouldReturnNotFound_ifUserNotFound() throws Exception {
        when(itemService.getItemsByOwnerId(1, DEFAULT_PAGEABLE)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemsByOwnerId(1, DEFAULT_PAGEABLE);
    }

    @Test
    void getById_ShouldReturnItemWithBookings_ifUserIsOwner() throws Exception {
        Item item1 = item1();
        User user = user();
        Booking lastBooking = lastBooking(user, item1);
        Booking nextBooking = nextBooking(user, item1);
        Comment comment1 = comment1(user, item1);
        Comment comment2 = comment2(user, item1);

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

        verify(itemService, times(1)).getItemById(item1.getId());
        verify(itemService, times(1)).getComments(item1.getId());
        verify(bookingService, times(1)).getLastItemBooking(item1.getId());
        verify(bookingService, times(1)).getNextItemBooking(item1.getId());
    }

    @Test
    void getById_ShouldReturnItemWithoutBookings_ifUserIsNotOwner() throws Exception {
        Item item1 = item1();
        User user = user();
        Booking lastBooking = lastBooking(user, item1);
        Booking nextBooking = nextBooking(user, item1);
        Comment comment1 = comment1(user, item1);
        Comment comment2 = comment2(user, item1);

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

        verify(itemService, times(1)).getItemById(item1.getId());
        verify(itemService, times(1)).getComments(item1.getId());
        verifyNoInteractions(bookingService);
    }

    @Test
    void getById_ShouldReturnNotFound_ifItemNotFound() throws Exception {
        when(itemService.getItemById(1)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/items/{id}", 1)
                        .header(USER_ID_HEADER, 1))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItemById(1);
    }

    @Test
    void search_shouldReturnItems() throws Exception {
        Item item1 = item1();
        Item item2 = item2();
        when(itemService.searchText("random text", DEFAULT_PAGEABLE)).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items/search")
                        .param("text", "random text")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(item1.getId()))
                .andExpect(jsonPath("$.[1].id").value(item2.getId()));

        verify(itemService, times(1)).searchText("random text", DEFAULT_PAGEABLE);
    }

    @Test
    void add_shouldReturnOk_ifValidationPassedAndUserFound() throws Exception {
        Item item1 = item1();
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

        verify(itemService, times(1)).addItem(any(Item.class));
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

        verify(itemService, times(1)).addItem(any(Item.class));
    }

    @Test
    void update_shouldReturnOk_ifItemFound() throws Exception {
        Item item1 = item1();
        long randomItemId = 14;
        ItemDto itemDto = ItemDto.builder().name("name").build();
        when(itemService.updateItem(any(Item.class))).thenReturn(item1);

        mockMvc.perform(patch("/items/{id}", randomItemId)
                        .header(USER_ID_HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item1.getId()));

        verify(itemService, times(1)).updateItem(any(Item.class));
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

        verify(itemService, times(1)).updateItem(any(Item.class));
    }

    @Test
    void addComment_shouldReturnOk_ifAuthorAndItemFoundAndValidationPassed() throws Exception {
        Item item1 = item1();
        User user = user();
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

        verify(itemService, times(1)).getItemById(item1.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(itemService, times(1)).addComment(any(Comment.class));
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

    private Item item1() {
        return Item.builder()
                .id(1L)
                .ownerId(1)
                .name("item 1")
                .description("text")
                .available(true)
                .build();
    }

    private Item item2() {
        return Item.builder()
                .id(2L)
                .ownerId(1)
                .name("item 1")
                .description("text")
                .available(false)
                .build();
    }

    private User user() {
        return User.builder()
                .id(2)
                .name("User")
                .email("user@email.com")
                .build();
    }

    private Booking lastBooking(User user, Item item) {
        return Booking.builder()
                .id(4L)
                .booker(user)
                .item(item)
                .start(LocalDateTime.of(2023, 3, 1, 12, 0))
                .end(LocalDateTime.of(2023, 3, 5, 12, 0))
                .status(BookingStatus.APPROVED)
                .build();
    }

    private Booking nextBooking(User user, Item item) {
        return Booking.builder()
                .id(12L)
                .booker(user)
                .item(item)
                .start(LocalDateTime.of(2023, 10, 1, 12, 0))
                .end(LocalDateTime.of(2023, 10, 5, 12, 0))
                .status(BookingStatus.APPROVED)
                .build();
    }

    private Comment comment1(User user, Item item) {
        return Comment.builder()
                .id(111)
                .author(user)
                .item(item)
                .text("text")
                .created(LocalDateTime.of(2023, 3, 5, 12, 0))
                .build();
    }

    private Comment comment2(User user, Item item) {
        return Comment.builder()
                .id(222)
                .author(user)
                .item(item)
                .text("text")
                .created(LocalDateTime.of(2023, 3, 5, 12, 0))
                .build();
    }
}