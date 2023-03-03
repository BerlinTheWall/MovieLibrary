package com.example.movielibrary.utils;

import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.WatchList;

import java.util.ArrayList;

public class DataStore {
    private static DataStore dataStore;
    User loggedInUser;
    ArrayList<Movie> movies;
    ArrayList<Movie> favoriteLists;
    ArrayList<Movie> watchLists;
    ArrayList<Comment> comments;

    private DataStore() {
        loggedInUser = new User();
        movies = new ArrayList<>();
        favoriteLists = new ArrayList<>();
        watchLists = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public static DataStore getInstance() {
        if (dataStore == null) {
            dataStore = new DataStore();
        }
        return dataStore;
    }

    public User getUser() {
        return loggedInUser;
    }

    public void setUser(User user) {
        this.loggedInUser = user;
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public ArrayList<Movie> getFavoriteLists() {
        return favoriteLists;
    }

    public ArrayList<Movie> getWatchLists() {
        return watchLists;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }
}
