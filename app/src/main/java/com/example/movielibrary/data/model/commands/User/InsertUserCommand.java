package com.example.movielibrary.data.model.commands.User;

import android.content.Context;

import com.example.movielibrary.data.db.DbManager;
import com.example.movielibrary.data.db.dao.UserDao;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.DbCommand;

public class InsertUserCommand implements DbCommand<User> {

  private final UserDao userDao;
  private final User user;

  public InsertUserCommand(Context context, User user) {
    DbManager dbManager = DbManager.getInstance(context);

    this.user = user;
    this.userDao = dbManager.userDao();
  }

  @Override
  public DbResult<User> execute() {
    DbResult<User> dbResult = new DbResult<>();

    long userId = userDao.insert(user);

    if (userId > 0) {
      dbResult.setResult(user);
    } else {
      dbResult.setError(new Error("Something went wrong"));
    }
    return dbResult;
  }
}
