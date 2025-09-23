package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import javax.annotation.Nonnull;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private final Context context;
    private final List<Object> items;
    public SearchResultAdapter(Context context, List<Object> items){
        this.context=context;
        this.items=items;
    }
    @Nonnull
    @Override
    public ViewHolder onCreateViewHolder(@Nonnull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_search_result,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@Nonnull ViewHolder holder, int position) {
        Object item=items.get(position);

        if (item instanceof Song){
            holder.bindSong((Song) item);
        }
        else if (item instanceof Artist){
            holder.bindArtist((Artist) item);
        }

    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivSong;
        ShapeableImageView imgArtist;
        TextView tvMain, tvSecond;
        public ViewHolder(@Nonnull View itemView){
            super(itemView);
            ivSong=itemView.findViewById(R.id.iv_item);
            imgArtist=itemView.findViewById(R.id.imgArtist);
            tvMain=itemView.findViewById(R.id.tv_textMain);
            tvSecond=itemView.findViewById(R.id.tv_textSecond);
        }

        void bindSong(Song song){
            ivSong.setVisibility(View.VISIBLE);
            imgArtist.setVisibility(View.GONE);
            tvMain.setText(song.getTitle());
            tvSecond.setText("Bài Hát");

            Glide.with(itemView.getContext())
                    .load(song.getCoverUrl())
                    .placeholder(R.drawable.artist)
                    .into(ivSong);
        }
        void bindArtist(Artist artist){
            ivSong.setVisibility(View.GONE);
            imgArtist.setVisibility(View.VISIBLE);
            tvMain.setText(artist.getName());
            tvSecond.setText("Nghệ Sĩ");

            Glide.with(itemView.getContext())
                    .load(artist.getAvatar())
                    .placeholder(R.drawable.artist)
                    .into(imgArtist);
        }
    }

}
