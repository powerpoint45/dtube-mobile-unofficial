package com.powerpoint45.dtube;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.material.navigation.NavigationView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    FrameLayout navigationHeader;
    ActionBarDrawerToggle drawerToggle;
    FrameLayout mainFrame;

    boolean activityPaused;

    VideoArrayList allVideos = new VideoArrayList();
    VideoArrayList videos = new VideoArrayList();
    int selectedTab;

    final int REQUEST_CODE_PLAY_VIDEO = 0;
    final int REQUEST_CODE_LOGIN = 1;
    final int REQUEST_CODE_PROFILE = 2;
    final int REQUEST_CODE_SEARCH = 3;
    final int REQUEST_CODE_UPLOAD = 4;
    final int REQUEST_CODE_SETTINGS = 5;

    final int FILES_REQUEST_PERMISSION = 10;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private FeedAdapter feedAdapter;
    LinearLayout bottomBar;
    boolean gettingMoreVideos;

    SteemitWebView steemWebView;
    Toolbar toolbar;

    Person accountInfo;

    boolean runningOnTV;

    //flag set from settings activity to let this activity know it needs to
    //change theme
    public static boolean changedDarkMode;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Preferences.loadPreferences(this);

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;



        //For proper functioning of this app, Android System Webview must be installed
        if (!Tools.isPackageInstalled("com.google.android.webview",getPackageManager())){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.webview_alert)
                    .setPositiveButton(R.string.install, (dialog, id) -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.webview"))))
                    .setNegativeButton(android.R.string.cancel, null);
            // Create the AlertDialog object and return it
            builder.create().show();
        }

        //Customize layout if in TV Mode
        if (uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION) {
            setContentView(R.layout.activity_main_tv);
            runningOnTV = true;
            findViewById(R.id.search_btn).setFocusableInTouchMode(true);
            findViewById(R.id.profile_image).setFocusableInTouchMode(true);
        }else
            setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.mainframe);

        findViewById(R.id.search_btn).setOnClickListener(this::searchButtonClicked);

        if (!runningOnTV)
            findViewById(R.id.upload_btn).setOnClickListener(v -> {
                if (accountInfo!=null) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                FILES_REQUEST_PERMISSION);
                    }else {
                        startActivityForResult(new Intent(MainActivity.this, UploadActivity.class), REQUEST_CODE_UPLOAD);
                    }

                }else
                    loginButtonClicked(v);
            });


        if (Preferences.darkMode) {
            ((AppCompatImageView) findViewById(R.id.logo)).setImageResource(R.drawable.logo_white);
            ((AppCompatImageView) findViewById(R.id.search_btn)).setImageResource(R.drawable.ic_search_white);
            if (!runningOnTV)
                ((AppCompatImageView) findViewById(R.id.upload_btn)).setImageResource(R.drawable.ic_file_upload_white);
        }



        setSupportActionBar(findViewById(R.id.toolbar));
        if (!runningOnTV) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.setProgressViewOffset(true, 0, Tools.numtodp(60, this));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.RED);
            swipeRefreshLayout.setColorSchemeColors(Color.WHITE);

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    switch (selectedTab) {
                        case DtubeAPI.CAT_SUBSCRIBED:
                            allVideos.removeAll(videos.getCategorizedVideos(DtubeAPI.CAT_SUBSCRIBED));
                            feedAdapter.notifyDataSetChanged();
                            steemWebView.getSubscriptionFeed(accountInfo.userName);
                            break;
                        case DtubeAPI.CAT_HOT:
                            allVideos.removeAll(videos.getCategorizedVideos(DtubeAPI.CAT_HOT));
                            feedAdapter.notifyDataSetChanged();
                            steemWebView.getHotVideosFeed();
                            break;

                        case DtubeAPI.CAT_NEW:
                            allVideos.removeAll(videos.getCategorizedVideos(DtubeAPI.CAT_NEW));
                            feedAdapter.notifyDataSetChanged();
                            steemWebView.getNewVideosFeed();
                            break;

                        case DtubeAPI.CAT_TRENDING:
                            allVideos.removeAll(videos.getCategorizedVideos(DtubeAPI.CAT_TRENDING));
                            feedAdapter.notifyDataSetChanged();
                            steemWebView.getTrendingVideosFeed();
                            break;
                        case DtubeAPI.CAT_HISTORY:
                            tabGoToHistoryClicked(new View(MainActivity.this));
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                    }
                }
            });
        }


        drawerLayout = findViewById(R.id.drawer_layout);

