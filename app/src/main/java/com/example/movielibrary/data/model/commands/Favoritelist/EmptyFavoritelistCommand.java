package com.example.movielibrary.data.model.commands.Favoritelist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.FavoriteListDao;
import com.example.movielibrary.data.db.dao.WatchListDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class EmptyFavoritelistCommand implements DbCommand<String> {

    private final FavoriteListDao favoriteListDao;

    public EmptyFavoritelistCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);

        this.favoriteListDao = dbManager.favoriteListDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = favoriteListDao.emptyFavoritelistTable();

//        if (affectedRows > 0) {
//        } else {
//            Error error = new Error("Something went wrong");
//            dbResult.setError(error);
//        }

        return dbResult;
    }
}