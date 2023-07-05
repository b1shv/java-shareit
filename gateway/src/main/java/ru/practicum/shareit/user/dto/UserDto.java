package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    @NotBlank(groups = {Create.class})
    private String name;

    @Email(groups = {Create.class, Update.class})
    @NotNull(groups = {Create.class})
    private String email;
}
