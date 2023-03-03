package com.example.movielibrary.data.model.commands.Watchlist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.FavoriteListDao;
import com.example.movielibrary.data.db.dao.WatchListDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetWatchlistCommand implements DbCommand<List<WatchList>> {

    private final WatchListDao watchListDao;

    public GetWatchlistCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.watchListDao = dbManager.watchListDao();
    }

    @Override
    public DbResult<List<WatchList>> execute() {
        DbResult<List<WatchList>> dbResult = new DbResult<>();

        List<WatchList> watchLists = watchListDao.getAll();

        if (watchLists != null) {
            dbResult.setResult(watchLists);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }
        return dbResult;
    }
}