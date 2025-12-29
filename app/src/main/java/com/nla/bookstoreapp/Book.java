package com.nla.bookstoreapp;

public class Book {
    private String id, title, author, isbn,year, ownerId;

    public Book() {}

    public Book(String id, String title, String author, String isbn, String year, String ownerId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.year = year;
        this.ownerId = ownerId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOwnerId() { return ownerId; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getYear() { return year; }
}