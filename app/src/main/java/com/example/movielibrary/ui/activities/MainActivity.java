package com.example.movielibrary.ui.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.movielibrary.R;

import com.example.movielibrary.data.async.DBAsyncTask;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.data.model.commands.User.DeleteUserCommand;
import com.example.movielibrary.data.model.commands.User.GetUsersCommand;
import com.example.movielibrary.data.model.commands.User.InsertUserCommand;
import com.example.movielibrary.databinding.ActivityMainBinding;
import com.example.movielibrary.utils.DataStore;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setListeners();
        dataStore = DataStore.getInstance();
        autoSignIn();
    }

    private void autoSignIn() {
        GetUsersCommand getUsersCommand = new GetUsersCommand(MainActivity.this);
        DBAsyncTask<List<User>> asyncTask = new DBAsyncTask<>(resultDb -> {
            if (resultDb.getError() == null && resultDb.getResult().size()!= 0) {
                    dataStore.setUser(resultDb.getResult().get(0));
                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                    startActivity(intent);
                    finish();
            }
        });
        asyncTask.execute(getUsersCommand);
    }

    private void setListeners() {
        binding.signInBtn.setOnClickListener(this::onLoginClicked);
        binding.toSignUpPageBtn.setOnClickListener(this::onSignUpClicked);
    }

    private void onSignUpClicked(View view) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private void onLoginClicked(View view) {

        String username = binding.usernameEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (!username.isEmpty() && !password.isEmpty()) {
            loginIfAccountExist(username, password);
        } else Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.emptyField),
                        Toast.LENGTH_LONG)
                .show();
    }

    private void loginIfAccountExist(String username, String password) {

        NetworkHelper networkHelper = NetworkHelper.getInstance(MainActivity.this);
        User user = new User(username, password);
        networkHelper.signInUser(user, new ResultListener<User>() {
            @Override
            public void onResult(Result<User> result) {
                Error error = (result != null) ? result.getError() : null;
                User signedInUser = (result != null) ? result.getItem() : null;
                if ((result == null) || (error != null) || (signedInUser == null)) {
                    String errorMsg = (error != null) ? error.getMessage() : getString(R.string.signInError);
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    return;
                }

                DeleteUserCommand deleteUserCommand = new DeleteUserCommand(MainActivity.this);
                DBAsyncTask<String> asyncTask2 = new DBAsyncTask<>(resultDb -> {
                    if (resultDb.getError() == null) {

                    } else {
                        Toast.makeText(MainActivity.this, R.string.insert_user_error, Toast.LENGTH_SHORT).show();
                    }
                });
                asyncTask2.execute(deleteUserCommand);


                InsertUserCommand insertUserCommand = new InsertUserCommand(MainActivity.this, signedInUser);
                DBAsyncTask<User> asyncTask = new DBAsyncTask<>(resultDb -> {
                    if (resultDb.getError() == null) {

                        binding.usernameEt.setText("");
                        binding.passwordEt.setText("");
                        Toast.makeText(MainActivity.this, R.string.signInSuccess, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.insert_user_error, Toast.LENGTH_SHORT).show();
                    }
                });
                asyncTask.execute(insertUserCommand);
            }
        });

    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

}