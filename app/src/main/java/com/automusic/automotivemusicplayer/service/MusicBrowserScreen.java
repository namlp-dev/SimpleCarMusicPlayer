package com.automusic.automotivemusicplayer.service;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.ListTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;

/**
 * Giao diện màn hình chính của ứng dụng trên Android Auto.
 */
public class MusicBrowserScreen extends Screen {

    public MusicBrowserScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        // 1. Tạo các hàng dữ liệu (Menu)
        ItemList.Builder listBuilder = new ItemList.Builder()
                .addItem(new Row.Builder().setTitle("🎵 Tất cả bài hát").build())
                .addItem(new Row.Builder().setTitle("⭐ Thư viện Yêu thích").build())
                .addItem(new Row.Builder().setTitle("☁️ Nhạc Ngoại tuyến (Offline)").build());

        // 2. Lắp các hàng này vào khuôn mẫu Danh sách (ListTemplate) của Google
        return new ListTemplate.Builder()
                .setTitle("Thư Viện AutoMusic")
                .setHeaderAction(Action.APP_ICON) // Hiển thị icon app ở góc trái
                .setSingleList(listBuilder.build())
                .build();
    }
}