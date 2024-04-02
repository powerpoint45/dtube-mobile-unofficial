package com.powerpoint45.dtube;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class PictureInPictureUpgradeActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private BillingClient billingClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pipm_upgrade_activity);

        billingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    Log.d("bill", "onBillingSetupFinished");
                    loadSKUs();

                    billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                            for (Purchase p : list) {
                                Log.d("billp", "purchases: " + p.toString());
                                //already purchased
                                PreferenceManager.getDefaultSharedPreferences(PictureInPictureUpgradeActivity.this)
                                        .edit().putBoolean("upgraded",true).apply();
                                Preferences.hasUpgrade = true;
                            }
                        }
                    });

//                    for (Purchase p: billingClient.queryPurchases("inapp" ).getPurchasesList()) {
//                        Log.d("billp", "purchases: " + p.getPurchaseToken());
//                        //already purchased
//                        PreferenceManager.getDefaultSharedPreferences(PictureInPictureUpgradeActivity.this)
//                                .edit().putBoolean("upgraded",true).apply();
//                        Preferences.hasUpgrade = true;
//                    }
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

    public void loadSKUs(){
        List<String> skuList = new ArrayList<>();
        skuList.add("upgrade");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !skuDetailsList.isEmpty()) {
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals("upgrade")){
                                    findViewById(R.id.upgrade_button).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            BillingFlowParams billingFlowParams = BillingFlowParams
                                                    .newBuilder()
                                                    .setSkuDetails(skuDetails)
                                                    .build();
                                            billingClient.launchBillingFlow(PictureInPictureUpgradeActivity.this, billingFlowParams);
                                        }
                                    });
                                }

                            }
                        }
                    }
                });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            Log.d("bill", "onAcknowledgePurchaseResponse");
        }
    };

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            //already purchased
            setContentView(R.layout.pipm_upgrade_activity_purchased);
            PreferenceManager.getDefaultSharedPreferences(PictureInPictureUpgradeActivity.this)
                    .edit().putBoolean("upgraded",true).apply();
            Preferences.hasUpgrade = true;

            setResult(RESULT_OK);


            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    public void dismissClick(View v){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void okayClick(View v){
        setResult(RESULT_OK);
        finish();
    }

}
