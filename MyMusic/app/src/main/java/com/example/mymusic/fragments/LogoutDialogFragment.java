package com.example.mymusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mymusic.R;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        dialog.setContentView(view);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        btnCancel.setOnClickListener(v -> dismiss());
        btnLogout.setOnClickListener(v -> {
            //FirebaseAuth.getInstance().signOut();

            dismiss();
        });

        return dialog;
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
