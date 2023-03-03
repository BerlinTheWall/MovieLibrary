package com.example.movielibrary.data.model.commands.Movie;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetMovieCommand implements DbCommand<Movie> {

    private final MovieDao movieDao;
    private final String movieId;

    public GetMovieCommand(Context context, String movieId) {
        DbManager dbManager = DbManager.getInstance(context);
        this.movieId = movieId;
        this.movieDao = dbManager.movieDao();
    }

    @Override
    public DbResult<Movie> execute() {
        DbResult<Movie> dbResult = new DbResult<>();

        Movie movie = movieDao.getMovie(movieId);

        if (movie != null) {
            dbResult.setResult(movie);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }
        return dbResult;
    }

}