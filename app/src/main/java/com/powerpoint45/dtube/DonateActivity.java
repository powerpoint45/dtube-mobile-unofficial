package com.powerpoint45.dtube;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by michael on 18/11/17.
 */

public class DonateActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donate);
    }

    public void copyAddress(View v){
        String address = null;
        switch (v.getId()){
            case R.id.copybtc:
                address = "148kJvCVMmHfEPKxerz6Y4jdc94765c5VK";
                break;
            case R.id.copyeth:
                address = "0x944e6bc2c86144B89EaF7612782a5F570F5967a2";
                break;
            case R.id.copyltc:
                address = "LPxnm3UQ7VpL9stRCgECZgxkXuvb2jw1ik";
                break;
        }


        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("address", address);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this,R.string.copied,Toast.LENGTH_LONG).show();
    }

    public void sendMoney(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/immawake"));
        startActivity(browserIntent);
    }
}
