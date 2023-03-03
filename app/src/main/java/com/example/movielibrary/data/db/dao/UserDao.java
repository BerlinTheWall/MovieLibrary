package com.example.movielibrary.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movielibrary.data.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Query("select * from user where id= :id")
    User get(String id);

    @Query("select * from user")
    List<User> getAll();

//    @Query("delete from user where id = :id")
//    int delete(String id);

    @Query("DELETE FROM user")
    int emptyUserTable();
}
