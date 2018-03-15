package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by michael on 22/11/17.
 */

public class WebViewVideoView extends WebView {
    boolean loadedPage;


    @SuppressLint("SetJavaScriptEnabled")
    public WebViewVideoView(Activity context) {
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
            public void onPageFinished(WebView view, String url) {
                loadedPage = true;
            }
        });

        VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(context.findViewById(R.id.video_content),
                (FrameLayout)context.findViewById(R.id.video_content_holder));

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
                    ((Activity)getContext()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
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


    public void killWebView(){
        loadUrl("about:blank");
    }
}
