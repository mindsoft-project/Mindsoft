package com.mindsoft.data.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import java.util.List;
import java.util.Map;

public class FaceImage {
    public static final String COLLECTION = "face_images";

    @PropertyName("id")
    private String id;

    @PropertyName("extra")
    private Map<String, List<Float>> embeddings;

    @Exclude
    public DocumentReference getReferenceUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(User.COLLECTION).document(id);
    }

    @Exclude
    public DocumentReference getReferenceStudent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(Student.COLLECTION).document(id);
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION).document(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, List<Float>> getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(Map<String, List<Float>> embeddings) {
        this.embeddings = embeddings;
    }
}
