package com.mindsoft.utils;

import com.mindsoft.data.model.Role;

import java.util.HashMap;

public class UserUtils {

    public static final HashMap<String, Role> USER_TYPE = new HashMap<>() {{
        put("student", Role.STUDENT);
        put("teaching_assistant", Role.TEACHING_ASSISTANT);
        put("professor", Role.PROFESSOR);
    }};

}
