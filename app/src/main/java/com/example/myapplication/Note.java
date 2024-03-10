package com.example.myapplication;

import java.util.List;

public class Note {
    private String title, description, documentId;
    private int priority;
    List<String> tags;

    public Note() {
        //Required empty constructor
    }

    public Note(String title, String description, int priority, List<String> tags) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }
}