//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        if (runningOnTV)
            drawerLayout.setScrimColor(Color.TRANSPARENT);


        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.menu_profile:
                    loginButtonClicked(new View(MainActivity.this));
                    break;
                case R.id.menu_update:
                    String url = getPreferences(MODE_PRIVATE).getString(BuildConfig.VERSION_NAME,null);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    break;
                case R.id.menu_subscribed:
                    tabGoToSubscribedClicked(menuItem.getActionView());
                    break;
                case R.id.menu_hot:
                    tabGoToHotClicked(menuItem.getActionView());
                    break;
                case R.id.menu_trending:
                    tabGoToTrendingClicked(menuItem.getActionView());
                    break;
                case R.id.menu_new:
                    tabGoToNewClicked(menuItem.getActionView());
                    break;
                case R.id.menu_history:
                    tabGoToHistoryClicked(menuItem.getActionView());
                    break;
                case R.id.menu_donate:
                    startActivity(new Intent(MainActivity.this, DonateActivity.class));
                    break;
                case R.id.menu_upgrade:
                    startActivity(new Intent(MainActivity.this,PictureInPictureUpgradeActivity.class));
                    break;
                case R.id.menu_settings:
                    startActivityForResult(new Intent(MainActivity.this, PreferencesActivity.class), REQUEST_CODE_SETTINGS);
                    break;
                case R.id.menu_about:
                    //Intent aboutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://steemit.com/utopian-io/@immawake/introducing-the-dtube-mobile-app-unofficial-android-app"));
                    Intent aboutIntent = new Intent(MainActivity.this,AboutActivity.class);
                    startActivity(aboutIntent);
                    break;
                case R.id.menu_more_apps:
                    Intent moreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=7725486445697122776"));
                    startActivity(moreIntent);
                    break;
                case R.id.subscription_id:
                    String username = menuItem.getTitle().toString();
                    Intent i = new Intent(MainActivity.this, ChannelActivity.class);
                    i.putExtra("username", username);
                    i.putExtra("userurl", "/#!/c/" + username);
                    startActivityForResult(i, REQUEST_CODE_PROFILE);
                    break;
                // TODO - Handle other items
            }
            return true;
        });

        navigationHeader = (FrameLayout) navigationView.getHeaderView(0);


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open, R.string.close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            }
        };


        if (runningOnTV) {
            drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                    ((DrawerLayout.LayoutParams) mainFrame.getLayoutParams()).leftMargin = (int) (slideOffset * drawerView.getWidth());
                    mainFrame.requestLayout();
                }

                @Override
                public void onDrawerOpened(@NonNull View drawerView) {

                }

                @Override
                public void onDrawerClosed(@NonNull View drawerView) {

                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
        }

        Menu m = navigationView.getMenu();

        //remove donation options to comply with Play Store policy
        if (getResources().getBoolean(R.bool.on_play_store)){
            m.findItem(R.id.menu_donate).setVisible(false);
        }

        //remove upgrade button if not compatible or already purchased
        if ((Preferences.hasUpgrade) || !Tools.deviceSupportsPIPMode(this)) {
            m.findItem(R.id.menu_upgrade).setVisible(false);
        }

        //remove more apps & about menu item if running on TV
        //trying to comply with play store policy for Android TV
        if (runningOnTV){
            m.findItem(R.id.menu_more_apps).setVisible(false);
            m.findItem(R.id.menu_about).setVisible(false);
            m.findItem(R.id.menu_donate).setVisible(false);
        }

        drawerToggle.setDrawerIndicatorEnabled(true);
        //noinspection deprecation
        drawerLayout.setDrawerListener(drawerToggle);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            drawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.colorAccent));
