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
import com.example.mymusic.adapters.SearchAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private MusicRepository repo;

    private final ArrayList<Song> allSongs = new ArrayList<>();
    private final ArrayList<Artist> allArtists = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerSearch);
        SearchView searchView = view.findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repo = new MusicRepository();

        // Load data từ Firestore
        repo.getAllSongs(result -> {
            allSongs.clear();
            allSongs.addAll(result);
        });

        repo.getAllArtists(result -> {
            allArtists.clear();
            allArtists.addAll(result);
        });


        // Load mặc định khi chưa search
        loadDefaultData();

        return view;
    }

    private void loadDefaultData() {
        List<Object> items = new ArrayList<>();

        repo.getAllArtists(artists -> {
            items.add("Nghệ sĩ thịnh hành");
            items.add(artists); // giữ nguyên thành list<Artist>

            repo.getAllGenres(genres -> {
                items.add("Khám phá thể loại");
                items.add(new ArrayList<>(genres)); // giữ nguyên thành list<String>
                adapter.updateData(items);
            });
        });
    }

}
