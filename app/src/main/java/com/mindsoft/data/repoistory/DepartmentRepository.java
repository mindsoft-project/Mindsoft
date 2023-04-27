package com.mindsoft.data.repoistory;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mindsoft.data.model.Department;

import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository {
    private static FirebaseAuth mAuth;

    private static DepartmentRepository instance;

    private FirebaseFirestore mFirestore;

    public DepartmentRepository() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static synchronized DepartmentRepository getInstance() {
        if (instance == null) {
            instance = new DepartmentRepository();
            mAuth = FirebaseAuth.getInstance();
        }
        return instance;
    }

    public MutableLiveData<List<Department>> getDepartments() {
        MutableLiveData<List<Department>> liveDepartments = new MutableLiveData<>();
        mFirestore.collection(Department.COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();

                        List<Department> departments = new ArrayList<>();

                        for (QueryDocumentSnapshot documentSnapshot : snapshot) {
                            Department department = documentSnapshot.toObject(Department.class);
                            departments.add(department);
                        }

                        liveDepartments.postValue(departments);
                    } else {
                        liveDepartments.postValue(new ArrayList<>());
                    }
                });
        return liveDepartments;
    }
}
