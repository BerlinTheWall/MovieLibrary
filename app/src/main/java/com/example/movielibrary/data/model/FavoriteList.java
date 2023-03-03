package com.example.movielibrary.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class FavoriteList {
    @PrimaryKey
    @NonNull
    @SerializedName("objectId")
    private String id;
    private String movieId;
    private String userId;

    public FavoriteList(@NonNull String id, String movieId, String userId) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
    }

    @Ignore
    public FavoriteList(String movieId, String userId) {
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
