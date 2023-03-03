package com.example.movielibrary.data.model;

import java.util.ArrayList;
import java.util.List;

public class ResultAPI<T> {
    private List<T> results = new ArrayList<>();

    public ResultAPI (List<T> results){
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

}
