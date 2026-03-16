package com.automusic.automotivemusicplayer.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.automusic.automotivemusicplayer.R;
import com.automusic.automotivemusicplayer.data.models.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SongViewHolder> {

    private List<Song> songList = new ArrayList<>();
    private final OnItemClickListener listener;
    private String currentPlayingUri = ""; // Biến để biết bài nào đang phát nhằm đổi màu

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public PlaylistAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }

    public void setCurrentPlaying(String uri) {
        this.currentPlayingUri = uri;
        notifyDataSetChanged(); // Tải lại danh sách để tô màu bài đang phát
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());

        // Nếu bài này đang phát, đổi màu chữ thành Xanh lá. Ngược lại thì Trắng.
        if (song.getFilePath().equals(currentPlayingUri)) {
            holder.tvTitle.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvTitle.setTextColor(Color.WHITE);
        }

        // Bắt sự kiện Click để chọn bài
        holder.itemView.setOnClickListener(v -> listener.onItemClick(song, position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvArtist = itemView.findViewById(R.id.tv_item_artist);
        }
    }
}