package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.StartBeforeEndValid;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@StartBeforeEndValid
public class BookingDto {
    @NotNull
    private Long itemId;

    @FutureOrPresent
    private LocalDateTime start;

    private LocalDateTime end;
}
