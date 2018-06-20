package com.powerpoint45.dtube;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import us.feras.mdv.MarkdownView;

/**
 * Created by michael on 22/11/17.
 */

public class AboutActivity extends AppCompatActivity {
    MarkdownView markdownView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        markdownView = findViewById(R.id.markdown_view);

        markdownView.loadMarkdownFile("https://raw.githubusercontent.com/powerpoint45/dtube-mobile-unofficial/master/README.md","file:///android_asset/paperwhite.css");
    }
}
