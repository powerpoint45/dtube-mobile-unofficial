package com.powerpoint45.dtube;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import us.feras.mdv.MarkdownView;

/**
 * Created by michael on 22/11/17.
 */

public class AboutActivity extends AppCompatActivity {
    MarkdownView markdownView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        markdownView = findViewById(R.id.markdown_view);

        if (Preferences.darkMode)
            markdownView.loadMarkdownFile("https://raw.githubusercontent.com/powerpoint45/dtube-mobile-unofficial/master/README.md","file:///android_asset/dark.css");
        else
            markdownView.loadMarkdownFile("https://raw.githubusercontent.com/powerpoint45/dtube-mobile-unofficial/master/README.md","file:///android_asset/paperwhite.css");
    }
}
