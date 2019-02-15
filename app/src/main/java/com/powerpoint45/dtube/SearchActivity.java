package com.powerpoint45.dtube;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by michael on 23/11/17.
 */

public class SearchActivity extends AppCompatActivity {

    final static String SEARCH_URL = "https://search.esteem.app/api/search";
    EditText searchBar;
    RecyclerView recyclerView;
    ImageView askSteemLogo;
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
        recyclerView = findViewById(R.id.search_list);
        askSteemLogo = findViewById(R.id.search_logo_asksteem);

//        if (Preferences.darkMode)
//            askSteemLogo.setImageResource(R.drawable.asksteemwhite);

        inflater = getLayoutInflater();
        videos = new VideoArrayList();



        layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChannelAdapter(videos,this);
        recyclerView.setAdapter(adapter);

        Toolbar tb = findViewById(R.id.search_toolbar);
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        searchBar = findViewById(R.id.searchView);
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
                        assert mgr != null;
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
                        findViewById(R.id.search_progress).setVisibility(View.VISIBLE);
                        esteemSearch(searchBar.getText().toString());
                        InputMethodManager mgr = (InputMethodManager)SearchActivity.this.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        assert mgr != null;
                        mgr.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

                    }

                    return false;
                }
                return false;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (hasMorePages && !gettingVideos) {
                    if (videos!=null && videos.size() - layoutManager.findLastVisibleItemPosition() < 5) {
                        esteemSearch(querry);
                    }
                }

            }
        });

    }


    protected void esteemSearch(final String q) {

        if (q!=null && querry!=null && !q.equals(querry)) {
            videos.clear();
            adapter.notifyDataSetChanged();
            pageNumber = 0;
        }
        querry = q;

        Thread t = new Thread() {
            public void run() {

                gettingVideos = true;
                pageNumber++;

                Log.d("dtube", "Loading Page "+pageNumber);
                Looper.prepare(); //For Preparing Message Pool for the childThread
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(SEARCH_URL);

                try {
                    // Request parameters and other properties.
                    List<NameValuePair> params = new ArrayList<>(3);
                    params.add(new BasicNameValuePair("q", q+"\"▶️ DTube\""));
                    params.add(new BasicNameValuePair("so", "newest"));
                    //in the future search method can be an option
                    //params.add(new BasicNameValuePair("so", "popularity"));
                    params.add(new BasicNameValuePair("pa", pageNumber+""));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

                    //Execute and get the response.
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {
                        try (InputStream in = entity.getContent()) {
                            InputStreamReader cin = new InputStreamReader(in);
                            StringBuilder buffer = new StringBuilder();
                            int c;
                            do {
                                c = cin.read();
                                if (c!=-1) {
                                    buffer.append((char) c);
                                }
                            } while(c !=-1);

                            JSONObject resultsObj = new JSONObject(buffer.toString());

                            if (resultsObj.has("pages")){
                                hasMorePages = pageNumber < resultsObj.getInt("pages");
                            }


                            if (resultsObj.has("results")){
                                JSONArray resultsArr = resultsObj.getJSONArray("results");

                                for (int i = 0; i<resultsArr.length(); i++) {
                                    JSONObject videoObject = resultsArr.getJSONObject(i);

                                    if (!videoObject.toString().contains("nsfw")) {
                                        if (videoObject.toString().contains("dtube\\/0.")
                                                || videoObject.toString().contains("oneloveipfs")) {

                                            Video v = new Video();
                                            v.title = videoObject.getString("title");
                                            v.user = videoObject.getString("author");
                                            v.setTime(videoObject.getString("created_at"));
                                            v.permlink = videoObject.getString("permlink");

                                            String body = videoObject.getString("body");


                                            //Extract img Hash from HTML such as <img src='https://ipfs.io/ipfs/QmQG6gPe6hnT8aTRvH3hskMiWbbn9HR6gVf2TH3vXYV71Q'>
                                            int imgTagIndex = body.indexOf("<img src='");
                                            if (imgTagIndex != -1) {
                                                int imgHashIndex = body.indexOf("/Qm", imgTagIndex) + 1;
                                                Log.d("json", videoObject.toString());
                                                v.snapHash = body.substring(imgHashIndex, body.indexOf("'>", imgHashIndex));
                                            }else {
                                                Log.d("dtube", "invalid snap. Replacing img");

                                                imgTagIndex = body.indexOf("(https://cdn.steemitimages.com/");


                                                //int imgHashIndex = body.indexOf("/Qm", imgTagIndex) + 1;
                                                String imgURL = body.substring(imgTagIndex+1, body.indexOf(".png",imgTagIndex)+4);
                                                v.setImageURL(imgURL);
                                            }


                                            videos.add(v);

                                            Log.d("dtube", v.user+","+v.snapHash);
                                        }
                                    }
                                }
                            }

                            SearchActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (videos.size()>0)
                                        askSteemLogo.setVisibility(View.GONE);
                                    else
                                        askSteemLogo.setVisibility(View.VISIBLE);
                                    adapter.setVideos(videos);
                                    findViewById(R.id.search_progress).setVisibility(View.GONE);
                                }
                            });

                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("dtube",e.getMessage());
                    SearchActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.search_progress).setVisibility(View.GONE);
                        }
                    });
                }
                finally {
                    gettingVideos = false;
                }



                Looper.loop(); //Loop in the message queue
            }
        };
        t.start();
    }

