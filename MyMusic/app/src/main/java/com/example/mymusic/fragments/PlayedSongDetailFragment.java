package com.example.mymusic.fragments;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusic.R;
import com.example.mymusic.adapters.HistoryAdapter;
import com.example.mymusic.adapters.HomeAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.repository.MusicRepository;
import com.example.mymusic.services.MusicPlayerService;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlayedSongDetailFragment extends Fragment implements MusicPlayerService.PlayerListener{
    private static final Logger log = LoggerFactory.getLogger(PlayedSongDetailFragment.class);
    private RecyclerView recyclerView;


    private List<Object> homeItems = new ArrayList<>();
    private SongAdapter songAdapter;
    private HistoryAdapter historyAdapter;
    private ArrayList<Song> songsList = new ArrayList<>();
    private  ArrayList<Song> playedList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private  ArrayList<Playlist> playList = new ArrayList<>();
    private  ArrayList<String> cachedGenres = new ArrayList<>();
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
            if (historyAdapter != null) {
                historyAdapter.setMusicPlayerService(musicPlayerService);

            }

            // Đăng ký listener (UI update)
            musicPlayerService.addPlayerListener(PlayedSongDetailFragment.this);

            // Cung cấp danh sách nghệ sĩ cho service để notification hiển thị đúng
            if (!artistList.isEmpty()) {
                musicPlayerService.setArtists(artistList);
            }
            Toast.makeText(getContext(),"hện tại"+ musicPlayerService.isPlaying(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
        String userID = FirebaseAuth.getInstance().getUid();
        if (userID == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        repo.listenRecentlyPlayed(songs -> {
            playedList.clear();
            playedList.addAll(songs);

            // Danh sách task để chờ tất cả cùng hoàn thành
            List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
            Map<String, List<Song>> groupedByDate = new LinkedHashMap<>();

            for (Song song : songs) {
                var task = db.collection("Users")
                        .document(userID)
                        .collection("RecentlyPlayed")
                        .document(song.getSongID())
                        .get()
                        .addOnSuccessListener(doc -> {
                            Timestamp ts = doc.getTimestamp("timestamp");
                            if (ts == null) return;

                            String date = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(ts.toDate());

                            groupedByDate
                                    .computeIfAbsent(date, k -> new ArrayList<>())
                                    .add(song);
                        });
                tasks.add(task);
            }

            // Khi tất cả get() hoàn thành → hiển thị
            com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                    .addOnSuccessListener(results -> {
                        List<Object> displayList = new ArrayList<>();

                        for (Map.Entry<String, List<Song>> entry : groupedByDate.entrySet()) {
                            displayList.add("Ngày " + entry.getKey());
                            displayList.addAll(entry.getValue());
                        }

                        if (historyAdapter == null) {
                            historyAdapter = new HistoryAdapter(getContext(), displayList, songAdapter);
                            if (musicPlayerService != null)
                                historyAdapter.setMusicPlayerService(musicPlayerService);
                            recyclerView.setAdapter(historyAdapter);
                        } else {
                            // update dữ liệu cũ thay vì notify từng lần
                            historyAdapter = new HistoryAdapter(getContext(), displayList, songAdapter);
                            recyclerView.setAdapter(historyAdapter);
                        }

                        Toast.makeText(getContext(),
                                "Đã tải " + songs.size() + " bài hát nghe gần đây",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi khi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ViewPager2 viewPager = activity.findViewById(R.id.vp_fragmain);
        FrameLayout container = activity.findViewById(R.id.container_main);
        if (viewPager != null) viewPager.setVisibility(View.VISIBLE);
        if (container != null) container.setVisibility(View.GONE);
    }
}
