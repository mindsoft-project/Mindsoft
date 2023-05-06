package com.mindsoft.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.CourseAttendance;
import com.mindsoft.data.model.CourseSession;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.User;
import com.mindsoft.data.repoistory.CourseRepository;
import com.mindsoft.databinding.FragmentCoursePageBinding;
import com.mindsoft.ui.adapters.CourseSessionsAdapter;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;

public class CoursePageFragment extends Fragment {
    private FragmentCoursePageBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private FusedLocationProviderClient mFusedLocationProvider;
    private NavController mNavController;
    private Course course;
    private boolean isAnStudent;
    private boolean isTheProfessor;
    private boolean isAnTeachingAssistant;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoursePageBinding.inflate(inflater, container, false);


        mNavController = NavHostFragment.findNavController(this);
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));

        assert getArguments() != null;
        String courseId = getArguments().getString("courseId");

        PopupMenu menu = new PopupMenu(requireActivity(), binding.menu);
        menu.getMenuInflater().inflate(R.menu.course_page_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this::onMenuItemClick);


        if (!courseId.isEmpty()) {
            DocumentReference ref = db.collection(Course.COLLECTION).document(courseId);

            CourseSessionsAdapter adapter = new CourseSessionsAdapter(new ArrayList<>(), session -> {
                Bundle bundle = new Bundle();
                bundle.putString("sessionId", session.getId());
                bundle.putString("courseId", courseId);
                mNavController.navigate(R.id.action_course_to_session, bundle);
            });
            recyclerView.setAdapter(adapter);

            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    this.course = task.getResult().toObject(Course.class);

                    if (course == null) return;

                    CourseRepository.getInstance().getSessions(course)
                            .observe(requireActivity(), adapter::setSessionList);

                    binding.refresh.setOnRefreshListener(() -> {
                        CourseRepository.getInstance().getSessions(course)
                                .observe(requireActivity(), adapter::setSessionList);
                        binding.refresh.setRefreshing(false);
                    });


                    binding.menu.setOnClickListener(v -> {
                        menu.show();
                    });

                    binding.courseName.setText(course.getName());
                    binding.courseDescription.setText(course.getDescription());

                    isTheProfessor = User.current.hasRole(Role.PROFESSOR) && course.getProfessor().getId().equals(User.current.getId());
                    isAnTeachingAssistant = User.current.hasRole(Role.TEACHING_ASSISTANT) && course.getTeachingAssistantsIds().contains(User.current.getId());
                    isAnStudent = User.current.hasRole(Role.STUDENT) && course.getEnrolledByIds().contains(User.current.getId());

                    menu.getMenu().findItem(R.id.unroll).setVisible(isAnStudent);

                    if (isAnTeachingAssistant || isTheProfessor) {
                        if (isTheProfessor) {
                            binding.delete.setVisibility(View.VISIBLE);
                            binding.delete.setOnClickListener(this::onDeleteCourse);
                            menu.getMenu().findItem(R.id.delete).setVisible(true);
                            menu.getMenu().findItem(R.id.edit).setVisible(true);
                        }
                        menu.getMenu().findItem(R.id.teaching_assistants).setVisible(true);
                        menu.getMenu().findItem(R.id.export_attendance).setVisible(true);
                        binding.start.setVisibility(View.VISIBLE);
                        binding.start.setOnClickListener(v -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("courseId", courseId);
                            mNavController.navigate(R.id.action_course_to_start_session, bundle);
                        });
                    }
                }
            });
        }

        return binding.getRoot();
    }


    public void onDeleteCourse(View view) {
        if (course == null) return;
        DocumentReference ref = db.collection(Course.COLLECTION).document(course.getId());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setMessage("Are you sure you want to delete this course");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            ref.delete().addOnCompleteListener(command -> {
                mNavController.navigate(R.id.action_course_to_main);
            });
        });

        builder.setNegativeButton("No", (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onUnsubscribe(View view) {
        if (course == null) return;
        DocumentReference ref = db.collection(Course.COLLECTION).document(course.getId());
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setMessage("Are you sure you want to unsubscribe this course");
        builder.setPositiveButton("Yes", (dialog, id) -> {
            int index = course.getEnrolledByIds().indexOf(User.current.getId());

            if (index != -1) {
                course.getEnrolledBy().remove(index);
            }

            ref.set(course).addOnSuccessListener(command -> {
                mNavController.navigate(R.id.action_course_to_main);
            });
        });

        builder.setNegativeButton("No", (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (course == null) return false;

        if (isAnTeachingAssistant || isTheProfessor) {
            if (item.getItemId() == R.id.teaching_assistants) {
                return true;
            } else if (item.getItemId() == R.id.export_attendance) {
                onExportAttendance();
                return true;
            }

            if (isTheProfessor) {
                if (item.getItemId() == R.id.delete) {
                    onDeleteCourse(null);
                    return true;
                } else if (item.getItemId() == R.id.edit) {
                    return true;
                }
            }
        } else if (isAnStudent) {
            if (item.getItemId() == R.id.unroll) {
                onUnsubscribe(null);
                return true;
            }
        }
        return false;
    }

    private void onExportAttendance() {
    }

}
