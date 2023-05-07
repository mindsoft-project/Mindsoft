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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.User;
import com.mindsoft.data.repoistory.CourseRepository;
import com.mindsoft.databinding.FragmentProfessorHomeBinding;
import com.mindsoft.ui.adapters.CourseAdapter;

public class ProfessorHomeFragment extends Fragment {
    private FragmentProfessorHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfessorHomeBinding.inflate(inflater, container, false);
        RecyclerView courses = binding.recyclerView;
        courses.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        NavController navController = NavHostFragment.findNavController(this);

        CourseRepository.getInstance()
                .getOwnedCourses(User.current)
                .observe(this.requireActivity(), courseList -> {

                    CourseAdapter.Options options = new CourseAdapter.Options();
                    options.setWidth(350);

                    CourseAdapter courseAdapter = new CourseAdapter(courseList, options, new CourseAdapter.OnCourseClickListener() {
                        @Override
                        public void onClick(Course course) {
                            Bundle bundle = new Bundle();
                            bundle.putString("courseId", course.getId());

                            navController.navigate(R.id.action_professor_to_course, bundle);
                        }

                        @Override
                        public void onAdd(Course course) {
                        }
                    });
                    courses.setAdapter(courseAdapter);
                });

        binding.add.setOnClickListener(v -> {
            navController.navigate(R.id.action_professor_to_add_course);
        });

        return binding.getRoot();
    }
}
