package com.cloudorder.inventory.service;

import com.cloudorder.inventory.repository.InventoryRepository;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(
            InventoryRepository repository) {

        this.repository = repository;
    }

    public void createInventory(

            String productId,

            String productName,

            int quantity) {

        repository.create(
                productId,
                productName,
                quantity
        );
    }

    public boolean reserve(

            String productId,

            int quantity) {

        return repository.reserve(
                productId,
                quantity
        );
    }
}