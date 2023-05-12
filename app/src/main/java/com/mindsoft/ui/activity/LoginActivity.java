package com.mindsoft.ui.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.ActivityLoginBinding;
import com.mindsoft.ui.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.create.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.findStudentCode.setOnClickListener(v -> {
            Intent intent = new Intent(this, FindMyStudentCodeActivity.class);
            startActivity(intent);
        });
        binding.showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.password.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                    // Show the password
                    binding.showPass.setText("   Hide Password___ ");
                    binding.password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    binding.showPass.setText("   Show Password__ ");

                    // Hide the password
                    binding.password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                binding.password.setSelection(binding.password.getText().length());
            }
        });
        binding.forgetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        binding.submit.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).create();
            dialog.setTitle("Required fields");

            if (email.isEmpty() || password.isEmpty()) {
                dialog.setMessage("One or more required field is required.");
                dialog.show();
                return;
            }

            binding.submit.setAlpha(0.5f);
            binding.submit.setEnabled(false);

            viewModel.loginUser(email, password, new LoginViewModel.OnLoginListener() {
                @Override
                public void onSuccess() {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (mUser != null) {
                        db.collection(User.COLLECTION).document(mUser.getUid())
                                .get()
                                .addOnSuccessListener(command -> {
                                    User user = command.toObject(User.class);
                                    if (user == null) return;

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                });
                    }
                }

                @Override
                public void onError(String message) {
                    binding.submit.setAlpha(1f);
                    binding.submit.setEnabled(true);
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).setTitle("Login Error").setMessage(message).create();
                    alertDialog.show();
                }
            });
        });
    }
}