package com.example.movielibrary.data.model.commands.User;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.DbCommand;

import java.util.List;

public class GetUsersCommand implements DbCommand<List<User>> {

  private final UserDao userDao;

  public GetUsersCommand(Context context) {
    DbManager dbManager = DbManager.getInstance(context);
    this.userDao = dbManager.userDao();
  }

  @Override
  public DbResult<List<User>> execute() {
    DbResult<List<User>> dbResult = new DbResult<>();

    List<User> users = userDao.getAll();

    if (users != null) {
      dbResult.setResult(users);
    } else {
      Error error = new Error("Something went wrong");
      dbResult.setError(error);
    }
    return dbResult;
  }
}
