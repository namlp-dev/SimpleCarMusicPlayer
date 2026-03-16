package com.automusic.automotivemusicplayer.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.automusic.automotivemusicplayer.data.models.Song;
import com.automusic.automotivemusicplayer.data.repository.SongRepository;
import com.automusic.automotivemusicplayer.data.repository.SongRepositoryImpl;
import com.automusic.automotivemusicplayer.service.AudioPlayerManager;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.lang.reflect.Field;
import com.automusic.automotivemusicplayer.R;

import java.util.List;

public class PlayerViewModel extends AndroidViewModel {

    private final SongRepository repository;
    private final AudioPlayerManager playerManager;

    // Dữ liệu quan sát được (LiveData)
    private final LiveData<List<Song>> allSongs;
    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        repository = new SongRepositoryImpl(application);
        playerManager = AudioPlayerManager.getInstance(application);
        allSongs = repository.getAllSongs();
    }

    // Các hàm để Giao diện (Activity) lấy dữ liệu ra hiển thị
    public LiveData<List<Song>> getAllSongs() { return allSongs; }
    public LiveData<Song> getCurrentSong() { return currentSong; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }

    // Logic phát nhạc
    // Chỉ cập nhật dữ liệu bài hát hiện tại, việc phát nhạc sẽ nhường cho MediaController ở Activity lo
    public void selectSong(Song song) {
        currentSong.setValue(song);
    }

    // Nạp một bài hát mẫu vào Database (để test offline)
    // Thay thế hàm addSampleSong() cũ bằng hàm này
    // Nạp tự động toàn bộ file nhạc trong thư mục raw
    public void addSamplePlaylist() {
        // Chạy trên một luồng phụ (Thread riêng) để việc đọc file không làm đơ màn hình
        new Thread(() -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            // Lấy toàn bộ danh sách các file nằm trong thư mục R.raw
            Field[] fields = R.raw.class.getFields();

            for (Field field : fields) {
                try {
                    // Lấy ID của file và tên gốc của file
                    int resourceId = field.getInt(field);
                    String fileName = field.getName();

                    // Tạo đường dẫn Uri đến file raw
                    Uri uri = Uri.parse("android.resource://" + getApplication().getPackageName() + "/" + resourceId);

                    try {
                        // Nạp file vào máy quét metadata
                        retriever.setDataSource(getApplication(), uri);

                        // Trích xuất thông tin (ID3 Tags)
                        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                        // Nếu file mp3 không có sẵn thông tin (bị trống), ta dùng tên file làm tên bài hát
                        if (title == null || title.trim().isEmpty()) {
                            title = fileName;
                        }
                        if (artist == null || artist.trim().isEmpty()) {
                            artist = "Unknown Artist";
                        }

                        long duration = 0;
                        if (durationStr != null) {
                            duration = Long.parseLong(durationStr);
                        }

                        // Lưu vào Cơ sở dữ liệu Room
                        Song song = new Song(title, artist, uri.toString(), duration);
                        repository.insertSong(song);

                    } catch (Exception e) {
                        e.printStackTrace(); // Bỏ qua nếu có file không phải là nhạc
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // Giải phóng máy quét sau khi quét xong toàn bộ file
            try {
                retriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}