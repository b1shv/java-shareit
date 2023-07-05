package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingForItemDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long bookerId;
    private long itemId;
}
