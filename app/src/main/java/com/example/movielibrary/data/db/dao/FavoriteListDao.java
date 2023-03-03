package com.example.movielibrary.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movielibrary.data.model.FavoriteList;

import java.util.List;

@Dao
public interface FavoriteListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FavoriteList favoriteList);

    @Query("select * from favoriteList where id= :id")
    FavoriteList get(long id);

    @Query("delete from favoriteList where id= :id")
    int delete(long id);

    @Query("select * from favoriteList")
    List<FavoriteList> getAll();

    @Query("DELETE FROM favoritelist")
    int emptyFavoritelistTable();
}
