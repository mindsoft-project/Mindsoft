package com.mindsoft.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.mindsoft.databinding.ActivityFindstudentcodeBinding;
import com.mindsoft.databinding.ActivityResetPasswordBinding;
import com.mindsoft.ui.viewmodel.FindStudentCodeViewModel;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.find.setOnClickListener(view -> {
            binding.find.setAlpha(0.5f);
            binding.find.setEnabled(true);
            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.sendPasswordResetEmail(binding.email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset request sent to your email address.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Could not send reset request.", Toast.LENGTH_SHORT).show();
                            binding.find.setAlpha(1f);
                            binding.find.setEnabled(true);
                        }
                    });
        });

    }
}
