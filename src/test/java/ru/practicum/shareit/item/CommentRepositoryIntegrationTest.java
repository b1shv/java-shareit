package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryIntegrationTest {
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Test
    void findAllByItemIdOrderByCreatedDesc() {
        User user = userRepository.save(User.builder().name("User").email("user@email.com").build());
        User user2 = userRepository.save(User.builder().name("User2").email("user2@email.com").build());
        Item item1 = itemRepository.save(Item.builder()
                .name("item1").description("description1").available(true).ownerId(user.getId()).build());
        Item item2 = itemRepository.save(Item.builder()
                .name("item2").description("description2").available(true).ownerId(user.getId()).build());
        Comment comment1 = commentRepository.save(Comment.builder().item(item1).text("Text").author(user2)
                .created(LocalDateTime.of(2001, 1, 1, 12, 0)).build());
        Comment comment2 = commentRepository.save(Comment.builder().item(item2).text("Text text").author(user2)
                .created(LocalDateTime.of(1995, 1, 1, 12, 0)).build());
        Comment comment3 = commentRepository.save(Comment.builder().item(item1).text("Text text text").author(user2)
                .created(LocalDateTime.of(1999, 1, 1, 12, 0)).build());
        Comment comment4 = commentRepository.save(Comment.builder().item(item2).text("Text text x2").author(user2)
                .created(LocalDateTime.of(2000, 1, 1, 12, 0)).build());
        Comment comment5 = commentRepository.save(Comment.builder().item(item1).text("Text text x2 + text").author(user2)
                .created(LocalDateTime.of(1999, 1, 1, 14, 30)).build());
        Comment comment6 = commentRepository.save(Comment.builder().item(item2).text("Text text text x2").author(user2)
                .created(LocalDateTime.of(1997, 4, 1, 12, 0)).build());

        List<Comment> item1expected = List.of(comment1, comment5, comment3);
        List<Comment> item2expected = List.of(comment4, comment6, comment2);

        assertThat(commentRepository.findAllByItemIdOrderByCreatedDesc(item1.getId())).isEqualTo(item1expected);
        assertThat(commentRepository.findAllByItemIdOrderByCreatedDesc(item2.getId())).isEqualTo(item2expected);
    }

}