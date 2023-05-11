package com.mindsoft.ui.introActivities.Intro;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Button;
import android.widget.Switch;
import com.mindsoft.R;
import com.mindsoft.ui.activity.CreateAccountActivity;
import com.mindsoft.ui.activity.LoginActivity;

public class LightDarkModeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_dark_mode);

        Switch switchLightDarkModeButton = findViewById(R.id.switchLightDarkModeButton);
        Button buttonNext2 = findViewById(R.id.buttonNext2);

        switchLightDarkModeButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        buttonNext2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LightDarkModeActivity.this, AboutUsActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }
}

