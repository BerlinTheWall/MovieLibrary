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
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.Favoritelist.EmptyFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.GetFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.InsertFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.EmptyWatchlistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.InsertWatchlistCommand;
import com.example.movielibrary.databinding.ActivityFavoriteListBinding;
import com.example.movielibrary.databinding.ActivityWatchListBinding;
import com.example.movielibrary.ui.adapter.MovieAdapter;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {
    private static final String INTENT_KEY_MOVIEID = "movieId";

    private ActivityFavoriteListBinding binding;
    private DataStore dataStore;
    private MovieAdapter favlistAdapter;
    private RecyclerView recyclerView;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);
        setTitle("Favorite list");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityFavoriteListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataStore = DataStore.getInstance();
        context = FavoriteListActivity.this;
        loadFavlist();
        search();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdapter(dataStore.getFavoriteLists());
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
                for (Movie movie : dataStore.getFavoriteLists()) {
                    if (movie.getName().toLowerCase().contains(newText) ||
                            movie.getDescription().toLowerCase().contains(newText)) {
                        searchedMovies.add(movie);
                    }
                }
                if (searchedMovies.size() == 0 && newText.equals("")) {
                    loadAdapter(dataStore.getFavoriteLists());
                } else {
                    loadAdapter(searchedMovies);

                }
                return false;
            }
        });
        return false;
    }

    private void loadFavlist() {
        dataStore.getFavoriteLists().clear();
        if (!isNetworkConnected()) {
            GetFavoritelistCommand getFavoritelistCommand = new GetFavoritelistCommand(FavoriteListActivity.this);
            DBAsyncTask<List<FavoriteList>> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    for (FavoriteList favoriteList : resultDb.getResult()) {
                        for (Movie m : dataStore.getMovies()) {
                            if (m.getId().equals(favoriteList.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                dataStore.getFavoriteLists().add(m);
                            }
                            loadAdapter(dataStore.getFavoriteLists());
                        }
                    }
                } else {
                    Toast.makeText(FavoriteListActivity.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask2.execute(getFavoritelistCommand);


        } else {
            NetworkHelper networkHelper = NetworkHelper.getInstance(this);
            networkHelper.getFavoritelist(dataStore.getUser().getId(), dataStore.getUser().getSessionToken(), new ResultListener<FavoriteList>() {
                @Override
                public void onResult(Result<FavoriteList> result) {
                    Error error = (result != null) ? result.getError() : null;
                    List<FavoriteList> resultStd = (result != null) ? result.getItems() : null;

                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.get_favlist_error);
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    EmptyFavoritelistCommand emptyFavoritelistCommand = new EmptyFavoritelistCommand(FavoriteListActivity.this);
                    DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {

                        } else {
                            Toast.makeText(FavoriteListActivity.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask2.execute(emptyFavoritelistCommand);

                    for (FavoriteList favoriteList : resultStd) {
                        InsertFavoritelistCommand insertFavoritelistCommand = new InsertFavoritelistCommand(FavoriteListActivity.this, favoriteList);
                        DBAsyncTask<FavoriteList> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                for (Movie m : dataStore.getMovies()) {
                                    for (FavoriteList f : resultStd) {
                                        if (m.getId().equals(f.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                            dataStore.getFavoriteLists().add(m);

                                        }
                                    }
                                }

                                loadAdapter(dataStore.getFavoriteLists());
                            } else {
                                Toast.makeText(FavoriteListActivity.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(insertFavoritelistCommand);
                    }

                }
            });
        }
    }

    private void loadAdapter(ArrayList<Movie> movies) {
        favlistAdapter = new MovieAdapter(this, movies, new MovieAdapter.MovieAdapterCallback() {

            @Override
            public void onShowDetail(int position) {
                Movie passingMovie = null;
                for (Movie movie : dataStore.getFavoriteLists()) {
                    if (movie.getId().equals(dataStore.getFavoriteLists().get(position).getId())) {
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
        recyclerView = findViewById(R.id.favlist_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(favlistAdapter);
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