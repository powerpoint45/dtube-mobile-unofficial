package com.powerpoint45.dtube;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by michael on 23/11/17.
 */

public class SearchActivity extends AppCompatActivity {

    final static String SEARCH_URL = "https://api.asksteem.com/search?include=meta&q=meta.video.info.title:*AND+dtube+AND+";
    EditText searchBar;
    RecyclerView listView;
    LinearLayoutManager layoutManager;
    LayoutInflater inflater;
    VideoArrayList videos;
    ChannelAdapter adapter;
    boolean hasMorePages;
    boolean gettingVideos;
    String querry;
    int pageNumber = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        setContentView(R.layout.activity_search);
        listView = findViewById(R.id.search_list);

        inflater = getLayoutInflater();
        videos = new VideoArrayList();


        layoutManager = new LinearLayoutManager(this);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(layoutManager);
        adapter = new ChannelAdapter(videos,this);
        listView.setAdapter(adapter);

        Toolbar tb = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        searchBar = (EditText) findViewById(R.id.searchView);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.requestFocus();
                        InputMethodManager mgr = (InputMethodManager)SearchActivity.this.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        mgr.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        },100);

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchBar.getText().toString().length()>0) {
                        goSearch(searchBar.getText().toString());
                        InputMethodManager mgr = (InputMethodManager)SearchActivity.this.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

                    }

                    return false;
                }
                return false;
            }
        });

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (hasMorePages && !gettingVideos) {
                    if (videos!=null && videos.size() - layoutManager.findLastVisibleItemPosition() < 5) {
                        goSearch(querry);
                    }
                }

            }
        });

    }

    public void goSearch(String q){
        if (q!=null && querry!=null && !q.equals(querry)) {
            videos.clear();
            adapter.notifyDataSetChanged();
            pageNumber = 0;
        }
        querry = q;
        class searchRunner implements Runnable{
            private String q;
            private searchRunner(String q){
                this.q = q;
            }

            @Override
            public void run() {
                Log.d("dtube5","to search for "+q);
                BufferedReader reader = null;
                try {
                    gettingVideos = true;
                    pageNumber++;
                    URL url = new URL(SEARCH_URL+q.replace(" ","%20")+"&pg="+pageNumber);
                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder buffer = new StringBuilder();
                    int read;
                    char[] chars = new char[1024];
                    while ((read = reader.read(chars)) != -1)
                        buffer.append(chars, 0, read);

                    JSONObject resultsObj = new JSONObject(buffer.toString());
                    if (resultsObj.has("pages")){

                        hasMorePages = resultsObj.getJSONObject("pages").getBoolean("has_next");
                        Log.d("dtube5","more:"+hasMorePages);
                    }
                    if (resultsObj.has("results")){
                        VideoArrayList videoResults = new VideoArrayList();
                        JSONArray resultsArr = resultsObj.getJSONArray("results");
                        for (int i = 0; i<resultsArr.length(); i++){
                            JSONObject videoObject = resultsArr.getJSONObject(i);
                            if (videoObject.has("meta")) {

                                Video v = new Video();
                                v.title = videoObject.getString("title");
                                v.user = videoObject.getString("author");
                                v.setTime(videoObject.getString("created"));
                                v.permlink = videoObject.getString("permlink");
                                JSONObject videoMeta = new JSONObject(videoObject.getString("meta"));

                                if (videoMeta.has("video")) {
                                    v.snapHash = videoMeta.getJSONObject("video").getJSONObject("info").getString("snaphash");

                                    Log.d("dtube5", v.title);
                                    videos.add(v);
                                }
                            }

                        }

                        SearchActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setVideos(videos);
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    gettingVideos = false;
                    if (reader!=null)
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }


            }
        };
        new Thread(new searchRunner(q)).start();
    }

    public void onItemClick(int pos){
        Intent data = new Intent();
        Bundle b = new Bundle();
        b.putSerializable("video", videos.get(pos));
        data.putExtra("video",b);
        setResult(RESULT_OK, data);
        finish();
    }


    public void subscribeButtonClicked(View v){

    }
}
