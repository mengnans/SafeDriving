package com.example.safedriving;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button gotoMainActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = (EditText) findViewById(R.id.usernameText);
        passwordEditText = (EditText) findViewById(R.id.passwordText);
        gotoMainActivityButton = (Button) findViewById(R.id.gotoMainActivityButton);

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

    public void onClickGotoMainActivity (View v) {
        Context context = LoginActivity.this;
        Class destinationActivity = SafeDrivingHomeActivity.class;
        Intent startSafeDrivingHomeActivityIntent = new Intent(context, destinationActivity);
        startActivity(startSafeDrivingHomeActivityIntent);
    }

}
