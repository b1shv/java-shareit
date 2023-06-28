package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import({BookingMapper.class, ItemMapper.class, UserMapper.class})
class BookingControllerTest {
    @MockBean
    BookingService bookingService;

    @MockBean
    UserService userService;

    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private final User user = User.builder().id(11).build();
    private final Item item = Item.builder().id(22).build();
    private final Booking booking1 = Booking.builder().id(33L).booker(user).item(item).build();
    private final Booking booking2 = Booking.builder().id(44L).booker(user).item(item).build();
    private static final String USER_ID = "X-Sharer-User-Id";

    @Test
    void getById_shouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(booking1);

        mockMvc.perform(get("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking1.getId()));
    }

    @Test
    void getById_shouldReturnNotFound_ifBookingNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong())).thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByBookerId_ShouldReturnBookings() throws Exception {
        when(bookingService.getBookingsByBookerId(user.getId(), "CURRENT", 0, 10))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$.[1].id").value(booking2.getId()));
    }

    @Test
    void getByBookerId_ShouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-10";
        String wrongSize = "0";

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .param("from", wrongFrom)
                        .header(USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .param("size", wrongSize)
                        .header(USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getBookingsByBookerId(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void getByBookerId_ShouldReturnNotFound_ifBookerNotFound() throws Exception {
        when(bookingService.getBookingsByBookerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByOwnerId_shouldReturnBookings() throws Exception {
        when(bookingService.getBookingsByOwnerId(user.getId(), "CURRENT", 0, 10))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(booking1.getId()))
                .andExpect(jsonPath("$.[1].id").value(booking2.getId()));
    }

    @Test
    void getByOwnerId_ShouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-10";
        String wrongSize = "0";

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .param("from", wrongFrom)
                        .header(USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .param("size", wrongSize)
                        .header(USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    void getByOwnerId_ShouldReturnNotFound_ifOwnerNotFound() throws Exception {
        when(bookingService.getBookingsByOwnerId(anyLong(), anyString(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException());

        mockMvc.perform(get("/bookings/owner")
                        .param("state", "CURRENT")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_shouldSendBookingToRepository() throws Exception {
        long randomBookingId = 100;
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
    }

    @Test
    void add_shouldReturnNotFound_ifBookerNotFound() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();
        when(itemService.getItemById(anyLong())).thenReturn(item);
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException());

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
        verify(bookingService, never()).addBooking(any(Booking.class));
    }

    @Test
    void add_shouldReturnNotFound_ifItemNotFound() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .bookerId(user.getId())
                .itemId(item.getId()).build();
        when(itemService.getItemById(anyLong())).thenThrow(new NotFoundException());
        when(userService.getUserById(anyLong())).thenReturn(user);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isNotFound());
        verify(bookingService, never()).addBooking(any(Booking.class));
    }

    @Test
    void add_shouldReturnBadRequest_ifValidationFailed() throws Exception {
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

        verify(bookingService, never()).addBooking(any(Booking.class));
    }

    @Test
    void approve_shouldCallService() throws Exception {
        when(bookingService.updateStatus(user.getId(), booking1.getId(), true)).thenReturn(booking1);

        mockMvc.perform(patch("/bookings/{bookingId}", booking1.getId())
                        .param("approved", "true")
                        .header(USER_ID, user.getId()))
                .andExpect(status().isOk());
        verify(bookingService, times(1)).updateStatus(user.getId(), booking1.getId(), true);
    }

    @Test
    void approve_shouldReturnBadRequest_ifWrongParam() throws Exception {
        when(bookingService.updateStatus(user.getId(), booking1.getId(), true)).thenReturn(booking1);

        mockMvc.perform(patch("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId())
                        .param("approved", "notBoolean"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId()))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).updateStatus(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void approve_shouldReturnNotFound_ifBookingOrUserNotFound() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean())).thenThrow(new NotFoundException());

        mockMvc.perform(patch("/bookings/{bookingId}", booking1.getId())
                        .header(USER_ID, user.getId())
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }
}