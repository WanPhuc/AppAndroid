package com.example.mymusic.fragments;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.mymusic.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountFragment extends Fragment {
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private TextView tvUsername;
    private Switch switchDarkMode;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);
        tvUsername = view.findViewById(R.id.nameuser);
        Button btnNameAndAvatar = view.findViewById(R.id.nameandavatar);
        Button btnTimeout = view.findViewById(R.id.timeout);
        Button btnLogout = view.findViewById(R.id.logout);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // ✅ Khôi phục trạng thái Dark Mode từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkModeEnabled", false);



        // ✅ Bật/tắt Dark Mode khi người dùng thay đổi switch
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                //Toast.makeText(getContext(), "Đã bật chế độ tối 🌙", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                //Toast.makeText(getContext(), "Đã tắt chế độ tối ☀️", Toast.LENGTH_SHORT).show();
            }
            ((AppCompatActivity) requireActivity()).getDelegate().applyDayNight();
            // 🔹 Lưu trạng thái
            prefs.edit().putBoolean("DarkModeEnabled", isChecked).apply();
        });

        btnNameAndAvatar.setOnClickListener(v -> {
            NameAvatarDialogFragment dialog = new NameAvatarDialogFragment();
            dialog.setOnNameUpdatedListener(newName -> {
                tvUsername.setText("Xin chào: " + newName);
                //Toast.makeText(getContext(), "Tên mới: " + newName, Toast.LENGTH_SHORT).show();
            });
            dialog.show(getParentFragmentManager(), "NameAvatarDialog");
        });

        btnTimeout.setOnClickListener(v -> {
            TimeoutDialogFragment dialog = new TimeoutDialogFragment();
            dialog.show(getParentFragmentManager(), "TimeoutDialog");
        });

        btnLogout.setOnClickListener(v -> {
            LogoutDialogFragment dialog = new LogoutDialogFragment();
            dialog.show(getParentFragmentManager(), "LogoutDialog");
        });
        loadUsername();
        return view;

    }
    private void loadUsername() {
        String userID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                if (username != null) {
                    tvUsername.setText("Xin chào: " + username);
                    //Toast.makeText(getContext(), "Xin chào: " + username, Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getContext(), "Không có username trong Firestore", Toast.LENGTH_SHORT).show();
                }
            } else {
                //Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

