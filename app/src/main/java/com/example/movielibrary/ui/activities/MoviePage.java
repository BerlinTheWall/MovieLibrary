package com.example.movielibrary.ui.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.BoringLayout;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.movielibrary.R;
import com.example.movielibrary.data.async.DBAsyncTask;
import com.example.movielibrary.data.model.Comment;
import com.example.movielibrary.data.model.FavoriteList;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.WatchList;
import com.example.movielibrary.data.model.commands.Comment.EmptyCommentCommand;
import com.example.movielibrary.data.model.commands.Comment.GetCommentCommand;
import com.example.movielibrary.data.model.commands.Comment.InsertCommentCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.EmptyFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.GetFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Favoritelist.InsertFavoritelistCommand;
import com.example.movielibrary.data.model.commands.Movie.DeleteMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.EmptyMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.GetMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.InsertMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.UpdateMovieCommand;
import com.example.movielibrary.data.model.commands.User.GetUsersCommand;
import com.example.movielibrary.data.model.commands.Watchlist.EmptyWatchlistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.GetWatchlistCommand;
import com.example.movielibrary.data.model.commands.Watchlist.InsertWatchlistCommand;
import com.example.movielibrary.databinding.ActivityAddEditMovieBinding;
import com.example.movielibrary.databinding.ActivityMoviePageBinding;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.util.ArrayList;
import java.util.List;

public class MoviePage extends AppCompatActivity {
    private ActivityMoviePageBinding binding;
    private DataStore dataStore;
    private String comment_txt = "";
    private Context context;

    private static final String INTENT_KEY_MOVIEID = "movieId";

    private static final String INTENT_ADD_MOVIE = "addEditMovie";

