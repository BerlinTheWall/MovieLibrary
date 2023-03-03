package com.example.movielibrary.data.async;

import android.os.AsyncTask;
import com.example.movielibrary.data.db.DbResponse;
import com.example.movielibrary.data.model.DbResult;
import com.example.movielibrary.data.model.commands.DbCommand;

public class DBAsyncTask<T> extends AsyncTask<DbCommand<T>, Void, DbResult<T>> {

  private final DbResponse<T> dbResponse;

  public DBAsyncTask(DbResponse<T> dbResponse) {
    this.dbResponse = dbResponse;
  }

  @Override
  protected DbResult<T> doInBackground(DbCommand<T>... dbCommands) {
    return dbCommands[0].execute();
  }

  @Override
  protected void onPostExecute(DbResult<T> dbResult) {
    super.onPostExecute(dbResult);
    dbResponse.onResponse(dbResult);
  }
}
