package com.example.mymusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.SearchItem;
import com.example.mymusic.models.Song;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import javax.annotation.Nonnull;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<SearchItem> items;
    private Context context;

    public SearchResultAdapter(List<SearchItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItem item = items.get(position);

        holder.tvMain.setText(item.getMainText());
        holder.tvSecond.setText(item.getSubText());

        if (item.isArtist()) {
            holder.ivItem.setVisibility(View.GONE);
            holder.imgArtist.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.artist)
                    .into(holder.imgArtist);
        } else {
            holder.imgArtist.setVisibility(View.GONE);
            holder.ivItem.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.artist)
                    .into(holder.ivItem);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<SearchItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        ShapeableImageView imgArtist;
        TextView tvMain, tvSecond;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = itemView.findViewById(R.id.iv_item);
            imgArtist = itemView.findViewById(R.id.imgArtist);
            tvMain = itemView.findViewById(R.id.tv_textMain);
            tvSecond = itemView.findViewById(R.id.tv_textSecond);
        }
    }
}

