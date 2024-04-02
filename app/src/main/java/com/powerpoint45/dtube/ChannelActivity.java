package com.powerpoint45.dtube;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    String lastAuthor = "";
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

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        String subscribers = getIntent().getExtras().getString("subscribers");
        channelName = getIntent().getExtras().getString("username");
        String profileURL =  getIntent().getExtras().getString("userurl");
        String profileImageURL = getIntent().getExtras().getString("profileimage");

        Picasso.get().load(DtubeAPI.getProfileImage(channelName)).placeholder(R.drawable.ic_account_circle).transform(new RoundedTransformationBuilder()
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
                        if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_AVALON))
                            steemitWebView.getChannelVideos(channelName, lastPermlink, lastAuthor, accountName);
                        else
                            steemitWebView.getChannelVideos2(channelName, lastPermlink, accountName);
                    }
                }

            }
        });

        ((TextView)findViewById(R.id.item_user)).setText(getResources().getString(R.string.videos_by)+" " + channelName);

        steemitWebView = new SteemitWebView(this, null);
        if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_AVALON))
            steemitWebView.getChannelVideos(channelName, lastPermlink, lastAuthor, accountName);
        else
            steemitWebView.getChannelVideos2(channelName, lastPermlink, accountName);

        gettingMoreVideos = true;

        accountName = DtubeAPI.getAccountName(this);
        if (accountName!=null && accountName.equals(channelName)){
            subscribeButton.setEnabled(false);
        }else if(accountName!=null)
            steemitWebView.getIsFollowing(channelName, accountName);
        steemitWebView.getSubscriberCount(channelName);
        steemitWebView.getBalance(channelName);
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

    public void addVideos(VideoArrayList videos, String lastPermlink, String lastAuthor){
        if (!gotAllItems) {
            if (videos == null && lastPermlink.equals("last")) {
                gotAllItems = true;
            }else {
                if (this.videos == null)
                    this.videos = new VideoArrayList();

                this.lastPermlink = lastPermlink;
                this.lastAuthor = lastAuthor;

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

    public void updateBalances(String accountName, String balance, String dollarBalance, String votingPower){
        //verify callback is for correct account
        if (this.channelName.equals(accountName)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.balance_text)).setText(balance);
                    ((TextView)findViewById(R.id.balance_dollar_text)).setText(dollarBalance);
                    ((TextView)findViewById(R.id.voting_power_text)).setText(votingPower);


                    if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_HIVE)){
                        ((AppCompatImageView)findViewById(R.id.balance_image)).setImageResource(R.drawable.balancehive);
                        ((AppCompatImageView)findViewById(R.id.balance_dollar_image)).setImageResource(R.drawable.balancehbd);
                        ((AppCompatImageView)findViewById(R.id.voting_power_image)).setImageResource(R.drawable.votingpower);
                    }else if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_STEEM)){
                        ((AppCompatImageView)findViewById(R.id.balance_image)).setImageResource(R.drawable.balance_steem);
                        ((AppCompatImageView)findViewById(R.id.balance_dollar_image)).setImageResource(R.drawable.balance_steem_dollar);
                        ((AppCompatImageView)findViewById(R.id.voting_power_image)).setImageResource(R.drawable.votingpower);
                    } else if (Preferences.selectedAPI.equals(DtubeAPI.PROVIDER_API_URL_AVALON)) {
                        ((AppCompatImageView)findViewById(R.id.balance_image)).setImageResource(R.drawable.balanceavalon);
                        ((AppCompatImageView)findViewById(R.id.balance_dollar_image)).setImageResource(R.drawable.votingpower);
                        ((AppCompatImageView)findViewById(R.id.voting_power_image)).setImageResource(R.drawable.votingpower);
                    }
                }
            });


        }
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
            steemitWebView.unfollowUser(channelName, accountName, DtubeAPI.getUserPrivateKey(this, DtubeAPI.getNetworkNumber(steemitWebView.api)));
        else
            steemitWebView.followUser(channelName, accountName, DtubeAPI.getUserPrivateKey(this, DtubeAPI.getNetworkNumber(steemitWebView.api)));
    }

}
