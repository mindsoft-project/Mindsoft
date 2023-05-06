package com.mindsoft.ui.introActivities.Intro;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import com.mindsoft.R;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LightDarkModeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
