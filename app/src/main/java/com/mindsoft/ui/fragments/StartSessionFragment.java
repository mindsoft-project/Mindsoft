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

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.R;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.CourseSession;
import com.mindsoft.data.model.User;
import com.mindsoft.databinding.FragmentStartSessionBinding;

import java.util.ArrayList;

public class StartSessionFragment extends Fragment {

    private FragmentStartSessionBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private NavController mNavController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStartSessionBinding.inflate(inflater, container, false);
        mNavController = NavHostFragment.findNavController(this);

        db = FirebaseFirestore.getInstance();

        assert getArguments() != null;

        String courseId = getArguments().getString("courseId");

        binding.start.setOnClickListener(v -> {
            if (!courseId.isEmpty()) {
                DocumentReference ref = db.collection(CourseSession.COLLECTION).document();
                CourseSession session = new CourseSession();
                session.setId(ref.getId());
                session.setCourse(db.collection(Course.COLLECTION).document(courseId));
                session.setInstructor(User.current.getReference());
                session.setDate(Timestamp.now());
                session.setTitle(binding.title.getText().toString());
                session.setStatus(0);
                ref.set(session).addOnSuccessListener(command -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("sessionId", session.getId());
                    bundle.putString("courseId", courseId);
                    mNavController.navigate(R.id.action_start_session_to_session, bundle);
                });
            }
        });

        return binding.getRoot();
    }
}
