package com.example.movielibrary.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.InvalidationTracker;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.movielibrary.utils.Converters;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Entity
@TypeConverters({Converters.class})
public class Movie{
    @PrimaryKey
    @NonNull
    @SerializedName("objectId")
    private String id;
    private String name;
    private String description;
    private String genre;
    private String cast;
    private int rate;
    private int year;
    private String status;
    private ArrayList<String> likeNum;
    private String userId;

    public Movie(@NonNull String id, String name, String description, String genre, String cast, int rate, int year, String status, ArrayList<String> likeNum, String userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.cast = cast;
        this.rate = rate;
        this.year = year;
        this.status = status;
        this.likeNum = likeNum;
        this.userId = userId;
    }

    @Ignore
    public Movie(@NonNull String id, String name, String description, String genre, String cast, int rate, int year, String status, String userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.cast = cast;
        this.rate = rate;
        this.year = year;
        this.status = status;
        this.userId = userId;
    }

    @Ignore
    public Movie(String name, String description, String genre, String cast, int rate, int year, String status, String userId) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.cast = cast;
        this.rate = rate;
        this.year = year;
        this.status = status;
        this.userId = userId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public int getLikeNumber() {
        return likeNum.size();
    }

    public ArrayList<String> getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(ArrayList<String> likeNum) {
        this.likeNum = likeNum;
    }
}
