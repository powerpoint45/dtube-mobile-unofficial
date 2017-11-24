package com.powerpoint45.dtube;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * Created by michael on 18/11/17.
 */

public class LoginActivity2 extends AppCompatActivity {
    SteemitWebView steemitWebView;
    EditText userNameEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        userNameEditText = ((EditText)findViewById(R.id.username));
        passwordEditText = ((EditText)findViewById(R.id.password));
        steemitWebView = new SteemitWebView(this);

//        RhinoJS rhinoJS = new RhinoJS(this);
//        rhinoJS.loadRhino();

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
