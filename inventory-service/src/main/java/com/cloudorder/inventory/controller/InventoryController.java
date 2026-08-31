package com.cloudorder.inventory.controller;

import com.cloudorder.inventory.dto.CreateInventoryRequest;
import com.cloudorder.inventory.dto.InventoryResponse;
import com.cloudorder.inventory.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(
            InventoryService service) {

        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> create(

            @Valid
            @RequestBody
            CreateInventoryRequest request) {

        service.createInventory(

                request.productId(),

                request.productName(),

                request.quantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> checkInventory(@PathVariable String productId) {
        try {
            Thread.sleep(5000);
        }catch(InterruptedException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(service.checkInventory(productId));
    }
}