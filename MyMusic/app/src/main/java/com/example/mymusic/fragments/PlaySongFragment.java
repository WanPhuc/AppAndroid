package com.example.mymusic.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.example.mymusic.adapters.MainAdapter;
import com.example.mymusic.adapters.SongAdapter;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Playlist;
import com.example.mymusic.models.Song;
import com.example.mymusic.models.User;
import com.example.mymusic.repository.MusicRepository;
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
public class PlaySongFragment extends Fragment {
    private ImageView imgSong, btnPlay, btnNext, btnPrev;
    private TextView tvSongName, tvPlayArtistName, tvDuration, tvTotal;
    private ImageButton btn_back;
    private RecyclerView recy_ActiPlaySong;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private ArrayList<Artist> artistList = new ArrayList<>();
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
        tvDuration = view.findViewById(R.id.duration_song);
        tvTotal = view.findViewById(R.id.totalDuration_song);
        seekBar = view.findViewById(R.id.seekBarSong);
        recy_ActiPlaySong = view.findViewById(R.id.recy_ActiPlaySong);

        Bundle args = getArguments();
        if (args != null) {
            Song playsong = (Song) args.getSerializable("selected_song");

            tvSongName.setText(playsong.getTitle());

            if (playsong.getCoverUrl() != null) {
                Glide.with(this)
                        .load(playsong.getCoverUrl())
                        .placeholder(R.drawable.ic_android_black_24dp)
                        .error(R.drawable.ic_android_black_24dp)
                        .into(imgSong);
            }

        }

        btn_back.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.vp_fragmain);
            viewPager.setCurrentItem(3, true); // tab LibraryFragment
        });
        return view;
    }
}
