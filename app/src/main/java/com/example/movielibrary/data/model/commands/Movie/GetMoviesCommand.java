package com.example.movielibrary.data.model.commands.Movie;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetMoviesCommand implements DbCommand<List<Movie>> {

    private final MovieDao movieDao;

    public GetMoviesCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.movieDao = dbManager.movieDao();
    }

    @Override
    public DbResult<List<Movie>> execute() {
        DbResult<List<Movie>> dbResult = new DbResult<>();

        List<Movie> movies = movieDao.getAll();

        if (movies != null) {
            dbResult.setResult(movies);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }
        return dbResult;
    }
}