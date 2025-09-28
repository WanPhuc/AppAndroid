package com.example.mymusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.models.Song;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private final ArrayList<Song> songs;
    public SongAdapter(ArrayList<Song> songs) {
        this.songs = songs;
    }
    @Nonnull
    @Override
    public SongViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song,parent,false);
        return new SongViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@Nonnull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.nameSong.setText(song.getTitle());
        holder.nameArtist.setText(song.getArtistID());

        Glide.with(holder.itemView.getContext())
                .load(song.getCoverUrl())
                .placeholder(R.drawable.song)
                .into(holder.imgSong);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong;
        TextView nameSong, nameArtist;
        public SongViewHolder(@Nonnull View itemView) {
            super(itemView);
            imgSong = itemView.findViewById(R.id.img_song);
            nameSong = itemView.findViewById(R.id.tv_nameSong);
            nameArtist = itemView.findViewById(R.id.tv_nameArtist);
        }

    }
}
