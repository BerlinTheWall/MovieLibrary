package com.example.movielibrary.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movielibrary.data.model.Movie;

import java.util.List;

@Dao
public interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Movie movie);

    @Update
    int update(Movie movie);

    @Query("delete from movie where id= :id")
    int delete(String id);

    @Query("select * from movie where id= :movieId")
    Movie getMovie(String movieId);

    @Query("select * from movie")
    List<Movie> getAll();

    @Query("select * from movie where userId= :userId")
    List<Movie> getUserMovies(String userId);

    @Query("DELETE FROM movie")
    int emptyMovieTable();
}
