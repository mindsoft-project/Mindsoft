package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.Department;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentAddCourseBinding;
import com.mindsoft.ui.adapters.DeptsCheckboxAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddCourseFragment extends Fragment {
    private FragmentAddCourseBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCourseBinding.inflate(inflater, container, false);
        RecyclerView departments = binding.departments;
        departments.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));

        List<Department> depts = new ArrayList<>();
        for (Map.Entry<Integer, Department> entry : Department.departments.entrySet()) {
            depts.add(entry.getValue());
        }
        DeptsCheckboxAdapter deptsCheckboxAdapter = new DeptsCheckboxAdapter(depts);
        departments.setAdapter(deptsCheckboxAdapter);

        binding.add.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference ref = db.collection(Course.COLLECTION).document();
            Course course = new Course();
            course.setId(ref.getId());
            course.setName(binding.courseName.getText().toString());
            course.setDescription(binding.courseDescription.getText().toString());
            course.setProfessor(User.current.getReference());
            for (int i = 0; i < departments.getChildCount(); ++i) {
                DeptsCheckboxAdapter.ViewHolder viewHolder = (DeptsCheckboxAdapter.ViewHolder) departments.getChildViewHolder(departments.getChildAt(i));
                if (viewHolder.isChecked()) {
                    course.addDepartment(viewHolder.getDepartment());
                }
            }
            ref.set(course).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireActivity(), "Could not add the course.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        return binding.getRoot();
    }
}
