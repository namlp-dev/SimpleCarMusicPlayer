package com.automusic.automotivemusicplayer.ui;

import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.automusic.automotivemusicplayer.R;
import com.automusic.automotivemusicplayer.databinding.ActivityPlayerBinding;
import com.automusic.automotivemusicplayer.data.models.Song;
import com.automusic.automotivemusicplayer.service.AutoMusicService;
import com.automusic.automotivemusicplayer.viewmodel.PlayerViewModel;
import com.automusic.automotivemusicplayer.voice.VoiceCommandManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;


public class PlayerActivity extends AppCompatActivity {

    private PlaylistAdapter playlistAdapter;
    private ActivityPlayerBinding binding;
    // Nằm bên dưới khai báo VoiceCommandManager
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressTask;
    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;

    // Khai báo các module quản lý
    private PlayerViewModel viewModel;
    private VoiceCommandManager voiceCommandManager;

    // Bộ công cụ xin quyền Micro của hệ điều hành
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    voiceCommandManager.startListening();
                    Toast.makeText(this, "Đang nghe...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cần cấp quyền Micro để dùng giọng nói!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel (Bộ não trung tâm)
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        // --- SETUP RECYCLERVIEW ---
        playlistAdapter = new PlaylistAdapter((song, position) -> {
            // Khi người dùng bấm vào 1 bài trong danh sách
            if (mediaController != null) {
                // Nhảy đến đúng vị trí (index) của bài đó trong Playlist của Media3
                mediaController.seekToDefaultPosition(position);
                mediaController.play();
            }
        });
        binding.rvPlaylist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPlaylist.setAdapter(playlistAdapter);
        // --------------------------
        // 2. Quan sát Bài hát hiện tại để cập nhật Giao diện
        viewModel.getCurrentSong().observe(this, song -> {
            if (song != null) {
                binding.tvTitle.setText(song.getTitle());
                binding.tvArtist.setText(song.getArtist());

                if (playlistAdapter != null) {
                    playlistAdapter.setCurrentPlaying(song.getFilePath());
                }
            }
        });

        // 3. Quan sát Database để nạp danh sách phát (Playlist)
        viewModel.getAllSongs().observe(this, songs -> {
            if (songs != null && songs.isEmpty()) {
                // Nếu DB trống, tạo playlist mẫu
                viewModel.addSamplePlaylist();
            } else if (songs != null && !songs.isEmpty()) {
                playlistAdapter.setSongs(songs);
                // Hiển thị bài đầu tiên nếu chưa có bài nào được chọn
                if (viewModel.getCurrentSong().getValue() == null) {
                    viewModel.selectSong(songs.get(0));
                }
                // Nạp danh sách vào Động cơ phát nhạc
                setupPlaylist(songs);
            }
        });

        // 4. Khởi tạo Voice Control (Nhận diện giọng nói)
        voiceCommandManager = new VoiceCommandManager(this, new VoiceCommandManager.VoiceCallback() {
            @Override
            public void onCommandRecognized(String command) {
                Toast.makeText(PlayerActivity.this, "Bạn nói: " + command, Toast.LENGTH_SHORT).show();
                processVoiceCommand(command);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(PlayerActivity.this, "Không nghe rõ, thử lại nhé!", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện bấm nút Micro
        binding.btnVoice.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                voiceCommandManager.startListening();
                Toast.makeText(this, "Đang nghe...", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
    }

    // Hàm chuyển đổi mili-giây thành định dạng 00:00
    private String formatTime(long millis) {
        if (millis < 0) millis = 0;
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bắt đầu kết nối với AutoMusicService (chạy ngầm)
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, AutoMusicService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                setupController(); // Cài đặt các nút bấm khi đã kết nối
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    // Hàm tiện ích: Biến danh sách Song thành Playlist cho ExoPlayer
    private void setupPlaylist(List<Song> songs) {
        if (mediaController == null || songs == null || songs.isEmpty()) return;

        // SỬA LỖI: Chỉ bỏ qua nếu số lượng bài hát trong Động cơ ĐÃ BẰNG với DB
        if (mediaController.getMediaItemCount() == songs.size()) return;

        // Lưu lại trạng thái đang phát để nhạc không bị giật/dừng khi nạp thêm bài mới
        int currentIndex = mediaController.getCurrentMediaItemIndex();
        long currentPosition = mediaController.getCurrentPosition();
        boolean wasPlaying = mediaController.isPlaying();

        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            mediaItems.add(new MediaItem.Builder()
                    .setMediaId(song.getFilePath()) // Gắn ID để dễ quản lý
                    .setUri(song.getFilePath())
                    .build());
        }

        // Nạp lại toàn bộ danh sách mới vào Động cơ
        mediaController.setMediaItems(mediaItems);

        // BẬT TÍNH NĂNG VÒNG TRÒN (Circular Playlist): Bài cuối -> Bài đầu
        mediaController.setRepeatMode(Player.REPEAT_MODE_ALL);

        mediaController.prepare();

        // Khôi phục lại bài hát đang phát dở (nếu có)
        if (currentIndex > -1 && currentIndex < mediaItems.size()) {
            mediaController.seekTo(currentIndex, currentPosition);
            if (wasPlaying) mediaController.play();
        }
    }

    private void setupController() {
        // Nạp playlist ngay khi kết nối thành công (trong trường hợp data đã tải xong từ trước)
        setupPlaylist(viewModel.getAllSongs().getValue());
        // Lắng nghe các trạng thái từ Động cơ (Media3)
        mediaController.addListener(new Player.Listener() {
            // Sự kiện: Khi bài hát thay đổi (Next/Prev/Tự chuyển bài)
            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                if (mediaItem != null && mediaItem.localConfiguration != null) {
                    String currentUri = mediaItem.localConfiguration.uri.toString();
                    playlistAdapter.setCurrentPlaying(currentUri);
                    List<Song> songs = viewModel.getAllSongs().getValue();
                    if (songs != null) {
                        // Tìm bài hát trong DB khớp với URI đang phát để cập nhật tên lên màn hình
                        for (Song s : songs) {
                            if (s.getFilePath().equals(currentUri)) {
                                viewModel.selectSong(s);
                                break;
                            }
                        }
                    }
                }
            }

            // Sự kiện: Khi nhạc Phát/Tạm dừng
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                binding.btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
        });

        // Gắn sự kiện Click cho các nút vật lý trên màn hình
        binding.btnPlayPause.setOnClickListener(v -> {
            if (mediaController.isPlaying()) mediaController.pause();
            else mediaController.play();
        });

        binding.btnNext.setOnClickListener(v -> mediaController.seekToNextMediaItem());
        binding.btnPrevious.setOnClickListener(v -> mediaController.seekToPreviousMediaItem());

        // --- LOGIC CHO SEEKBAR VÀ THỜI GIAN ---

        // Bắt sự kiện người dùng kéo thanh gạt để tua nhạc
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaController != null) {
                    mediaController.seekTo(progress);
                    binding.tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Tạm dừng cập nhật giao diện khi đang dùng tay kéo
                progressHandler.removeCallbacks(updateProgressTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Tiếp tục cập nhật giao diện khi thả tay ra
                progressHandler.post(updateProgressTask);
            }
        });

        // Tạo vòng lặp cập nhật thời gian mỗi 1 giây (1000ms)
        updateProgressTask = new Runnable() {
            @Override
            public void run() {
                if (mediaController != null && mediaController.isPlaying()) {
                    long currentPos = mediaController.getCurrentPosition();
                    long duration = mediaController.getDuration();

                    // Cập nhật giá trị lên thanh SeekBar
                    binding.seekBar.setMax((int) duration);
                    binding.seekBar.setProgress((int) currentPos);

                    // Cập nhật số liệu phút:giây
                    binding.tvCurrentTime.setText(formatTime(currentPos));
                    binding.tvTotalTime.setText(formatTime(duration));
                }
                progressHandler.postDelayed(this, 1000);
            }
        };
        // Kích hoạt vòng lặp lần đầu tiên
        progressHandler.post(updateProgressTask);
    }

    // Xử lý logic câu lệnh giọng nói
    private void processVoiceCommand(String command) {
        if (mediaController == null) return;

        if (command.contains("phát") || command.contains("chơi")) {
            mediaController.play();
        } else if (command.contains("dừng") || command.contains("tạm dừng")) {
            mediaController.pause();
        } else if (command.contains("tiếp")) {
            mediaController.seekToNextMediaItem();
        } else if (command.contains("trước")) {
            mediaController.seekToPreviousMediaItem();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Hủy vòng lặp đếm thời gian
        if (progressHandler != null && updateProgressTask != null) {
            progressHandler.removeCallbacks(updateProgressTask);
        }

        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture);
        }
        if (voiceCommandManager != null) {
            voiceCommandManager.destroy();
        }
    }
}