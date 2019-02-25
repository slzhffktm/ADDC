package com.example.addc;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    // attributes
    private String id;
    private String email;
    private String name;
    private String picture;
    private MataKuliah[] mataKuliahs;
    private double latitude;
    private double longitude;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public User(String id, String email, String name, String picture) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.longitude = 0;
        this.latitude = 0;
    }

    public User(String id, String email, String name, String picture, double latitude, double longitude) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public MataKuliah[] getMataKuliahs() {
        return mataKuliahs;
    }

    public void setMataKuliahs(MataKuliah[] mataKuliahs) {
        this.mataKuliahs = mataKuliahs;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> user =  new HashMap<String,String>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("picture", picture);

        return user;
    }

}
