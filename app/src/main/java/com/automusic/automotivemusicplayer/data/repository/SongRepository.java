package com.automusic.automotivemusicplayer.data.repository;

import androidx.lifecycle.LiveData;
import com.automusic.automotivemusicplayer.data.models.Song;
import java.util.List;

public interface SongRepository {
    LiveData<List<Song>> getAllSongs();
    void insertSong(Song song);
}