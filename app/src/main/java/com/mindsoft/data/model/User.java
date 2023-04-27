package com.mindsoft.data.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.PropertyName;
import com.mindsoft.R;

public class User {

    public static String COLLECTION = "users";

    public static User current;

    @Exclude
    private String id;

    @PropertyName("first_name")
    private String firstName;

    @PropertyName("last_name")
    private String lastName;

    @PropertyName("email")
    private String email;

    @PropertyName("national_id")
    private String nationalID;

    @PropertyName("roles")
    private int roles;

    @PropertyName("title")
    @StringRes
    private int title;

    @PropertyName("validated")
    private boolean isValidated;

    @PropertyName("face_validated")
    private boolean isFaceValidated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNationalID() {
        return nationalID;
    }

    public void setNationalID(String nationalID) {
        this.nationalID = nationalID;
    }

    public int getRoles() {
        return roles;
    }

    public void setRoles(int roles) {
        this.roles = roles;

    }

    @StringRes
    public int getTitle() {
        return title;
    }

    public void setTitle(@StringRes int title) {
        this.title = title;
    }

    @Exclude
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public boolean hasRole(int role) {
        return (roles & role) == role;
    }

    public boolean hasRole(Role role) {
        return hasRole(role.getId());
    }

    public void grantRole(Role role) {
        this.roles |= role.getId();
    }


    public void revokeRole(Role role) {
        this.roles &= ~role.getId();
    }

    @Exclude
    public DocumentReference getReference() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection(COLLECTION).document(id);
    }

    @Exclude
    @StringRes
    public int getPreferredTitle() {
        if (hasRole(Role.ADMIN)) {
            return R.string.title_admin;
        } else if (hasRole(Role.DEPT_MANAGER)) {
            return R.string.title_dept_manager;
        } else if (hasRole(Role.PROFESSOR)) {
            return R.string.title_professor;
        } else if (hasRole(Role.TEACHING_ASSISTANT)) {
            return R.string.title_teaching_assistant;
        } else if (hasRole(Role.STUDENT)) {
            return R.string.title_student;
        }
        return R.string.title_none;
    }

    public void fetchLocation(Activity activity) {
        FusedLocationProviderClient mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(activity);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(activity);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    PackageManager.PERMISSION_GRANTED
            );
        } else {
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
            mFusedLocationProvider.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    UserLocation userLocation = new UserLocation();
                    userLocation.setId(this.id);
                    userLocation.setLocation(location);
                    database.getReference("user_location").child(this.id).setValue(userLocation);
                }
            });
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", nationalID='" + nationalID + '\'' +
                ", roles=" + roles +
                ", title=" + title +
                ", isValidated=" + isValidated +
                ", isFaceValidated=" + isFaceValidated +
                '}';
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    public boolean isFaceValidated() {
        return isFaceValidated;
    }

    public void setFaceValidated(boolean faceValidated) {
        isFaceValidated = faceValidated;
    }
}
