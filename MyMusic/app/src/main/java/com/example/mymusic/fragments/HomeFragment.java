package com.example.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.HomeAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private final List<Object> homeItems = new ArrayList<>();
    private SongAdapter songAdapter;
    private ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<Playlist> playList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupHomeData();

        homeAdapter = new HomeAdapter(getContext(), homeItems);
        recyclerView.setAdapter(homeAdapter);

        return view;
    }

    private void setupHomeData() {
        // 1. Greeting section
        String greeting = getGreetingText();
        List<Song> recentSongs = getDummySongs("Recently Played");
        homeItems.add(new HomeAdapter.GreetingSection(greeting, recentSongs));

        // 2. More sections
        homeItems.add(new HomeAdapter.SectionData("Dành cho bạn", getDummySongs("For You")));
        homeItems.add(new HomeAdapter.SectionData("Nghệ sĩ yêu thích", getDummySongs("Favorite Artists")));
        homeItems.add(new HomeAdapter.SectionData("Playlist thịnh hành", getDummySongs("Trending")));
    }

    private String getGreetingText() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng ☀️";
        else if (hour < 18) return "Chào buổi chiều 🌤️";
        else return "Chào buổi tối 🌙";
    }

    // Dữ liệu mẫu
    private List<Song> getDummySongs(String category) {
        List<Song> list = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Song s = new Song();
            list.add(s);
        }
        return list;
    }
}
