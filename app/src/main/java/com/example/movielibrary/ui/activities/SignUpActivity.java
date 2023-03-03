package com.example.movielibrary.ui.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.movielibrary.R;
import com.example.movielibrary.data.model.User;
import com.example.movielibrary.databinding.ActivityMainBinding;
import com.example.movielibrary.databinding.ActivitySignUpBinding;
import com.example.movielibrary.utils.NetworkHelper;
import com.example.movielibrary.utils.Result;
import com.example.movielibrary.utils.ResultListener;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setTitle("Sign Up");
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        setListeners();
    }

    private void setListeners() {
        binding.signUpSignUpBtn.setOnClickListener(this::onSignUpClicked);
    }

    private void onSignUpClicked(View view) {
        if (!binding.signUpFirstNameEt.getText().toString().isEmpty() && !binding.signUpLastNameEt.getText().toString().isEmpty() && !binding.signUpUsernameEt.getText().toString().isEmpty() && !binding.signUpPasswordEt.getText().toString().isEmpty() && !binding.signUpConfPasswordEt.getText().toString().isEmpty()) {
                if (binding.signUpPasswordEt.getText().toString().equals(binding.signUpConfPasswordEt.getText().toString())) {
                    User user = new User(binding.signUpFirstNameEt.getText().toString(), binding.signUpLastNameEt.getText().toString(), binding.signUpUsernameEt.getText().toString(), binding.signUpPasswordEt.getText().toString());

                    NetworkHelper networkHelper = NetworkHelper.getInstance(this);
                    networkHelper.signupUser(user, new ResultListener<User>() {
                        @Override
                        public void onResult(Result<User> result) {
                            Error error = (result != null) ? result.getError() : null;
                            if ((result == null) || (error != null)) {
                                String errorMsg = (error != null) ? error.getMessage() : getString(R.string.signupFailed);
                                Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                return;
                            }

                            Toast.makeText(SignUpActivity.this, R.string.signUpSuccess, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                } else
                    Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.unmatchedPass),
                                    Toast.LENGTH_LONG)
                            .show();


        } else Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.emptyField),
                        Toast.LENGTH_LONG)
                .show();

    }
}