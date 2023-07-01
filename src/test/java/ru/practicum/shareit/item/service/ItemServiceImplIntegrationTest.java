package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {
    private final ItemServiceImpl itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestService itemRequestService;

    @BeforeAll
    static void setup(@Autowired ItemRepository itemRepository,
                      @Autowired UserRepository userRepository) {
        userRepository.save(User.builder().name("Name1").email("user1@email.com").build());
        userRepository.save(User.builder().name("Name2").email("user2@email.com").build());
        itemRepository.save(Item.builder()
                .ownerId(1).name("Item1")
                .description("Something useless but pretty")
                .available(true)
                .build());
        itemRepository.save(Item.builder()
                .ownerId(2)
                .name("Item2")
                .description("Something very useful")
                .available(true)
                .build());
        itemRepository.save(Item.builder()
                .available(true)
                .ownerId(1)
                .name("Item3")
                .description("Everyone's favorite thing")
                .build());
        itemRepository.save(Item.builder()
                .available(true)
                .ownerId(2)
                .name("Barbie doll")
                .description("Nothing to say")
                .build());
    }

    @Test
    void getItemsByOwnerId() {
        List<Item> expected = List.of(itemService.getItemById(1), itemService.getItemById(3));
        List<Item> actual = itemService.getItemsByOwnerId(1, PageRequest.of(0, 5));

        assertThat(actual).isEqualTo(expected);
        assertThatThrownBy(() -> itemService.getItemsByOwnerId(100, PageRequest.of(0, 5)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchText() {
        assertThat(itemService.searchText("uSe", PageRequest.of(0, 15)))
                .isEqualTo(List.of(itemService.getItemById(1), itemService.getItemById(2)));
        assertThat(itemService.searchText("doll", PageRequest.of(0, 5)))
                .isEqualTo(List.of(itemService.getItemById(4)));
        assertThat(itemService.searchText("garbage", PageRequest.of(0, 5))).isEmpty();
    }

    @Test
    void getItemsByRequestId() {
        ItemRequest request1 = itemRequestService.addRequest(ItemRequest.builder()
                .requesterId(1)
                .description("Text")
                .created(LocalDateTime.now())
                .build());
        ItemRequest request2 = itemRequestService.addRequest(ItemRequest.builder()
                .requesterId(2)
                .description("Another text")
                .created(LocalDateTime.now())
                .build());
        Item item5 = itemService.addItem(Item.builder()
                .ownerId(2)
                .name("Item5")
                .description("Item")
                .available(true)
                .requestId(request1.getId())
                .build());
        Item item6 = itemService.addItem(Item.builder()
                .ownerId(2)
                .name("Item6")
                .description("Another item")
                .available(true)
                .requestId(request1.getId())
                .build());
        Item item7 = itemService.addItem(Item.builder()
                .ownerId(2)
                .name("Item7")
                .description("One more item")
                .available(true)
                .requestId(request2.getId()).build());

        assertThat(itemService.getItemsByRequestId(request1.getId())).isEqualTo(List.of(item5, item6));
        assertThat(itemService.getItemsByRequestId(request2.getId())).isEqualTo(List.of(item7));
    }
}