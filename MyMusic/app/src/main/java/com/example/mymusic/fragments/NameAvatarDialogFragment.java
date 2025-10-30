package com.example.mymusic.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.mymusic.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NameAvatarDialogFragment extends DialogFragment {

    private ImageView imgAvatar;
    private EditText edtName;

    public interface OnNameUpdatedListener {
        void onNameUpdated(String newName);
    }
    private OnNameUpdatedListener listener;

    public void setOnNameUpdatedListener(OnNameUpdatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_name_avatar, null);
        dialog.setContentView(view);

        edtName = view.findViewById(R.id.edtName);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Khi nhấn vào ảnh → mở thư viện


        // Nút lưu
        btnSave.setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên mới", Toast.LENGTH_SHORT).show();
                return;
            }
            updateUserName(newName);
            dismiss();
        });

        return dialog;
    }
    private void updateUserName(String newName) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }
        String userID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newName);

        db.collection("Users")
                .document(userID)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    dismiss();
                    if (listener != null) {
                        listener.onNameUpdated(newName);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
