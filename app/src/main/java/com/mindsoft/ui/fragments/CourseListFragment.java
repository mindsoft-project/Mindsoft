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
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;
import com.mindsoft.data.repoistory.CourseRepository;
import com.mindsoft.databinding.FragmentCourseListBinding;
import com.mindsoft.ui.adapters.CourseAdapter;

public class CourseListFragment extends Fragment {
    private FragmentCourseListBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseListBinding.inflate(inflater, container, false);
        RecyclerView courses = binding.recyclerView;
        courses.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));

        NavController navController = NavHostFragment.findNavController(this);

        CourseRepository.getInstance()
                .getAvailableCourses(Student.current)
                .observe(this.requireActivity(), courseList -> {
                    CourseAdapter.Options options = new CourseAdapter.Options();
                    options.setHasAddButton(true);
                    CourseAdapter courseAdapter = new CourseAdapter(courseList, options, null);
                    courseAdapter.listener = new CourseAdapter.OnCourseClickListener() {
                        @Override
                        public void onClick(Course course) {

                        }

                        @Override
                        public void onAdd(Course course) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            course.getEnrolledBy().add(User.current.getReference());

                            db.collection(Course.COLLECTION)
                                    .document(course.getId())
                                    .set(course).addOnSuccessListener(command -> {
                                        int index = courseAdapter.getCourseList().indexOf(course);
                                        courseAdapter.getCourseList().remove(index);
                                        courseAdapter.notifyItemRemoved(index);
                                    });
                        }
                    };
                    courses.setAdapter(courseAdapter);
                });

        return binding.getRoot();
    }
}
