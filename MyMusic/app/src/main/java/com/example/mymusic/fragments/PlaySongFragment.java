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
    private TextView tvSongName, tvPlayArtistName, tvTotal, tvCurrentTime;
    private ImageButton btn_back;
    private RecyclerView rcvplayingsong;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Song song;
    private MusicPlayerService musicPlayerService;
    private boolean isServiceBound = false;
    private android.os.Handler handler = new android.os.Handler();
    private Runnable updateSeekBarRunnable;
    private ArrayList<Song> songsList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<Playlist> playlist = new ArrayList<>();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicPlayerService = binder.getService();
            musicPlayerService.addPlayerListener(PlaySongFragment.this);
            isServiceBound = true;
            updateUIPLaysongFragmentUI();
            startUpdatingSeekBar();
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
        tvCurrentTime = view.findViewById(R.id.duration_song);
        seekBar = view.findViewById(R.id.seekBarSong);
        btnShuffle = view.findViewById(R.id.RandomSong_icon);
        rcvplayingsong = view.findViewById(R.id.recyclerplayingsong);
        Bundle bundle = getArguments();
        if (bundle != null && song == null) {
            song = (Song) bundle.getSerializable("song");
            SetUpPlaySong(song);
        }
        SongAdapter songAdapter = new SongAdapter(getContext(), songsList, playlist, artistList);
        rcvplayingsong.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvplayingsong.setAdapter(songAdapter);
        songAdapter.setOnSongClickListener((song, position) -> {
                    if (!isServiceBound) return;
                    List<Song> currentServicePlaylist = musicPlayerService.getOriginalSongs();
                    boolean isDifferentPlaylist = currentServicePlaylist == null
                            || currentServicePlaylist.isEmpty()
                            || currentServicePlaylist.size() != songsList.size()
                            || !currentServicePlaylist.get(0).getSongID().equals(songsList.get(0).getSongID());

                    if (isDifferentPlaylist) {
                        // 🛑 Stop playlist cũ
                        musicPlayerService.stop();
                        // 🔁 Set playlist mới
                        musicPlayerService.setSongs(new ArrayList<>(songsList));
                    }

                    // ▶️ Phát bài được click
                    musicPlayerService.playSong(song);
                    //songAdapter.setSelectedPosition(position);
                    Log.d("ArtistName", "Tên nghệ sĩ: " + song);
                    SetUpPlaySong(song);
                    //songAdapter.setSelectedPosition(position);


                });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean wasPlayingBeforeSeek = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicPlayerService != null) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // ✅ Ghi lại trạng thái trước khi kéo
                if (musicPlayerService != null) {
                    wasPlayingBeforeSeek = musicPlayerService.isPlaying();
                    if (wasPlayingBeforeSeek) {
                        musicPlayerService.playPause(); // tạm dừng nhạc
                    }
                }
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicPlayerService != null) {
                    musicPlayerService.seekTo(seekBar.getProgress());
                    // ✅ Phát lại nếu bài hát đang chạy trước khi tua
                    if (wasPlayingBeforeSeek) {
                        musicPlayerService.playPause();
                    }
                }
                startUpdatingSeekBar();
            }
        });
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
        btnPrev.setOnClickListener(v -> {
            if (isServiceBound && musicPlayerService != null) {
                musicPlayerService.playPrevious();
            }
        });

        btn_back.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showMiniPlayerUI();
            }
        });


        btnShuffle.setOnClickListener(v -> {
            if (isServiceBound) {
                musicPlayerService.toggleShuffle();
                btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
            }
        });

        int newIndex = -1;
        for (int i = 0; i < songsList.size(); i++) {
            if (songsList.get(i).getSongID().equals(song.getSongID())) {
                newIndex = i;
                break;
            }
        }

        // 🔹 Highlight bài hiện tại
        if (newIndex != -1 && rcvplayingsong.getAdapter() instanceof SongAdapter) {
            SongAdapter adapter = (SongAdapter) rcvplayingsong.getAdapter();
            adapter.setSelectedPosition(newIndex);

            // 🔹 Cuộn đến bài đó nếu đang ở xa
            rcvplayingsong.smoothScrollToPosition(newIndex);
        }
        return view;
    }
    public void SetUpPlaySong(Song song){
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
        for (Song songs : songsList) {
            Log.d("bai hát", songs.getTitle());
        }


    }
    public void updateUIPLaysongFragmentUI() {
        if (musicPlayerService == null || !isServiceBound) return;
        btnPlay.setImageResource(musicPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        btnShuffle.setImageResource(musicPlayerService.isShuffling() ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_disabled);
    }
    private void startUpdatingSeekBar() {
        if (musicPlayerService == null) return;

        seekBar.setMax(musicPlayerService.getDuration());

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (musicPlayerService != null && isServiceBound && musicPlayerService.isPlaying()) {
                    int currentPosition = musicPlayerService.getCurrentPosition();
                    seekBar.setProgress(currentPosition);

                    // Cập nhật thời gian hiện tại lên TextView
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 500); // cập nhật mỗi 0.5 giây
            }
        };
        handler.post(updateSeekBarRunnable);
    }
    @Override
    public void onSongChanged(Song song) {
        SetUpPlaySong(song);
        seekBar.setMax(musicPlayerService.getDuration());
        SongAdapter adapter = (SongAdapter) rcvplayingsong.getAdapter();
        int newIndex = -1;
        for (int i = 0; i < songsList.size(); i++) {
            if (songsList.get(i).getSongID().equals(song.getSongID())) {
                newIndex = i;
                break;
            }
        }
        adapter.setSelectedPosition(newIndex);
    }

    @Override
    public void onPlayerStateChanged(boolean isPlaying) {
        updateUIPLaysongFragmentUI();
    }

    @Override
    public void onPlaylistChanged(List<Song> playlist) {

    }
    public void SetArrayListSong(ArrayList<Song> songs,ArrayList<Artist> artists){
        this.songsList = songs;
        this.artistList = artists;
    }
    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

}
