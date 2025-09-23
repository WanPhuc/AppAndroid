package com.example.mymusic.activities;

import android.os.Bundle;
import android.widget.FrameLayout;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mymusic.fragments.SignInFragment;
import com.example.mymusic.R;

public class RegisterActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        frameLayout=findViewById(R.id.fl_register);

        setFragment(new SignInFragment());
    }
    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}
