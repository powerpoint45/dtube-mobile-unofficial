package com.powerpoint45.dtube;

import static com.powerpoint45.dtube.DtubeAPI.PROVIDER_YOUTUBE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * Created by michael on 23/11/17.
 */

public class SearchActivity extends AppCompatActivity {
    final static String SEARCH_URL_HIVE = "https://api.hivesearcher.com/search";
    final static String SEARCH_URL_AVALON = "https://search.d.tube/avalon.contents/_search/?";
    private final static String APIKey = "DOUXSIWXDDUW24VJBJLLHIFYFBAWRJFYPWS9ZTKRSPOMH7DG33OANUK73ESB";
    //final static String SEARCH_URL = "https://search.esteem.app/api/search";
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
    int scrollID;
    String scrollIDString;
    int totalHits;


    int selectedPlatform = DtubeAPI.NET_SELECT_AVION;


    final private static String SORT_POPULARITY_HIVE = "popularity";
    final private static String SORT_RELEVANCE_HIVE = "relevance";
    final private static String SORT_NEWEST_HIVE = "newest";


    final private static String SORT_DATE_AVALON = "ts:desc";
    final private static String SORT_RELEVANCE_AVALON = "_score";
    final private static String SORT_POPULAR_AVALON = "votes.gross";

    static String searchMode = SORT_DATE_AVALON;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        setContentView(R.layout.activity_search);
        recyclerView = findViewById(R.id.search_list);
        askSteemLogo = findViewById(R.id.search_logo_asksteem);
        searchBar = findViewById(R.id.searchView);

        if (Preferences.darkMode){
            ((AppCompatImageView)findViewById(R.id.filter_btn)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }


        ((TextView)findViewById(R.id.hivesearch_txt)).setText(Html.fromHtml(getString(R.string.hivesearcher_url)));
        ((TextView)findViewById(R.id.hivesearch_txt)).setMovementMethod(LinkMovementMethod.getInstance());

        ((ChipGroup)findViewById(R.id.platform_select_group)).setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                if (i == R.id.platform_select_avalon){

                    selectedPlatform = DtubeAPI.NET_SELECT_AVION;
                    setModes();
                    videos.clear();
                    adapter.notifyDataSetChanged();
                    search(searchBar.getText().toString());
                    findViewById(R.id.search_logo_asksteem).setVisibility(View.GONE);
                    findViewById(R.id.hivesearch_txt).setVisibility(View.GONE);
                    Log.d("dd","LOGIN_SELECT_AVION");
                }else if (i == R.id.platform_select_hive){
                    selectedPlatform = DtubeAPI.NET_SELECT_HIVE;
                    setModes();
                    videos.clear();
                    adapter.notifyDataSetChanged();
                    search(searchBar.getText().toString());
                    Log.d("dd","LOGIN_SELECT_HIVE");
                    findViewById(R.id.search_logo_asksteem).setVisibility(View.VISIBLE);
                    findViewById(R.id.hivesearch_txt).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.search_logo_asksteem).setVisibility(View.GONE);
                    findViewById(R.id.hivesearch_txt).setVisibility(View.GONE);
                }
            }
        });



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
        tb.setTouchscreenBlocksFocus(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tb.setKeyboardNavigationCluster(false);
        }
        setSupportActionBar(tb);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
                        if (selectedPlatform == DtubeAPI.NET_SELECT_AVION) {
                            scrollID = 0;
                            avalonSearch(searchBar.getText().toString());
                            InputMethodManager mgr = (InputMethodManager) SearchActivity.this.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            assert mgr != null;
                            mgr.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                        }else if (selectedPlatform == DtubeAPI.NET_SELECT_HIVE){
                            scrollIDString = null;
                            hiveSearch(searchBar.getText().toString());
                            InputMethodManager mgr = (InputMethodManager)SearchActivity.this.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            assert mgr != null;
                            mgr.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);

                        }

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
                        search(querry);
                    }
                }

            }
        });

        Log.d("ddd","tug");
        if (Objects.equals(Preferences.selectedAPI, DtubeAPI.PROVIDER_API_URL_AVALON)){
            ((Chip)findViewById(R.id.platform_select_avalon)).setChecked(true);
            ((ChipGroup)findViewById(R.id.platform_select_group)).check(R.id.platform_select_avalon);
        }else if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_HIVE)){
            ((Chip)findViewById(R.id.platform_select_hive)).setChecked(true);
            ((ChipGroup)findViewById(R.id.platform_select_group)).check(R.id.platform_select_hive);
        }

    }

    protected void hiveSearch(final String q) {
        findViewById(R.id.search_progress).setVisibility(View.VISIBLE);

        if (q!=null && querry!=null && !q.equals(querry)) {
            videos.clear();
            adapter.notifyDataSetChanged();
            scrollIDString = null;
        }
        querry = q;

        Thread t = new Thread() {
            public void run() {

                gettingVideos = true;

                URL searchURL = null;

                try {
                    searchURL = new URL(SEARCH_URL_HIVE);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                Log.d("dtube", "Loading Page "+scrollIDString);
                Looper.prepare(); //For Preparing Message Pool for the childThread

                //HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost(SEARCH_URL);

                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) searchURL.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true); // Allow Inputs
                    urlConnection.setDoOutput(true); // Allow Outputs
                    urlConnection.setUseCaches(false); // Don't use a Cached Copy
                    urlConnection.setRequestProperty("Authorization", APIKey);

                    urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept", "application/json; charset=UTF-8");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");

                    DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());

                    String qStandardized = q+" \"▶️ DTube\"";
                    //String qStandardized = q+" \"dtube\\/1.2\"";

                    JSONObject job = new JSONObject();
                    job.put("q", qStandardized);
                    job.put("since", "2021-09-19T13:11:00");
                    job.put("sort", searchMode);
                    if (scrollIDString!=null)
                        job.put("scroll_id", scrollIDString);

                    Log.d("dtubej",job.toString());
                    dos.write(job.toString().getBytes("UTF-8"));
                    //dos.writeBytes(new String(job.toString().getBytes(),StandardCharsets.UTF_8));
                    dos.flush();
                    dos.close();
                    //dos.writeBytes(myJson);
