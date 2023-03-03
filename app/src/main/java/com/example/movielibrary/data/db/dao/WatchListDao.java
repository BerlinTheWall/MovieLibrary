package com.example.movielibrary.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movielibrary.data.model.WatchList;

import java.util.List;

@Dao
public interface WatchListDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long insert(WatchList watchList);

        @Query("select * from watchList where id= :id")
        WatchList get(long id);

        @Query("delete from watchList where id= :id")
        int delete(long id);

        @Query("select * from watchList")
        List<WatchList> getAll();

        @Query("DELETE FROM watchlist")
        int emptyWatchlistTable();
}
