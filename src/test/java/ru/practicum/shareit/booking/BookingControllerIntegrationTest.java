package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import({BookingMapper.class, ItemMapper.class, UserMapper.class})
class BookingControllerIntegrationTest {
    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10, Sort.by("start").descending());
    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "X-Sharer-User-Id";

    @Test
    void getById_shouldReturnBooking() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
        when(bookingService.getBookingById(booking1.getId(), user.getId())).thenReturn(booking1);

        mockMvc.perform(get("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId()));
        verify(bookingService, times(1)).getBookingById(33, 11);
    }

    @Test
    void getById_shouldReturnNotFound_ifBookingNotFound() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
        when(bookingService.getBookingById(booking1.getId(), user.getId())).thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId()))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).getBookingById(33, 11);
    }

    @Test
    void getByBookerId_ShouldReturnBookings() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
        Booking booking2 = Booking.builder().id(44L).booker(user).item(item).build();
        when(bookingService.getBookingsByBookerId(user.getId(), "CURRENT",
                DEFAULT_PAGEABLE))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$.[1].id").value(booking2.getId()));
        verify(bookingService, times(1)).getBookingsByBookerId(11, "CURRENT",
                DEFAULT_PAGEABLE);
    }

    @Test
    void getByBookerId_ShouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-10";
        String wrongSize = "0";

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .param("from", wrongFrom)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .param("size", wrongSize)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByBookerId_ShouldReturnNotFound_ifBookerNotFound() throws Exception {
        when(bookingService.getBookingsByBookerId(1, "CURRENT", DEFAULT_PAGEABLE))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).getBookingsByBookerId(1, "CURRENT",
                DEFAULT_PAGEABLE);
    }

    @Test
    void getByOwnerId_shouldReturnBookings() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
        Booking booking2 = Booking.builder().id(44L).booker(user).item(item).build();
        when(bookingService.getBookingsByOwnerId(user.getId(), "CURRENT", DEFAULT_PAGEABLE))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$.[1].id").value(booking2.getId()));
        verify(bookingService, times(1)).getBookingsByOwnerId(11, "CURRENT",
                DEFAULT_PAGEABLE);
    }

    @Test
    void getByOwnerId_ShouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-10";
        String wrongSize = "0";

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .param("from", wrongFrom)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .param("size", wrongSize)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByOwnerId_ShouldReturnNotFound_ifOwnerNotFound() throws Exception {
        when(bookingService.getBookingsByOwnerId(1, "CURRENT", DEFAULT_PAGEABLE))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingsByOwnerId(1, "CURRENT",
                DEFAULT_PAGEABLE);
    }

    @Test
    void add_shouldSendBookingToRepository() throws Exception {
        long randomBookingId = 100;
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(itemService.getItemById(item.getId())).thenReturn(item);
        when(bookingService.addBooking(any(Booking.class)))
                .thenAnswer(InvocationOnMock -> {
                    Booking booking = InvocationOnMock.getArgument(0);
                    booking.setId(randomBookingId);
                    return booking;
                });

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomBookingId))
                .andExpect(jsonPath("$.item.id").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(user.getId()));

        verify(bookingService, times(1)).addBooking(any(Booking.class));
    }

    @Test
    void add_shouldReturnNotFound_ifBookerNotFound() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();
        when(itemService.getItemById(item.getId())).thenReturn(item);
        when(userService.getUserById(user.getId())).thenThrow(new NotFoundException());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(bookingService);
    }

    @Test
    void add_shouldReturnNotFound_ifItemNotFound() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();
        when(itemService.getItemById(item.getId())).thenThrow(new NotFoundException());
        when(userService.getUserById(user.getId())).thenReturn(user);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(bookingService);
    }

    @Test
    void add_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        bookingDto.setStart(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void approve_shouldCallService() throws Exception {
        User user = User.builder().id(11).build();
        Item item = Item.builder().id(22).build();
        Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
        when(bookingService.updateStatus(user.getId(), booking1.getId(), true)).thenReturn(booking1);

        mockMvc.perform(patch("/bookings/{bookingId}", booking1.getId())
                        .param("approved", "true")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).updateStatus(user.getId(), booking1.getId(), true);
    }

    @Test
    void approve_shouldReturnBadRequest_ifWrongParam() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(USER_ID, 1)
                        .param("approved", "notBoolean"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(USER_ID, 1))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void approve_shouldReturnNotFound_ifBookingOrUserNotFound() throws Exception {
        when(bookingService.updateStatus(1, 1, true)).thenThrow(new NotFoundException());

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header(USER_ID, 1)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).updateStatus(1, 1, true);
    }
}