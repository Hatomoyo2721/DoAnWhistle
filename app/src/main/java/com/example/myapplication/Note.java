package com.example.myapplication;

public class Note {
    private String title, description;

    public Note() {
        //Required empty constructor
    }

    public Note(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

}
