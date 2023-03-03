package com.example.movielibrary.data.model;

public class DbResult<T> {

  private T result;
  private Error error;

  public void setResult(T result) {
    this.result = result;
  }

  public void setError(Error error) {
    this.error = error;
  }

  public T getResult() {
    return result;
  }

  public Error getError() {
    return error;
  }
}
