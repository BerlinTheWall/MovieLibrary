package com.example.movielibrary.data.model.commands.Favoritelist;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.FavoriteListDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetFavoritelistCommand implements DbCommand<List<FavoriteList>> {

    private final FavoriteListDao favoriteListDao;

    public GetFavoritelistCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.favoriteListDao = dbManager.favoriteListDao();
    }

    @Override
    public DbResult<List<FavoriteList>> execute() {
        DbResult<List<FavoriteList>> dbResult = new DbResult<>();

        List<FavoriteList> favoriteLists = favoriteListDao.getAll();

        if (favoriteLists != null) {
            dbResult.setResult(favoriteLists);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }
        return dbResult;
    }
}