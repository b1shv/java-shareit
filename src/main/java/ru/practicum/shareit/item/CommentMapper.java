package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .text(comment.getText())
                .build();
    }

    public List<CommentDto> toDto(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Comment toComment(CommentDto commentDto, User author, Item item, LocalDateTime created) {
        return Comment.builder()
                .author(author)
                .item(item)
                .text(commentDto.getText())
                .created(created)
                .build();
    }
}
