package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.databinding.FragmentAssistantHomeBinding;
import com.mindsoft.databinding.FragmentProfessorHomeBinding;

public class AssistantHomeFragment extends Fragment {
    private FragmentAssistantHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssistantHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
