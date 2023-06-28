package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest {
    @Autowired
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    private final UserRepository userRepository;

    private static User user1;
    private static User user2;
    private static ItemRequest request1;
    private static ItemRequest request2;
    private static ItemRequest request3;
    private static ItemRequest request4;
    private static ItemRequest request5;

    @BeforeAll
    static void setup(@Autowired ItemRequestRepository itemRequestRepository,
                      @Autowired UserRepository userRepository) {
        user1 = userRepository.save(User.builder().name("User1").email("user13@email.com").build());
        user2 = userRepository.save(User.builder().name("User2").email("user14@email.com").build());
        request1 = itemRequestRepository.save(ItemRequest.builder()
                .requesterId(user1.getId())
                .description("text")
                .created(LocalDateTime.now().minusDays(10))
                .build());
        request2 = itemRequestRepository.save(ItemRequest.builder()
                .requesterId(user2.getId())
                .description("text")
                .created(LocalDateTime.now().minusDays(10))
                .build());
        request3 = itemRequestRepository.save(ItemRequest.builder()
                .requesterId(user1.getId())
                .description("text")
                .created(LocalDateTime.now().minusDays(9))
                .build());
        request4 = itemRequestRepository.save(ItemRequest.builder()
                .requesterId(user2.getId())
                .description("text")
                .created(LocalDateTime.now().minusDays(9))
                .build());
        request5 = itemRequestRepository.save(ItemRequest.builder()
                .requesterId(user1.getId())
                .description("text")
                .created(LocalDateTime.now().minusDays(1))
                .build());
    }

    @Test
    void findAllByRequesterIdNot() {
        List<ItemRequest> expected = List.of(request2, request4);
        List<ItemRequest> actual = itemRequestRepository.findAllByRequesterIdNot(user1.getId(), Pageable.ofSize(5));

        assertEquals(expected, actual);
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc() {
        List<ItemRequest> expectedForUser1 = List.of(request5, request3, request1);
        List<ItemRequest> expectedForUser2 = List.of(request4, request2);
        List<ItemRequest> actualForUser1 = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user1.getId());
        List<ItemRequest> actualForUser2 = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user2.getId());

        assertEquals(expectedForUser1, actualForUser1);
        assertEquals(expectedForUser2, actualForUser2);
    }
}