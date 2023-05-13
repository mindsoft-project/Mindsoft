package com.mindsoft.data.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;

import java.io.InputStream;
import java.net.URL;
import java.util.Observable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Student {
    public static String COLLECTION = "students";

    public static Student current;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

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

    @PropertyName("picture")
    private String pictureUrl;

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

    @Exclude
    public void loadImage(OnImageLoadedListener listener) {
        executor.execute(() -> {
            try {
                InputStream in = new URL(getPictureUrl()).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                handler.post(() -> {
                    listener.onImageLoaded(bitmap);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public interface OnImageLoadedListener {
        void onImageLoaded(Bitmap bitmap);
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
                ", pictureUrl='" + pictureUrl + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
