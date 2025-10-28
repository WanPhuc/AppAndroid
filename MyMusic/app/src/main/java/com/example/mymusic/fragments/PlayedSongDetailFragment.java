package com.example.mymusic.fragments;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
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
import com.example.mymusic.adapters.HistoryAdapter;
import com.example.mymusic.adapters.HomeAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayedSongDetailFragment extends Fragment {
    private static final Logger log = LoggerFactory.getLogger(PlayedSongDetailFragment.class);
    private RecyclerView recyclerView;

    private HomeAdapter homeAdapter;
    private List<Object> homeItems = new ArrayList<>();
    private SongAdapter songAdapter;
    private ArrayList<Song> songsList = new ArrayList<>();
    private  ArrayList<Song> playedList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private  ArrayList<Playlist> playList = new ArrayList<>();
    private  ArrayList<String> cachedGenres = new ArrayList<>();
    private MusicRepository repo;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_recently_played_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_items_played);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repo = new MusicRepository();
        songAdapter = new SongAdapter(getContext(), playedList, playList, artistList);
        recyclerView.setAdapter(songAdapter);
        loadDefaultData();
        return view;
    }

    private void loadDefaultData() {
        repo.listenAllArtists(artists -> {
            artistList.clear();
            artistList.addAll(artists);
            songAdapter.notifyDataSetChanged();
            songAdapter.updateArtists(artists);
        });
        List<Object> displayList = new ArrayList<>();
        HistoryAdapter historyAdapter = new HistoryAdapter(getContext(), displayList, songAdapter);
        recyclerView.setAdapter(historyAdapter);

        repo.listenRecentlyPlayed(songs -> {
            displayList.clear();
            final String[] lastDate = {""};

            for (Song song : songs) {
                String userID = FirebaseAuth.getInstance().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Users")
                        .document(userID)
                        .collection("RecentlyPlayed")
                        .document(song.getSongID())
                        .get()
                        .addOnSuccessListener(doc -> {
                            Timestamp ts = doc.getTimestamp("timestamp");
                            if (ts == null) return;

                            String date = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ts.toDate());

                            if (!date.equals(lastDate)) {
                                displayList.add(date);
                                lastDate[0] = date;    // ✅ Cập nhật biến ngày
                            }

                            displayList.add(song);
                            historyAdapter.notifyDataSetChanged();
                        });
            }
        });
        //loadSongsFromFirestore();
    }

}
