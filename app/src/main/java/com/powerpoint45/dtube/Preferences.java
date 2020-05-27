package com.powerpoint45.dtube;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import static android.content.Context.UI_MODE_SERVICE;

public class Preferences {
    public static boolean darkMode;
    public static boolean hasUpgrade;


    public static void loadPreferences(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (!prefs.contains("dark_mode"))
            prefs.edit().putBoolean("dark_mode",false).apply();
        else
            darkMode = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("dark_mode",false);
        hasUpgrade = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("upgraded",false);



        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(UI_MODE_SERVICE);
        boolean runningOnTV = uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION;

        PackageManager packageManager = c.getApplicationContext().getPackageManager();
        boolean supportsPIP = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            supportsPIP = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
        }

        if (!hasUpgrade && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !runningOnTV && supportsPIP) {
            //Check if app already has upgraded
            BillingClient billingClient = BillingClient.newBuilder(c).setListener(new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {

                }
            }).enablePendingPurchases().build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d("bill", "onBillingSetupFinished");

                        for (Purchase p : billingClient.queryPurchases("inapp").getPurchasesList()) {
                            Log.d("billp", "purchases: " + p.getPurchaseToken());
                            //already purchased
                            PreferenceManager.getDefaultSharedPreferences(c)
                                    .edit().putBoolean("upgraded", true).apply();
                            Preferences.hasUpgrade = true;
                        }
                        billingClient.endConnection();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.d("bill", "onBillingServiceDisconnected");
                }
            });
        }


    }

}
