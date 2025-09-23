package com.example.mymusic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mymusic.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {
    private TextInputEditText edt_username, edt_email, edt_password, edt_repeate_password;
    private TextInputLayout layout_username, layout_email, layout_password, layout_repeate_password;
    private Button btn_signup;
    private TextView tv_signin;
    private FrameLayout frameLayout;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        frameLayout = getActivity().findViewById(R.id.fl_register);
        mAuth = FirebaseAuth.getInstance();

        // Init views
        edt_username = view.findViewById(R.id.edt_username);
        edt_email = view.findViewById(R.id.edt_email);
        edt_password = view.findViewById(R.id.edt_password);
        edt_repeate_password = view.findViewById(R.id.edt_repeate_password);

        layout_username = view.findViewById(R.id.layout_username);
        layout_email = view.findViewById(R.id.layout_email);
        layout_password = view.findViewById(R.id.layout_password);
        layout_repeate_password = view.findViewById(R.id.layout_repeate_password);

        tv_signin = view.findViewById(R.id.tv_signin);
        btn_signup = view.findViewById(R.id.btn_signup);

        tv_signin.setOnClickListener(v -> setFragment(new SignInFragment()));
        btn_signup.setOnClickListener(v -> SignUpFirebase());

        return view;
    }

    private void SignUpFirebase() {
        String username = edt_username.getText().toString().trim();
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString();
        String repeatPassword = edt_repeate_password.getText().toString();

        // Reset lỗi trước khi check
        layout_username.setError(null);
        layout_username.setErrorEnabled(false);

        layout_email.setError(null);
        layout_email.setErrorEnabled(false);

        layout_password.setError(null);
        layout_password.setErrorEnabled(false);

        layout_repeate_password.setError(null);
        layout_repeate_password.setErrorEnabled(false);


        // Check rỗng từng field
        if (username.isEmpty()) {
            layout_username.setError("Vui lòng nhập tên đăng nhập");
            return;
        }
        if (email.isEmpty()) {
            layout_email.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            layout_password.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (repeatPassword.isEmpty()) {
            layout_repeate_password.setError("Vui lòng nhập lại mật khẩu");
            return;
        }

        // Check email
        if (!isValidEmail(email)) {
            layout_email.setError("Email không hợp lệ");
            return;
        }

        // Check password
        if (password.length() < 6 || !Character.isUpperCase(password.charAt(0))) {
            layout_password.setError("Mật khẩu phải có ít nhất 6 ký tự và bắt đầu bằng chữ hoa");
            return;
        }

        // Check repeat password
        if (!password.equals(repeatPassword)) {
            layout_repeate_password.setError("Mật khẩu không trùng khớp");
            return;
        }

        // Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userID = user.getUid();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("email", email);

                                db.collection("Users").document(userID)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            SignInFragment signInFragment = new SignInFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("username", username);
                                            bundle.putString("password", password);
                                            signInFragment.setArguments(bundle);
                                            setFragment(signInFragment);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.from_left, R.anim.out_from_right);
        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}
