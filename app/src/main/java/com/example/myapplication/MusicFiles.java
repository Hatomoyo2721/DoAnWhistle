package com.example.myapplication;

public class MusicFiles { //Khởi tạo class lấy thông tin của một audio, song bao gồm các attribute như sau.
    private String path; //Đường dẫn
    private String title; //Tên bài hát
    private String artist; //Tác giả
    private String album; //Album của tác giả
    private int duration; //Thời lượng
    private String image;
    private String id_song;
    private String key;
    private static int count = 0;

    public MusicFiles(String path, String title, String artist, int duration, String album, String image) {
        this.id_song = "MUSIC_" + count++;
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.image = image;
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

    public String getId_song() {
        return id_song;
    }

    public void setId_song(String id_song) {
        this.id_song = id_song;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MusicFiles() {
    } ///Create object with empty attribute!!
}
