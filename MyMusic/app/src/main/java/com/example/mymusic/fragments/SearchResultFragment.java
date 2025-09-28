package com.example.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusic.R;
import com.example.mymusic.adapters.SearchResultAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.SearchItem;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchResultFragment extends Fragment {

    private RecyclerView rvResult;
    private TextView tvEmpty;
    private SearchResultAdapter adapter;
    private List<SearchItem> searchList = new ArrayList<>();
    private MusicRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result, container, false);

        rvResult = v.findViewById(R.id.rv_search_result);
        tvEmpty = v.findViewById(R.id.tvEmptyResult);

        adapter = new SearchResultAdapter(searchList, getContext());
        rvResult.setLayoutManager(new LinearLayoutManager(getContext()));
        rvResult.setAdapter(adapter);

        repo = new MusicRepository(); // class bạn đã có để getAllSongs + getAllArtists

        return v;
    }

    /**
     * Hàm search được gọi từ FragmentSearch
     */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateUI(new ArrayList<>());
            return;
        }

        String lowerQuery = query.toLowerCase(Locale.ROOT);

        repo.getAllArtists(artists -> {
            repo.getAllSongs(songs -> {
                List<SearchItem> result = new ArrayList<>();

                // --- Tìm theo Artist ---
                for (Artist a : artists) {
                    if (a.getName().toLowerCase().contains(lowerQuery)) {
                        // add chính Artist
                        result.add(new SearchItem(
                                a.getArtistID(),
                                a.getName(),
                                "Nghệ sĩ",
                                a.getAvatar(),
                                true
                        ));

                        // add toàn bộ bài hát của Artist này
                        for (Song s : songs) {
                            if (s.getArtistID().equals(a.getArtistID())) {
                                result.add(new SearchItem(
                                        s.getSongID(),
                                        s.getTitle(),
                                        "Bài hát - " + a.getName(),
                                        s.getCoverUrl(),
                                        false
                                ));
                            }
                        }
                    }
                }

                // --- Tìm theo Song ---
                for (Song s : songs) {
                    if (s.getTitle().toLowerCase().contains(lowerQuery)) {
                        // add Song
                        result.add(new SearchItem(
                                s.getSongID(),
                                s.getTitle(),
                                "Bài hát - " + getArtistName(s.getArtistID(), artists),
                                s.getCoverUrl(),
                                false
                        ));

                        // add Nghệ sĩ của bài hát
                        Artist songArtist = getArtistById(s.getArtistID(), artists);
                        if (songArtist != null) {
                            // tránh trùng lặp nếu đã add rồi
                            boolean exists = false;
                            for (SearchItem item : result) {
                                if (item.isArtist() && item.getId().equals(songArtist.getArtistID())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                result.add(new SearchItem(
                                        songArtist.getArtistID(),
                                        songArtist.getName(),
                                        "Nghệ sĩ",
                                        songArtist.getAvatar(),
                                        true
                                ));
                            }
                        }
                    }
                }

                updateUI(result);
            });
        });
    }

    /**
     * Update UI kết quả tìm kiếm
     */
    private void updateUI(List<SearchItem> result) {
        if (result.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvResult.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvResult.setVisibility(View.VISIBLE);
            adapter.updateList(result);
        }
    }

    /**
     * Tìm tên nghệ sĩ từ danh sách
     */
    private String getArtistName(String artistID, List<Artist> artists) {
        for (Artist a : artists) {
            if (a.getArtistID().equals(artistID)) return a.getName();
        }
        return "Unknown";
    }

    private Artist getArtistById(String artistID, List<Artist> artists) {
        for (Artist a : artists) {
            if (a.getArtistID().equals(artistID)) return a;
        }
        return null;
    }
}
