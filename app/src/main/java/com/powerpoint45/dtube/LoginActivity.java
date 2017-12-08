package com.powerpoint45.dtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by michael on 18/11/17.
 */

public class LoginActivity extends AppCompatActivity {
    SteemitWebView steemitWebView;
    EditText userNameEditText;
    EditText passwordEditText;

    final int RESULT_QR_CODE = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userNameEditText = ((EditText)findViewById(R.id.username));
        passwordEditText = ((EditText)findViewById(R.id.password));
        steemitWebView = new SteemitWebView(this);
    }

    public void gotLoginResult(final boolean sucess){
        if (sucess){
            DtubeAPI.saveUserCredentials(userNameEditText.getText().toString(),passwordEditText.getText().toString(), this);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userNameEditText.setEnabled(true);
                passwordEditText.setEnabled(true);
                Log.d("dtube4", sucess ? "logged in":"login failed");
                if (sucess){
                    finish();
                }
            }
        });

    }

    public void qrButtonClicked(View v){
        if (passwordEditText.isEnabled()){

            Intent qrIntent = new Intent(LoginActivity.this, SimpleScannerActivity.class);
            startActivityForResult(qrIntent, RESULT_QR_CODE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_QR_CODE){
            if (resultCode == RESULT_OK){
                passwordEditText.setText(data.getExtras().getString("password"));
                if (userNameEditText.getText().toString().length()>0)
                    loginButtonClicked(new View(this));
            }
        }
    }

    public void loginButtonClicked(View v){
        String username = userNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        Log.d("dtube4","U:"+username);
        Log.d("dtube4","P:"+password);
        steemitWebView.login(username,password);
        userNameEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
    }
}
