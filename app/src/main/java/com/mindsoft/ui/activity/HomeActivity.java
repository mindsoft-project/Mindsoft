package com.mindsoft.ui.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.R;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    public ActivityHomeBinding binding;
    public NavigationView mNavigationView;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("mindsoft_channel", "Mindsoft Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);


        assert navHostFragment != null;

        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mNavigationView = binding.navView;

        mNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        View head = mNavigationView.getHeaderView(0);

        TextView displayName = head.findViewById(R.id.displayName);

        if (mUser == null) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            displayName.setText(mUser.getDisplayName());
        }
    }


    private boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.home) {
            if (User.current.hasRole(Role.STUDENT)) {
                navController.navigate(R.id.action_to_student_home);
            } else if (User.current.hasRole(Role.PROFESSOR)) {
                navController.navigate(R.id.action_to_professor_home);
            } else if (User.current.hasRole(Role.TEACHING_ASSISTANT)) {
                navController.navigate(R.id.action_to_assistant_home);
            }
            return true;
        } else if (menuItem.getItemId() == R.id.pending_requests && User.current.hasRole(Role.ADMIN)) {
            navController.navigate(R.id.action_to_pending_requests);
            return true;
        } else if (menuItem.getItemId() == R.id.signout) {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}