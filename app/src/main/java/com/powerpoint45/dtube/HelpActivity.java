package com.powerpoint45.dtube;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import us.feras.mdv.MarkdownView;

/**
 * Created by michael on 22/11/17.
 */

public class HelpActivity extends AppCompatActivity {
    MarkdownView markdownView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        markdownView = findViewById(R.id.markdown_view);

        int loginType = getIntent().getIntExtra("logintype",0);
        String helpPage = "file:///android_asset/login.md";

        if (loginType == DtubeAPI.NET_SELECT_AVION)
            helpPage = "file:///android_asset/createaccountavalon.md";

        if (Preferences.darkMode)
            markdownView.loadMarkdownFile(helpPage,"file:///android_asset/dark.css");
        else
            markdownView.loadMarkdownFile(helpPage,"file:///android_asset/paperwhite.css");
    }
}
