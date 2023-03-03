package com.example.movielibrary.data.model.commands.Movie;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class DeleteMovieCommand implements DbCommand<String> {

    private final MovieDao movieDao;
    private final String movieId;

    public DeleteMovieCommand(Context context, String movieId) {
        DbManager dbManager = DbManager.getInstance(context);

        this.movieId = movieId;
        this.movieDao = dbManager.movieDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = movieDao.delete(movieId);

        if (affectedRows > 0) {
            dbResult.setResult(movieId);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }

        return dbResult;
    }
}