    private String movieId;
    private String title;
    private String desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Movie Detail");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityMoviePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataStore = DataStore.getInstance();
        context = MoviePage.this;
        loadDetails();
        loadComments();
        setListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetails();
    }

    private void setListeners() {
        binding.movieDetailCommentBtn.setOnClickListener(this::onCommentClick);
        binding.movieDetailLikeBtn.setOnClickListener(this::onLikeClick);
        binding.movieDetailShareBtn.setOnClickListener(this::onShareClick);
        binding.movieDetailWatchlistBtn.setOnClickListener(this::onWatchListClick);
        binding.movieDetailFavoriteBtn.setOnClickListener(this::onFavoriteListClick);
        binding.editBtn.setOnClickListener(this::onEditClick);
        binding.deleteBtn.setOnClickListener(this::onDeleteClick);
    }

    private void onDeleteClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure to delete this movie?");

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NetworkHelper networkHelper = NetworkHelper.getInstance(MoviePage.this);
                networkHelper.deleteMovie(movieId, dataStore.getUser().getSessionToken(), new ResultListener<Movie>() {
                    @Override
                    public void onResult(Result<Movie> result) {
                        Error error = (result != null) ? result.getError() : null;
                        Movie resultStd = (result != null) ? result.getItem() : null;
                        if ((result == null) || (resultStd == null) || (error != null)) {
                            String errorMsg = (error != null) ? error.getMessage() : getString(R.string.delete_movie_error);
                            Toast.makeText(MoviePage.this, errorMsg, Toast.LENGTH_LONG).show();
                            return;
                        }
                        DeleteMovieCommand deleteMovieCommand = new DeleteMovieCommand(MoviePage.this, movieId);
                        DBAsyncTask<String> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                dataStore.getMovies().remove(resultDb.getResult());
                                Toast.makeText(MoviePage.this, R.string.delete_movie, Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(MoviePage.this, R.string.delete_movie_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(deleteMovieCommand);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void onEditClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AddEditMovie.class);
        intent.putExtra(INTENT_KEY_MOVIEID, movieId);
        intent.putExtra(INTENT_ADD_MOVIE, "Edit Movie");
        startActivity(intent);
    }

    private void onShareClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Movie name: " + title + "\n" + "Description: " + desc);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void onWatchListClick(View view) {
        boolean alreadyExist = false;
        for (Movie w : dataStore.getWatchLists()) {
            if (w.getId().equals(movieId)) {
                alreadyExist = true;
            }
        }
        if (!alreadyExist) {
            NetworkHelper networkHelper = NetworkHelper.getInstance(MoviePage.this);
            WatchList watchList = new WatchList(movieId, dataStore.getUser().getId());
            networkHelper.addWatchlist(watchList, dataStore.getUser().getSessionToken(), new ResultListener<WatchList>() {
                @Override
                public void onResult(Result<WatchList> result) {
                    Error error = (result != null) ? result.getError() : null;
                    WatchList resultStd = (result != null) ? result.getItem() : null;
                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.insert_watchlist_error);
                        Toast.makeText(MoviePage.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    InsertWatchlistCommand insertWatchlistCommand = new InsertWatchlistCommand(MoviePage.this, resultStd);
                    DBAsyncTask<WatchList> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            for (Movie m : dataStore.getMovies()) {
                                if (m.getId().equals(movieId) && !checkMovieId(dataStore.getWatchLists(), m)) {
                                    dataStore.getWatchLists().add(m);
                                    Toast.makeText(MoviePage.this, R.string.insert_watchlist, Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(MoviePage.this, R.string.insert_watchlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask.execute(insertWatchlistCommand);
                }
            });
        } else
            Toast.makeText(MoviePage.this, R.string.insert_watchlist_exist, Toast.LENGTH_SHORT).show();
    }

    private void onFavoriteListClick(View view) {
        boolean alreadyExist = false;
        for (Movie w : dataStore.getFavoriteLists()) {
            if (w.getId().equals(movieId)) {
                alreadyExist = true;
            }
        }
        if (!alreadyExist) {
            NetworkHelper networkHelper = NetworkHelper.getInstance(MoviePage.this);
            FavoriteList favoriteList = new FavoriteList(movieId, dataStore.getUser().getId());
            networkHelper.addFavoritelist(favoriteList, dataStore.getUser().getSessionToken(), new ResultListener<FavoriteList>() {
                @Override
                public void onResult(Result<FavoriteList> result) {
                    Error error = (result != null) ? result.getError() : null;
                    FavoriteList resultStd = (result != null) ? result.getItem() : null;
                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.insert_favoritelist_error);
                        Toast.makeText(MoviePage.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    InsertFavoritelistCommand insertFavoritelistCommand = new InsertFavoritelistCommand(MoviePage.this, resultStd);
                    DBAsyncTask<FavoriteList> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            for (Movie m : dataStore.getMovies()) {
                                if (m.getId().equals(movieId) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                    dataStore.getFavoriteLists().add(m);
                                    Toast.makeText(MoviePage.this, R.string.insert_favoritelist, Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Toast.makeText(MoviePage.this, R.string.insert_favoritelist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask.execute(insertFavoritelistCommand);
                }
            });
        } else
            Toast.makeText(MoviePage.this, R.string.insert_favoritelist_exist, Toast.LENGTH_SHORT).show();
    }

    private void onLikeClick(View view) {
        Movie tempMovie = null;
        boolean hasLiked = false;
        for (Movie movie : dataStore.getMovies()) {
            if (movie.getId().equals(movieId)) {
                tempMovie = movie;
                break;
            }
        }
        assert tempMovie != null;
        for (String likeId : tempMovie.getLikeNum()) {
            if (likeId.equals(dataStore.getUser().getId())) {
                hasLiked = true;
            }
        }
        if (!hasLiked) {
            tempMovie.getLikeNum().add(dataStore.getUser().getId());

            NetworkHelper networkHelper = NetworkHelper.getInstance(MoviePage.this);
            Movie updatedMovie = tempMovie;
            networkHelper.updateMovie(updatedMovie, dataStore.getUser().getSessionToken(), new ResultListener<Movie>() {
                @Override
                public void onResult(Result<Movie> result) {
                    Error error = (result != null) ? result.getError() : null;
                    Movie resultStd = (result != null) ? result.getItem() : null;
                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.insert_like_movie_error);
                        Toast.makeText(MoviePage.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    UpdateMovieCommand updateMovieCommand = new UpdateMovieCommand(MoviePage.this, updatedMovie);
                    DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            Toast.makeText(MoviePage.this, R.string.insert_movie_like, Toast.LENGTH_LONG).show();
                            loadDetails();
                        }
                    });
                    asyncTask.execute(updateMovieCommand);
                }
            });
        } else
            Toast.makeText(MoviePage.this, R.string.insert_movie_like_exist, Toast.LENGTH_LONG).show();


    }

    private void loadDetails() {
        loadLists();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movieId = extras.getString(INTENT_KEY_MOVIEID);

            GetMovieCommand getMovieCommand = new GetMovieCommand(MoviePage.this, movieId);
            DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    binding.movieDetailTitle.setText(resultDb.getResult().getName());
                    binding.movieDetailDescTv.setText(resultDb.getResult().getDescription());
                    binding.movieDetailPublishedDate.setText("Year: " + resultDb.getResult().getYear());
                    binding.movieDetailStatus.setText("Status: " + resultDb.getResult().getStatus());
                    binding.movieDetailRate.setText("Rate: " + resultDb.getResult().getRate());
                    binding.movieDetailGenre.setText("Genre: " + resultDb.getResult().getGenre());
                    binding.movieDetailCast.setText(resultDb.getResult().getCast());
                    title = resultDb.getResult().getName();
                    desc = resultDb.getResult().getDescription();
                    if (!dataStore.getUser().getId().equals(resultDb.getResult().getUserId())) {
                        binding.editBtn.setVisibility(View.INVISIBLE);
                        binding.deleteBtn.setVisibility(View.INVISIBLE);
                    }

                    if (resultDb.getResult().getLikeNum() != null) {
                        for (String like : resultDb.getResult().getLikeNum()) {
                            if (dataStore.getUser().getId().equals(like)) {
                                binding.movieDetailLikeBtn.setImageResource(R.drawable.like_heart);
                                break;
                            }
                            binding.movieDetailLikeBtn.setImageResource(R.drawable.dislike_heart);
                        }
                    }
                } else {
                    Toast.makeText(MoviePage.this, R.string.get_movie_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask.execute(getMovieCommand);

        }
    }

    private void loadLists() {
        dataStore.getWatchLists().clear();
        dataStore.getFavoriteLists().clear();
        if (!isNetworkConnected()) {
            GetWatchlistCommand getWatchlistCommand = new GetWatchlistCommand(MoviePage.this);
            DBAsyncTask<List<WatchList>> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    for (WatchList watchList : resultDb.getResult()) {
                        for (Movie m : dataStore.getMovies()) {
                            if (m.getId().equals(watchList.getMovieId()) && !checkMovieId(dataStore.getWatchLists(), m)) {
                                dataStore.getWatchLists().add(m);
                            }
                        }
                    }
                } else {
                    Toast.makeText(MoviePage.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask2.execute(getWatchlistCommand);

            GetFavoritelistCommand favoritelistCommand = new GetFavoritelistCommand(MoviePage.this);
            DBAsyncTask<List<FavoriteList>> asyncTask = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    for (FavoriteList favoriteList : resultDb.getResult()) {
                        for (Movie m : dataStore.getMovies()) {
                            if (m.getId().equals(favoriteList.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                dataStore.getFavoriteLists().add(m);
                            }
                        }
                    }
                } else {
                    Toast.makeText(MoviePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask.execute(favoritelistCommand);


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

                    EmptyWatchlistCommand emptyWatchlistCommand = new EmptyWatchlistCommand(MoviePage.this);
                    DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {

                        } else {
                            Toast.makeText(MoviePage.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask2.execute(emptyWatchlistCommand);

                    for (WatchList watchList : resultStd) {
                        InsertWatchlistCommand insertWatchlistCommand = new InsertWatchlistCommand(MoviePage.this, watchList);
                        DBAsyncTask<WatchList> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                for (Movie m : dataStore.getMovies()) {
                                    for (WatchList w : resultStd) {
                                        if (m.getId().equals(w.getMovieId()) && !checkMovieId(dataStore.getWatchLists(), m)) {
                                            dataStore.getWatchLists().add(m);
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(MoviePage.this, R.string.get_watchlist_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(insertWatchlistCommand);
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

                    EmptyFavoritelistCommand emptyFavoritelistCommand = new EmptyFavoritelistCommand(MoviePage.this);
                    DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {

                        } else {
                            Toast.makeText(MoviePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask2.execute(emptyFavoritelistCommand);

                    for (FavoriteList favoriteList : resultStd) {
                        InsertFavoritelistCommand insertFavoritelistCommand = new InsertFavoritelistCommand(MoviePage.this, favoriteList);
                        DBAsyncTask<FavoriteList> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                for (Movie m : dataStore.getMovies()) {
                                    for (FavoriteList f : resultStd) {
                                        if (m.getId().equals(f.getMovieId()) && !checkMovieId(dataStore.getFavoriteLists(), m)) {
                                            dataStore.getFavoriteLists().add(m);

                                        }
                                    }
                                }

                            } else {
                                Toast.makeText(MoviePage.this, R.string.get_favlist_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(insertFavoritelistCommand);
                    }

                }
            });
        }
    }

    private void setComments() {
        String cmt = "";
        for (Comment comment : dataStore.getComments()) {
            cmt = cmt + "- " + comment.getComment() + "\n";
        }
        binding.movieDetailComment.setText(cmt);
    }

    private void loadComments() {
        dataStore.getComments().clear();
        if (!isNetworkConnected()) {
            GetCommentCommand getCommentCommand = new GetCommentCommand(MoviePage.this);
            DBAsyncTask<List<Comment>> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    for (Comment comment : resultDb.getResult()) {
                        if (!checkCommentId(dataStore.getComments(), comment)  && comment.getMovieId().equals(movieId)) {
                            dataStore.getComments().add(comment);
                            setComments();
                        }
                    }
                } else {
                    Toast.makeText(MoviePage.this, R.string.get_comment_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask2.execute(getCommentCommand);
        } else {
            NetworkHelper networkHelper = NetworkHelper.getInstance(this);
            networkHelper.getComment(movieId, dataStore.getUser().getSessionToken(), new ResultListener<Comment>() {
                @Override
                public void onResult(Result<Comment> result) {
                    Error error = (result != null) ? result.getError() : null;
                    List<Comment> resultStd = (result != null) ? result.getItems() : null;

                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.get_comment_error);
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    EmptyCommentCommand emptyCommentCommand = new EmptyCommentCommand(MoviePage.this);
                    DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            dataStore.getComments().clear();
                        } else {
                            Toast.makeText(MoviePage.this, R.string.get_comment_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask2.execute(emptyCommentCommand);

                    for (Comment comment : resultStd) {
                        InsertCommentCommand insertCommentCommand = new InsertCommentCommand(MoviePage.this, comment);
                        DBAsyncTask<Comment> asyncTask = new DBAsyncTask<>(resultDb -> {
                            if (resultDb.getError() == null) {
                                if (!checkCommentId(dataStore.getComments(), comment)) {
                                    dataStore.getComments().add(resultDb.getResult());
                                    setComments();
                                }
                            } else {
                                Toast.makeText(MoviePage.this, R.string.get_comment_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                        asyncTask.execute(insertCommentCommand);
                    }

                    for (Comment comment : resultStd) {
                        dataStore.getComments().add(comment);
                    }
                }
            });
        }

    }

    private void onCommentClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Comment");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                comment_txt = input.getText().toString();
                saveComment(comment_txt);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveComment(String comment_txt) {
        NetworkHelper networkHelper = NetworkHelper.getInstance(MoviePage.this);
        Comment comment = new Comment(movieId, dataStore.getUser().getId(), comment_txt);
        networkHelper.addComment(comment, dataStore.getUser().getSessionToken(), new ResultListener<Comment>() {
            @Override
            public void onResult(Result<Comment> result) {
                Error error = (result != null) ? result.getError() : null;
                Comment resultStd = (result != null) ? result.getItem() : null;
                if ((result == null) || (resultStd == null) || (error != null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.insert_comment_error);
                    Toast.makeText(MoviePage.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                InsertCommentCommand insertCommentCommand = new InsertCommentCommand(MoviePage.this, resultStd);
                DBAsyncTask<Comment> asyncTask = new DBAsyncTask<>(resultDb -> {
                    if (resultDb.getError() == null) {
                        if (!checkCommentId(dataStore.getComments(), resultStd)) {
                            dataStore.getComments().add(comment);
                            Toast.makeText(MoviePage.this, R.string.insert_comment, Toast.LENGTH_LONG).show();
                            setComments();
                        }

                    } else {
                        Toast.makeText(MoviePage.this, R.string.insert_comment_error, Toast.LENGTH_SHORT).show();
                    }
                });
                asyncTask.execute(insertCommentCommand);
            }
        });

    }

    public boolean checkCommentId(List<Comment> comments, Comment comment) {
        for (Comment c : comments) {
            if (c.getId().equals(comment.getId())) return true;
        }
        return false;
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

