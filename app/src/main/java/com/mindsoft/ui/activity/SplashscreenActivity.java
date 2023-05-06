package com.mindsoft.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.R;
import com.mindsoft.data.repoistory.SystemRepository;
import com.mindsoft.databinding.ActivitySplashscreenBinding;
import com.mindsoft.ui.introActivities.Intro.WelcomeActivity;

import java.util.concurrent.TimeUnit;

public class SplashscreenActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN_TIMEOUT = 4000;
    ImageView logo;
    View s1;
    View s2;
    View s3;
    private ActivitySplashscreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        /////Declaration----
         logo = binding.logo;
         s1 = binding.view1;
        s2 = binding.viwe2;
        s3 = binding.viwe3;

        ////----///Animations-------
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.base_line_anim);
        logo.startAnimation(animation);
         animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.appere_left);
        s1.startAnimation(animation);
         animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.appere_right);
        s2.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.appere_right);
        s3.startAnimation(animation);
        ///Animations-------



        FirebaseApp.initializeApp(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(() -> {
            if (user != null) {
                Intent home = new Intent(this, HomeActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
            } else {
                Intent main = new Intent(this, WelcomeActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(main);
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}