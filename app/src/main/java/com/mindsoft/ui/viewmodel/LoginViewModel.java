package com.mindsoft.ui.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginViewModel extends ViewModel {

    private FirebaseAuth mAuth;

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(String email, String password, OnLoginListener listener) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCanceledListener(() -> {
                })
                .addOnFailureListener(command -> {
                })
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                listener.onSuccess();
                            } else
                                listener.onError(task.getException().getMessage());
                        }
                );
    }

    public interface OnLoginListener {
        void onSuccess();

        void onError(String message);
    }
}
