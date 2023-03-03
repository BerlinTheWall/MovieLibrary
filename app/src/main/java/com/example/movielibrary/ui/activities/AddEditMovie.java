package com.example.movielibrary.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movielibrary.R;
import com.example.movielibrary.data.async.DBAsyncTask;
import com.example.movielibrary.data.model.Movie;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.Movie.EmptyMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.GetMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.GetMoviesCommand;
import com.example.movielibrary.data.model.commands.Movie.InsertMovieCommand;
import com.example.movielibrary.data.model.commands.Movie.UpdateMovieCommand;
import com.example.movielibrary.data.model.commands.User.GetUsersCommand;
import com.example.movielibrary.databinding.ActivityAddEditMovieBinding;
import com.example.movielibrary.databinding.ActivityMainBinding;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.InputFilterMinMax;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.NotificationHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AddEditMovie extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String INTENT_KEY_MOVIEID = "movieId";

    private NotificationHelper notificationHelper;
    private String movieId;


    private ImageView datePickerButton;
    private EditText datePickerEt;
    private Spinner genreSpinner;
    private Spinner statusSpinner;
    public int year = 2023;
    public int month = 0;
    public int day = 1;
    private ActivityAddEditMovieBinding binding;
    private static final String INTENT_ADD_MOVIE = "addEditMovie";
    private String pageStatus;
    private DataStore dataStore;
    private String prevStatus;
    private String newStatus;


    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_movie);
        setTitle("Add/Edit Movie");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityAddEditMovieBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        dataStore = DataStore.getInstance();
        setListeners();
        notificationHelper = NotificationHelper.getInstance(AddEditMovie.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pageStatus = extras.getString(INTENT_ADD_MOVIE);
            setTitle(pageStatus);
            if (pageStatus.equals("Edit Movie")) {
                loadDetails();
            }
        }

        @SuppressLint("CutPasteId") Spinner spinnerLanguages = findViewById(R.id.genre_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genres, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLanguages.setAdapter(adapter);
        genreSpinner = findViewById(R.id.genre_spinner);

        @SuppressLint("CutPasteId") Spinner spinnerLanguages2 = findViewById(R.id.status_spinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.status, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLanguages2.setAdapter(adapter2);
        statusSpinner = findViewById(R.id.status_spinner);

        datePickerButton = binding.movieDateBtn;
        datePickerEt = binding.movieYearEt;
        binding.rateEt.setFilters(new InputFilter[]{new InputFilterMinMax("1", "10")});

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
        binding.addSaveMovieBtn.setOnClickListener(this::onSaveClicked);
    }

    private void onSaveClicked(View view) {
        if (pageStatus.equals("Add Movie")) {
            addNewMove();
        } else {
            editMovie();
        }
    }

    private void addNewMove() {
        String title = binding.addMovieName.getText().toString();
        String desc = binding.addMovieDesc.getText().toString();
        String status = binding.statusSpinner.getSelectedItem().toString();
        String genre = binding.genreSpinner.getSelectedItem().toString();
        String cast = binding.castTv.getText().toString();
        if (!title.isEmpty() && !desc.isEmpty() && !binding.movieYearEt.getText().toString().equals("") && !binding.rateEt.getText().toString().equals("") && !cast.isEmpty()) {
            int year = Integer.parseInt(binding.movieYearEt.getText().toString());
            int rate = Integer.parseInt(binding.rateEt.getText().toString());
            NetworkHelper networkHelper = NetworkHelper.getInstance(AddEditMovie.this);
            Movie movie = new Movie(title, desc, genre, cast, rate, year, status, dataStore.getUser().getId());
            networkHelper.addMovie(movie, dataStore.getUser().getSessionToken(), new ResultListener<Movie>() {
                @Override
                public void onResult(Result<Movie> result) {
                    Error error = (result != null) ? result.getError() : null;
                    Movie resultStd = (result != null) ? result.getItem() : null;
                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.insert_movie_error);
                        Toast.makeText(AddEditMovie.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    InsertMovieCommand insertMovieCommand = new InsertMovieCommand(AddEditMovie.this, resultStd);
                    DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            if (!checkMovieId(dataStore.getMovies(), resultStd)) {
                                dataStore.getMovies().add(resultDb.getResult());
                                Toast.makeText(AddEditMovie.this, R.string.insert_movie, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AddEditMovie.this, R.string.insert_movie_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    asyncTask.execute(insertMovieCommand);
                }
            });
        } else Toast.makeText(AddEditMovie.this, R.string.emptyField, Toast.LENGTH_SHORT).show();

    }

    private void editMovie() {
        String title = binding.addMovieName.getText().toString();
        String desc = binding.addMovieDesc.getText().toString();
        String status = binding.statusSpinner.getSelectedItem().toString();
        String genre = binding.genreSpinner.getSelectedItem().toString();
        String cast = binding.castTv.getText().toString();
        if (!title.isEmpty() && !desc.isEmpty() && !binding.movieYearEt.getText().toString().equals("") && !binding.rateEt.getText().toString().equals("") && !cast.isEmpty()) {
            int year = Integer.parseInt(binding.movieYearEt.getText().toString());
            int rate = Integer.parseInt(binding.rateEt.getText().toString());
            newStatus = binding.statusSpinner.getSelectedItem().toString();
            NetworkHelper networkHelper = NetworkHelper.getInstance(AddEditMovie.this);
            Movie movie = new Movie(movieId, title, desc, genre, cast, rate, year, status, dataStore.getUser().getId());
            networkHelper.updateMovie(movie, dataStore.getUser().getSessionToken(), new ResultListener<Movie>() {
                @Override
                public void onResult(Result<Movie> result) {
                    Error error = (result != null) ? result.getError() : null;
                    Movie resultStd = (result != null) ? result.getItem() : null;
                    if ((result == null) || (resultStd == null) || (error != null)) {
                        String errorMsg = (error != null) ? error.getMessage() : getString(R.string.update_movie_error);
                        Toast.makeText(AddEditMovie.this, errorMsg, Toast.LENGTH_LONG).show();
                        return;
                    }
                    UpdateMovieCommand updateMovieCommand = new UpdateMovieCommand(AddEditMovie.this, movie);
                    DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                        if (resultDb.getError() == null) {
                            Toast.makeText(AddEditMovie.this, R.string.update_movie, Toast.LENGTH_LONG).show();
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!prevStatus.equals(newStatus) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                sendNotification(movieId, title, prevStatus, newStatus);
                            }
//                            }
                            finish();
                        }
                    });
                    asyncTask.execute(updateMovieCommand);
                }

            });
        } else Toast.makeText(AddEditMovie.this, R.string.emptyField, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void sendNotification(String movieId, String title, String prevStatus, String newStatus) {
        Intent intent = new Intent(this, MoviePage.class);
        intent.putExtra(INTENT_KEY_MOVIEID, movieId);
        intent.setAction(movieId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = notificationHelper.createNotificationBuilder("Movie Library", title + " status has changed from " + prevStatus + " to " + newStatus, pendingIntent, this);
        notificationHelper.makeNotification(builder, NotificationHelper.NOTIFICATION_ID);
    }

    private void loadDetails() {
        List<String> statuses = Arrays.asList(getResources().getStringArray(R.array.status));
        List<String> genres = Arrays.asList(getResources().getStringArray(R.array.genres));
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movieId = extras.getString(INTENT_KEY_MOVIEID);

            GetMovieCommand getMovieCommand = new GetMovieCommand(AddEditMovie.this, movieId);
            DBAsyncTask<Movie> asyncTask = new DBAsyncTask<>(resultDb -> {
                if (resultDb.getError() == null) {
                    prevStatus = resultDb.getResult().getStatus();
                    binding.addMovieName.setText(resultDb.getResult().getName());
                    binding.addMovieDesc.append(resultDb.getResult().getDescription());
                    binding.movieYearEt.append(" " + resultDb.getResult().getYear());
                    int i = statuses.indexOf(resultDb.getResult().getStatus());
                    int j = genres.indexOf(resultDb.getResult().getGenre());
                    binding.statusSpinner.post(new Runnable() {
                        public void run() {
                            binding.statusSpinner.setSelection(i);
                        }
                    });
                    binding.genreSpinner.post(new Runnable() {
                        public void run() {
                            binding.genreSpinner.setSelection(j);
                        }
                    });
                    String rate = String.valueOf(resultDb.getResult().getRate());
                    binding.rateEt.append(rate);
                    binding.castTv.setText(resultDb.getResult().getCast());

                } else {
                    Toast.makeText(AddEditMovie.this, R.string.get_movie_error, Toast.LENGTH_SHORT).show();
                }
            });
            asyncTask.execute(getMovieCommand);
        }
    }

    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, datePickerListener, year, month, day);
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @SuppressLint("SetTextI18n")
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            day = selectedDay;
            month = selectedMonth;
            year = selectedYear;
            datePickerEt.setText("" + selectedYear);
        }
    };

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
    }

    public boolean checkMovieId(List<Movie> movies, Movie movie) {
        for (Movie m : movies) {
            if (m.getId().equals(movie.getId())) return true;
        }
        return false;
    }
}