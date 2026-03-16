package com.automusic.automotivemusicplayer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.automusic.automotivemusicplayer.data.models.Song;

import java.util.List;

@Dao
public interface SongDao {

    // Lấy toàn bộ danh sách bài hát và bọc trong LiveData để quan sát
    @Query("SELECT * FROM songs")
    LiveData<List<Song>> getAllSongs();

    // Thêm một bài hát mới. Nếu trùng lặp thì thay thế (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Song song);

    // Lệnh xóa toàn bộ bài hát (dùng khi reset app)
    @Query("DELETE FROM songs")
    void deleteAll();
}