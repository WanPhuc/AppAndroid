package com.example.mymusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mymusic.R;

public class TimeoutDialogFragment extends DialogFragment {

    private Handler handler = new Handler();
    private Runnable closeAppRunnable;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_timeout, null);
        dialog.setContentView(view);

        SeekBar seekBar = view.findViewById(R.id.seekBarTime);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnSet = view.findViewById(R.id.btnSet);

        seekBar.setMax(180);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTime.setText(progress + " phút");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSet.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đặt hẹn tắt sau " + seekBar.getProgress() + " phút", Toast.LENGTH_SHORT).show();
            startAutoClose(seekBar.getProgress());
            dismiss();
        });

        return dialog;
    }

    private void startAutoClose(int minutes) {
        long millis = minutes * 60 * 1000L;

        // Nếu đã có timer trước đó thì hủy
        if (closeAppRunnable != null) handler.removeCallbacks(closeAppRunnable);

        closeAppRunnable = () -> {
            // ✅ Đóng toàn bộ ứng dụng

            System.exit(0); // đảm bảo app được kill hoàn toàn
        };
        handler.postDelayed(closeAppRunnable, millis);
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
