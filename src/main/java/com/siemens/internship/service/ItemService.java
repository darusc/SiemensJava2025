package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private final List<Item> processedItems = new CopyOnWriteArrayList<>();
    private AtomicInteger processedCount = new AtomicInteger(0);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        // Clear the processed item list and reset the processed count
        processedItems.clear();
        processedCount.set(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Item item = itemRepository.findById(id).orElse(null);

                    // Skip already processed items as well
                    if (item == null || item.getStatus().equals("PROCESSED")) {
                        return;
                    }

                    item.setStatus("PROCESSED");

                    itemRepository.save(item);

                    // Processed items is now a thread safe synchronized list
                    processedItems.add(item);
                    processedCount.incrementAndGet();

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        // Wait for all items to be processed
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> processedItems);
    }
}