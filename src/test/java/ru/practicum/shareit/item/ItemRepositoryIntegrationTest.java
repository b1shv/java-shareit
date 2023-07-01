package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryIntegrationTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @BeforeAll
    static void setup(@Autowired UserRepository userRepository,
                      @Autowired ItemRepository itemRepository,
                      @Autowired ItemRequestRepository itemRequestRepository) {
        User user1 = userRepository.save(User.builder().id(1).name("Name 1").email("email@email.com").build());
        User user2 = userRepository.save(User.builder().id(2).name("Name 2").email("email2@email.com").build());
        ItemRequest request1 = itemRequestRepository.save(ItemRequest.builder()
                .id(1)
                .requesterId(user2.getId())
                .description("description")
                .created(LocalDateTime.now())
                .build());
        ItemRequest request2 = itemRequestRepository.save(ItemRequest.builder()
                .id(2)
                .requesterId(user2.getId())
                .description("description")
                .created(LocalDateTime.now())
                .build());
        itemRepository.save(Item.builder()
                .id(1)
                .name("Wonderwall")
                .description("Today is gonna be the day")
                .ownerId(user1.getId())
                .available(true)
                .requestId(request1.getId())
                .build());
        itemRepository.save(Item.builder()
                .id(2)
                .name("Champagne Supernova")
                .description("How many lives are livin' strange")
                .ownerId(user2.getId())
                .available(true)
                .requestId(request2.getId())
                .build());
        itemRepository.save(Item.builder()
                .id(3)
                .name("Supersonic")
                .description("Give me gin and tonic")
                .ownerId(user1.getId())
                .available(true)
                .build());
        System.out.println(user1.getId());
        System.out.println(user2.getId());
        System.out.println(request1.getId());
        System.out.println(request2.getId());
        System.out.println(userRepository.findAll());
        System.out.println(itemRequestRepository.findAll());
    }

    @Test
    void findAllByOwnerId() {
        List<Item> expectedUser1 = List.of(itemRepository.findById(1L).get(), itemRepository.findById(3L).get());
        List<Item> expectedUser2 = List.of(itemRepository.findById(2L).get());
        List<Item> actualUser1 = itemRepository.findAllByOwnerId(1, PageRequest.of(0, 5));
        List<Item> actualUser2 = itemRepository.findAllByOwnerId(2, PageRequest.of(0, 5));

        assertThat(actualUser1).isEqualTo(expectedUser1);
        assertThat(actualUser2).isEqualTo(expectedUser2);
        assertThat(itemRepository.findAllByOwnerId(10, PageRequest.of(0, 5))).isEmpty();
    }

    @Test
    void searchText() {
        List<Item> expectedSuper = List.of(itemRepository.findById(2L).get(), itemRepository.findById(3L).get());
        List<Item> expectedToday = List.of(itemRepository.findById(1L).get());

        assertThat(itemRepository.searchText("super", PageRequest.of(0, 5))).isEqualTo(expectedSuper);
        assertThat(itemRepository.searchText("toDaY", PageRequest.of(0, 5))).isEqualTo(expectedToday);
    }

    @Test
    void findAllByRequestId() {
        assertThat(itemRepository.findAllByRequestId(1)).isEqualTo(List.of(itemRepository.findById(1L).get()));
        assertThat(itemRepository.findAllByRequestId(2)).isEqualTo(List.of(itemRepository.findById(2L).get()));
        assertThat(itemRepository.findAllByRequestId(5)).isEmpty();
    }
}