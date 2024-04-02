package com.powerpoint45.dtube;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.List;
import java.util.Objects;

import static android.content.Context.UI_MODE_SERVICE;

public class Preferences {
    public static boolean darkMode;
    public static boolean hasUpgrade;
    static String selectedAPI = DtubeAPI.PROVIDER_API_URL_AVALON;



    public static void loadPreferences(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (!prefs.contains("dark_mode"))
            prefs.edit().putBoolean("dark_mode",false).apply();
        else
            darkMode = prefs.getBoolean("dark_mode",false);
        hasUpgrade = prefs.getBoolean("upgraded",false);
        selectedAPI = DtubeAPI.getSelectedAPI(c);


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



                        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
                            @Override
                            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                                for (Purchase p : list) {
                                    Log.d("billp", "purchases: " + p.toString());
                                    //already purchased
                                    PreferenceManager.getDefaultSharedPreferences(c)
                                            .edit().putBoolean("upgraded", true).apply();
                                    Preferences.hasUpgrade = true;
                                    billingClient.endConnection();
                                }
                            }
                        });


//                        for (Purchase p : billingClient.queryPurchases("inapp").getPurchasesList()) {
//                            Log.d("billp", "purchases: " + p.getPurchaseToken());
//                            //already purchased
//                            PreferenceManager.getDefaultSharedPreferences(c)
//                                    .edit().putBoolean("upgraded", true).apply();
//                            Preferences.hasUpgrade = true;
//                        }

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
