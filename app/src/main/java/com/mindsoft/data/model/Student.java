package com.mindsoft.data.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

public class Student {
    public static String COLLECTION = "students";

    public static Student current;

    @PropertyName("id")
    private String id;

    @PropertyName("user")
    private DocumentReference user;

    @Exclude
    private Department department;

    @PropertyName("semester")
    private int semester;

    @PropertyName("student_code")
    private String studentCode;

    @PropertyName("section")
    private int section;

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    @Exclude
    public int getYear() {
        return (int) Math.round(semester / 2.0);
    }

    @Exclude
    public Department getDepartment() {
        return department;
    }

    @Exclude
    public void setDepartment(Department department) {
        this.department = department;
    }


    @PropertyName("department")
    public int getDepartmentID() {
        return department.getId();
    }

    @PropertyName("department")
    public void setDepartmentID(int id) {
        this.department = Department.departments.get(id);
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return user == null ? null : db.collection(COLLECTION).document(user.getId());
    }

    @NonNull
    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", department=" + department +
                ", semester=" + semester +
                ", studentCode='" + studentCode + '\'' +
                ", section=" + section +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
