package com.example.addc;

import java.io.Serializable;

public class Todo implements Serializable {

    private String name;
    private String description;
    private String dueDate;
    private String dueTime;
    private String mataKuliahId;
    private boolean done;

    public Todo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Todo(String name, String description, String dueDate, String dueTime, String mataKuliahId, boolean done) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.mataKuliahId = mataKuliahId;
        this.done = done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getMataKuliahId() {
        return mataKuliahId;
    }

    public void setMataKuliahId(String mataKuliahId) {
        this.mataKuliahId = mataKuliahId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
