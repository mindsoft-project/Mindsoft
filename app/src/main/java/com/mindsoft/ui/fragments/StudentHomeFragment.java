package com.mindsoft.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.repoistory.CourseRepository;
import com.mindsoft.databinding.FragmentStudentHomeBinding;
import com.mindsoft.ui.adapters.CourseAdapter;

public class StudentHomeFragment extends Fragment {
    private FragmentStudentHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentHomeBinding.inflate(inflater, container, false);
        RecyclerView courses = binding.recyclerView;
        courses.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));

        NavController navController = NavHostFragment.findNavController(this);
        Animation animation = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.base_line_anim);
        Animation animation2 = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_infinite);


        binding.imageView4.startAnimation(animation);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.imageView4.startAnimation(animation2);
            }
        }, 1500);
        binding.addCourseImageView.setOnClickListener(v -> {
            navController.navigate(R.id.action_student_to_course_list);
        });
        CourseRepository.getInstance()
                .getEnrolledCourses(Student.current)
                .observe(this.requireActivity(), courseList -> {
                    CourseAdapter.Options options = new CourseAdapter.Options();
                    CourseAdapter courseAdapter = new CourseAdapter(courseList, options, new CourseAdapter.OnCourseClickListener() {
                        @Override
                        public void onClick(Course course) {
                            Bundle bundle = new Bundle();
                            bundle.putString("courseId", course.getId());
                            navController.navigate(R.id.action_student_to_course, bundle);
                        }

                        @Override
                        public void onAdd(Course course) {

                        }
                    });
                    courses.setAdapter(courseAdapter);
                });
        return binding.getRoot();
    }
}
