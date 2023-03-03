package com.example.movielibrary.data.model.commands.Comment;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.CommentDao;
import com.example.movielibrary.data.db.dao.MovieDao;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class EmptyCommentCommand implements DbCommand<String> {

    private final CommentDao commentDao;

    public EmptyCommentCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);

        this.commentDao = dbManager.commentDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = commentDao.emptyCommentTable();

//        if (affectedRows > 0) {
//        } else {
//            Error error = new Error("Something went wrong");
//            dbResult.setError(error);
//        }

        return dbResult;
    }
}