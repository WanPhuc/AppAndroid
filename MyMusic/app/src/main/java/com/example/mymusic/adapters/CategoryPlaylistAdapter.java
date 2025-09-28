package com.example.mymusic.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusic.R;
import com.example.mymusic.models.Playlist;

import java.util.ArrayList;
import java.util.Random;

public class CategoryPlaylistAdapter extends RecyclerView.Adapter<CategoryPlaylistAdapter.CategoryPlaylistViewHolder> {
    private final ArrayList<Playlist> playlists;
    private final Random random;
    public CategoryPlaylistAdapter(ArrayList<Playlist> playlists) {
        this.playlists = playlists;
        this.random = new Random();
    }
    @Override
    public CategoryPlaylistViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_playlist_search,parent,false);
        return new CategoryPlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryPlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        // show title
        holder.tvCategoryPlaylist.setText(playlist.getTitle());
        holder.itemView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            ViewPager2 viewPager = activity.findViewById(R.id.vp_fragmain);
            MainAdapter adapter = (MainAdapter) viewPager.getAdapter();

            Bundle args = new Bundle();
            args.putString("type", "playlist");
            args.putString("value", playlist.getPlaylistID()); // truyền ID để lấy song list

            adapter.setCategoryArgs(args);
            viewPager.setCurrentItem(3, true);
        });


        int backgroundColor;
        int textColor = holder.tvCategoryPlaylist.getCurrentTextColor();
        do {
            backgroundColor = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        } while (ColorUtils.calculateContrast(textColor, backgroundColor) < 1.5); // Ensure minimum contrast ratio

        Drawable background = holder.itemView.getBackground();
        if (background instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) background;
            Drawable currentDrawable = stateListDrawable.getCurrent();
            if (currentDrawable instanceof GradientDrawable) {
                // Mutate the drawable to avoid affecting other items if they share the same drawable instance
                GradientDrawable gradientDrawable = (GradientDrawable) currentDrawable.mutate();
                gradientDrawable.setColor(backgroundColor);
            } else {

                holder.itemView.setBackgroundColor(backgroundColor); // Or some other fallback
            }
        } else if (background instanceof GradientDrawable) {
            // If the background was already a GradientDrawable (e.g., if not using a selector)
            GradientDrawable gradientDrawable = (GradientDrawable) background.mutate();
            gradientDrawable.setColor(backgroundColor);
        } else {
            // Fallback for other drawable types or if no background is set
            holder.itemView.setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class CategoryPlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryPlaylist;
        public CategoryPlaylistViewHolder(android.view.View itemView) {
            super(itemView);
            tvCategoryPlaylist = itemView.findViewById(R.id.tvCategoryPlaylistName);
        }
    }
}
