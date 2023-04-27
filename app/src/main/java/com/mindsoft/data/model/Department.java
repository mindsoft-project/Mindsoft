package com.mindsoft.data.model;

import androidx.annotation.StringRes;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import com.mindsoft.R;

import java.util.HashMap;

public class Department {
    public static String COLLECTION = "departments";

    public static Department ICT = new Department(1, "ict", R.string.dept_ict);
    public static Department MECHATRONICS = new Department(1 << 1, "mechatronics", R.string.dept_mechatronics);
    public static Department AUTOTRONICS = new Department(1 << 2, "autotronics", R.string.dept_autotronics);
    public static Department PETROLEUM = new Department(1 << 3, "petroleum", R.string.dept_petroleum);
    public static Department PROSTHETICS = new Department(1 << 4, "prosthetics", R.string.dept_prosthetics);
    public static Department RENEWABLE_ENERGY = new Department(1 << 5, "renewable_energy", R.string.dept_renewable_energy);

    public static HashMap<Integer, Department> departments = new HashMap<>() {{
        put(ICT.id, ICT);
        put(MECHATRONICS.id, MECHATRONICS);
        put(AUTOTRONICS.id, AUTOTRONICS);
        put(PETROLEUM.id, PETROLEUM);
        put(PROSTHETICS.id, PROSTHETICS);
        put(RENEWABLE_ENERGY.id, RENEWABLE_ENERGY);
    }};

    public Department() {
    }

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Department(int id, String name, @StringRes int resourceId) {
        this(id, name);
        this.resourceId = resourceId;
    }

    @Exclude
    private int id;

    @Exclude
    @StringRes
    private int resourceId;

    @Exclude
    private User head;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    @PropertyName("head")
    public DocumentReference getReferenceHead() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return head == null ? null : db.collection(User.COLLECTION).document(head.getId());
    }

    @PropertyName("head")
    public void setReferenceHead(DocumentReference reference) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        reference.get().addOnSuccessListener(command -> {
            this.head = command.toObject(User.class);
        });
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION).document(String.valueOf(id));
    }

    @Exclude
    public User getHead() {
        return head;
    }

    @Exclude
    public void setHead(User head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", head=" + head +
                ", resourceId=" + resourceId +
                '}';
    }

}
