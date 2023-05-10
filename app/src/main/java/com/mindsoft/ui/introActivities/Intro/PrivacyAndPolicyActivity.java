package com.mindsoft.ui.introActivities.Intro;



import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.mindsoft.R;

public class PrivacyAndPolicyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_and_policy);

        Button buttonNext = findViewById(R.id.buttonNext);
        ImageView imageView = findViewById(R.id.sampleImageView1);

        Glide.with(this)
                .asGif()
                .load(R.raw.facescan)
                .into(imageView);

        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AboutUsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
