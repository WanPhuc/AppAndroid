package com.example.mymusic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mymusic.R;
import com.example.mymusic.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {
    private EditText edt_email,edt_password;
    private Button btn_signin;
    private TextView tv_signup,tv_forgot_password;
    private TextInputLayout layout_email, layout_password;
    private FrameLayout frameLayout;
    private FirebaseAuth mAuth;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);

        frameLayout=getActivity().findViewById(R.id.fl_register);
        edt_email=view.findViewById(R.id.edt_email);
        edt_password=view.findViewById(R.id.edt_password);
        btn_signin=view.findViewById(R.id.btn_signin);
        tv_forgot_password=view.findViewById(R.id.tv_forgot_password);
        tv_signup=view.findViewById(R.id.tv_signup);
        layout_email = view.findViewById(R.id.layout_email);
        layout_password = view.findViewById(R.id.layout_password);
        mAuth=FirebaseAuth.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String email=bundle.getString("email");
            String password=bundle.getString("password");
            edt_email.setText(email);
            edt_password.setText(password);
        }
        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new SignUpFragment());
            }
        });
        btn_signin.setOnClickListener(v->{SigninFirebase();});

        return view;
    }
    private void SigninFirebase(){
        String email=edt_email.getText().toString();
        String password=edt_password.getText().toString();

        layout_email.setError(null);
        layout_email.setErrorEnabled(false);

        layout_password.setError(null);
        layout_password.setErrorEnabled(false);
        if (email.isEmpty()) {
            layout_email.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            layout_password.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (!isValidEmail(email)) {
            layout_email.setError("Email không hợp lệ");
            return;
        }
        if (password.length() < 6 || !Character.isUpperCase(password.charAt(0))) {
            layout_password.setError("Mật khẩu phải có ít nhất 6 ký tự và bắt đầu bằng chữ hoa");
            return;
        }
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent in = new Intent(getActivity(), MainActivity.class);
                    startActivity(in);
                    getActivity().finish();
                }
            }
        });
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction=getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_right,R.anim.out_from_left);
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}
