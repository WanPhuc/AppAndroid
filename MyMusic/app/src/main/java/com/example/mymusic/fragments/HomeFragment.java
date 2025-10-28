package com.example.mymusic.fragments;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.mymusic.repository.MusicRepository;
import com.example.mymusic.services.MusicPlayerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements MusicPlayerService.PlayerListener{

    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private  List<Object> homeItems = new ArrayList<>();
    private SongAdapter songAdapter;
    private ArrayList<Song> songsList = new ArrayList<>();
    private  ArrayList<Song> playedList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private  ArrayList<Playlist> playList = new ArrayList<>();
    private  ArrayList<String> cachedGenres = new ArrayList<>();
    private FirebaseFirestore db;
    private MusicRepository repo;
    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;

    @Override
    public void onStart() {
        super.onStart();

        if (!isServiceBound) {
            android.content.Intent intent = new android.content.Intent(requireContext(), MusicPlayerService.class);
            requireContext().bindService(intent, serviceConnection, android.content.Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            isServiceBound = true;

            // Truyền service vào adapter
            if (homeAdapter != null) {
                homeAdapter.setMusicPlayerService(musicPlayerService);

            }

            // Đăng ký listener (UI update)
            musicPlayerService.addPlayerListener(HomeFragment.this);

            // Cung cấp danh sách nghệ sĩ cho service để notification hiển thị đúng
            if (!artistList.isEmpty()) {
                musicPlayerService.setArtists(artistList);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repo = new MusicRepository();
        homeAdapter = new HomeAdapter(getContext(), homeItems, playList, artistList);
        songAdapter = new SongAdapter(getContext(), songsList, playList, artistList);
        recyclerView.setAdapter(homeAdapter);


        db = FirebaseFirestore.getInstance();
        //loadSongsFromFirestore();
        loadDefaultData();

        return view;
    }
    private void loadDefaultData() {

        repo.listenAllArtists(artists -> {
            artistList.clear();
            artistList.addAll(artists);
            updateItems();
        });
        repo.listenAllSongs(song -> {
            songsList.clear();
            songsList.addAll(song);
            updateItems();
        });

        repo.getTop3RecentlyPlayed(playedsong -> {
            playedList.clear();
            playedList.addAll(playedsong);
            updateItems();
        });
        repo.listenPlaylistsByType(playlist -> {
            playList.clear();
            playList.addAll(playlist);
            updateItems();
        });

        repo.listenAllGenres(genres -> {
            cachedGenres.clear();
            cachedGenres.addAll(genres);
            updateItems();
        });
    }
    private void updateItems() {
        if (songsList.isEmpty() || artistList.isEmpty() || playList.isEmpty()) {
            return; // đợi đủ dữ liệu rồi mới cập nhật
        }

        ArrayList<Song> random1 = new ArrayList<>(songsList);
        ArrayList<Song> random2 = new ArrayList<>(songsList);
        Collections.shuffle(random1);
        Collections.shuffle(random2);
        random1 = new ArrayList<>(random1.subList(0, Math.min(5, random1.size())));
        random2 = new ArrayList<>(random2.subList(0, Math.min(5, random2.size())));

        homeItems = new ArrayList<>();
        homeItems.add(new HomeAdapter.GreetingSection("Gợi ý cho bạn", random1));
        homeItems.add(new HomeAdapter.ArtistData("Ca sĩ nổi tiếng", artistList));
        homeItems.add(new HomeAdapter.GreetingSection("Bài hát phổ biến", random2));
        homeItems.add(new HomeAdapter.PlayedData("Đã nghe trước đây", playedList));
        homeItems.add(new HomeAdapter.CategoryData("Kham phá theo chủ đề", playList));

        homeAdapter.updateData(homeItems, playList, artistList); // adapter có hàm này
    }

    @Override
    public void onSongChanged(Song song) {

    }

    @Override
    public void onPlayerStateChanged(boolean isPlaying) {

    }

    @Override
    public void onPlaylistChanged(List<Song> playlist) {

    }
}


