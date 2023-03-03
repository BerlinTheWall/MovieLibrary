package com.example.movielibrary.data.model.commands.Favoritelist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.FavoriteListDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.commands.DbCommand;

public class InsertFavoritelistCommand implements DbCommand<FavoriteList> {

    private final FavoriteListDao favoriteListDao;
    private final FavoriteList favoriteList;

    public InsertFavoritelistCommand(Context context, FavoriteList favoriteList) {
        DbManager dbManager = DbManager.getInstance(context);

        this.favoriteList = favoriteList;
        this.favoriteListDao = dbManager.favoriteListDao();
    }

    @Override
    public DbResult<FavoriteList> execute() {
        DbResult<FavoriteList> dbResult = new DbResult<>();

        long favoriteListId = favoriteListDao.insert(favoriteList);

        if (favoriteListId > 0) {
            dbResult.setResult(favoriteList);
        } else {
            dbResult.setError(new Error("Something went wrong"));
        }
        return dbResult;
    }
}