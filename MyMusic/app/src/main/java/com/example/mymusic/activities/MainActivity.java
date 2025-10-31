package com.example.mymusic.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mymusic.R;
import com.example.mymusic.adapters.MainAdapter;
import com.example.mymusic.fragments.MiniPlayerFragment;
import com.example.mymusic.fragments.PlaySongFragment;
import com.example.mymusic.models.Artist;
import com.example.mymusic.models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout containerMain,fullContainer;
    private FrameLayout miniPlayerContainer;
    private MiniPlayerFragment miniPlayerFragment;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Handle permission grant or denial
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- BẮT ĐẦU: Kích hoạt Firestore Offline Persistence ---
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Tùy chọn: Tăng giới hạn cache
                .build();
        db.setFirestoreSettings(settings);
        // --- KẾT THÚC ---

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager2 = findViewById(R.id.vp_fragmain);
        bottomNavigationView = findViewById(R.id.bottom_bar);
        containerMain = findViewById(R.id.container_main);
        viewPager2.setUserInputEnabled(false);
        MainAdapter adapter=new MainAdapter(this);
        viewPager2.setAdapter(adapter);
        setupNavigation();


        miniPlayerContainer = findViewById(R.id.fl_miniplay);
        fullContainer = findViewById(R.id.fl_fullplay);
        miniPlayerFragment = new MiniPlayerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_miniplay, miniPlayerFragment)
                .commitNow();
        miniPlayerContainer.setVisibility(View.GONE);


    }
    private void setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // If a fragment is open, pop it from the back stack.
            // The OnBackStackChangedListener will handle the visibility changes.
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            int itemId = item.getItemId();
            if (itemId == R.id.homeicon) {
                viewPager2.setCurrentItem(0, false);
            } else if (itemId == R.id.searchicon) {
                viewPager2.setCurrentItem(1, false);
            } else if (itemId == R.id.libraryicon) {
                viewPager2.setCurrentItem(2, false);
            } else if (itemId == R.id.accounticon) {
                viewPager2.setCurrentItem(3, false);
            }

            return true;
        });
    }


    public void showMiniPlayer(Song song, ArrayList<Song> songs, ArrayList<Artist> artists) {
        miniPlayerContainer.setVisibility(View.VISIBLE);
        miniPlayerFragment.bindSong(song);
        miniPlayerFragment.SetArrayListSong(songs,artists);
        //Toast.makeText(this, "MiniPlayer nhận " + songs.size() + " bài và " + artists.size() + " nghệ sĩ", Toast.LENGTH_SHORT).show();
    }
    public void setMiniPlayer() {

    }
    public void openFullPlayer(Song song, ArrayList<Song> songs, ArrayList<Artist> artists) {
        if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.GONE);
        if (fullContainer != null) fullContainer.setVisibility(View.VISIBLE);

        PlaySongFragment playSongFragment = new PlaySongFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        playSongFragment.setArguments(bundle);
        int newIndex = -1;

        playSongFragment.SetArrayListSong(songs,artists);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out_down
                )
                .replace(R.id.fl_fullplay, playSongFragment)
                .addToBackStack("full_player")
                .commit();
    }


    public void showMiniPlayerUI() {
        if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.VISIBLE);
        if (fullContainer != null) fullContainer.setVisibility(View.GONE);
    }

}
