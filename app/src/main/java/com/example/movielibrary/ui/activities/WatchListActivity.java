package com.example.movielibrary.ui.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.movielibrary.R;
import com.example.movielibrary.data.async.DBAsyncTask;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.Movie.EmptyMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.InsertMovieCommand;
import com.example.movielibrary.data.model.commands.Watchlist.EmptyWatchlistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.GetWatchlistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.InsertWatchlistCommand;
import com.example.movielibrary.databinding.ActivityMoviePageBinding;
import com.example.movielibrary.databinding.ActivityWatchListBinding;
import com.example.movielibrary.ui.adapter.MovieAdapter;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.util.ArrayList;
import java.util.List;

public class WatchListActivity extends AppCompatActivity {

    private static final String INTENT_KEY_MOVIEID = "movieId";

    private ActivityWatchListBinding binding;
    private DataStore dataStore;
    private MovieAdapter watchlistAdapter;
    private RecyclerView recyclerView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_list);
        setTitle("Watch list");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityWatchListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataStore = DataStore.getInstance();
        context = WatchListActivity.this;
        loadWatchlist();
        search();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdapter(dataStore.getWatchLists());
    }

    private boolean search() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Movie> searchedMovies = new ArrayList<>();
                for (Movie movie : dataStore.getWatchLists()) {
                    if (movie.getName().toLowerCase().contains(newText) ||
                            movie.getDescription().toLowerCase().contains(newText)) {
                        searchedMovies.add(movie);
                    }
                }
                if (searchedMovies.size() == 0 && newText.equals("")) {
                    loadAdapter(dataStore.getWatchLists());
                } else {
                    loadAdapter(searchedMovies);

                }
                return false;
            }
        });
        return false;
    }

    private void loadWatchlist() {
        dataStore.getWatchLists().clear();
        if (!isNetworkConnected()) {
            GetWatchlistCommand getWatchlistCommand = new GetWatchlistCommand(WatchListActivity.this);
            DBAsyncTask<List<WatchList>> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    for (WatchList watchList : resultDb.getResult()) {
                        for (Movie m : dataStore.getMovies()) {
                            if (m.getId().equals(watchList.getMovieId()) && !checkMovieId(dataStore.getWatchLists(), m)) {
                                dataStore.getWatchLists().add(m);
                            }
                        }
                        loadAdapter(dataStore.getWatchLists());
                    }
                } else {
                    Toast.makeText(WatchListActivity.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask2.execute(getWatchlistCommand);

        } else {
            NetworkHelper networkHelper = NetworkHelper.getInstance(this);
            networkHelper.getWatchlist(dataStore.getUser().getId(), dataStore.getUser().getSessionToken(), new ResultListener<WatchList>() {
                @Override
                public void onResult(Result<WatchList> result) {
                    Error error = (result != null) ? result.getError() : null;
                    List<WatchList> resultStd = (result != null) ? result.getItems() : null;

                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.get_watchlist_error);
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    EmptyWatchlistCommand emptyWatchlistCommand = new EmptyWatchlistCommand(WatchListActivity.this);
                    DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {

                        } else {
                            Toast.makeText(WatchListActivity.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask2.execute(emptyWatchlistCommand);

                    for (WatchList watchList : resultStd) {
                        InsertWatchlistCommand insertWatchlistCommand = new InsertWatchlistCommand(WatchListActivity.this, watchList);
                        DBAsyncTask<WatchList> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                for (Movie m : dataStore.getMovies()) {
                                    for (WatchList w : resultStd) {
                                        if (m.getId().equals(w.getMovieId()) && !checkMovieId(dataStore.getWatchLists(), m)) {
                                            dataStore.getWatchLists().add(m);
                                        }
                                    }
                                }
                                loadAdapter(dataStore.getWatchLists());
                            } else {
                                Toast.makeText(WatchListActivity.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(insertWatchlistCommand);
                    }
                }
            });
        }
    }

    private void loadAdapter(ArrayList<Movie> movies) {
        watchlistAdapter = new MovieAdapter(this, movies, new MovieAdapter.MovieAdapterCallback() {

            @Override
            public void onShowDetail(int position) {
                Movie passingMovie = null;
                for (Movie movie : dataStore.getWatchLists()) {
                    if (movie.getId().equals(dataStore.getWatchLists().get(position).getId())) {
                        passingMovie = new Movie(movie.getId(), movie.getName(), movie.getDescription(), movie.getGenre(), movie.getCast(), movie.getRate(), movie.getYear(), movie.getStatus(), movie.getLikeNum(), movie.getUserId());
                        break;
                    }
                }
                Intent intent = new Intent(getApplicationContext(), MoviePage.class);
                assert passingMovie != null;
                intent.putExtra(INTENT_KEY_MOVIEID, passingMovie.getId());
                startActivity(intent);
            }

        }, 0);
        recyclerView = findViewById(R.id.watchlist_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(watchlistAdapter);
    }

    public boolean checkMovieId(List<Movie> movies, Movie movie) {
        for (Movie m : movies) {
            if (m.getId().equals(movie.getId())) return true;
        }
        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return ((net != null) && net.isConnected());
    }
}