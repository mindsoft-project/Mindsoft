package com.mindsoft.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentMainPageBinding;
import com.mindsoft.ui.activity.DetectorActivity;
import com.mindsoft.ui.activity.HomeActivity;
import com.mindsoft.ui.activity.LoginActivity;

public class MainPageFragment extends Fragment {

    private FragmentMainPageBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainPageBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        navController = NavHostFragment.findNavController(this);
        HomeActivity activity = (HomeActivity) getActivity();

        assert activity != null;
        TextView roleView = activity.mNavigationView.getHeaderView(0).findViewById(R.id.role);

        if (mUser != null) {
            DocumentReference dr_user = db.collection(User.COLLECTION).document(mUser.getUid());
            dr_user.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    User.current = snapshot.toObject(User.class);

                    if (User.current == null) {
                        mAuth.signOut();
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return;
                    }


                    if (User.current.getTitle() != 0) {
                        roleView.setText(User.current.getTitle());
                    } else {
                        roleView.setText(User.current.getPreferredTitle());
                    }

                    if (User.current.hasRole(Role.STUDENT)) {
                        DocumentReference dr_student = db.collection(Student.COLLECTION).document(mUser.getUid());
                        dr_student.get()
                                .addOnCompleteListener(command -> {
                                    if (command.isSuccessful()) {
                                        Student.current = command.getResult().toObject(Student.class);

                                        if (Student.current == null) return;


                                        if (User.current.getTitle() == 0) {
                                            String roleTitle = getString(User.current.getPreferredTitle());
                                            String dept = getString(Student.current.getDepartment().getResourceId());
                                            String year = Student.current.getYear() == 1 ? "1st" : Student.current.getYear() == 2 ? "2nd" : Student.current.getYear() == 3 ? "3rd" : Student.current.getYear() == 4 ? "4th" : "Graduation";
                                            roleView.setText(getString(R.string.student_title, roleTitle, dept, year));
                                        }

                                        boolean missingSection = Student.current.getSection() == 0;
                                        if (missingSection) {
                                            Bundle bundle = new Bundle();
                                            bundle.putBoolean("section", true);
                                            navController.navigate(R.id.action_main_page_to_additional_info, bundle);
                                            return;
                                        }

                                        if (!User.current.isFaceValidated()) {
                                            Intent intent = new Intent(activity, DetectorActivity.class);
                                            intent.putExtra("train_mode", true);
                                            intent.putExtra("title", "We need to identify your identity to continue your sign up request.");

                                            startActivity(intent);
                                        }

                                        if (!User.current.isValidated()) {
                                            navController.navigate(R.id.action_main_page_to_validation_required);
                                            return;
                                        }

                                        navController.navigate(R.id.action_main_page_to_student_home);
                                    } else {
                                        mAuth.signOut();
                                        Intent intent = new Intent(activity, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                    }

                    if (User.current.hasRole(Role.ADMIN)) {
                        activity.mNavigationView.getMenu().findItem(R.id.pending_requests).setVisible(true);
                    }

                    if (!User.current.isValidated()) {
                        navController.navigate(R.id.action_main_page_to_validation_required);
                        return;
                    }

                    if (User.current.hasRole(Role.PROFESSOR)) {
                        navController.navigate(R.id.action_main_page_to_professor_home);
                        return;
                    }
                    if (User.current.hasRole(Role.TEACHING_ASSISTANT)) {
                        navController.navigate(R.id.action_main_page_to_assistant_home);
                    }
                } else {
                    mAuth.signOut();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }

        return binding.getRoot();
    }


}
