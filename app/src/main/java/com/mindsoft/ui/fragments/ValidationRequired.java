package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentValidationRequiredBinding;

public class ValidationRequired extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private FragmentValidationRequiredBinding binding;
    private NavController mNavController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentValidationRequiredBinding.inflate(inflater, container, false);

        binding.getRoot().setOnRefreshListener(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mNavController = NavHostFragment.findNavController(this);

        if (User.current != null) {
            User.current.getReference().get().addOnSuccessListener(command -> {
                User user = command.toObject(User.class);
                assert user != null;

                if (user.isValidated()) {
                    mNavController.navigate(R.id.action_validation_required_to_main_page);
                }
            });
        }

        return binding.getRoot();
    }

    @Override
    public void onRefresh() {
        if (User.current != null) {
            User.current.getReference().get().addOnSuccessListener(command -> {
                User user = command.toObject(User.class);
                binding.getRoot().setRefreshing(false);
                assert user != null;

                if (user.isValidated()) {
                    mNavController.navigate(R.id.action_validation_required_to_main_page);
                }
            });
        }
    }
}
