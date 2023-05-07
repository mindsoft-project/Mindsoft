package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.Student;
import com.mindsoft.databinding.FragmentAdditionalInfoBinding;

public class AdditionalInfoFragment extends Fragment {
    private FragmentAdditionalInfoBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdditionalInfoBinding.inflate(inflater, container, false);
        NavController navController = NavHostFragment.findNavController(this);

        if (getArguments() != null) {
            boolean section = getArguments().getBoolean("section");
            if (section) {
                binding.sectionLayout.setVisibility(View.VISIBLE);
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        binding.submit.setOnClickListener(v -> {
            if (binding.section.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "You should enter section number", Toast.LENGTH_SHORT).show();
                return;
            }
            Student.current.setSection(Integer.parseInt(binding.section.getText().toString()));
            Student.current.getReference().set(Student.current).addOnSuccessListener(command -> {
                navController.navigate(R.id.action_additional_info_to_main_page);
            });
        });

        return binding.getRoot();
    }
}
