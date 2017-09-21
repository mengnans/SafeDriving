package com.example.safedriving;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = (EditText) findViewById(R.id.usernameText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);

    }

    public void onClickLogin(View v) {
        if (usernameEditText.getText().toString().matches("") || passwordEditText.getText().toString().matches("")) {
            Log.d("","do nothing, chill");
        }
        else {
            Log.d("",usernameEditText.getText().toString());
            Log.d("",passwordEditText.getText().toString());
        }

    }

}
