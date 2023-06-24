package com.mindsoft.ui.introActivities.Intro;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mindsoft.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button buttonNext = findViewById(R.id.buttonNext);

        ImageView imageView = findViewById(R.id.sampleVideoView1);
        ImageView imageView2 = findViewById(R.id.sampleVideoView2);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.base_line_anim);
imageView.startAnimation(animation);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.base_line_anim);
        imageView2.startAnimation(animation2);



        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PrivacyAndPolicyActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
