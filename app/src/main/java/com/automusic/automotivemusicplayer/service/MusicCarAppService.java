package com.automusic.automotivemusicplayer.service;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Session;
import androidx.car.app.Screen;
import androidx.car.app.validation.HostValidator;

/**
 * Cầu nối giúp ứng dụng hiển thị lên giao diện Android Auto chuẩn của Google.
 */
public class MusicCarAppService extends CarAppService {

    @NonNull
    @Override
    public HostValidator createHostValidator() {
        // Cho phép chạy trên mọi máy ảo và thiết bị để dễ dàng test
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        // Tạo một phiên làm việc (Session) mới khi xe hơi kết nối vào
        return new Session() {
            @NonNull
            @Override
            public Screen onCreateScreen(@NonNull Intent intent) {
                // Điều hướng xe hiển thị màn hình Thư viện nhạc đầu tiên
                return new MusicBrowserScreen(getCarContext());
            }
        };
    }
}