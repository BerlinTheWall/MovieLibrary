package com.example.movielibrary.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class User {
    @PrimaryKey @NonNull @SerializedName("objectId")
    private String id;
    private String firstName;
    private String lastName;
    private String password;
    private String sessionToken;
    private String username;

    public User(@NonNull String id, String firstName, String lastName, String username, String sessionToken) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sessionToken = sessionToken;
        this.username = username;
    }

    @Ignore
    public User(String firstName, String lastName, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.username = username;
    }

    @Ignore
    public User(String username, String password) {
        this.password = password;
        this.username = username;
    }

    @Ignore
    public User() {}

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
