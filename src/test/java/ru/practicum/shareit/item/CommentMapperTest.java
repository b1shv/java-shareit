package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {
    private final CommentMapper commentMapper = new CommentMapper();

    @Test
    void toDto() {
        User user = User.builder().id(11).name("Name").build();
        Item item = Item.builder().id(14).build();
        LocalDateTime created = LocalDateTime.of(2023, 1, 1, 10, 0);
        Comment comment = Comment.builder()
                .id(1)
                .text("Text")
                .author(user)
                .item(item)
                .created(created)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .id(1)
                .text("Text")
                .created(created)
                .authorName(user.getName())
                .build();

        assertThat(commentMapper.toDto(comment)).isEqualTo(commentDto);
    }

    @Test
    void toDtoList() {
        Item item = Item.builder().id(1).build();
        User user1 = User.builder().id(1).name("Name").build();
        User user2 = User.builder().id(2).name("Name2").build();
        User user3 = User.builder().id(3).name("Name3").build();
        LocalDateTime created1 = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime created2 = LocalDateTime.of(2023, 2, 8, 10, 0);
        LocalDateTime created3 = LocalDateTime.of(2023, 5, 4, 10, 0);
        Comment comment1 = Comment.builder().id(1).text("Text").author(user1).item(item).created(created1).build();
        Comment comment2 = Comment.builder().id(2).text("Text2").author(user2).item(item).created(created2).build();
        Comment comment3 = Comment.builder().id(3).text("Text3").author(user3).item(item).created(created3).build();
        CommentDto commentDto1 = CommentDto.builder().id(1).authorName(user1.getName()).text("Text").created(created1).build();
        CommentDto commentDto2 = CommentDto.builder().id(2).authorName(user2.getName()).text("Text2").created(created2).build();
        CommentDto commentDto3 = CommentDto.builder().id(3).authorName(user3.getName()).text("Text3").created(created3).build();

        List<CommentDto> expected = List.of(commentDto1, commentDto2, commentDto3);
        List<CommentDto> actual = commentMapper.toDto(List.of(comment1, comment2, comment3));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toComment() {
        User user = User.builder().id(15).name("Name").build();
        Item item = Item.builder().id(26).build();
        LocalDateTime created = LocalDateTime.of(2023, 4, 1, 10, 0);
        CommentDto commentDto = CommentDto.builder()
                .text("Text")
                .build();
        Comment comment = Comment.builder()
                .text("Text")
                .author(user)
                .item(item)
                .created(created)
                .build();

        assertThat(commentMapper.toComment(commentDto, user, item, created)).isEqualTo(comment);
    }
}