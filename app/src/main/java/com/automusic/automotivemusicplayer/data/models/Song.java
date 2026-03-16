package com.automusic.automotivemusicplayer.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Room sẽ tự động tạo một bảng tên là "songs" trong cơ sở dữ liệu
@Entity(tableName = "songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int id; // ID tự tăng

    private String title;
    private String artist;
    private String filePath; // Đường dẫn file nhạc trên xe hoặc URL
    private long duration;   // Thời lượng bài hát

    // Constructor
    public Song(String title, String artist, String filePath, long duration) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.duration = duration;
    }

    // --- Getters và Setters (Bắt buộc để Room có thể đọc/ghi dữ liệu) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}