package com.example.movielibrary.data.db;

import com.example.movielibrary.data.model.DbResult;

public interface DbResponse<T> {

  void onResponse(DbResult<T> t);
}
