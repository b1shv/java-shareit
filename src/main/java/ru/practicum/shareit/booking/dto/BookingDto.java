package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private long id;

    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    private long bookerId;
    private UserDto booker;
    private long itemId;
    private ItemDto item;
    private BookingStatus status;
}
