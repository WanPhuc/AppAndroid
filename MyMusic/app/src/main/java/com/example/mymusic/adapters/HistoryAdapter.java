package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SONG = 1;
    private  ArrayList<Playlist> playlists;
    private  ArrayList<Artist> artists;
    private  ArrayList<Song> songs;

    private List<Object> items; // DateHeader hoặc Song
    private Context context;
    private SongAdapter songAdapter;
    public MusicPlayerService musicPlayerService;
    private  boolean isServiceBound = false;
    public void setMusicPlayerService(MusicPlayerService service) {
        this.musicPlayerService = service;
    }

    public HistoryAdapter(Context context, List<Object> items,SongAdapter songAdapter ) {
        this.context = context;
        this.items = items;
        this.songAdapter = songAdapter;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) return TYPE_HEADER; // ngày
        else return TYPE_SONG; // bài hát
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_history_header, parent, false);
            return new HeaderHolder(v);
        } else {
            // 👉 Reuse SongAdapter ViewHolder + layout bài hát
            View v = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
            return new SongAdapter.SongViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).tvDate.setText((String) item);
        } else if (item instanceof Song) {
            Song song = (Song) item;
            SongAdapter.SongViewHolder vh = (SongAdapter.SongViewHolder) holder;
            songAdapter.bindSongView(vh, song, position,musicPlayerService);
        }

    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderHolder(View v) { super(v); tvDate = v.findViewById(R.id.tv_date); }
    }
}