//    public void goSearch(String q){
//        if (q!=null && querry!=null && !q.equals(querry)) {
//            videos.clear();
//            adapter.notifyDataSetChanged();
//            pageNumber = 0;
//        }
//        querry = q;
//        class searchRunner implements Runnable{
//            private String q;
//            private searchRunner(String q){
//                this.q = q;
//            }
//
//            @Override
//            public void run() {
//                Log.d("dtube5","to search for "+q);
//                BufferedReader reader = null;
//                try {
//                    gettingVideos = true;
//                    pageNumber++;
//                    URL url = new URL(SEARCH_URL+q.replace(" ","%20")+"&pg="+pageNumber);
//                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
//                    StringBuilder buffer = new StringBuilder();
//                    int read;
//                    char[] chars = new char[1024];
//                    while ((read = reader.read(chars)) != -1)
//                        buffer.append(chars, 0, read);
//
//                    JSONObject resultsObj = new JSONObject(buffer.toString());
//                    if (resultsObj.has("pages")){
//                        hasMorePages = resultsObj.getJSONObject("pages").getBoolean("has_next");
//                        Log.d("dtube5","more:"+hasMorePages);
//                    }
//                    if (resultsObj.has("results")){
//                        JSONArray resultsArr = resultsObj.getJSONArray("results");
//                        for (int i = 0; i<resultsArr.length(); i++){
//                            JSONObject videoObject = resultsArr.getJSONObject(i);
//                            if (!videoObject.toString().contains("nsfw")) {
//                                if (videoObject.has("meta")) {
//
//                                    Video v = new Video();
//                                    v.title = videoObject.getString("title");
//                                    v.user = videoObject.getString("author");
//                                    v.setTime(videoObject.getString("created"));
//                                    v.permlink = videoObject.getString("permlink");
//                                    JSONObject videoMeta = new JSONObject(videoObject.getString("meta"));
//
//                                    if (videoMeta.has("video")) {
//                                        v.snapHash = videoMeta.getJSONObject("video").getJSONObject("info").getString("snaphash");
//
//                                        videos.add(v);
//                                    }
//                                }
//                            }
//
//                        }
//
//                        SearchActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (videos.size()>0)
//                                    askSteemLogo.setVisibility(View.GONE);
//                                else
//                                    askSteemLogo.setVisibility(View.VISIBLE);
//                                adapter.setVideos(videos);
//                            }
//                        });
//                    }
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }finally {
//                    gettingVideos = false;
//                    if (reader!=null)
//                        try {
//                            reader.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                }
//            }
//        };
//        new Thread(new searchRunner(q)).start();
//    }

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
