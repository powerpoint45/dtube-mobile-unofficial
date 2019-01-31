package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.app.Activity.RESULT_OK;

public class UploadWebView extends WebView {
    private boolean loadedPage;

    final String JS_PRE = "javascript: (function(){";
    final String JS_POST = "})();";


    public UploadWebView(Context context) {
        super(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public UploadWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        WebView.setWebContentsDebuggingEnabled(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setDatabaseEnabled(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setMediaPlaybackRequiresUserGesture(false);

        Log.d("DT","UPLOAD WV SETUP");

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                loadedPage = true;
                view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
                if (url.equals(DtubeAPI.DTUBE_LOGIN_URL)) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (Preferences.darkMode){
                                setDarkMode();
                            }
                            removeTopBar();
                            setUsername(DtubeAPI.getAccountName(getContext()));
                            setPassword(DtubeAPI.getUserPrivateKey(getContext()));
                            clickLogin();
                        }
                    }, 101);
                }else if (url.equals(DtubeAPI.DTUBE_UPLOAD_URL)){
                    removeTopBar();
                    if (Preferences.darkMode){
                        setDarkMode();
                    }
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("DT", "shouldOverrideUrlLoading:"+url);
                view.loadUrl(url);
                return true;
            }


        });

        addJavascriptInterface(new MyJavaScriptInterface(),
                "android");
    }

    public UploadWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            Log.d("DT", "onUrlChange" + url);
            if (url.equals(DtubeAPI.DTUBE_HOME_URL)) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        loadUrl(DtubeAPI.DTUBE_UPLOAD_URL);
                    }
                });
            }else if (url.contains(DtubeAPI.DTUBE_VIDEO_URL)){
                class FinishRunner implements Runnable{
                    private String url;

                    private FinishRunner(String url){
                        this.url = url;
                    }

                    @Override
                    public void run() {
                        Intent data = new Intent();
                        Bundle b = new Bundle();

                        Video v = new Video();
                        v.permlink = url.substring(url.lastIndexOf("/")+1);
                        v.user = DtubeAPI.getAccountName(getContext());

                        b.putSerializable("video", v);
                        data.putExtra("video",b);
                        ((Activity)getContext()).setResult(RESULT_OK, data);
                        ((Activity)getContext()).finish();
                    }
                }

                post(new FinishRunner(url));
            }

        }
    }

    public void setDarkMode(){
        loadUrl(JS_PRE+"document.getElementsByClassName('nightMode nightmodetext')[0].click();"+JS_POST);
        loadUrl(JS_PRE+"document.getElementsByClassName('nightMode')[0].click();"+JS_POST);
    }

    public void removeTopBar(){
        loadUrl(JS_PRE+"document.getElementsByClassName(\"mobiletopbar\")[0].parentNode.removeChild(document.getElementsByClassName(\"mobiletopbar\")[0]);"+JS_POST);
    }
    public void setUsername(String username){
        loadUrl(JS_PRE+"document.getElementsByName('username')[0].value='" + username + "';"+JS_POST);
    }

    public void setPassword(String pwd){
        loadUrl(JS_PRE+"document.getElementsByName('privatekey')[0].value='" + pwd + "';"+JS_POST);
    }

    public void clickLogin(){
        loadUrl(JS_PRE+"document.getElementsByName('button')[0].click();"+JS_POST);
    }

    public void queURL(String url){
        if (loadedPage) {
            loadUrl(url);
            Log.d("dtube4", "open url");
        }else {
            class LoadRunner implements Runnable{

                private String url;
                private LoadRunner(String url){
                    this.url = url;
                }

                @Override
                public void run() {
                    do{
                        Log.d("dtube4","waiting to run url");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }while (!loadedPage);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadUrl(url);
                        }
                    });
                }
            }

            new Thread(new LoadRunner(url)).start();

        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BaseInputConnection(this, false); //this is needed for #dispatchKeyEvent() to be notified.
    }


    public void killWebView(){
        loadUrl("about:blank");
    }
}
