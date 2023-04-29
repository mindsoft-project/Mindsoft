package com.mindsoft.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.data.repoistory.SystemRepository;
import com.mindsoft.databinding.ActivitySplashscreenBinding;

public class SplashscreenActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT = 2000;

    private ActivitySplashscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ImageView logo = binding.logo;

        FirebaseApp.initializeApp(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(() -> {
            if (user != null) {
                Intent home = new Intent(this, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
            } else {
                Intent main = new Intent(this, LoginActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}