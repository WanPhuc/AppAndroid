package com.example.mymusic.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mymusic.fragments.CategoryPlaylistFragment;
import com.example.mymusic.fragments.HomeFragment;
import com.example.mymusic.fragments.LibraryFragment;
import com.example.mymusic.fragments.PlaySongFragment;
import com.example.mymusic.fragments.SearchFragment;

public class MainAdapter extends FragmentStateAdapter {
    private Bundle categoryArgs, playsongArgs; // để truyền bundle cho CategoryPlaylistFragment

    public MainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new SearchFragment();
            case 2: return new LibraryFragment();
            case 3:
                CategoryPlaylistFragment fragment = new CategoryPlaylistFragment();
                if (categoryArgs != null) fragment.setArguments(categoryArgs);
                return fragment;
            case 4:
                PlaySongFragment playsongFragment = new PlaySongFragment();
                if (playsongArgs != null) playsongFragment.setArguments(playsongArgs);
                return playsongFragment;
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // tăng lên 4
    }

    // Hàm này để set bundle cho tab CategoryPlaylist
    public void setCategoryArgs(Bundle args) {
        this.categoryArgs = args;
        notifyItemChanged(3); // refresh tab 3 (CategoryPlaylistFragment)
    }
    public void setPlaySongArgs(Bundle args) {
        this.playsongArgs = args;
        notifyItemChanged(4); // refresh tab 3 (CategoryPlaylistFragment)
    }
}

