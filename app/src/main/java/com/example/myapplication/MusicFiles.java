package com.example.myapplication;

public class MusicFiles { //Khởi tạo class lấy thông tin của một audio, song bao gồm các attribute như sau.
    private String path; //Đường dẫn
    private String title; //Tên bài hát
    private String artist; //Tác giả
    private String album; //Album của tác giả
    private String duration; //Thời lượng
    private String id;

    public MusicFiles(String path, String title, String artist, String album, String duration, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
    }

    public String getPath() {
        return path;
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
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public MusicFiles() {} ///Create object with empty attribute!!

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
