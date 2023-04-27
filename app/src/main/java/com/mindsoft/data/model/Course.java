package com.mindsoft.data.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Course {
    public static String COLLECTION = "courses";

    private String id;

    private String name;

    private String description;

    private int departments;

    @PropertyName("enrolled_by")
    private List<DocumentReference> enrolledBy = new ArrayList<>();

    @PropertyName("professor")
    private DocumentReference professor;

    @PropertyName("teaching_assistants")
    private List<DocumentReference> teachingAssistants = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepartments() {
        return departments;
    }

    public void setDepartments(int departments) {
        this.departments = departments;
    }

    @Exclude
    public boolean hasDepartment(int departmentId) {
        return (this.departments & departmentId) == departmentId;
    }

    @Exclude
    public boolean hasDepartment(Department department) {
        return (this.departments & department.getId()) == department.getId();
    }

    public void addDepartment(Department department) {
        this.departments |= department.getId();
    }

    public void removeDepartment(Department department) {
        this.departments &= ~department.getId();
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION).document(id);
    }

    public DocumentReference getProfessor() {
        return professor;
    }

    public void setProfessor(DocumentReference professor) {
        this.professor = professor;
    }

    @PropertyName("teaching_assistants")
    public List<DocumentReference> getTeachingAssistants() {
        return teachingAssistants;
    }

    @PropertyName("teaching_assistants")
    public void setTeachingAssistants(List<DocumentReference> teachingAssistants) {
        this.teachingAssistants = teachingAssistants;
    }

    @Exclude
    public List<String> getTeachingAssistantsIds() {
        return teachingAssistants.stream().map(DocumentReference::getId).collect(Collectors.toList());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("enrolled_by")
    public List<DocumentReference> getEnrolledBy() {
        return enrolledBy;
    }

    @PropertyName("enrolled_by")
    public void setEnrolledBy(List<DocumentReference> enrolledBy) {
        this.enrolledBy = enrolledBy;
    }

    @Exclude
    public List<String> getEnrolledByIds() {
        return enrolledBy.stream().map(DocumentReference::getId).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", departments=" + departments +
                ", enrolledBy=" + enrolledBy +
                ", professor=" + professor +
                ", teachingAssistants=" + teachingAssistants +
                '}';
    }

}
