package com.example.movielibrary.data.model.commands.Comment;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.CommentDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.commands.DbCommand;

public class InsertCommentCommand implements DbCommand<Comment> {

    private final CommentDao commentDao;
    private final Comment comment;

    public InsertCommentCommand(Context context, Comment comment) {
        DbManager dbManager = DbManager.getInstance(context);

        this.comment = comment;
        this.commentDao = dbManager.commentDao();
    }

    @Override
    public DbResult<Comment> execute() {
        DbResult<Comment> dbResult = new DbResult<>();

        long movieId = commentDao.insert(comment);

        if (movieId > 0) {
            dbResult.setResult(comment);
        } else {
            dbResult.setError(new Error("Something went wrong"));
        }
        return dbResult;
    }
}