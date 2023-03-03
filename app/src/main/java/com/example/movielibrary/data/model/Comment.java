package com.example.movielibrary.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class Comment {
    @PrimaryKey
    @NonNull
    @SerializedName("objectId")
    private String id;
    private String movieId;
    private String comment;
    private String userId;

    public Comment(@NonNull String id, String movieId, String userId, String comment) {
        this.id = id;
        this.movieId = movieId;
        this.comment = comment;
        this.userId = userId;
    }

    @Ignore
    public Comment(String movieId, String userId, String comment) {
        this.movieId = movieId;
        this.comment = comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
