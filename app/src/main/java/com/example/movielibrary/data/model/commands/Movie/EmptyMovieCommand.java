package com.example.movielibrary.data.model.commands.Movie;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class EmptyMovieCommand implements DbCommand<String> {

    private final MovieDao movieDao;

    public EmptyMovieCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);

        this.movieDao = dbManager.movieDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = movieDao.emptyMovieTable();

//        if (affectedRows > 0) {
//        } else {
//            Error error = new Error("Something went wrong");
//            dbResult.setError(error);
//        }

        return dbResult;
    }
}
