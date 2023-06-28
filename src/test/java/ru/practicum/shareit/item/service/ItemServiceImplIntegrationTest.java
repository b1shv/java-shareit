package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {
    @Autowired
    private final ItemServiceImpl itemService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final ItemRequestService itemRequestService;

    private static User user1;
    private static User user2;
    private static Item item1;
    private static Item item2;
    private static Item item3;
    private static Item item4;

    @BeforeAll
    static void setup(@Autowired ItemServiceImpl itemService,
                      @Autowired UserService userService,
                      @Autowired ItemRequestService itemRequestService) {
        user1 = userService.addUser(User.builder().name("Name1").email("user1@email.com").build());
        user2 = userService.addUser(User.builder().name("Name2").email("user2@email.com").build());
        item1 = itemService.addItem(Item.builder()
                .ownerId(user1.getId()).name("Item1")
                .description("Something useless but pretty")
                .available(true)
                .build());
        item2 = itemService.addItem(Item.builder()
                .ownerId(user2.getId())
                .name("Item2")
                .description("Something very useful")
                .available(true)
                .build());
        item3 = itemService.addItem(Item.builder()
                .available(true)
                .ownerId(user1.getId())
                .name("Item3")
                .description("Everyone's favorite thing")
                .build());
        item4 = itemService.addItem(Item.builder()
                .available(true)
                .ownerId(user2.getId())
                .name("Barbie doll")
                .description("Nothing to say")
                .build());
    }

    @Test
    void getItemsByOwnerId() {
        List<Item> expected = List.of(item1, item3);
        List<Item> actual = itemService.getItemsByOwnerId(user1.getId(), 0, 5);

        assertEquals(expected, actual);
        assertThrows(NotFoundException.class, () -> itemService.getItemsByOwnerId(100, 0, 5));
    }

    @Test
    void searchText() {
        assertEquals(List.of(item1, item2), itemService.searchText("uSe", 0, 5));
        assertEquals(List.of(item4), itemService.searchText("doll", 0, 5));
        assertEquals(Collections.emptyList(), itemService.searchText("garbage", 0, 5));
    }

    @Test
    void getItemsByRequestId() {
        ItemRequest request1 = itemRequestService.addRequest(ItemRequest.builder()
                .requesterId(user1.getId())
                .description("Text")
                .created(LocalDateTime.now())
                .build());
        ItemRequest request2 = itemRequestService.addRequest(ItemRequest.builder()
                .requesterId(user2.getId())
                .description("Another text")
                .created(LocalDateTime.now())
                .build());
        Item item5 = itemService.addItem(Item.builder()
                .ownerId(user2.getId())
                .name("Item5")
                .description("Item")
                .available(true)
                .requestId(request1.getId())
                .build());
        Item item6 = itemService.addItem(Item.builder()
                .ownerId(user2.getId())
                .name("Item6")
                .description("Another item")
                .available(true)
                .requestId(request1.getId())
                .build());
        Item item7 = itemService.addItem(Item.builder()
                .ownerId(user2.getId())
                .name("Item7")
                .description("One more item")
                .available(true)
                .requestId(request2.getId()).build());

        assertEquals(List.of(item5, item6), itemService.getItemsByRequestId(request1.getId()));
        assertEquals(List.of(item7), itemService.getItemsByRequestId(request2.getId()));
    }
}