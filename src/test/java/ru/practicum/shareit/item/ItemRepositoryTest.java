package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {
    @Autowired
    private final ItemRepository itemRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final ItemRequestRepository itemRequestRepository;

    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setup() {
        user1 = userRepository.save(User.builder().name("Name 1").email("email@email.com").build());
        user2 = userRepository.save(User.builder().name("Name 2").email("email2@email.com").build());
        item1 = itemRepository.save(Item.builder().name("Wonderwall").description("Today is gonna be the day")
                .ownerId(user1.getId()).available(true).build());
        item2 = itemRepository.save(Item.builder().name("Champagne Supernova")
                .description("How many lives are livin' strange").ownerId(user2.getId()).available(true).build());
        item3 = itemRepository.save(Item.builder().name("Supersonic").description("Give me gin and tonic")
                .ownerId(user1.getId()).available(true).build());
        System.out.println(itemRepository.findAll());
    }

    @Test
    void findAllByOwnerId() {
        List<Item> expectedUser1 = List.of(item1, item3);
        List<Item> expectedUser2 = List.of(item2);
        List<Item> actualUser1 = itemRepository.findAllByOwnerId(user1.getId(), PageRequest.of(0, 5));
        List<Item> actualUser2 = itemRepository.findAllByOwnerId(user2.getId(), PageRequest.of(0, 5));

        assertEquals(expectedUser1, actualUser1);
        assertEquals(expectedUser2, actualUser2);
        assertEquals(Collections.EMPTY_LIST,
                itemRepository.findAllByOwnerId(10, PageRequest.of(0, 5)));
    }

    @Test
    void searchText() {
        List<Item> expectedSuper = List.of(item2, item3);
        List<Item> expectedToday = List.of(item1);

        assertEquals(expectedSuper, itemRepository.searchText("super", PageRequest.of(0, 5)));
        assertEquals(expectedToday, itemRepository.searchText("toDaY", PageRequest.of(0, 5)));
    }

    @Test
    void findAllByRequestId() {
        ItemRequest itemRequest1 = ItemRequest.builder()
                .requesterId(user2.getId())
                .description("description")
                .created(LocalDateTime.now()).build();
        ItemRequest itemRequest2 = ItemRequest.builder()
                .requesterId(user1.getId())
                .description("description")
                .created(LocalDateTime.now()).build();
        item1.setRequestId(1L);
        item2.setRequestId(2L);
        itemRequestRepository.save(itemRequest1);
        itemRequestRepository.save(itemRequest2);
        itemRepository.save(item1);
        itemRepository.save(item2);

        assertEquals(List.of(item1), itemRepository.findAllByRequestId(1));
        assertEquals(List.of(item2), itemRepository.findAllByRequestId(2));
        assertEquals(Collections.EMPTY_LIST, itemRepository.findAllByRequestId(5));
    }
}