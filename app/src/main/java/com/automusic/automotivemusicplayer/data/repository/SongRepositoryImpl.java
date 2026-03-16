package com.automusic.automotivemusicplayer.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.automusic.automotivemusicplayer.data.local.AppDatabase;
import com.automusic.automotivemusicplayer.data.local.SongDao;
import com.automusic.automotivemusicplayer.data.models.Song;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongRepositoryImpl implements SongRepository {

    private final SongDao songDao;
    private final LiveData<List<Song>> allSongs;
    // Dùng ExecutorService để chạy ngầm các lệnh DB, tránh làm đơ UI chính
    private final ExecutorService executorService;

    public SongRepositoryImpl(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        songDao = database.songDao();
        allSongs = songDao.getAllSongs();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    @Override
    public void insertSong(Song song) {
        // Thực thi lệnh insert trên một luồng phụ (background thread)
        executorService.execute(() -> songDao.insert(song));
    }
}