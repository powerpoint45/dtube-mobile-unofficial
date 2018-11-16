package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.app.Activity.RESULT_OK;

public class UploadWebView extends WebView {
    private boolean loadedPage;

    @SuppressLint("SetJavaScriptEnabled")
    public UploadWebView(Context context) {
        super(context);

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
                            removeTopBar();
                            if (Preferences.darkMode){
                                setDarkMode();
                            }
                            setUsername(DtubeAPI.getAccountName(getContext()));
                            setPassword(DtubeAPI.getUserPrivateKey(getContext()));
                            //selectRememberMe();
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
        loadUrl("javascript:document.getElementsByClassName('nightMode nightmodetext')[0].click();");
        loadUrl("javascript:document.getElementsByClassName('nightMode')[0].click();");
    }

    public void removeTopBar(){
        loadUrl("javascript:document.getElementsByClassName(\"mobiletopbar\")[0].parentNode.removeChild(document.getElementsByClassName(\"mobiletopbar\")[0]);");
    }
    public void setUsername(String username){
        loadUrl("javascript: (function(){document.getElementsByName('username')[0].value='" + username + "';})();");
    }

    public void setPassword(String pwd){
        loadUrl("javascript: (function(){document.getElementsByName('privatekey')[0].value='" + pwd + "';})();");
    }

    public void clickLogin(){
        loadUrl("javascript:document.getElementsByName('button')[0].click();");
    }

    public void selectRememberMe(){
        loadUrl("javascript:document.getElementsByName('rememberme')[0].checked = true;");
    }

    //For Android TV mode we do not need the controlbar controls
    private void removeControlBarChildren(){
        loadUrl("javascript:removeControlBarChildren();");
    }

    public void pauseVideo(){
        loadUrl("javascript:document.getElementsByTagName('video')[0].pause();");
    }

    public void playVideo(){
        loadUrl("javascript:document.getElementsByTagName('video')[0].play();");
    }

    public void makeFullscreen(){
        queURL("javascript:document.getElementsByTagName('video')[0].webkitRequestFullscreen();");
    }

    public void fastForward(){
        loadUrl("javascript:player.currentTime(player.currentTime() + 10);");
    }

    public void rewind(){
        loadUrl("javascript:player.currentTime(player.currentTime() - 10);");
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
