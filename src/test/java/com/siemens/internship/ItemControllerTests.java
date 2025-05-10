package com.siemens.internship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        item1 = new Item(1L, "Item1", "Description1", "Available", "item1@example.com");
        item2 = new Item(2L, "Item2", "Description2", "Unavailable", "item2@example.com");
    }

    @Test
    void testGetAllItems() throws Exception {
        when(itemService.findAll()).thenReturn(Arrays.asList(item1, item2));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Item1")))
                .andExpect(jsonPath("$[1].name", is("Item2")));

        verify(itemService, times(1)).findAll();
    }

    @Test
    void testGetItemById() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item1));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Item1")));

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testGetItemById_NotFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void testCreateItem() throws Exception {
        Item newItem = new Item(null, "NewItem", "NewDescription", "Available", "valid@example.com");

        when(itemService.save(ArgumentMatchers.any(Item.class))).thenReturn(item1);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Item1")));

        verify(itemService, times(1)).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item1));
        when(itemService.save(ArgumentMatchers.any(Item.class))).thenReturn(item1);

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Item1")));

        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void testDeleteItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item1));
        doNothing().when(itemService).deleteById(1L);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isOk());

        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void testProcessItems() throws Exception {
        List<Item> processedItems = Arrays.asList(
                new Item(1L, "Item1", "Description1", "PROCESSED", "item1@example.com"),
                new Item(2L, "Item2", "Description2", "PROCESSED", "item2@example.com")
        );

        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(processedItems));

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("PROCESSED")))
                .andExpect(jsonPath("$[1].status", is("PROCESSED")));

        verify(itemService, times(1)).processItemsAsync();
    }
}