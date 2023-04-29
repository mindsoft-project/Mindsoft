package com.mindsoft.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;

public class SignupViewModel extends ViewModel {
    private FirebaseAuth mAuth;

    public SignupViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void singUpUser(String email, String password, User user, SignupViewModel.OnSignupListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser mUser = mAuth.getCurrentUser();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        if (mUser == null) {
                            listener.onError("Unexpected error 0, 1.");
                            return;
                        }

                        DocumentReference collection = db.collection(User.COLLECTION).document(mUser.getUid());
                        user.setId(mUser.getUid());
                        collection.set(user)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(user.getFirstName() + " " + user.getLastName())
                                                .build();

                                        mUser.updateProfile(profileUpdates).addOnCompleteListener(command1 -> {
                                            if (command1.isSuccessful()) {
                                                listener.onSuccess();
                                            } else {
                                                listener.onError(command1.getException().getMessage());
                                            }
                                        });
                                    } else {
                                        listener.onError(command.getException().getMessage());
                                    }
                                });
                    } else {
                        listener.onError(command.getException().getMessage());
                    }
                });
    }


    public void singUpUser(String email, String password, User user, Student student, SignupViewModel.OnSignupListener listener) {
        singUpUser(email, password, user, new OnSignupListener() {
            @Override
            public void onSuccess() {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser mUser = mAuth.getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if (mUser == null) {
                    listener.onError("Unexpected error 0, 0.");
                    return;
                }

                student.setUser(user.getReference());
                student.setId(user.getId());

                DocumentReference collection = db.collection(Student.COLLECTION).document(mUser.getUid());
                collection.set(student)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                listener.onSuccess();
                            } else {
                                listener.onError(task.getException().getMessage());
                            }
                        });
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });
    }

    public interface OnSignupListener {
        void onSuccess();

        void onError(String message);
    }
}

