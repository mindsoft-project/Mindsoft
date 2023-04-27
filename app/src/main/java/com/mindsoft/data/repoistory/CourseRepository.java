package com.mindsoft.data.repoistory;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mindsoft.data.model.Course;
import com.mindsoft.data.model.CourseSession;
import com.mindsoft.data.model.Student;
import com.mindsoft.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class CourseRepository {
    private static FirebaseAuth mAuth;

    private static CourseRepository instance;

    private FirebaseFirestore mFirestore;

    public CourseRepository() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static synchronized CourseRepository getInstance() {
        if (instance == null) {
            instance = new CourseRepository();
            mAuth = FirebaseAuth.getInstance();
        }
        return instance;
    }

    public MutableLiveData<List<Course>> getEnrolledCourses(Student student) {
        MutableLiveData<List<Course>> courses = new MutableLiveData<>();
        mFirestore.collection(Course.COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        List<Course> courseList = new ArrayList<>();

                        for (QueryDocumentSnapshot snapshot : snapshots) {
                            Course course = snapshot.toObject(Course.class);
                            if (course.hasDepartment(student.getDepartmentID())) {
                                if (course.getEnrolledByIds().contains(student.getId())) {
                                    courseList.add(course);
                                }
                            }
                        }
                        courses.postValue(courseList);
                    } else {
                        courses.postValue(new ArrayList<>());
                    }
                });
        return courses;
    }

    public MutableLiveData<List<Course>> getAvailableCourses(Student student) {
        MutableLiveData<List<Course>> courses = new MutableLiveData<>();
        mFirestore.collection(Course.COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        List<Course> courseList = new ArrayList<>();

                        for (QueryDocumentSnapshot snapshot : snapshots) {
                            Course course = snapshot.toObject(Course.class);
                            if (course.hasDepartment(student.getDepartmentID())) {
                                if (!course.getEnrolledByIds().contains(student.getId())) {
                                    courseList.add(course);
                                }
                            }
                        }
                        courses.postValue(courseList);
                    } else {
                        courses.postValue(new ArrayList<>());
                    }
                });
        return courses;
    }

    public MutableLiveData<List<Course>> getOwnedCourses(User professor) {
        MutableLiveData<List<Course>> courses = new MutableLiveData<>();
        mFirestore.collection(Course.COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        List<Course> courseList = new ArrayList<>();

                        for (QueryDocumentSnapshot snapshot : snapshots) {
                            Course course = snapshot.toObject(Course.class);
                            if (course.getProfessor() != null && course.getProfessor().getId().equals(professor.getId())) {
                                courseList.add(course);
                            }
                        }
                        courses.postValue(courseList);
                    } else {
                        courses.postValue(new ArrayList<>());
                    }
                });
        return courses;
    }

    public MutableLiveData<List<User>> getEnrolledUsers(Course course) {
        MutableLiveData<List<User>> students = new MutableLiveData<>();

        if (course.getEnrolledByIds().isEmpty()) {
            return students;
        }

        FirebaseFirestore.getInstance()
                .collection(User.COLLECTION)
                .whereIn(FieldPath.documentId(), course.getEnrolledByIds())
                .get()
                .addOnSuccessListener(command -> {
                    List<User> studentList = new ArrayList<>();
                    List<DocumentSnapshot> snapshots = command.getDocuments();

                    for (DocumentSnapshot snapshot : snapshots) {
                        User student = snapshot.toObject(User.class);
                        studentList.add(student);
                    }

                    students.postValue(studentList);
                });
        return students;

    }

    public MutableLiveData<List<CourseSession>> getSessions(Course course) {
        MutableLiveData<List<CourseSession>> sessions = new MutableLiveData<>();
        FirebaseFirestore.getInstance()
                .collection(CourseSession.COLLECTION)
                .whereEqualTo("course", course.getReference())
                .get()
                .addOnSuccessListener(command -> {
                    List<CourseSession> sessionList = new ArrayList<>();
                    List<DocumentSnapshot> snapshots = command.getDocuments();

                    for (DocumentSnapshot snapshot : snapshots) {
                        CourseSession session = snapshot.toObject(CourseSession.class);
                        sessionList.add(session);
                    }

                    sessions.postValue(sessionList);
                });
        return sessions;
    }
}
