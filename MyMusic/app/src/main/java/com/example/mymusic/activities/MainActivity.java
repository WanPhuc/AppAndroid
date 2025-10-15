package com.example.mymusic.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

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
import com.example.mymusic.models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout containerMain;
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
        setupFragmentListener();

        miniPlayerContainer = findViewById(R.id.fl_miniplay);
        miniPlayerFragment = new MiniPlayerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_miniplay, miniPlayerFragment)
                .commit();
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
            }

            return true;
        });
    }

    private void setupFragmentListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                long animDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    containerMain.setVisibility(View.GONE);
                    viewPager2.setVisibility(View.VISIBLE);
                }, animDuration);
            }
        });
    }
    public void showMiniPlayer(Song song) {
        miniPlayerContainer.setVisibility(View.VISIBLE);
        miniPlayerFragment.bindSong(song);
    }

}
