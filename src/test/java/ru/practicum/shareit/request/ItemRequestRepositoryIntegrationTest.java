package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryIntegrationTest {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @BeforeAll
    static void setup(@Autowired ItemRequestRepository itemRequestRepository,
                      @Autowired UserRepository userRepository) {
        userRepository.save(User.builder().name("User1").email("user13@email.com").build());
        userRepository.save(User.builder().name("User2").email("user14@email.com").build());
        itemRequestRepository.save(ItemRequest.builder()
                .id(1)
                .requesterId(1)
                .description("text")
                .created(LocalDateTime.now().minusDays(10))
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .id(2)
                .requesterId(2)
                .description("text")
                .created(LocalDateTime.now().minusDays(10))
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .id(3)
                .requesterId(1)
                .description("text")
                .created(LocalDateTime.now().minusDays(9))
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .id(4)
                .requesterId(2)
                .description("text")
                .created(LocalDateTime.now().minusDays(9))
                .build());
        itemRequestRepository.save(ItemRequest.builder()
                .id(5)
                .requesterId(1)
                .description("text")
                .created(LocalDateTime.now().minusDays(1))
                .build());
    }

    @Test
    void findAllByRequesterIdNot() {
        List<ItemRequest> expected = List.of(
                itemRequestRepository.findById(2L).get(),
                itemRequestRepository.findById(4L).get());
        List<ItemRequest> actual = itemRequestRepository.findAllByRequesterIdNot(1, Pageable.ofSize(5));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc() {
        List<ItemRequest> expectedForUser1 = List.of(
                itemRequestRepository.findById(5L).get(),
                itemRequestRepository.findById(3L).get(),
                itemRequestRepository.findById(1L).get());
        List<ItemRequest> expectedForUser2 = List.of(
                itemRequestRepository.findById(4L).get(),
                itemRequestRepository.findById(2L).get());
        List<ItemRequest> actualForUser1 = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(1);
        List<ItemRequest> actualForUser2 = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(2);

        assertThat(actualForUser1).isEqualTo(expectedForUser1);
        assertThat(actualForUser2).isEqualTo(expectedForUser2);
    }
}