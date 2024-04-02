package com.powerpoint45.dtube;

import static com.powerpoint45.dtube.DtubeAPI.NET_SELECT_AVION;
import static com.powerpoint45.dtube.DtubeAPI.NET_SELECT_HIVE;
import static com.powerpoint45.dtube.DtubeAPI.NET_SELECT_STEEM;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;

public class LoginSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_choose_login);
        refreshLogins();
    }

    View.OnClickListener logoutChipCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.account_chip_avalon:
                    DtubeAPI.logout(LoginSelectActivity.this, NET_SELECT_AVION);
                    break;

                case R.id.account_chip_hive:
                    DtubeAPI.logout(LoginSelectActivity.this, NET_SELECT_HIVE);
                    break;

                case R.id.account_chip_steemit:
                    DtubeAPI.logout(LoginSelectActivity.this, NET_SELECT_STEEM);
                    break;
            }
            v.setVisibility(View.GONE);
            refreshLogins();
        }
    };

    public void sendResult(int loginType){
        Intent data = new Intent();
        data.putExtra("logintype", loginType);
        setResult(RESULT_OK, data);
        finish();
    }


    public void refreshLogins(){
        if (DtubeAPI.getAccountName(this, NET_SELECT_AVION)!=null){
            ((Chip)findViewById(R.id.account_chip_avalon)).setVisibility(View.VISIBLE);
            ((Chip)findViewById(R.id.account_chip_avalon)).setText("@"+DtubeAPI.getAccountName(this, NET_SELECT_AVION));
            findViewById(R.id.avalon_body).setVisibility(View.GONE);
            ((Chip)findViewById(R.id.account_chip_avalon)).setOnCloseIconClickListener(logoutChipCloseClickListener);
        }else{
            findViewById(R.id.avalon_body).setVisibility(View.VISIBLE);
            ((Chip)findViewById(R.id.account_chip_avalon)).setVisibility(View.GONE);
        }

        if (DtubeAPI.getAccountName(this, NET_SELECT_HIVE)!=null){
            ((Chip)findViewById(R.id.account_chip_hive)).setVisibility(View.VISIBLE);
            ((Chip)findViewById(R.id.account_chip_hive)).setText("@"+DtubeAPI.getAccountName(this, NET_SELECT_HIVE));
            findViewById(R.id.hive_body).setVisibility(View.GONE);
            ((Chip)findViewById(R.id.account_chip_hive)).setOnCloseIconClickListener(logoutChipCloseClickListener);

        }else {
            ((Chip)findViewById(R.id.account_chip_hive)).setVisibility(View.GONE);
            findViewById(R.id.hive_body).setVisibility(View.VISIBLE);
        }

        if (DtubeAPI.getAccountName(this, NET_SELECT_STEEM)!=null){
            ((Chip)findViewById(R.id.account_chip_steemit)).setVisibility(View.VISIBLE);
            ((Chip)findViewById(R.id.account_chip_steemit)).setText("@"+DtubeAPI.getAccountName(this, NET_SELECT_STEEM));
            ((Chip)findViewById(R.id.account_chip_steemit)).setOnCloseIconClickListener(logoutChipCloseClickListener);

        }else {
            ((Chip)findViewById(R.id.account_chip_steemit)).setVisibility(View.GONE);
        }


        findViewById(R.id.login_select_avalon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(NET_SELECT_AVION);
            }
        });

        findViewById(R.id.login_select_steem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendResult(NET_SELECT_STEEM);

            }
        });
        findViewById(R.id.login_select_hive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(NET_SELECT_HIVE);

            }
        });
    }

}
