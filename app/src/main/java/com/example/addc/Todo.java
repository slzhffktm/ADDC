package com.example.addc;

import java.io.Serializable;

public class Todo implements Serializable {

    private String name;
    private String description;
    private String dueDate;
    private String dueTime;
    private MataKuliah mataKuliah;
    private User[] users;
    private boolean done;

    public Todo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Todo(String name, String description, String dueDate, String dueTime, MataKuliah mataKuliah, User[] users, boolean done) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.mataKuliah = mataKuliah;
        this.users = users;
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

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public MataKuliah getMataKuliah() {
        return mataKuliah;
    }

    public void setMataKuliah(MataKuliah mataKuliah) {
        this.mataKuliah = mataKuliah;
    }

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
