package com.automusic.automotivemusicplayer.service;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

/**
 * Service chạy ngầm chịu trách nhiệm phát nhạc.
 * MediaSessionService của Media3 sẽ tự động hiển thị Notification điều khiển nhạc.
 */
public class AutoMusicService extends MediaSessionService {

    private MediaSession mediaSession;
    private AudioPlayerManager playerManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Lấy instance của ExoPlayer từ Singleton Manager
        playerManager = AudioPlayerManager.getInstance(this);
        ExoPlayer player = playerManager.getPlayer();

        // 2. Khởi tạo MediaSession và gắn ExoPlayer vào
        // Hành động này giúp xe hơi biết được app đang phát nhạc và tự động hiển thị lên bảng điều khiển
        mediaSession = new MediaSession.Builder(this, player).build();
    }

    // Hàm bắt buộc: Trả về session khi các UI (như PlayerActivity) hoặc vô lăng xe hơi kết nối vào
    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    // Hủy bỏ tài nguyên khi Service bị tắt để tránh tràn RAM (Memory Leak)
    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.getPlayer().release();
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }

    // Đảm bảo Service không bị lỗi khi tắt đi bật lại trong một số trường hợp của Android Auto
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}