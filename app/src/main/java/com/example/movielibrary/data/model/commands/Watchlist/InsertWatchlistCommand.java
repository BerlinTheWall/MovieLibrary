package com.example.movielibrary.data.model.commands.Watchlist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.WatchListDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.DbCommand;

public class InsertWatchlistCommand implements DbCommand<WatchList> {

    private final WatchListDao watchListDao;
    private final WatchList watchList;

    public InsertWatchlistCommand(Context context, WatchList watchList) {
        DbManager dbManager = DbManager.getInstance(context);

        this.watchList = watchList;
        this.watchListDao = dbManager.watchListDao();
    }

    @Override
    public DbResult<WatchList> execute() {
        DbResult<WatchList> dbResult = new DbResult<>();

        long watchListId = watchListDao.insert(watchList);

        if (watchListId > 0) {
            dbResult.setResult(watchList);
        } else {
            dbResult.setError(new Error("Something went wrong"));
        }
        return dbResult;
    }
}