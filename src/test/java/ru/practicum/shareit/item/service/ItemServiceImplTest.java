package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Item item1 = Item.builder().id(1).name("Item 1").ownerId(1).build();
    private final Item item2 = Item.builder().id(2).name("Item 2").ownerId(1).build();
    private final Item item3 = Item.builder().id(3).name("Item 3").ownerId(2).build();

    @Test
    void getItemsByOwnerId_shouldReturnItems_ifUserExists() {
        int from = 4;
        int size = 3;
        int expectedPage = 1;
        List<Item> expected = List.of(item1, item2);
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.findAllByOwnerId(1, PageRequest.of(expectedPage, size)))
                .thenReturn(expected);

        assertEquals(expected, itemService.getItemsByOwnerId(1, from, size));
        verify(itemRepository, times(1))
                .findAllByOwnerId(1, PageRequest.of(expectedPage, size));
        verify(userRepository, times(1))
                .existsById(anyLong());
    }

    @Test
    void getItemsByOwnerId_shouldThrowException_ifUserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.getItemsByOwnerId(1, 1, 1));
        verify(itemRepository, never()).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    void getItemById_shouldReturnItem_ifExists() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        assertEquals(item1, itemService.getItemById(1));
    }

    @Test
    void getItemById_shouldThrowException_ifItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1));
    }

    @Test
    void searchText_shouldReturnItems_ifTextNotBlank() {
        String text = "text";
        List<Item> expected = List.of(item1, item3);
        when(itemRepository.searchText(anyString(), any(Pageable.class))).thenReturn(expected);

        assertEquals(expected, itemService.searchText(text, 1, 2));
    }

    @Test
    void searchText_shouldReturnEmptyList_ifTextBlank() {
        String text = "  ";

        assertEquals(Collections.emptyList(), itemService.searchText(text, 1, 2));
        verify(itemRepository, never()).searchText(anyString(), any(Pageable.class));
    }

    @Test
    void addItem_shouldSendItemToRepo_ifUserExists() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        itemService.addItem(item1);

        verify(itemRepository, times(1)).save(item1);
    }

    @Test
    void addItem_shouldThrowException_ifUserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.addItem(item1));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void addItem_shouldThrowException_ifRequestNotNullAndNotFound() {
        Item item6 = Item.builder().id(98).ownerId(99).requestId(100L).build();
        when(userRepository.existsById(99L)).thenReturn(true);
        when(itemRequestRepository.existsById(100L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.addItem(item6));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_shouldThrowException_ifItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(item1));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_shouldThrowException_ifUserIsNotOwner() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(item3));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_shouldUpdateItemFields() {
        Item item = Item.builder()
                .id(100)
                .name("old name")
                .description("old description")
                .available(false)
                .ownerId(1)
                .build();

        String newName = "new name";
        String newDescription = "new description";
        String newNewName = "new new name";
        String newNewDescription = "new new description";
        Item justName = Item.builder().id(100).ownerId(1).name(newName).build();
        Item justDescription = Item.builder().id(100).ownerId(1).description(newDescription).build();
        Item justAvailable = Item.builder().id(100).ownerId(1).available(true).build();
        Item allFields = Item.builder().id(100).ownerId(1).name(newNewName).description(newNewDescription)
                .available(false).build();
        InOrder inOrder = Mockito.inOrder(itemRepository);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        itemService.updateItem(justName);
        inOrder.verify(itemRepository).save(argThat(arg -> arg.getName().equals(newName)));

        itemService.updateItem(justDescription);
        inOrder.verify(itemRepository).save(argThat(arg -> arg.getDescription().equals(newDescription)));

        itemService.updateItem(justAvailable);
        inOrder.verify(itemRepository).save(argThat(arg -> arg.getAvailable().equals(true)));

        itemService.updateItem(allFields);
        inOrder.verify(itemRepository).save(argThat(
                arg -> arg.getName().equals(newNewName)
                        && arg.getDescription().equals(newNewDescription)
                        && arg.getAvailable().equals(false)));
    }

    @Test
    void addComment_shouldAddComment_ifItemWasBookedByCommentAuthor() {
        Comment comment = Comment.builder()
                .item(item1)
                .author(User.builder().id(100).build())
                .created(LocalDateTime.now())
                .build();
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(BookingStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Booking()));

        itemService.addComment(comment);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void addComment_shouldThrowException_ifItemWasNotBookedByCommentAuthor() {
        Comment comment = Comment.builder()
                .item(item1)
                .author(User.builder().id(100).build())
                .created(LocalDateTime.now())
                .build();
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(BookingStatus.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(comment));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getComments_shouldReturnComments() {
        Comment comment1 = Comment.builder().id(1).build();
        Comment comment2 = Comment.builder().id(2).build();
        Comment comment3 = Comment.builder().id(3).build();
        List<Comment> expected = List.of(comment1, comment2, comment3);
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(anyLong())).thenReturn(expected);

        assertEquals(expected, itemService.getComments(1));
        verify(commentRepository, times(1)).findAllByItemIdOrderByCreatedDesc(1);
    }

    @Test
    void getItemsByRequestId() {
        List<Item> expected = List.of(item1, item2, item3);
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(expected);

        assertEquals(expected, itemService.getItemsByRequestId(1));
        verify(itemRepository, times(1)).findAllByRequestId(1);
    }
}