//                    dos.writeBytes("Content-Disposition: form-data; name=\"q\";"+lineEnd);
//                    dos.writeBytes(lineEnd+qStandardized);
//                    dos.writeBytes(lineEnd+twoHyphens + boundary + twoHyphens+ lineEnd);
//                    dos.writeBytes("Content-Disposition: form-data; name=\"so\";"+lineEnd);
//                    dos.writeBytes(lineEnd+"newest");
//                    dos.writeBytes(lineEnd+twoHyphens + boundary + twoHyphens+ lineEnd);
//                    dos.writeBytes("Content-Disposition: form-data; name=\"pa\";"+lineEnd);
//                    dos.writeBytes(lineEnd+pageNumber);
//                    dos.writeBytes(lineEnd+twoHyphens + boundary + twoHyphens+ lineEnd);



                    int serverResponseCode = urlConnection.getResponseCode();
                    String serverResponseMessage = urlConnection.getResponseMessage();


                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseMessage != null) {
                        try (InputStream in = urlConnection.getInputStream()) {
                            InputStreamReader cin = new InputStreamReader(in);
                            StringBuilder buffer = new StringBuilder();
                            int c;
                            do {
                                c = cin.read();
                                if (c!=-1) {
                                    buffer.append((char) c);
                                }
                            } while(c !=-1);

                            String json = buffer.toString();

                            JSONObject resultsObj = new JSONObject(json);

                            hasMorePages = true;

                            if (resultsObj.has("scroll_id"))
                                scrollIDString = resultsObj.getString("scroll_id");


                            if (resultsObj.has("results")){
                                JSONArray resultsArr = resultsObj.getJSONArray("results");


                                for (int i = 0; i<resultsArr.length(); i++) {
                                    JSONObject videoObject = resultsArr.getJSONObject(i);

                                    Log.d("dtubev",i+": "+videoObject.toString());

                                    if (!videoObject.toString().contains("nsfw")) {
                                        if (videoObject.toString().contains("dtube\\/0.")||videoObject.toString().contains("dtube\\/1.")
                                                || videoObject.toString().contains("oneloveipfs")) {

                                            Video v = new Video();
                                            v.title = videoObject.getString("title");
                                            v.user = videoObject.getString("author");
                                            v.setTime(videoObject.getString("created_at"));
                                            v.permlink = videoObject.getString("permlink");

                                            String body = videoObject.getString("body");


                                            //Extract img Hash from HTML such as <img src='https://ipfs.io/ipfs/QmQG6gPe6hnT8aTRvH3hskMiWbbn9HR6gVf2TH3vXYV71Q' >
                                            int imgTagIndex = body.indexOf("<img src='");
                                            if (imgTagIndex != -1) {
                                                String imageURL = body.substring(imgTagIndex+10,body.indexOf(">", imgTagIndex)-1);
                                                if (imageURL.endsWith("'"))
                                                    imageURL = imageURL.substring(0,imageURL.length()-1);
                                                v.setImageURL(imageURL);
                                            }else {


                                                imgTagIndex = body.indexOf("(https://cdn.steemitimages.com/");

                                                if (imgTagIndex!=-1) {
                                                    String imgURL = body.substring(imgTagIndex + 1, body.indexOf(".png", imgTagIndex) + 4);
                                                    v.setImageURL(imgURL);
                                                }else{
                                                    if (videoObject.has("img_url")){
                                                        v.setImageURL(videoObject.getString("img_url"));
                                                    }
                                                }
                                            }


                                            videos.add(v);

                                            Log.d("dtube", v.user+","+v.imageURL);
                                        }
                                    }
                                }
                            }

                            SearchActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (videos.size()>0)
                                        askSteemLogo.setVisibility(View.GONE);
                                    else {
                                        if (selectedPlatform == DtubeAPI.NET_SELECT_HIVE)
                                            askSteemLogo.setVisibility(View.VISIBLE);
                                    }
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

    protected void avalonSearch(final String q) {

        findViewById(R.id.search_progress).setVisibility(View.VISIBLE);

        if (q!=null && querry!=null && !q.equals(querry)) {
            videos.clear();
            adapter.notifyDataSetChanged();
            scrollID = 0;
        }
        querry = q;

        Thread t = new Thread() {
            public void run() {
                gettingVideos = true;

                Log.d("dtube", "Loading Page "+scrollID);
                Looper.prepare(); //For Preparing Message Pool for the childThread

                //HttpClient httpclient = new DefaultHttpClient();
                //HttpPost httppost = new HttpPost(SEARCH_URL);


                try {

                    JSONObject resultsObj = JSONParser.makeHttpsRequest(SEARCH_URL_AVALON+"q="+ URLEncoder.encode(q)
                        + "&sort=" +searchMode+"&from="+scrollID+"&size=50");

                    JSONObject hitsObj =  resultsObj.getJSONObject("hits");

                    totalHits =hitsObj.getJSONObject("total").getInt("value");
                    JSONArray resultsArr = hitsObj.getJSONArray("hits");


                    hasMorePages = (videos.size() + resultsArr.length())<=totalHits;
                    scrollID = (videos.size() + resultsArr.length());




                    for (int i = 0; i<resultsArr.length(); i++) {
                        JSONObject videoObject = resultsArr.getJSONObject(i).getJSONObject("_source");

                        Log.d("dtubev",i+": "+videoObject.toString());

                        //if (!videoObject.toString().contains("nsfw")) {


                        Video v = new Video();



                        v.user = videoObject.getString("author");

                        v.setTimeLong(videoObject.getLong("ts"));
                        v.permlink = videoObject.getString("link");

                        JSONObject body = videoObject.getJSONObject("json");

                        if (body.has("files")) {
                            JSONObject filesJ = body.getJSONObject("files");

                            if (filesJ.has("ipfs")) {
                                JSONObject ipfsObj = filesJ.getJSONObject("ipfs");
                                if (ipfsObj.has("img")){
                                    JSONObject imgObj = ipfsObj.getJSONObject("img");
                                    if (imgObj.has("360"))
                                        v.snapHash = imgObj.getString("360");
                                    else if (imgObj.has("spr"))
                                        v.snapHash = imgObj.getString("spr");
                                }
                            }

                            if (filesJ.has("youtube")) {
                                v.setProvider(PROVIDER_YOUTUBE);
                                v.hash = filesJ.getString("youtube");
                            }


                        }


                        v.title = body.getString("title");



                        if (body.has("thumbnailUrl") && body.getString("thumbnailUrl").length()>0){
                            v.setImageURL(body.getString("thumbnailUrl"));
                        }else {
                            String bodyString = body.toString();
                            //Extract img Hash from HTML such as <img src='https://ipfs.io/ipfs/QmQG6gPe6hnT8aTRvH3hskMiWbbn9HR6gVf2TH3vXYV71Q' >
                            int imgTagIndex = body.toString().indexOf("<img src='");
                            if (imgTagIndex != -1) {
                                String imageURL = bodyString.substring(imgTagIndex + 10, bodyString.indexOf(">", imgTagIndex) - 1);
                                if (imageURL.endsWith("'"))
                                    imageURL = imageURL.substring(0, imageURL.length() - 1);
                                v.setImageURL(imageURL);
                            }
                        }

                        videos.add(v);

                        Log.d("dtube", v.user+","+v.imageURL);

                        //}
                    }

                    SearchActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (videos.size()>0)
                                askSteemLogo.setVisibility(View.GONE);
                            else if (selectedPlatform == DtubeAPI.NET_SELECT_HIVE)
                                askSteemLogo.setVisibility(View.VISIBLE);
                            adapter.setVideos(videos);
                            findViewById(R.id.search_progress).setVisibility(View.GONE);
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    //Log.d("dtube",e.getMessage());
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
        videos.get(pos).blockchain = selectedPlatform;
        data.putExtra("platform",selectedPlatform);
        setResult(RESULT_OK, data);
        finish();
    }


    public void search(String q){
        if (selectedPlatform == DtubeAPI.NET_SELECT_AVION)
            avalonSearch(searchBar.getText().toString());
        else
            hiveSearch(searchBar.getText().toString());
    }


    public void setModes(){
        if (searchMode == SORT_DATE_AVALON || searchMode == SORT_NEWEST_HIVE) {
            SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_DATE_AVALON:SORT_NEWEST_HIVE;
        }else if (searchMode == SORT_POPULAR_AVALON || searchMode == SORT_POPULARITY_HIVE) {
            SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_RELEVANCE_AVALON:SORT_RELEVANCE_HIVE;
        }else if (searchMode == SORT_RELEVANCE_AVALON || searchMode == SORT_RELEVANCE_HIVE) {
            SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_POPULAR_AVALON:SORT_POPULARITY_HIVE;
        }
    }



    public void filterClicked(View v){
        // Initializing the popup menu and giving the reference as current context
        PopupMenu popupMenu = new PopupMenu(SearchActivity.this, v);

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
        if (searchMode == SORT_DATE_AVALON || searchMode == SORT_NEWEST_HIVE)
            popupMenu.getMenu().findItem(R.id.newest).setEnabled(false);
        else if (searchMode == SORT_POPULAR_AVALON || searchMode == SORT_POPULARITY_HIVE)
            popupMenu.getMenu().findItem(R.id.popularity).setEnabled(false);
        else if (searchMode == SORT_RELEVANCE_AVALON || searchMode == SORT_RELEVANCE_HIVE)
            popupMenu.getMenu().findItem(R.id.relevance).setEnabled(false);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // Toast message on menu item clicked
                switch (menuItem.getItemId()){
                    case R.id.newest:
                        SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_DATE_AVALON:SORT_NEWEST_HIVE;
                        break;
                    case R.id.relevance:
                        SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_RELEVANCE_AVALON:SORT_RELEVANCE_HIVE;
                        break;
                    case R.id.popularity:
                        SearchActivity.searchMode = selectedPlatform == DtubeAPI.NET_SELECT_AVION ? SORT_POPULAR_AVALON:SORT_POPULARITY_HIVE;
                        break;
                }

                videos.clear();
                adapter.notifyDataSetChanged();
                scrollID = 0;


                search((searchBar.getText().toString()));

                return true;
            }
        });
        // Showing the popup menu
        popupMenu.show();
    }

}
