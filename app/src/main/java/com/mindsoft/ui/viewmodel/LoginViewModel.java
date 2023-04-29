package com.mindsoft.ui.viewmodel;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mindsoft.data.model.Department;
import com.mindsoft.data.model.Role;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.StudentInfo;
import com.mindsoft.data.model.User;
import com.mindsoft.data.repoistory.SystemRepository;

public class LoginViewModel extends ViewModel {

    private FirebaseAuth mAuth;

    private SystemRepository system = new SystemRepository();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(String email, String password, OnLoginListener listener) {
        if (email.length() == 8 && email.matches("\\d+")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Student.COLLECTION).whereEqualTo("studentCode", email).get().addOnSuccessListener(command -> {
                if (command.size() > 0) {
                    DocumentSnapshot first = command.getDocuments().get(0);
                    Student student = first.toObject(Student.class);
                    if (student != null) {
                        student.getUser().get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);
                                if (user != null) {
                                    loginUser(user.getEmail(), password, listener);
                                } else {
                                    listener.onError("Could not find user.");
                                }
                            } else {
                                listener.onError("Could not find user.");
                            }
                        });
                    }
                } else {
                    system.getStudentInfo(email, password).observeForever(studentInfo -> {
                        if (studentInfo != null) {
                            User user = new User();
                            user.setEmail(studentInfo.getEmail());
                            user.setFaceValidated(false);
                            user.setNationalID(studentInfo.getNationalID());
                            user.setFirstName(studentInfo.getFirstName());
                            user.setLastName(studentInfo.getLastName());
                            user.setRoles(Role.STUDENT.getId());
                            user.setValidated(false);

                            Student student = new Student();
                            student.setDepartment(Department.departments.get(studentInfo.getDepartmentID()));
                            student.setSemester(studentInfo.getSemester());
                            student.setStudentCode(studentInfo.getStudentCode());

                            mAuth.createUserWithEmailAndPassword(user.getEmail(), password).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser mUser = mAuth.getCurrentUser();
                                    if (mUser == null) {
                                        listener.onError("Unexpected error 0, 1.");
                                        return;
                                    }
                                    DocumentReference collection = db.collection(User.COLLECTION).document(mUser.getUid());
                                    user.setId(mUser.getUid());
                                    collection.set(user).addOnCompleteListener(cmd -> {
                                        if (cmd.isSuccessful()) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(user.getFirstName() + " " + user.getLastName()).build();

                                            mUser.updateProfile(profileUpdates).addOnCompleteListener(command1 -> {
                                                if (command1.isSuccessful()) {
                                                    student.setId(mUser.getUid());
                                                    student.setUser(user.getReference());
                                                    db.collection(Student.COLLECTION).document(user.getId()).set(student).addOnCompleteListener(stdcmd -> {
                                                        if (task.isSuccessful()) {
                                                            listener.onSuccess();
                                                        } else {
                                                            listener.onError("Could not create user with this data");
                                                        }
                                                    });
                                                } else {
                                                    listener.onError(command1.getException().getMessage());
                                                }
                                            });
                                        } else {
                                            listener.onError("Could not create user with this data.");
                                        }
                                    });
                                } else {
                                    listener.onError("Could not create user with this data.");
                                }
                            });
                        } else {
                            listener.onError("Could not find student with this student code and national id");
                        }
                    });
                }
            });
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onSuccess();
            } else listener.onError(task.getException().getMessage());
        });
    }

    public interface OnLoginListener {
        void onSuccess();

        void onError(String message);
    }
}
