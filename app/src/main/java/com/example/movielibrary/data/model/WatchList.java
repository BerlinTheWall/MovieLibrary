package com.example.movielibrary.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class WatchList {
    @PrimaryKey
    @NonNull
    @SerializedName("objectId")
    private String id;
    private String movieId;
    private String userId;

    public WatchList(@NonNull String id, String movieId, String userId) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
    }

    @Ignore
    public WatchList(String movieId, String userId) {
        this.movieId = movieId;
        this.userId = userId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
