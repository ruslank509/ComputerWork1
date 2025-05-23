package com.example.computerwork;
public class Product {
    public String name;
    public String price;
    public String model;
    public String inventoryId;

    public Product(String name, String price, String model, String inventoryId) {
        this.name = name;
        this.price = price;
        this.model = model;
        this.inventoryId = inventoryId;
    }
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getModel() {
        return model;
    }

    public String getInventory() {
        return inventoryId;
    }
}