//        } else {
//            drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));
//        }

        steemWebView = new SteemitWebView(this);

        bottomBar = findViewById(R.id.bottom_bar);
        toolbar = findViewById(R.id.toolbar);

        recyclerView = findViewById(R.id.feed_rv);

        //set recyclerView either landscape or portrait
        onConfigurationChanged(getResources().getConfiguration());

        //Animate toolbar when scrolling feed for non-TV Mode
        //Enable infinite scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!runningOnTV) {
                    if (toolbar.getHeight() - dy <= 0) {
                        if (toolbar.getVisibility() == View.VISIBLE)
                            toolbar.setVisibility(View.GONE);
                        if (toolbar.getHeight() != 0)
                            toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                    } else {
                        if (toolbar.getVisibility() == View.GONE)
                            toolbar.setVisibility(View.VISIBLE);

                        if (toolbar.getHeight() - dy > getResources().getDimension(R.dimen.toolbar_size))
                            toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.toolbar_size)));
                        else
                            toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbar.getHeight() - dy));
                    }
                }

                if(((LinearLayoutManager)layoutManager).findLastVisibleItemPosition() == feedAdapter.getItemCount()-1){

                    if (feedAdapter.getItemCount()>0 && !gettingMoreVideos
                            && selectedTab != DtubeAPI.CAT_HISTORY && selectedTab != DtubeAPI.CAT_SUBSCRIBED){
                        //endless scrolling. Get more videos here
                        if (getResources().getBoolean(R.bool.debug)) {
                            Toast.makeText(MainActivity.this, "getting more videos", Toast.LENGTH_SHORT).show();
                        }

                        gettingMoreVideos = true;

                        //if videos are loaded at current time
                        if (videos.size()>0) {
                            Video lastVideo = videos.get(videos.size() - 1);
                            switch (selectedTab) {
                                case DtubeAPI.CAT_HOT:
                                    steemWebView.getHotVideosFeed(lastVideo.user, lastVideo.permlink);
                                    break;

                                case DtubeAPI.CAT_NEW:
                                    steemWebView.getNewVideosFeed(lastVideo.user, lastVideo.permlink);
                                    break;

                                case DtubeAPI.CAT_TRENDING:
                                    steemWebView.getTrendingVideosFeed(lastVideo.user, lastVideo.permlink);
                                    break;
                            }
                        }else{
                            switch (selectedTab) {
                                case DtubeAPI.CAT_HOT:
                                    steemWebView.getHotVideosFeed();
                                    break;

                                case DtubeAPI.CAT_NEW:
                                    steemWebView.getNewVideosFeed();
                                    break;

                                case DtubeAPI.CAT_TRENDING:
                                    steemWebView.getTrendingVideosFeed();
                                    break;
                            }
                            Log.d("dtube9", "get Feeds");
                        }

                    }
                }


            }
        });


        feedAdapter = new FeedAdapter(this, runningOnTV);
        recyclerView.setAdapter(feedAdapter);

        updateBottomBar();
        tabGoToHotClicked(recyclerView);
        setProfileInfoUI();

        if (accountInfo!=null)
            steemWebView.getSubscriptionFeed(accountInfo.userName);

        getInitialFeeds();

        addVideos(Video.getRecentVideos(this));

        m.findItem(R.id.menu_update).setVisible(false);

        //only check for updates on GitHub if app was not downloaded from PlayStore
        if (getResources().getBoolean(R.bool.on_play_store)) {
            updateCheck();
        }

