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

import com.mindsoft.databinding.ActivityFindstudentcodeBinding;
import com.mindsoft.ui.viewmodel.FindStudentCodeViewModel;

public class FindMyStudentCodeActivity extends AppCompatActivity {
    private ActivityFindstudentcodeBinding binding;

    private FindStudentCodeViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindstudentcodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FindStudentCodeViewModel.class);

        final TextView nationalID = binding.nationalId;

        binding.find.setOnClickListener(view -> {
            binding.find.setAlpha(0.5f);
            binding.find.setEnabled(true);
            viewModel.getStudentCode(nationalID.getText().toString()).observe(this, studentCode -> {
                binding.find.setAlpha(1f);
                binding.find.setEnabled(true);
                if (studentCode != null) {
                    binding.resultFailed.setVisibility(View.GONE);
                    binding.resultSuccess.setVisibility(View.VISIBLE);
                    binding.studentCode.setText(studentCode);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("student_code", studentCode);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(this, "Student code copied to clipboard.", Toast.LENGTH_SHORT).show();
                } else {
                    binding.resultSuccess.setVisibility(View.GONE);
                    binding.resultFailed.setVisibility(View.VISIBLE);
                }
            });
        });

    }
}
