package com.example.movielibrary.data.model.commands;

import com.example.movielibrary.data.model.DbResult;

public interface DbCommand<T> {

  DbResult<T> execute();
}
