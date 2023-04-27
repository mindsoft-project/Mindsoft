package com.mindsoft.data.model;

import android.location.Location;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

public class UserLocation {

    @PropertyName("id")
    private String id;

    @PropertyName("longitude")
    private double longitude;

    @PropertyName("latitude")
    private double latitude;


    @Exclude
    public Location getLocation() {
        Location location = new Location("");
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        return location;
    }

    @Exclude
    public void setLocation(Location location) {
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
