package com.example.movielibrary.data.model.commands.Movie;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.DbCommand;

public class InsertMovieCommand implements DbCommand<Movie> {

    private final MovieDao movieDao;
    private final Movie movie;

    public InsertMovieCommand(Context context, Movie movie) {
        DbManager dbManager = DbManager.getInstance(context);

        this.movie = movie;
        this.movieDao = dbManager.movieDao();
    }

    @Override
    public DbResult<Movie> execute() {
        DbResult<Movie> dbResult = new DbResult<>();

        long movieId = movieDao.insert(movie);

        if (movieId > 0) {
            dbResult.setResult(movie);
        } else {
            dbResult.setError(new Error("Something went wrong"));
        }
        return dbResult;
    }
}