//        //init Builder
//        SteemConnect.InstanceBuilder instanceBuilder = new SteemConnect.InstanceBuilder();
//
//        //set parameters
//        instanceBuilder
//                .setApp("powerpoint45")
//                .setBaseUrl("https://v2.steemconnect.com")
//                //.setAcessToken("accessToken")
//                .setState("state")
//                .setCallbackUrl("dtube://main/")
//                .setScope(new String[]{"comment", "vote"});  //scopes of your application
//
//
//
//        // build the SteemConnect object.
//        SteemConnect steemConnect = instanceBuilder.build();
//
//        try {
//            Log.d("dt", steemConnect.getLoginUrl(true));
//        } catch (SteemConnectException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FILES_REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(MainActivity.this,UploadActivity.class), REQUEST_CODE_UPLOAD);
                }
            }
        }
    }

    public void onItemClick(int pos){
        if (!activityPaused)
            steemWebView.getVideoInfo(videos.get(pos).user, videos.get(pos).permlink, DtubeAPI.getAccountName(this));
    }

    public void onItemLongClick(final int pos){
        if (selectedTab == DtubeAPI.CAT_HISTORY) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.remove_history);
            builder.setPositiveButton(android.R.string.yes, (dialog, id) -> {
                String permLink = videos.get(pos).permlink;
                Video.removeVideoFromRecents(permLink, MainActivity.this);
                Video videoToRemove = allVideos.findVideo(permLink, DtubeAPI.CAT_HISTORY);
                allVideos.remove(videoToRemove);
                initFeed();
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void playVideo(Video v){
        if (!activityPaused) {
            Intent videoPlayIntent = new Intent(MainActivity.this, VideoPlayActivity.class);
            videoPlayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle videoBundle = new Bundle();
            videoBundle.putSerializable("video", v);
            videoPlayIntent.putExtra("video", videoBundle);

            if (accountInfo != null)
                videoPlayIntent.putExtra("clientprofileimage", accountInfo.getImageURL());

            startActivityForResult(videoPlayIntent, REQUEST_CODE_PLAY_VIDEO);
        }
    }

    public boolean addVideos(VideoArrayList videos){
        Log.d("dtube4", "adding videos");
        if (swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);

        if (videos == null)
            return false;

        if (videos.size() == 0)
            return false;


        if (allVideos.hasNewContent(videos)) {

            for (Video videoToAdd: videos){
                if (!allVideos.containsVideo(videoToAdd)){
                    if (videoToAdd.categoryId == DtubeAPI.CAT_HISTORY)
                        allVideos.add(0,videoToAdd);
                    else
                        allVideos.add(videoToAdd);
                }
            }
            return true;
        }

        return false;
    }

//    public boolean setAllVideos(VideoArrayList videos){
//
//        if (videos == null)
//            return false;
//
//        if (videos.size()<1)
//            return false;
//
//        if (allVideos.hasNewContent(videos)) {
//            if (videos.size()>=allVideos.size()) {
//                if (videos.getCategorizedVideos(selectedTab).size()>0) {
//                    allVideos = (VideoArrayList) videos.clone();
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_PLAY_VIDEO:
                addVideos(Video.getRecentVideos(this));
                if (accountInfo!=null)
                    steemWebView.getSubscriptions(accountInfo.userName);
                if (accountInfo!=null)
                    steemWebView.getSubscriptionFeed(accountInfo.userName);

                //load any new videos
                steemWebView.getNewVideosFeed();
                break;

            case REQUEST_CODE_LOGIN:
                setProfileInfoUI();

                if (accountInfo!=null)
                    steemWebView.getSubscriptionFeed(accountInfo.userName);
                getInitialFeeds();
                initFeed();
                break;
            case REQUEST_CODE_PROFILE:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    if (v!=null)
                        steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
                break;
            case REQUEST_CODE_SEARCH:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    if (v!=null)
                        steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
                break;
            case REQUEST_CODE_UPLOAD:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    if (v!=null)
                        steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
                break;
            case REQUEST_CODE_SETTINGS:
                if (resultCode == RESULT_OK){
                    if (data.getBooleanExtra("logout",false)) {
                        finish();
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                    }


                }
                break;
            default:
                break;

        }
    }

    private void getInitialFeeds() {
        Log.d("dtube9","getInitialFeeds()");
        steemWebView.getHotVideosFeed();
        steemWebView.getTrendingVideosFeed();
        steemWebView.getNewVideosFeed();
    }


    @SuppressLint("SetTextI18n")
    public void setSubscribers(String account, final int subscribers){
        if (account.equals(accountInfo.userName)){
            runOnUiThread(() -> ((TextView)navigationHeader.findViewById(R.id.header_status)).setText(subscribers+" "+getResources().getString(R.string.subscribers)));
        }
    }

    final List<CustomMenuTarget> targets = new ArrayList<>();
    public void setSubscriptions(final ArrayList<Person> persons){

        runOnUiThread(() -> {
            Menu m = navigationView.getMenu();
            m.removeItem(R.id.subscriptions_id);
            SubMenu topChannelMenu = m.addSubMenu(0,R.id.subscriptions_id,0,getResources().getString(R.string.subscriptions)+" ("+persons.size()+")");
            for (int i = 0; i< persons.size(); i++){
                topChannelMenu.add(0,R.id.subscription_id,0,persons.get(i).userName);

                CustomMenuTarget target = new CustomMenuTarget(topChannelMenu.getItem(i),MainActivity.this, targets);
                targets.add(target);
                Picasso.get().load(DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username",persons.get(i).userName))
                        .into(target);
            }
        });

    }


    private void setProfileInfoUI(){

        String accountName = DtubeAPI.getAccountName(this);
        if (accountName!=null){
            accountInfo = new Person();
            accountInfo.userName = accountName;

            steemWebView.login(DtubeAPI.getAccountName(MainActivity.this),DtubeAPI.getUserPrivateKey(MainActivity.this),false, false);
        }

        if (accountInfo!=null) {

            Transformation transformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.get().load(accountInfo.getImageURL()).placeholder(R.drawable.login).transform(transformation).into(
                    ((ImageView) findViewById(R.id.profile_image)));

            Picasso.get().load(DtubeAPI.PROFILE_IMAGE_MEDIUM_URL.replace("username",accountInfo.userName)).placeholder(R.drawable.login).transform(transformation).into(
                    (ImageView)navigationHeader.findViewById(R.id.header_icon));

            ((TextView)navigationHeader.findViewById(R.id.header_name)).setText(accountInfo.userName);
            ((TextView)navigationHeader.findViewById(R.id.header_status)).setText("");
            navigationHeader.findViewById(R.id.header_login_iv).setVisibility(View.GONE);

            steemWebView.getSubscriberCount(accountInfo.userName);
            steemWebView.getSubscriptions(accountInfo.userName);
        }

    }

    public void loginButtonClicked(View v){
        if (accountInfo!=null && accountInfo.userName!=null) {
            Intent i = new Intent(MainActivity.this, ChannelActivity.class);
            i.putExtra("username", accountInfo.userName);
            i.putExtra("userurl", "/#!/c/" + accountInfo.userName);
            startActivityForResult(i, REQUEST_CODE_PROFILE);
        }else {
            startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), REQUEST_CODE_LOGIN);
        }
    }

    @SuppressLint("InflateParams")
    public void initFeed(){

        new Thread(() -> {
            videos = allVideos.getCategorizedVideos(selectedTab);

            class SortVideos implements Comparator<Video>
            {
                // Used for sorting in ascending order of
                // roll number
                public int compare(Video a, Video b)
                {
                    return Long.compare(b.getDate(), a.getDate());
                }
            }

            if (selectedTab == DtubeAPI.CAT_SUBSCRIBED || selectedTab == DtubeAPI.CAT_NEW) {
                synchronized (videos) {
                    Collections.sort(videos, new SortVideos());
                }
            }

            Log.d("dtube","UI: initFeed " + videos.size());

            MainActivity.this.runOnUiThread(() -> {
                feedAdapter.setVideos(videos);
                feedAdapter.notifyDataSetChanged();
                if (videos.size()>0)
                Log.d("dtube4","added vids ending with "+videos.get(videos.size()-1).title);
            });
        }).start();

        //Show button to login if user clicked on subscription feed and not logged in
        if (selectedTab == DtubeAPI.CAT_SUBSCRIBED) {
            if (accountInfo == null) {
                if (findViewById(R.id.login_for_subs) == null) {
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.gravity = Gravity.CENTER;
                    Button v = (Button)LayoutInflater.from(this).inflate(R.layout.login_for_subs_btn, null);
                    if (Preferences.darkMode)
                        v.setTextColor(Color.WHITE);
                    v.setOnClickListener(view -> loginButtonClicked(null));
                    mainFrame.addView(v, lp);
                    recyclerView.setFocusable(false);
                }
            }else if (findViewById(R.id.login_for_subs) != null) {
                mainFrame.removeView(findViewById(R.id.login_for_subs));
                recyclerView.setFocusable(true);
            }
        }else if (findViewById(R.id.login_for_subs) != null) {
            mainFrame.removeView(findViewById(R.id.login_for_subs));
            recyclerView.setFocusable(true);
        }

        gettingMoreVideos = false;
        updateBottomBar();
    }

    public void updateBottomBar(){
            for (int i = 0; i < 5; i++) {
                if (Preferences.darkMode)
                    ((ImageView) bottomBar.getChildAt(i)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                else
                    ((ImageView) bottomBar.getChildAt(i)).setColorFilter(null);
            }
            ((ImageView) bottomBar.getChildAt(selectedTab)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
    }


    public void expandToolbar(){
        toolbar.postDelayed(() -> {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.getLayoutParams().height = (int)getResources().getDimension(R.dimen.toolbar_size);
            toolbar.requestLayout();
        },200);

    }


    public void checkMenuItem(int rID){
        navigationView.getMenu().findItem(R.id.menu_subscribed).setChecked(false);
        navigationView.getMenu().findItem(R.id.menu_hot).setChecked(false);
        navigationView.getMenu().findItem(R.id.menu_trending).setChecked(false);
        navigationView.getMenu().findItem(R.id.menu_new).setChecked(false);
        navigationView.getMenu().findItem(R.id.menu_history).setChecked(false);

        navigationView.getMenu().findItem(rID).setChecked(true);
    }

    public void removeRecentClicked(View v){
        String permLink = v.getTag().toString();
        Video.removeVideoFromRecents(permLink, this);
        Video videoToRemove = allVideos.findVideo(permLink, DtubeAPI.CAT_HISTORY);
        allVideos.remove(videoToRemove);
        initFeed();
    }

    public void tabGoToHotClicked(View v){
        selectedTab = DtubeAPI.CAT_HOT;
        initFeed();
        checkMenuItem(R.id.menu_hot);
        scrollToTop();
        expandToolbar();
    }

    public void tabGoToTrendingClicked(View v){
        selectedTab = DtubeAPI.CAT_TRENDING;
        initFeed();
        checkMenuItem(R.id.menu_trending);
        scrollToTop();
        expandToolbar();
    }

    public void tabGoToNewClicked(View v){
        selectedTab = DtubeAPI.CAT_NEW;
        initFeed();
        checkMenuItem(R.id.menu_new);
        scrollToTop();
        expandToolbar();
    }

    public void tabGoToHistoryClicked(View v){
        selectedTab = DtubeAPI.CAT_HISTORY;
        initFeed();
        checkMenuItem(R.id.menu_history);
        scrollToTop();
        expandToolbar();
    }

    public void tabGoToSubscribedClicked(View v){
        selectedTab = DtubeAPI.CAT_SUBSCRIBED;
        initFeed();
        checkMenuItem(R.id.menu_subscribed);
        scrollToTop();
        expandToolbar();
    }

    public void scrollToTop(){
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(0);
            }
        },100);
    }

    public void goToTab(int tab){
        if (tab>4)
            tab =0;
        if (tab<0)
            tab =4;

        if (swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(false);

        switch (tab){
            case DtubeAPI.CAT_HISTORY:
                tabGoToHistoryClicked(null);
                break;
            case DtubeAPI.CAT_HOT:
                tabGoToHotClicked(null);
                break;
            case DtubeAPI.CAT_NEW:
                tabGoToNewClicked(null);
                break;
            case DtubeAPI.CAT_SUBSCRIBED:
                tabGoToSubscribedClicked(null);
                break;
            case DtubeAPI.CAT_TRENDING:
                tabGoToTrendingClicked(null);
                break;
        }

        if (feedAdapter.getItemCount()>0)
            recyclerView.requestFocus();


    }

    public void searchButtonClicked(View v){
        startActivityForResult(new Intent(MainActivity.this,SearchActivity.class), REQUEST_CODE_SEARCH);
    }

    public void addUpdateMenu(){
        Menu m = navigationView.getMenu();
        m.findItem(R.id.menu_update).setVisible(true);
    }

    public void updateCheck(){boolean updateAvailable = getPreferences(MODE_PRIVATE).getString(BuildConfig.VERSION_NAME, null)!=null;
        if (updateAvailable)
            addUpdateMenu();
        else{
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(final Update update, Boolean isUpdateAvailable) {
                            Log.d("Latest Version", update.getLatestVersion());
                            Log.d("Latest Version Code", "" + update.getLatestVersionCode());
                            Log.d("Release notes", update.getReleaseNotes());
                            Log.d("URL", "" + update.getUrlToDownload());
                            Log.d("Is update available?", Boolean.toString(isUpdateAvailable));

                            if (isUpdateAvailable) {
                                //set that an update is available for this version
                                addUpdateMenu();
                                getPreferences(MODE_PRIVATE).edit().putString(BuildConfig.VERSION_NAME,update.getUrlToDownload().toString()).apply();

                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle(R.string.update_available);
                                alertDialog.setMessage(getResources().getString(R.string.update_summary));
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.update_now), (dialog, id) -> {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(update.getUrlToDownload().toString()));
                                    startActivity(browserIntent);
                                });

                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.later), (dialog, id) -> {});


                                alertDialog.show();
                            }

                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {
                            Log.d("AppUpdater Error", "Something went wrong");
                        }
                    });
            appUpdaterUtils.setUpdateFrom(UpdateFrom.XML);
            appUpdaterUtils.setUpdateXML("https://raw.githubusercontent.com/powerpoint45/dtube-mobile-unofficial/master/app/AutoUpdate.xml");
            appUpdaterUtils.start();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (runningOnTV) {
            recyclerView.setLayoutManager(layoutManager = new GridLayoutManager(recyclerView.getContext(), 2, GridLayoutManager.HORIZONTAL, false));
        }else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(layoutManager = new GridLayoutManager(this, 2));
        }else
            recyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(this));

        //remove cached recycled views because feed_item layout needs to change for orientation
        recyclerView.getRecycledViewPool().clear();
        if (feedAdapter!=null)
            feedAdapter.notifyDataSetChanged();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:

                if (!runningOnTV) {
                    if (!drawerLayout.isDrawerOpen(navigationView)) {

                        int feedItem = -1;
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            feedItem = -2;
                        else if (feedAdapter.getItemCount() != 0) {
                            try {
                                if (getCurrentFocus() != null)
                                    feedItem = Integer.parseInt(getCurrentFocus().getTag().toString());
                            } catch (Exception ignored) {
                            }
                        } else
                            feedItem = -2;

                        if (getCurrentFocus() != null && getCurrentFocus().getId() == R.id.search_btn) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                drawerLayout.openDrawer(navigationView);
                                if (drawerLayout.findViewById(R.id.header_icon) != null)
                                    drawerLayout.findViewById(R.id.header_icon).requestFocus();
                                else
                                    navigationView.requestFocus();
                            }
                        }

                        if (feedItem != -1 || (getCurrentFocus() != null && getCurrentFocus().getId() == R.id.search_btn)) {
                            //pressing left on the very left item
                            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && ((feedItem % 2 == 0) || feedItem == -2)) {
                                if (selectedTab == 0) {
                                    drawerLayout.openDrawer(navigationView);
                                    if (drawerLayout.findViewById(R.id.header_icon) != null)
                                        drawerLayout.findViewById(R.id.header_icon).requestFocus();
                                    else
                                        navigationView.requestFocus();

                                } else
                                    goToTab(selectedTab - 1);

                            }

                            //pressing right on the very right item
                            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && ((feedItem % 2 != 0) || feedItem == -2)) {
                                if (selectedTab == 4)
                                    goToTab(0);
                                else
                                    goToTab(selectedTab + 1);

                            }


                            Log.d("dtube", feedItem % 2 == 0 ? "left item" : "right item");
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                        drawerLayout.closeDrawers();
                }else {
                    //TV MODE INPUT
                    if (!drawerLayout.isDrawerOpen(navigationView)) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            if (getCurrentFocus() != null &&
                                    (getCurrentFocus().getId() == R.id.search_btn
                                            || getCurrentFocus().getId() == R.id.tab_history)
                                    || getCurrentFocus().getId() == R.id.tab_hot
                                    || getCurrentFocus().getId() == R.id.tab_new
                                    || getCurrentFocus().getId() == R.id.tab_subscribed
                                    || getCurrentFocus().getId() == R.id.tab_trending) {

                                drawerLayout.openDrawer(navigationView);
                                if (drawerLayout.findViewById(R.id.header_icon) != null)
                                    drawerLayout.findViewById(R.id.header_icon).requestFocus();
                                else
                                    navigationView.requestFocus();
                            }
                        }
                    }else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        drawerLayout.closeDrawers();
                    }
                }
                break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (!drawerLayout.isDrawerOpen(navigationView)) {
                        if (getCurrentFocus() != null && (getCurrentFocus().getId() == R.id.search_btn
                                || getCurrentFocus().getId() == R.id.profile_image)
                                && !drawerLayout.isDrawerOpen(navigationView) && !recyclerView.isFocused()) {
                            if (getCurrentFocus().getId() == R.id.search_btn) {
                                findViewById(R.id.tab_subscribed).requestFocus();
                                return true;
                            }else
                                recyclerView.requestFocus();
                            Log.d("dtube", "focusing");
                        }

                        if (runningOnTV && feedAdapter.getFocusedItem() % 2 != 0
                                && getCurrentFocus()!=null &&getCurrentFocus().getId()==R.id.feed_item) {
                            goToTab(selectedTab + 1);
                        }
                    }
                    return false;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!drawerLayout.isDrawerOpen(navigationView)) {
                    if (selectedTab != 0 && runningOnTV && feedAdapter.getFocusedItem() % 2 == 0
                            && getCurrentFocus()!=null &&getCurrentFocus().getId()==R.id.feed_item) {
                        goToTab(selectedTab - 1);
                        return true;
                    }
                }
                return false;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView))
            drawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }


    @Override
    protected void onPause() {
        super.onPause();
        activityPaused = true;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onResume(){
        super.onResume();
        activityPaused = false;

        if (changedDarkMode){
            changedDarkMode = false;
            finish();
            startActivity(new Intent(MainActivity.this,MainActivity.class));
        }
    }


}
