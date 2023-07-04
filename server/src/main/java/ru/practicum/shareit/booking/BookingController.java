package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_ID = "X-Sharer-User-Id";
    private static final String DEFAULT_STATE = "ALL";
    private static final String DEFAULT_FROM = "0";
    private static final String DEFAULT_SIZE = "10";
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID) long userId,
                              @PathVariable long bookingId) {
        log.debug("GET request: booking ID {}", bookingId);
        return bookingMapper.toDto(bookingService.getBookingById(bookingId, userId));
    }

    @GetMapping
    public List<BookingDto> getByBookerId(@RequestHeader(USER_ID) long bookerId,
                                          @RequestParam(name = "state", defaultValue = DEFAULT_STATE) String stateName,
                                          @RequestParam(defaultValue = DEFAULT_FROM) int from,
                                          @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.debug("GET request: all booking of user ID {}, state {}", bookerId, stateName);
        int page = from / size;
        return bookingService.getBookingsByBookerId(bookerId, stateName, PageRequest.of(page, size, Sort.by("start").descending())).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwnerId(@RequestHeader(USER_ID) long ownerId,
                                         @RequestParam(name = "state", defaultValue = DEFAULT_STATE) String stateName,
                                         @RequestParam(defaultValue = DEFAULT_FROM) int from,
                                         @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
        log.debug("GET request: all bookings of items of user ID {}, state {}", ownerId, stateName);
        int page = from / size;
        return bookingService.getBookingsByOwnerId(ownerId, stateName, PageRequest.of(page, size, Sort.by("start").descending())).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public BookingDto add(@RequestHeader(USER_ID) long bookerId,
                          @RequestBody BookingDto bookingDto) {
        log.debug("POST request: new booking, booker: {}", bookerId);
        Item item = itemService.getItemById(bookingDto.getItemId());
        User booker = userService.getUserById(bookerId);
        Booking booking = bookingMapper.toBooking(bookingDto, item, booker);

        return bookingMapper.toDto(bookingService.addBooking(booking));
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID) long userId,
                              @PathVariable long bookingId,
                              @RequestParam boolean approved) {
        log.debug("PATCH request: approving booking ID {}: {}", bookingId, approved);
        return bookingMapper.toDto(bookingService.updateStatus(userId, bookingId, approved));
    }
}
