package com.mindsoft.ui.introActivities.Intro;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.mindsoft.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        VideoView sampleVideoView1 = findViewById(R.id.sampleVideoView1);
        Button buttonNext = findViewById(R.id.buttonNext);

        Uri uri = Uri.parse("android.resource://"
                + getPackageName() + "/" + R.raw.welcome
        );

        sampleVideoView1.setVideoURI(uri);
        sampleVideoView1.start();

        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PrivacyAndPolicyActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
