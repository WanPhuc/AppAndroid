package com.example.mymusic.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.fragments.CategoryPlaylistFragment;
import com.example.mymusic.models.Artist;

import java.util.ArrayList;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    private final ArrayList<Artist> artists;

    public ArtistAdapter(ArrayList<Artist> artists) {
        this.artists = artists;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.tvName.setText(artist.getName());

        Glide.with(holder.itemView.getContext())
                .load(artist.getAvatar())
                .placeholder(R.drawable.artist)
                .into(holder.ivAvatar);

        holder.itemView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            CategoryPlaylistFragment fragment = new CategoryPlaylistFragment();
            Bundle args = new Bundle();
            args.putString("type", "artist");
            args.putString("value", artist.getArtistID());
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_up,  // enter
                            R.anim.fade_out,     // exit
                            R.anim.fade_in,      // popEnter
                            R.anim.slide_out_down // popExit
                    )
                    .replace(R.id.container_main, fragment)
                    .addToBackStack(null)
                    .commit();

            ViewPager2 viewPager = activity.findViewById(R.id.vp_fragmain);
            viewPager.setVisibility(View.GONE);

            FrameLayout container = activity.findViewById(R.id.container_main);
            container.setVisibility(View.VISIBLE);
        });


    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;

        ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.imgArtist);
            tvName = itemView.findViewById(R.id.tvArtistName);
        }
    }
}
