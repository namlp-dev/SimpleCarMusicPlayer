package com.automusic.automotivemusicplayer.service;

import android.content.Context;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Trình quản lý phát nhạc (Singleton Pattern).
 * Đảm bảo chỉ có một instance của ExoPlayer tồn tại trong toàn bộ vòng đời ứng dụng.
 */
public class AudioPlayerManager {

    private static AudioPlayerManager instance;
    private ExoPlayer player;

    // Private constructor để chặn việc khởi tạo từ bên ngoài bằng từ khóa 'new'
    private AudioPlayerManager(Context context) {
        // Khởi tạo ExoPlayer của thư viện Media3
        player = new ExoPlayer.Builder(context.getApplicationContext()).build();
    }

    // Phương thức lấy instance duy nhất (Singleton)
    public static AudioPlayerManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AudioPlayerManager.class) {
                if (instance == null) {
                    instance = new AudioPlayerManager(context);
                }
            }
        }
        return instance;
    }

    // Trả về đối tượng Player để kết nối với MediaSession
    public ExoPlayer getPlayer() {
        return player;
    }

    // Hàm tiện ích để phát 1 URL hoặc đường dẫn file
    public void playMedia(String url) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    // Giải phóng tài nguyên khi không còn sử dụng
    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
        instance = null;
    }
}