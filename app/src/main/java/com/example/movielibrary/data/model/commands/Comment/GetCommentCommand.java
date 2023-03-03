package com.example.movielibrary.data.model.commands.Comment;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.CommentDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetCommentCommand implements DbCommand<List<Comment>> {

    private final CommentDao commentDao;

    public GetCommentCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);
        this.commentDao = dbManager.commentDao();
    }

    @Override
    public DbResult<List<Comment>> execute() {
        DbResult<List<Comment>> dbResult = new DbResult<>();

        List<Comment> comments = commentDao.getAll();

        if (comments != null) {
            dbResult.setResult(comments);
        } else {
            Error error = new Error("Something went wrong");
            dbResult.setError(error);
        }
        return dbResult;
    }
}