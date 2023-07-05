package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.StartBeforeEndValid;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@StartBeforeEndValid
public class BookingDto {
    @FutureOrPresent
    private LocalDateTime start;

    @Future
    private LocalDateTime end;

    @NotNull
    private Long itemId;
}
