package com.virtualthreads.bookstore.model;

import lombok.Data;

@Data
public class Book {
    private String title;
    private String author;
    private double price;
}
