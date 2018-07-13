package com.powerpoint45.dtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;

/**
 * Created by michael on 13/11/17.
 */

public class ChannelActivity extends AppCompatActivity {

    ChannelAdapter adapter;
    SteemitWebView steemitWebView;
    Toolbar toolbar;
    RecyclerView recyclerView;
    VideoArrayList videos;
    boolean gotAllItems = false;
    boolean gettingMoreVideos = false;
    String lastPermlink = "";
    LinearLayoutManager layoutManager;
    String channelName;
    String accountName;
    Button subscribeButton;
    View subscribeLoader;

    boolean subscribed;
    boolean restrictClicks;
    //number of off the screen until app gets more videos
    final int VIDEO_LIMIT = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        String subscribers = getIntent().getExtras().getString("subscribers");
        channelName = getIntent().getExtras().getString("username");
        String profileURL =  getIntent().getExtras().getString("userurl");
        String profileImageURL = getIntent().getExtras().getString("profileimage");


        Picasso.with(this).load(DtubeAPI.PROFILE_IMAGE_MEDIUM_URL.replace("username",channelName)).placeholder(R.drawable.ic_account_circle).transform(new RoundedTransformationBuilder()
                .cornerRadiusDp(50)
                .oval(false)
                .build())
                .into(((ImageView) findViewById(R.id.item_profileimage)));


        subscribeLoader = findViewById(R.id.subscribe_loader);
        subscribeButton = findViewById(R.id.item_subscribe);
        layoutManager = new LinearLayoutManager(this);
        toolbar = findViewById(R.id.toolbar_small);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setTitle(channelName);

        toolbar = findViewById(R.id.toolbar);


        recyclerView = ((RecyclerView) findViewById(R.id.channel_video_list));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        //Animate toolbar when scrolling feed
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (toolbar.getHeight()-dy<=(int)getResources().getDimension(R.dimen.toolbar_size)){
                    if (toolbar.getHeight()!=(int)getResources().getDimension(R.dimen.toolbar_size))
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)getResources().getDimension(R.dimen.toolbar_size)));
                }else {

                    if (toolbar.getHeight()-dy>getResources().getDimension(R.dimen.toolbar_size_mixed))
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getResources().getDimension(R.dimen.toolbar_size_mixed)));
                    else
                        toolbar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbar.getHeight() - dy));
                }


                if (!gotAllItems && !gettingMoreVideos) {
                    if (videos!=null && videos.size() - layoutManager.findLastVisibleItemPosition() < VIDEO_LIMIT) {
                        gettingMoreVideos = true;
                        steemitWebView.getChannelVideos(channelName, lastPermlink);
                    }
                }

            }
        });

        ((TextView)findViewById(R.id.item_user)).setText(getResources().getString(R.string.videos_by)+" " + channelName);

        steemitWebView = new SteemitWebView(this);
        steemitWebView.getChannelVideos(channelName, lastPermlink);
        gettingMoreVideos = true;

        accountName = DtubeAPI.getAccountName(this);
        if (accountName!=null && accountName.equals(channelName)){
            subscribeButton.setEnabled(false);
        }else if(accountName!=null)
            steemitWebView.getIsFollowing(channelName, accountName);
        steemitWebView.getSubscriberCount(channelName);
    }

    public void setIsFollowing(boolean b){
        subscribed = b;

        subscribeLoader.setVisibility(View.GONE);
        findViewById(R.id.item_subscribe).setClickable(true);

        if (accountName!=null)
            restrictClicks = false;

        if (subscribed){
            ((Button)findViewById(R.id.item_subscribe)).setText(R.string.unsubscribe);
        }else
            ((Button)findViewById(R.id.item_subscribe)).setText(R.string.subscribe);
    }

    public void setNumberOfSubscribers(int count){
        ((TextView)findViewById(R.id.subscribers)).setText(""+count);
    }

    public void addVideos(VideoArrayList videos, String lastPermlink){
        if (!gotAllItems) {
            if (videos == null && lastPermlink.equals("last")) {
                gotAllItems = true;
            }else {
                if (this.videos == null)
                    this.videos = new VideoArrayList();

                this.lastPermlink = lastPermlink;
                assert videos != null;
                this.videos.addAll(videos);
                gettingMoreVideos = false;
                if (adapter == null) {
                    adapter = new ChannelAdapter(this.videos, this);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.setVideos(this.videos);
                }
            }
        }
    }

    public void setVideos(VideoArrayList videos){
        this.videos = videos;
        adapter = new ChannelAdapter(videos, this);
        recyclerView.setAdapter(adapter);
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
        subscribeLoader.setVisibility(View.VISIBLE);
        findViewById(R.id.item_subscribe).setClickable(false);

        if (subscribed)
            steemitWebView.unfollowUser(channelName, accountName, DtubeAPI.getUserPrivateKey(this));
        else
            steemitWebView.followUser(channelName, accountName, DtubeAPI.getUserPrivateKey(this));
    }

}
