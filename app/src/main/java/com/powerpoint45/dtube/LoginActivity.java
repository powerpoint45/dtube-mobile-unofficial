package com.powerpoint45.dtube;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by michael on 18/11/17.
 */

public class LoginActivity extends AppCompatActivity {
    SteemitWebView steemitWebView;
    EditText userNameEditText;
    EditText passwordEditText;
    Switch upvoteSwitch;
    Switch followSwitch;

    final int RESULT_QR_CODE = 0;

    boolean runningOnTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        setContentView(R.layout.activity_login);
        userNameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        upvoteSwitch = findViewById(R.id.upvote_switch);
        followSwitch = findViewById(R.id.follow_switch);
        steemitWebView = new SteemitWebView(this);

        if (Preferences.darkMode){
            ((ImageView)findViewById(R.id.login_logo)).setImageResource(R.drawable.logo_white);
        }

        //Remove QR button if system has no camera
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            ((ViewGroup)findViewById(R.id.password_holder)).removeView(findViewById(R.id.qr_button));
        }

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;

        //Customize layout if in TV Mode
        if (uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION)
            runningOnTV = true;

        //enable link clicks if not on TV
        if (!runningOnTV) {
            ((TextView) findViewById(R.id.upvote_text)).setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView) findViewById(R.id.follow_text)).setMovementMethod(LinkMovementMethod.getInstance());
        }
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
                upvoteSwitch.setEnabled(true);
                followSwitch.setEnabled(true);
                Log.d("dtube4", sucess ? "logged in":"login failed");
                if (sucess){
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, R.string.login_failed,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    final int CAMERA_REQUEST_PERMISSION = 10;

    public void qrButtonClicked(View v){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_PERMISSION);
        }else {
            if (passwordEditText.isEnabled()) {

                Intent qrIntent = new Intent(LoginActivity.this, SimpleScannerActivity.class);
                startActivityForResult(qrIntent, RESULT_QR_CODE);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_QR_CODE){
            if (resultCode == RESULT_OK){
                if (data.getExtras()!=null)
                    passwordEditText.setText(data.getExtras().getString("password"));
                if (userNameEditText.getText().toString().length()>0)
                    loginButtonClicked(new View(this));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    qrButtonClicked(new View(this));
                }
            }
        }
    }

    public void loginButtonClicked(View v){
        String username = userNameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.startsWith("@"))
            username = username.substring(1);

        steemitWebView.login(username, password, upvoteSwitch.isChecked(), followSwitch.isChecked());
        userNameEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        upvoteSwitch.setEnabled(false);
        followSwitch.setEnabled(false);
    }

    public void helpButtonClicked(View v){
        Intent aboutIntent = new Intent(LoginActivity.this,HelpActivity.class);
        startActivity(aboutIntent);
    }
}
