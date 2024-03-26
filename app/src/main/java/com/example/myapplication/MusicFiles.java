package com.example.myapplication;

public class MusicFiles { //Khởi tạo class lấy thông tin của một audio, song bao gồm các attribute như sau.
    private String path; //Đường dẫn
    private String title; //Tên bài hát
    private String artist; //Tác giả
    private String album; //Album của tác giả
    private int duration; //Thời lượng
    private String image;
    private String id;
    private String key;

    public MusicFiles(String path, String title, String artist, int duration, String album, String image) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.image = image;
    }

    public MusicFiles(String path, String title, String artist, String album, int duration, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
    }

    public String getPath() {
        return path != null ? path : "";
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MusicFiles() {} ///Create object with empty attribute!!

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}