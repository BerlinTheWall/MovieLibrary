package com.example.movielibrary.data.model.commands.User;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class DeleteUserCommand implements DbCommand<String> {

    private final UserDao userDao;

    public DeleteUserCommand(Context context) {
        DbManager dbManager = DbManager.getInstance(context);

        this.userDao = dbManager.userDao();
    }

    @Override
    public DbResult<String> execute() {
        DbResult<String> dbResult = new DbResult<>();

        int affectedRows = userDao.emptyUserTable();

//        if (affectedRows > 0) {
//        } else {
//            Error error = new Error("Something went wrong");
//            dbResult.setError(error);
//        }

        return dbResult;
    }
}
