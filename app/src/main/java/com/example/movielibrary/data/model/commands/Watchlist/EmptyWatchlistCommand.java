package com.example.movielibrary.data.model.commands.Watchlist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.WatchListDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class EmptyWatchlistCommand implements DbCommand<String> {

    private final WatchListDao watchListDao;

    public EmptyWatchlistCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);

        this.watchListDao = dbManager.watchListDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = watchListDao.emptyWatchlistTable();

//        if (affectedRows > 0) {
//        } else {
//            Error error = new Error("Something went wrong");
//            dbResult.setError(error);
//        }

        return dbResult;
    }
}