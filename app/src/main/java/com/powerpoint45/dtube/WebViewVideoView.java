package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by michael on 22/11/17.
 */

public class WebViewVideoView extends WebView {
    private boolean loadedPage;
    private boolean isPlaying;
    private final boolean runningTVMode;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewVideoView(Activity context, boolean tvMode) {
        super(context);
        runningTVMode = tvMode;

        if (tvMode) {
            setFocusable(false);
            setClickable(false);
            getSettings().setNeedInitialFocus(false);
        }

        if (getResources().getBoolean(R.bool.debug)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setBackgroundColor(Color.BLACK);

        getSettings().setJavaScriptEnabled(true);
        getSettings().setDatabaseEnabled(true);
        getSettings().setAllowContentAccess(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setMediaPlaybackRequiresUserGesture(false);
        getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        getSettings().setUserAgentString(getSettings().getUserAgentString().replace("; wv", ""));
        getSettings().setSaveFormData(true);
        //setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);


        setLongClickable(false);
        // Below line prevent vibration on Long click
        setHapticFeedbackEnabled(false);

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        setId(R.id.embeded_video_view);

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("dtube9","pagestart:"+ url);
            }

            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                return url.startsWith("https://m.youtube.com/") || url.startsWith("https://3speak.co/watch"); // reject loading this page
            }

            public void onPageFinished(WebView view, String url) {
                if (clickWhenReady)
                    clickCenter();

                if (url.startsWith("https://m.youtube.com/"))
                    goBack();
                else {

                    loadedPage = true;
                    if (runningTVMode)
                        removeControlBarChildren();
                }
            }
        });

        VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(context.findViewById(R.id.video_content),
                context.findViewById(R.id.video_content_holder));

        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = ((Activity)getContext()).getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    ((Activity)getContext()).getWindow().setAttributes(attrs);
                    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    ((Activity)getContext()).getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                }
                else
                {
                    WindowManager.LayoutParams attrs = ((Activity)getContext()).getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    ((Activity)getContext()).getWindow().setAttributes(attrs);
                    ((Activity)getContext()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

            }
        });

        setWebChromeClient(webChromeClient);

        addJavascriptInterface(new VideoJavascriptClient(), "androidAppProxy");
    }

    class VideoJavascriptClient{

        @JavascriptInterface
        public void isPlaying(boolean playing){
            isPlaying = playing;
        }

    }

    public boolean isPlaying(){
        return isPlaying;
    }

    //For Android TV mode we do not need the controlbar controls
    private void removeControlBarChildren(){
        loadUrl("javascript:removeControlBarChildren();");
    }

//    public void pauseVideo(){
//        Log.d("Avalon", "pauseVideo called");
//
//        loadUrl("javascript:(function(){document.getElementsByClassName('html5-video-player')[0].pauseVideo();})()");
//        //loadUrl("javascript:document.getElementsByClassName('html5-video-player')[0].pauseVideo();");
//
//        //loadUrl("javascript:document.getElementsByTagName('video')[0].pause();");
//    }
//
//    public void playVideo(){
//
//        Log.d("Avalon", "playVideo called");
//
//        loadUrl("javascript:(function(){document.getElementsByClassName('html5-video-player')[0].playVideo();})()");
//
//        //loadUrl("javascript:(function(){document.getElementsByClassName('html5-video-player')[0].playVideo();})()");
//
//        //loadUrl("javascript:document.getElementsByClassName('html5-video-player')[0].playVideo();");
//        //loadUrl("javascript:document.getElementsByTagName('video')[0].play();");
//    }

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

                private final String url;
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

    boolean paused = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("d", "key in video player");
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                clearFocus();
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    boolean clickWhenReady;

    public void queClickCenter(){
        clickWhenReady = true;
    }
    public void clickCenter(){
        requestFocus();
        MotionEvent motionEvent = MotionEvent.obtain(200,200,MotionEvent.ACTION_DOWN, getWidth()/2, getHeight()/2,0);
        dispatchTouchEvent(motionEvent);
        MotionEvent motionEvent2 = MotionEvent.obtain(200,200,MotionEvent.ACTION_UP, getWidth()/2, getHeight()/2,0);
        dispatchTouchEvent(motionEvent2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((VideoEnabledWebChromeClient)getWebChromeClient()).playPause();
        }


    }

    public void killWebView(){
        loadUrl("about:blank");
    }
}
