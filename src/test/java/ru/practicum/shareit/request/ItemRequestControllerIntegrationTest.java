package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@Import({ItemRequestMapper.class, ItemMapper.class})
class ItemRequestControllerIntegrationTest {
    private static final String USER_ID = "X-Sharer-User-Id";

    @MockBean
    private ItemRequestService itemRequestService;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllShouldReturnRequests() throws Exception {
        long userId = 11;
        ItemRequest itemRequest1 = ItemRequest.builder().id(1).build();
        ItemRequest itemRequest2 = ItemRequest.builder().id(2).build();
        when(itemService.getItemsByRequestId(userId)).thenReturn(Collections.emptyList());
        when(itemRequestService.getAllRequests(userId, PageRequest.of(0, 10, Sort.by("created").descending())))
                .thenReturn(List.of(itemRequest1, itemRequest2));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(itemRequest1.getId()))
                .andExpect(jsonPath("$.[1].id").value(itemRequest2.getId()));
    }

    @Test
    void getAllShouldReturnBadRequest_ifValidationFailed() throws Exception {
        String wrongFrom = "-10";
        String wrongSize = "-2";

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("from", wrongFrom))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID, 1)
                        .param("size", wrongSize))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllRequests(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllByRequester_shouldReturnRequests() throws Exception {
        long userId = 11;
        ItemRequest itemRequest1 = ItemRequest.builder().id(1).build();
        ItemRequest itemRequest2 = ItemRequest.builder().id(2).build();
        when(itemService.getItemsByRequestId(anyLong())).thenReturn(Collections.emptyList());
        when(itemRequestService.getAllByRequesterId(userId)).thenReturn(List.of(itemRequest1, itemRequest2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[0].id").value(itemRequest1.getId()))
                .andExpect(jsonPath("$.[1].id").value(itemRequest2.getId()));
    }

    @Test
    void getAllByRequester_shouldReturnNotFound_ifRequesterNotFound() throws Exception {
        when(itemRequestService.getAllByRequesterId(1)).thenThrow(new NotFoundException());

        mockMvc.perform(get("/requests")
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        int requestId = 88;
        ItemRequest itemRequest1 = ItemRequest.builder().id(requestId).build();
        when(itemService.getItemsByRequestId(requestId)).thenReturn(Collections.emptyList());
        when(itemRequestService.getRequestById(1, requestId)).thenReturn(itemRequest1);

        mockMvc.perform(get("/requests/{id}", requestId)
                        .header(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));
    }

    @Test
    void getById_shouldReturnNotFound_ifUserOrRequestNotFound() throws Exception {
        when(itemRequestService.getRequestById(1L, 1L)).thenThrow(new NotFoundException());

        mockMvc.perform(get("/requests/{id}", 1)
                        .header(USER_ID, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_shouldSendRequestToService() throws Exception {
        int requestId = 7;
        ItemRequestDto dto = ItemRequestDto.builder().description("text").build();
        when(itemRequestService.addRequest(any(ItemRequest.class))).thenAnswer(InvocationOnMock -> {
            ItemRequest request = InvocationOnMock.getArgument(0);
            request.setId(requestId);
            return request;
        });

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));
    }

    @Test
    void add_shouldReturnBadRequest_ifValidationFailed() throws Exception {
        ItemRequestDto dtoWithoutDescription = ItemRequestDto.builder().build();

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithoutDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_shouldReturnNotFound_ifRequesterNotFound() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder().description("text").build();
        when(itemRequestService.addRequest(any(ItemRequest.class))).thenThrow(new NotFoundException());

        mockMvc.perform(post("/requests")
                        .header(USER_ID, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}