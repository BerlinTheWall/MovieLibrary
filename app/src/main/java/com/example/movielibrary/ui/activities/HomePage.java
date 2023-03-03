package com.example.movielibrary.ui.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.movielibrary.R;
import com.example.movielibrary.data.async.DBAsyncTask;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.Comment.EmptyCommentCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.EmptyFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.GetFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.InsertFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Movie.EmptyMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.GetMoviesCommand;
import com.example.movielibrary.data.model.commands.User.DeleteUserCommand;
import com.example.movielibrary.data.model.commands.User.GetUsersCommand;
import com.example.movielibrary.data.model.commands.Movie.InsertMovieCommand;
import com.example.movielibrary.databinding.ActivityHomePageBinding;
import com.example.movielibrary.ui.adapter.MovieAdapter;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomePage extends AppCompatActivity {
    private ActivityHomePageBinding binding;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private DataStore dataStore;
    private Context context;

    private static final String INTENT_KEY_MOVIEID = "movieId";
    private static final String INTENT_ADD_MOVIE = "addEditMovie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.context = HomePage.this;
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataStore = DataStore.getInstance();

        setListeners();
        loadMovies();
        loadUsers();
        search();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListeners();
        loadUsers();
        loadMovies();
    }

    private void setListeners() {
        binding.addMovieBtn.setOnClickListener(this::onAddMovieClicked);
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
                for (Movie movie : dataStore.getMovies()) {
                    if (movie.getName().toLowerCase().contains(newText.toLowerCase(Locale.ROOT)) ||
                            movie.getDescription().toLowerCase().contains(newText.toLowerCase(Locale.ROOT))) {
                        searchedMovies.add(movie);
                    }
                }
                if (searchedMovies.size() == 0 && newText.equals("")) {
                    loadAdapter(dataStore.getMovies());
                } else {
                    loadAdapter(searchedMovies);

                }
                return false;
            }
        });
        return false;
    }


    private void onAddMovieClicked(View view) {
        Intent intent = new Intent(this, AddEditMovie.class);
        intent.putExtra(INTENT_ADD_MOVIE, "Add Movie");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.favList:
                intent = new Intent(this, FavoriteListActivity.class);
                startActivity(intent);
                return true;
            case R.id.watchList:
                intent = new Intent(this, WatchListActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOut:
                logout();
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadUsers() {
        GetUsersCommand getUsersCommand = new GetUsersCommand(HomePage.this);
        DBAsyncTask<List<User>> asyncTask = new DBAsyncTask<>(resultDb -> {
            if (resultDb.getError() == null) {
                dataStore.setUser(resultDb.getResult().get(0));
                loadMovies();
            } else {
                Toast.makeText(HomePage.this, R.string.insert_user_error, Toast.LENGTH_SHORT).show();
            }
        });
        asyncTask.execute(getUsersCommand);
    }

    private void loadMovies() {
        dataStore.getMovies().clear();
        dataStore.getFavoriteLists().clear();
        if (!isNetworkConnected()) {
        GetMoviesCommand getMoviesCommand = new GetMoviesCommand(HomePage.this);
        DBAsyncTask<List<Movie>> asyncTask = new DBAsyncTask<>(resultDb -> {
            if (resultDb.getResult().size() <= 0) {
                loadMoviesServer();
            } else {
                if (resultDb.getError() == null) {
                    for (Movie movie : resultDb.getResult()) {
                        if (!checkMovieId(dataStore.getMovies(), movie)) {
                            dataStore.getMovies().add(movie);
                        }
                        loadAdapter(dataStore.getMovies());
                    }
                } else {
                    Toast.makeText(HomePage.this, R.string.get_movie_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        asyncTask.execute(getMoviesCommand);

        GetFavoritelistCommand getFavoritelistCommand = new GetFavoritelistCommand(HomePage.this);
        DBAsyncTask<List<FavoriteList>> asyncTask2 = new DBAsyncTask<>(resultDb -> {
            if (resultDb.getError() == null) {
                for (FavoriteList favoriteList : resultDb.getResult()) {
                    for (Movie m : dataStore.getMovies()) {
                        if (m.getId().equals(favoriteList.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                            dataStore.getFavoriteLists().add(m);
                        }
                        loadAdapter(dataStore.getMovies());
                    }
                }
            } else {
                Toast.makeText(HomePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
            }
        });
        asyncTask2.execute(getFavoritelistCommand);

        } else {
            loadMoviesServer();
        }
    }

    private void loadMoviesServer() {
        dataStore.getMovies().clear();
        dataStore.getFavoriteLists().clear();
        NetworkHelper networkHelper = NetworkHelper.getInstance(this);
        networkHelper.getMovie(dataStore.getUser().getSessionToken(), new ResultListener<Movie>() {
            @Override
            public void onResult(Result<Movie> result) {
                Error error = (result != null) ? result.getError() : null;
                List<Movie> resultStd = (result != null) ? result.getItems() : null;

                if ((result == null) || (resultStd == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.get_movie_error);
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                EmptyMovieCommand emptyMovieCommand = new EmptyMovieCommand(HomePage.this);
                DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                    if (resultDb.getError() == null) {

                    } else {
                        Toast.makeText(HomePage.this, R.string.get_movie_error, Toast.LENGTH_SHORT).show();
                    }
                });
                asyncTask2.execute(emptyMovieCommand);

                for (Movie movie : resultStd) {
                    InsertMovieCommand insertMovieCommand = new InsertMovieCommand(HomePage.this, movie);
                    DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            if (!checkMovieId(dataStore.getMovies(), movie)) {
                                dataStore.getMovies().add(resultDb.getResult());
                            }
                            loadAdapter(dataStore.getMovies());
                        } else {
                            Toast.makeText(HomePage.this, R.string.get_movie_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask.execute(insertMovieCommand);
                }

            }

        });

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

                EmptyFavoritelistCommand emptyFavoritelistCommand = new EmptyFavoritelistCommand(HomePage.this);
                DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                    if (resultDb.getError() == null) {

                    } else {
                        Toast.makeText(HomePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                    }
                });
                asyncTask2.execute(emptyFavoritelistCommand);

                for (FavoriteList favoriteList : resultStd) {
                    InsertFavoritelistCommand insertFavoritelistCommand = new InsertFavoritelistCommand(HomePage.this, favoriteList);
                    DBAsyncTask<FavoriteList> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            for (Movie m : dataStore.getMovies()) {
                                for (FavoriteList f : resultStd) {
                                    if (m.getId().equals(f.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                        dataStore.getFavoriteLists().add(m);

                                    }
                                }
                            }

                            loadAdapter(dataStore.getMovies());
                        } else {
                            Toast.makeText(HomePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask.execute(insertFavoritelistCommand);
                }

            }
        });
    }

    private void loadAdapter(ArrayList<Movie> movies) {
        movieAdapter = new MovieAdapter(this, movies, new MovieAdapter.MovieAdapterCallback() {

            @Override
            public void onShowDetail(int position) {
                Movie passingMovie = null;
                for (Movie movie : dataStore.getMovies()) {
                    if (movie.getId().equals(dataStore.getMovies().get(position).getId())) {
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
        recyclerView = findViewById(R.id.allMovies_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(movieAdapter);
    }

    private void logout() {
        dataStore.getMovies().clear();
        dataStore.getWatchLists().clear();
        dataStore.getFavoriteLists().clear();
        dataStore.getComments().clear();

        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(HomePage.this);
        DBAsyncTask<String> asyncTask = new DBAsyncTask<>(resultDb -> {
            if (resultDb.getError() == null) {
                dataStore.setUser(new User());
            }
        });
        asyncTask.execute(deleteUserCommand);
        Toast.makeText(HomePage.this, R.string.logout_str, Toast.LENGTH_SHORT).show();
        finish();
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