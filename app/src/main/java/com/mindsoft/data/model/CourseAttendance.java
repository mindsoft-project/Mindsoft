package com.mindsoft.data.model;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class CourseAttendance {
    private String courseId;

    private String sessionId;

    private boolean taking;

    private Map<String, Boolean> attended = new HashMap<>();

    public boolean isTaking() {
        return taking;
    }

    public void setTaking(boolean taking) {
        this.taking = taking;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @NonNull
    @Override
    public String toString() {
        return "CourseAttendance{" +
                "courseId='" + courseId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", taking=" + taking +
                '}';
    }

    public Map<String, Boolean> getAttended() {
        return attended;
    }

    public void setAttended(Map<String, Boolean> attended) {
        this.attended = attended;
    }
}
