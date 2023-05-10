package ru.practicum.shareit.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    private long id;
    private String description;
    private long requesterId;
    private LocalDateTime created;
}
