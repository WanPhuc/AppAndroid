package com.example.mymusic.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mymusic.fragments.AccountFragment;
import com.example.mymusic.fragments.HomeFragment;
import com.example.mymusic.fragments.LibraryFragment;
import com.example.mymusic.fragments.SearchFragment;

public class MainAdapter extends FragmentStateAdapter {

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
            case 3: return new AccountFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // chỉ còn 3 tab chính
    }
}
