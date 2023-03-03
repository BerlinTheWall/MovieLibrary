package com.example.movielibrary.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.movielibrary.data.db.dao.CommentDao;
import com.example.movielibrary.data.db.dao.FavoriteListDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.db.dao.WatchListDao;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.WatchList;

@Database(entities = {Movie.class, User.class, FavoriteList.class, Comment.class, WatchList.class}, version = 1)
public abstract class DbManager extends RoomDatabase {

    private static final String DB_NAME = "MovieLibrary";
    private static DbManager dbManager;

    public abstract MovieDao movieDao();
    public abstract UserDao userDao();
    public abstract WatchListDao watchListDao();
    public abstract FavoriteListDao favoriteListDao();
    public abstract CommentDao commentDao();

    public static DbManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = Room.databaseBuilder(context, DbManager.class, DB_NAME)
                    .fallbackToDestructiveMigration().build();
        }
        return dbManager;
    }
}
