package com.example.mymusic.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.activities.MainActivity;
import com.example.mymusic.adapters.MainAdapter;
import com.example.mymusic.models.Song;
import com.example.mymusic.services.MusicPlayerService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.mymusic.fragments.PlaySongFragment;
import java.util.List;

public class MiniPlayerFragment extends Fragment implements MusicPlayerService.PlayerListener {
    private ImageView imgCover;
    private TextView tvTitle,tvArtist;
    private ImageButton btnPlayPause,btnNext;
    private Song playsong;

    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.addPlayerListener(MiniPlayerFragment.this);
            isServiceBound = true;
            updateUIMinifragmentUI();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.miniplayer, container, false);
        imgCover = view.findViewById(R.id.imgCover);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnNext = view.findViewById(R.id.btnNext);
        btnPlayPause.setOnClickListener(v -> {
            //musicPlayerService.playPause();
            if (musicPlayerService.isPlaying()){
                musicPlayerService.playPause();
            }
            else {
                musicPlayerService.playPause();
            }

        });
        btnNext.setOnClickListener(v -> {
            if (isServiceBound && musicPlayerService != null) {
                musicPlayerService.playNext();
            }
        });
        view.setOnClickListener(v -> {
            if (isServiceBound && musicPlayerService != null) {
                Song currentSong = musicPlayerService.getCurrentSong();
                if (currentSong != null) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).openFullPlayer(currentSong);
                    }
                }
            }
        });


        return view;
    }
    public void bindSong(Song song) {
        if (song == null || getView() == null) return;
        // Cập nhật tên bài hát
        TextView tvTitle = getView().findViewById(R.id.tvTitle);
        tvTitle.setText(song.getTitle());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String artistID = song.getArtistID();

        db.collection("Artists")
                .document(artistID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String artistName = documentSnapshot.getString("name");
                        // 👉 Ví dụ hiển thị ra TextView hoặc Log
                        Log.d("ArtistName", "Tên nghệ sĩ: " + artistName);
                        tvArtist.setText(artistName);
                        // tvArtist.setText(artistName);
                    } else {
                        Log.d("ArtistName", "Không tìm thấy nghệ sĩ có ID: " + artistID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ArtistName", "Lỗi khi lấy tên nghệ sĩ", e);
                });
        // Nếu bạn có TextView hiển thị nghệ sĩ:


        // Cập nhật ảnh bìa
        ImageView imgCover = getView().findViewById(R.id.imgCover);
        Glide.with(this)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.img_default_song)
                .into(imgCover);

    }

    public void updateUIMinifragmentUI() {
        if (musicPlayerService == null || !isServiceBound) return;
        btnPlayPause.setImageResource(musicPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
    }


    @Override
    public void onSongChanged(Song song) {
        bindSong(song);
    }

    @Override
    public void onPlayerStateChanged(boolean isPlaying) {
        updateUIMinifragmentUI();
    }

    @Override
    public void onPlaylistChanged(List<Song> playlist) {

    }
}
