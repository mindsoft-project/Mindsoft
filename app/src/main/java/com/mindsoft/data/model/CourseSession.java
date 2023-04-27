package com.mindsoft.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.stream.Collectors;

public class CourseSession {
    public static String COLLECTION = "course_sessions";

    private String id;

    @PropertyName("title")
    private String title;

    @PropertyName("course")
    private DocumentReference course;

    @PropertyName("date")
    private Timestamp date;

    @PropertyName("status")
    private int status;

    @PropertyName("instructor")
    private DocumentReference instructor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getCourse() {
        return course;
    }

    public void setCourse(DocumentReference course) {
        this.course = course;
    }

    @Exclude
    public Status getStatusValue() {
        return Status.values()[status];
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public DocumentReference getInstructor() {
        return instructor;
    }

    public void setInstructor(DocumentReference instructor) {
        this.instructor = instructor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION).document(id);
    }

    public enum Status {
        ONGOING,
        ENDED
    }
}
