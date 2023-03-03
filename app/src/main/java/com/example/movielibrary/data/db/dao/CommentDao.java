package com.example.movielibrary.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.movielibrary.data.model.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Comment comment);

    @Query("select * from comment where id= :id")
    Comment get(long id);

    @Query("delete from comment where id= :id")
    int delete(long id);

    @Query("select * from comment")
    List<Comment> getAll();

    @Query("DELETE FROM comment")
    int emptyCommentTable();
}
