package com.powerpoint45.dtube;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
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

    boolean activityPaused;

    VideoArrayList allVideos = new VideoArrayList();
    VideoArrayList videos = new VideoArrayList();
    int selectedTab;

    final int REQUEST_CODE_PLAY_VIDEO = 0;
    final int REQUEST_CODE_LOGIN = 1;
    final int REQUEST_CODE_PROFILE = 2;
    final int REQUEST_CODE_SEARCH = 3;

    RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    LinearLayout bottomBar;

    SteemitWebView steemWebView;
    Toolbar toolbar;

    Person accountInfo;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
                    case R.id.menu_about:
                        Intent aboutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://steemit.com/utopian-io/@immawake/introducing-the-dtube-mobile-app-unofficial-android-app"));
                        startActivity(aboutIntent);
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
            }
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

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            drawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.colorAccent));
        } else {
            drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.colorAccent));
        }

        steemWebView = new SteemitWebView(this);

        bottomBar = (LinearLayout)findViewById(R.id.bottom_bar);
        toolbar = (Toolbar)findViewById(R.id.toolbar);

        recyclerView = ((RecyclerView) findViewById(R.id.feed_rv));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Animate toolbar when scrolling feed
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (toolbar.getHeight()-dy<=0){
                    if (toolbar.getVisibility()==View.VISIBLE)
                        toolbar.setVisibility(View.GONE);
                    if (toolbar.getHeight()!=0)
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0));
                }else {
                    if (toolbar.getVisibility() == View.GONE)
                        toolbar.setVisibility(View.VISIBLE);

                    if (toolbar.getHeight()-dy>getResources().getDimension(R.dimen.toolbar_size))
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.toolbar_size)));
                    else
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbar.getHeight() - dy));
                }

            }
        });

        feedAdapter = new FeedAdapter(this);
        recyclerView.setAdapter(feedAdapter);

        updateBottomBar();
        tabGoToHotClicked(recyclerView);
        setProfileInfoUI();

        if (accountInfo!=null)
            steemWebView.getSubscriptionFeed(accountInfo.userName);
        steemWebView.getHotVideosFeed();
        steemWebView.getTrendingVideosFeed();
        steemWebView.getNewVideosFeed();

        addVideos(Video.getRecentVideos(this));

        Menu m = navigationView.getMenu();
        m.findItem(R.id.menu_update).setVisible(false);

        updateCheck();
    }

    public void onItemClick(int pos){
        if (!activityPaused)
            steemWebView.getVideoInfo(videos.get(pos).user, videos.get(pos).permlink, DtubeAPI.getAccountName(this));
    }


    public void playVideo(Video v){
        if (!activityPaused) {
            Intent videoPlayIntent = new Intent(MainActivity.this, VideoPlayActivity.class);
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

    public boolean setAllVideos(VideoArrayList videos){

        if (videos == null)
            return false;

        if (videos.size()<1)
            return false;

        if (allVideos.hasNewContent(videos)) {
            if (videos.size()>=allVideos.size()) {
                if (videos.getCategorizedVideos(selectedTab).size()>0) {
                    allVideos = (VideoArrayList) videos.clone();
                    return true;
                }
            }
        }
        return false;
    }


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
                steemWebView.getHotVideosFeed();
                steemWebView.getTrendingVideosFeed();
                steemWebView.getNewVideosFeed();
                break;

            case REQUEST_CODE_LOGIN:
                setProfileInfoUI();

                if (accountInfo!=null)
                    steemWebView.getSubscriptionFeed(accountInfo.userName);
                steemWebView.getHotVideosFeed();
                steemWebView.getTrendingVideosFeed();
                steemWebView.getNewVideosFeed();
                break;
            case REQUEST_CODE_PROFILE:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
                break;
            case REQUEST_CODE_SEARCH:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
                break;
            default:
                break;

        }
    }


    public void setSubscribers(String account, final int subscribers){
        if (account.equals(accountInfo.userName)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)navigationHeader.findViewById(R.id.header_status)).setText(subscribers+" "+getResources().getString(R.string.subscribers));
                }
            });
        }
    }

    final List<CustomMenuTarget> targets = new ArrayList<CustomMenuTarget>();
    public void setSubscriptions(final ArrayList<Person> persons){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Menu m = navigationView.getMenu();
                m.removeItem(R.id.subscriptions_id);
                SubMenu topChannelMenu = m.addSubMenu(0,R.id.subscriptions_id,0,getResources().getString(R.string.subscriptions)+" ("+persons.size()+")");
                for (int i = 0; i< persons.size(); i++){
                    topChannelMenu.add(0,R.id.subscription_id,0,persons.get(i).userName);

                    CustomMenuTarget target = new CustomMenuTarget(topChannelMenu.getItem(i),MainActivity.this, targets);
                    targets.add(target);
                    Picasso.with(MainActivity.this).load(DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username",persons.get(i).userName))
                            .into(target);
                }
            }
        });

    }


    private void setProfileInfoUI(){

        String accountName = DtubeAPI.getAccountName(this);
        if (accountName!=null){
            accountInfo = new Person();
            accountInfo.userName = accountName;

            steemWebView.login(DtubeAPI.getAccountName(MainActivity.this),DtubeAPI.getUserPrivateKey(MainActivity.this));
        }

        if (accountInfo!=null) {

            Transformation transformation = new RoundedTransformationBuilder()
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.with(this).load(accountInfo.getImageURL()).placeholder(R.drawable.login).transform(transformation).into(
                    ((ImageView) findViewById(R.id.profile_image)));

            Picasso.with(this).load(DtubeAPI.PROFILE_IMAGE_MEDIUM_URL.replace("username",accountInfo.userName)).placeholder(R.drawable.login).transform(transformation).into(
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

    public void initFeed(){
        Log.d("dtube","UI:initFeed");
        new Thread(new Runnable() {
            @Override
            public void run() {
                videos = allVideos.getCategorizedVideos(selectedTab);
                class SortVideos implements Comparator<Video>
                {
                    // Used for sorting in ascending order of
                    // roll number
                    public int compare(Video a, Video b)
                    {
                        if (a.getDate() > b.getDate()) {
                            return  -1;
                        } else {
                            return 1;
                        }
                    }
                }
                Collections.sort(videos, new SortVideos());
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        feedAdapter.setVideos(videos);
                        feedAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();


        updateBottomBar();
    }

    public void updateBottomBar(){
        if (((ImageView)bottomBar.getChildAt(selectedTab)).getColorFilter()==null) {
            for (int i = 0; i < 5; i++) {
                ((ImageView) bottomBar.getChildAt(i)).setColorFilter(null);
            }
            ((ImageView) bottomBar.getChildAt(selectedTab)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }
    }


    public void expandToolbar(){
        toolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                toolbar.getLayoutParams().height = (int)getResources().getDimension(R.dimen.toolbar_size);
                toolbar.requestLayout();
            }
        },100);

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
        recyclerView.smoothScrollToPosition(0);
        expandToolbar();
    }

    public void tabGoToTrendingClicked(View v){
        selectedTab = DtubeAPI.CAT_TRENDING;
        initFeed();
        checkMenuItem(R.id.menu_trending);
        recyclerView.smoothScrollToPosition(0);
        expandToolbar();
    }

    public void tabGoToNewClicked(View v){
        selectedTab = DtubeAPI.CAT_NEW;
        initFeed();
        checkMenuItem(R.id.menu_new);
        recyclerView.smoothScrollToPosition(0);
        expandToolbar();
    }

    public void tabGoToHistoryClicked(View v){
        selectedTab = DtubeAPI.CAT_HISTORY;
        initFeed();
        checkMenuItem(R.id.menu_history);
        recyclerView.smoothScrollToPosition(0);
        expandToolbar();
    }

    public void tabGoToSubscribedClicked(View v){
        selectedTab = DtubeAPI.CAT_SUBSCRIBED;
        initFeed();
        checkMenuItem(R.id.menu_subscribed);
        recyclerView.smoothScrollToPosition(0);
        expandToolbar();
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
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.update_now), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(update.getUrlToDownload().toString()));
                                        startActivity(browserIntent);
                                    }
                                });

                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.later), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {}});


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
    }
}
