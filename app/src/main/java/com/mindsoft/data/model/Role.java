package com.mindsoft.data.model;

import androidx.annotation.StringRes;

import com.google.firebase.firestore.Exclude;
import com.mindsoft.R;

import java.util.HashMap;

public class Role {
    public static Role STUDENT = new Role(1, "student", new Metadata(R.string.title_student));
    public static Role TEACHING_ASSISTANT = new Role(1 << 1, "teaching_assistant", new Metadata(R.string.title_teaching_assistant));
    public static Role PROFESSOR = new Role(1 << 2, "professor", new Metadata(R.string.title_professor));
    public static Role DEPT_MANAGER = new Role(1 << 3, "department_manager", new Metadata(R.string.title_dept_manager));
    public static Role ADMIN = new Role(1 << 4, "admin", new Metadata(R.string.title_admin));

    public static HashMap<Integer, Role> roles = new HashMap<>() {{
        put(STUDENT.id, STUDENT);
        put(TEACHING_ASSISTANT.id, TEACHING_ASSISTANT);
        put(PROFESSOR.id, PROFESSOR);
        put(DEPT_MANAGER.id, DEPT_MANAGER);
        put(ADMIN.id, ADMIN);
    }};

    private int id;

    private String name;

    @Exclude
    private Role.Metadata metadata;


    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Role(int id, String name, Metadata metadata) {
        this(id, name);
        this.metadata = metadata;
    }

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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static class Metadata {
        @StringRes
        private int stringId;

        public Metadata(@StringRes int stringId) {
            this.stringId = stringId;
        }

        public int getStringId() {
            return stringId;
        }

        public void setStringId(int stringId) {
            this.stringId = stringId;
        }
    }
}
