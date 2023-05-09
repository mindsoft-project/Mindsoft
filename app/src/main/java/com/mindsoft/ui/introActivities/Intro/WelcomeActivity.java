package com.mindsoft.ui.introActivities.Intro;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

        Glide.with(this)
                .asGif()
                .load(R.raw.welcome)
                .into(imageView);


        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PrivacyAndPolicyActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
