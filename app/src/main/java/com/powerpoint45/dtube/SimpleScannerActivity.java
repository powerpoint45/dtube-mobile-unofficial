package com.powerpoint45.dtube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    final String TAG = "dtubeQR";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here

        if (rawResult!=null && rawResult.getBarcodeFormat().toString().equals("QR_CODE")) {
            if (rawResult.getText().equals("http://")){
                //Camera is pointed at the wrong QRcode. You need to click on the small QRcode to see full QR
                Toast.makeText(this,R.string.wrong_qr_code, Toast.LENGTH_LONG).show();
                mScannerView.resumeCameraPreview(this);
            }else if (rawResult.getText()!=null){
                Log.v(TAG, rawResult.getText()); // Prints scan results
                Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
                Intent i = new Intent();
                i.putExtra("password",rawResult.getText());
                setResult(RESULT_OK, i);
                finish();
            }else {
                Toast.makeText(this,R.string.no_data_qr_code, Toast.LENGTH_LONG).show();
                mScannerView.resumeCameraPreview(this);
            }
        }
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}