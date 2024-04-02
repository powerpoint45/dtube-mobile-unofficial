package com.powerpoint45.dtube;

import android.Manifest;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
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
    String selectedAPI;
    int loginType;

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

        loginType = getIntent().getIntExtra("logintype",-1);

        ImageView logo = findViewById(R.id.login_network_logo);
        switch (loginType){
            case DtubeAPI.NET_SELECT_AVION:
                selectedAPI = DtubeAPI.PROVIDER_API_URL_AVALON;
                logo.setImageResource(R.drawable.logo_black);
                findViewById(R.id.upvote_project).setVisibility(View.GONE);
                findViewById(R.id.help_holder).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.follow_text)).setText(R.string.follow_us_avalon);
                break;

            case DtubeAPI.NET_SELECT_HIVE:
                selectedAPI = DtubeAPI.PROVIDER_API_URL_HIVE;
                logo.setImageResource(R.drawable.hive);
                findViewById(R.id.help_holder).setVisibility(View.GONE);
                break;

            case DtubeAPI.NET_SELECT_STEEM:
                selectedAPI = DtubeAPI.PROVIDER_API_URL_STEEM;
                logo.setImageResource(R.drawable.steemit);
                break;
        }


        steemitWebView = new SteemitWebView(this, selectedAPI);

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
            Preferences.selectedAPI = selectedAPI;
            DtubeAPI.saveUserCredentials(userNameEditText.getText().toString(),passwordEditText.getText().toString(), DtubeAPI.getNetworkNumber(selectedAPI), this);
            DtubeAPI.saveSelectedAPI(this, selectedAPI);
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

    public void createAccountButtonClicked(View v){
        Intent browserIntent = null;
        if (loginType == DtubeAPI.NET_SELECT_STEEM) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://signup.steemit.com/"));
        } else if (loginType == DtubeAPI.NET_SELECT_HIVE) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://signup.hive.io/"));
        } else if (loginType == DtubeAPI.NET_SELECT_AVION) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://signup.dtube.fso.ovh/"));
        }

        startActivity(browserIntent);
    }

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
        if (loginType == DtubeAPI.NET_SELECT_AVION){
            String url = "https://hive.blog/hive-196037/@immawake/how-to-create-a-dtube-account-and-use-gitcoin-web3-passport";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }else {
            Intent aboutIntent = new Intent(LoginActivity.this, HelpActivity.class);
            aboutIntent.putExtra("logintype", loginType);
            startActivity(aboutIntent);
        }
    }
}
