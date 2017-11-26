package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

/**
 * Created by michael on 22/11/17.
 */

public class WebViewVideoView extends WebView {
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

    public void killWebView(){
        loadUrl("about:blank");
    }
}
