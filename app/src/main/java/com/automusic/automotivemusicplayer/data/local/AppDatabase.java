package com.automusic.automotivemusicplayer.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.automusic.automotivemusicplayer.data.models.Song;

// Khai báo các Entity (bảng) sẽ có trong DB. Version = 1
@Database(entities = {Song.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SongDao songDao(); // Cung cấp DAO

    private static volatile AppDatabase instance;

    // Singleton Pattern: Đảm bảo chỉ có 1 kết nối DB duy nhất
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "automusic_db")
                            .fallbackToDestructiveMigration() // Tự động xóa data cũ nếu đổi version kiến trúc
                            .build();
                }
            }
        }
        return instance;
    }
}