package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void getAllRequests_shouldReturnRequests() {
        int userId = 1;
        int from = 5;
        int size = 2;
        int page = from / size;
        ItemRequest request1 = ItemRequest.builder().id(1).build();
        ItemRequest request2 = ItemRequest.builder().id(2).build();
        List<ItemRequest> requests = List.of(request1, request2);
        when(itemRequestRepository.findAllByRequesterIdNot(
                userId, PageRequest.of(page, size, Sort.by("created").descending()))).thenReturn(requests);

        assertThat(itemRequestService.getAllRequests(userId, PageRequest.of(page, size, Sort.by("created").descending())))
                .isEqualTo(requests);
    }

    @Test
    void getAllByRequesterId_shouldCallRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);
        itemRequestService.getAllByRequesterId(1);

        verify(itemRequestRepository, times(1)).findAllByRequesterIdOrderByCreatedDesc(1);
    }

    @Test
    void getAllByRequesterId_shouldThrowException_ifRequesterNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.getAllByRequesterId(1)).isInstanceOf(NotFoundException.class);
        verify(itemRequestRepository, never()).findAllByRequesterIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getRequestById_shouldReturnRequest() {
        ItemRequest request = ItemRequest.builder().id(1).build();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThat(itemRequestService.getRequestById(1, 1)).isEqualTo(request);
    }

    @Test
    void getRequestById_shouldThrowException_ifUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.getRequestById(1, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getRequestById_shouldThrowException_ifRequestNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(1, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void addRequest_shouldSendRequestToRepository() {
        ItemRequest request = ItemRequest.builder().id(1).build();
        when(userRepository.existsById(request.getRequesterId())).thenReturn(true);

        itemRequestService.addRequest(request);

        verify(itemRequestRepository, times(1)).save(request);
    }

    @Test
    void addRequest_shouldThrowException_ifRequesterNotFound() {
        ItemRequest request = ItemRequest.builder().id(1).build();
        when(userRepository.existsById(request.getRequesterId())).thenReturn(false);

        assertThatThrownBy(() -> itemRequestService.addRequest(request)).isInstanceOf(NotFoundException.class);
        verify(itemRequestRepository, never()).save(request);
    }
}