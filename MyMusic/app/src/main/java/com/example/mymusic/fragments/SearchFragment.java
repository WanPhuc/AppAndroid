package com.example.mymusic.fragments;

import android.app.appsearch.SearchResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.SearchAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private MusicRepository repo;
    private SearchView searchView;
    private FrameLayout frameLayout;

    private final ArrayList<Song> allSongs = new ArrayList<>();
    private final ArrayList<Artist> allArtists = new ArrayList<>();
    private final List<Artist> cachedArtists = new ArrayList<>();
    private final List<Playlist> cachedPlaylists = new ArrayList<>();
    private final List<String> cachedGenres = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerSearch);
        searchView = view.findViewById(R.id.searchView);
        frameLayout= view.findViewById(R.id.containerResult);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repo = new MusicRepository();

        // load fragment kết quả ngay từ đầu (ẩn sẵn)
        getChildFragmentManager().beginTransaction()
                .replace(R.id.containerResult, new SearchResultFragment())
                .commit();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                showResult(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    showDefault();
                } else {
                    showResult(newText);
                }
                return true;
            }
        });

        loadDefaultData();

        return view;
    }

    private void loadDefaultData() {
        repo.listenAllArtists(artists -> {
            cachedArtists.clear();
            cachedArtists.addAll(artists);
            updateItems();
        });

        repo.listenPlaylistsByType(playlists -> {
            cachedPlaylists.clear();
            cachedPlaylists.addAll(playlists);
            updateItems();
        });

        repo.listenAllGenres(genres -> {
            cachedGenres.clear();
            cachedGenres.addAll(genres);
            updateItems();
        });
    }

    private void updateItems() {
        List<Object> items = new ArrayList<>();

        if (!cachedArtists.isEmpty()) {
            items.add("Nghệ sĩ thịnh hành");
            items.add(new ArrayList<>(cachedArtists));
        }

        if (!cachedPlaylists.isEmpty()) {
            items.add("Khám phá theo chủ đề");
            items.add(new ArrayList<>(cachedPlaylists));
        }

        if (!cachedGenres.isEmpty()) {
            items.add("Khám phá thể loại");
            items.add(new ArrayList<>(cachedGenres));
        }

        adapter.updateData(items);
    }

    private void showResult(String query) {
        recyclerView.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

        SearchResultFragment resultFragment = (SearchResultFragment)
                getChildFragmentManager().findFragmentById(R.id.containerResult);

        if (resultFragment != null) {
            resultFragment.search(query);
        }
    }

    private void showDefault() {
        recyclerView.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
    }

}
