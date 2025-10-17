package com.example.mymusic.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.example.mymusic.SongDownloadManager;
import com.example.mymusic.activities.MainActivity;
import com.example.mymusic.adapters.MainAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.example.mymusic.repository.MusicRepository;
import com.example.mymusic.services.MusicPlayerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
public class PlaySongFragment extends Fragment implements MusicPlayerService.PlayerListener {
    private ImageView imgSong, btnPlay, btnNext, btnPrev,btnShuffle;
    private TextView tvSongName, tvPlayArtistName, tvDuration, tvTotal;
    private ImageButton btn_back;
    private RecyclerView recy_ActiPlaySong;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Song song;
    private ArrayList<Artist> artistList = new ArrayList<>();
    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.addPlayerListener(PlaySongFragment.this);
            isServiceBound = true;
            updateUIPLaysongFragmentUI();
            Toast.makeText(getContext(), "Service connected", Toast.LENGTH_SHORT).show();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_song, container, false);
        btn_back = view.findViewById(R.id.imgToMinimizePlayer);
        imgSong = view.findViewById(R.id.imgSong_ActiPlaySong);
        btnPlay = view.findViewById(R.id.playSong_icon);
        btnNext = view.findViewById(R.id.playNextSong_icon);
        btnPrev = view.findViewById(R.id.playPreSong_icon);
        tvSongName = view.findViewById(R.id.tvNameSong_ActiPlaySong);
        tvPlayArtistName = view.findViewById(R.id.tvNameArtist_ActiPlaySong);
        tvTotal = view.findViewById(R.id.totalDuration_song);
        seekBar = view.findViewById(R.id.seekBarSong);
        btnShuffle = view.findViewById(R.id.RandomSong_icon);

        Bundle bundle = getArguments();
        if (bundle != null) {
            song = (Song) bundle.getSerializable("song");

        }

        btnPlay.setOnClickListener(v -> {
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

        btn_back.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMiniPlayerUI();
            }
        });
        SetUpPlaySong(song);

        btnShuffle.setOnClickListener(v -> {
//            if (isServiceBound) {
//                if (musicPlayerService.getCurrentSong() == null) {
//                    musicPlayerService.setSongs(songsList);
//                }
//                musicPlayerService.toggleShuffle();
//                btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
//            }
        });

        return view;
    }
    private void SetUpPlaySong(Song song){

        if (song != null) {
            //tên bài
            tvSongName.setText(song.getTitle());
            //thời gian
            int durationInSeconds = song.getDuration(); // ví dụ 225 giây = 3 phút 45 giây
            int minutes = durationInSeconds / 60;
            int seconds = durationInSeconds % 60;
            String time = String.format("%02d:%02d", minutes, seconds);
            tvTotal.setText(time);
            //ảnh
            Glide.with(this)
                    .load(song.getCoverUrl())
                    .placeholder(R.drawable.img_default_song)
                    .into(imgSong);
        }
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
                        tvPlayArtistName.setText(artistName);
                        // tvArtist.setText(artistName);
                    } else {
                        Log.d("ArtistName", "Không tìm thấy nghệ sĩ có ID: " + artistID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ArtistName", "Lỗi khi lấy tên nghệ sĩ", e);
                });


    }
    public void updateUIPLaysongFragmentUI() {
        if (musicPlayerService == null || !isServiceBound) return;
        btnPlay.setImageResource(musicPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
    }

    @Override
    public void onSongChanged(Song song) {
        SetUpPlaySong(song);
    }

    @Override
    public void onPlayerStateChanged(boolean isPlaying) {
        updateUIPLaysongFragmentUI();
    }

    @Override
    public void onPlaylistChanged(List<Song> playlist) {

    }
}
