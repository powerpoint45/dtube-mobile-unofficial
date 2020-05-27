package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


/**
 * Created by michael on 5/11/17.
 */


public class VideoPlayActivity extends AppCompatActivity {
    static final int REQUEST_CHANNEL = 0;
    static final int REQUEST_UPGRADE = 1;

    FrameLayout videoLayoutHolder;

    SteemitWebView steemWebView;
    EditText replyBox;
    EditText commentReplyBox;
    TextView descriptionBox;
    View subscribeLoader;
    View likedislikeLoader;

    ListView commentsListView;
    ListView suggestedVideosListView;

    VideoArrayList suggestedVideos;
    Video videoToPlay;

    boolean restrictClicks = true;
    boolean subscribed;
    String subscribers;
    String profileImageURL;
    String clientProfileImageURL;
    CommentsList commentsList = null;
    CommentsAdapter commentsAdapter;
    SuggestionAdapter suggestionAdapter;
    Transformation transformation;

    boolean setFullDescription;
    String accountName;

    boolean runningOnTV;
    boolean fullscreen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);

        Log.d("dtube2", "onCreate");

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;

        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);

        //Customize layout if in TV Mode
        if (uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION) {
            setContentView(R.layout.activity_videoplay_tv);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            findViewById(R.id.undervideo_padding).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    height));
            runningOnTV = true;
            if (Preferences.darkMode)
                findViewById(R.id.contents_bg).setBackgroundColor(getResources().getColor(R.color.transparentBlack));

        }else {
            setContentView(R.layout.activity_videoplay);

            //set height of comments section
            findViewById(R.id.undervideo_contents).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom!=oldBottom)
                    findViewById(R.id.comments_lv).setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                            ,bottom-top-(int)Tools.dptopx(75)));
            });
        }

        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        commentsListView = findViewById(R.id.comments_lv);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Bundle videoBundle = getIntent().getBundleExtra("video");
        videoToPlay = ((Video)videoBundle.getSerializable("video"));
        clientProfileImageURL = getIntent().getStringExtra("clientprofileimage");
        accountName = DtubeAPI.getAccountName(this);

        steemWebView = new SteemitWebView(this);
        steemWebView.getReplies(videoToPlay.user, videoToPlay.permlink, accountName);
        if (accountName!=null)
            steemWebView.getIsFollowing(videoToPlay.user, accountName);
        steemWebView.getSuggestedVideos(videoToPlay.user);
        steemWebView.getSubscriberCount(videoToPlay.user);

        subscribeLoader = findViewById(R.id.subscribe_loader);
        likedislikeLoader = findViewById(R.id.likedislike_loader);
        replyBox = findViewById(R.id.item_comment_edittext);
        videoLayoutHolder = findViewById(R.id.player_holder);
        descriptionBox = findViewById(R.id.item_description);
        descriptionBox.setBackgroundColor(Color.TRANSPARENT);

        transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        if (clientProfileImageURL!=null) {
            Picasso.get().load(clientProfileImageURL).placeholder(R.drawable.login).transform(transformation)
                    .into(((ImageView) findViewById(R.id.item_account_comment_image)));
        }

        descriptionBox.setText(Tools.fromHtml(videoToPlay.longDescriptionHTML));

        //disable links if running on TV
        if (!runningOnTV)
            descriptionBox.setMovementMethod(LinkMovementMethod.getInstance());

        if (accountName!=null)
            replyBox.setOnEditorActionListener(editorActionListener);
        else
            replyBox.setText(R.string.login_comment);

        updateUI();
        setupVideoView();



    }

    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode){
            MediaPlayerSingleton.getInstance(this).removeControls();
        }else {
            if (onStopCalled) {
                finish();
            }else {
                MediaPlayerSingleton.getInstance(this).restoreControls();
                videoLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Tools.numtodp(250, this)));
                updateUI();
            }
        }
    }


    EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Log.d("d","KEY EVENT");
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Log.d("d","IME_ACTION_SEND");
                //replace ' in order to prevent syntax error when running javascript
                String comment = v.getText().toString().replaceAll("'","’");
                String permlink = null;
                String author = null;

                switch (v.getId()){
                    case R.id.item_comment_edittext:
                        permlink = videoToPlay.permlink;
                        author = videoToPlay.user;
                        break;

                    case R.id.item_comment_reply_edittext:
                        permlink = v.getTag().toString();
                        author = commentsList.getCommentByID(permlink).userName;
                        break;
                }

                if (permlink!=null && comment.length()>0){
                    steemWebView.commentPost(author, permlink, DtubeAPI.getAccountName(VideoPlayActivity.this),
                            DtubeAPI.getUserPrivateKey(VideoPlayActivity.this), comment, videoToPlay.permlink, videoToPlay.user);
                }

                v.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (commentReplyBox!=null) {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(commentReplyBox.getWindowToken(), 0);
                }
                else {
                    assert imm != null;
                    imm.hideSoftInputFromWindow(replyBox.getWindowToken(), 0);
                }

                v.clearFocus();

                //Only on TV
                if (findViewById(R.id.comment_et_holder)!=null)
                    findViewById(R.id.comment_et_holder).requestFocus();

                if (commentsAdapter!=null)
                    commentsAdapter.notifyDataSetChanged();
                // do your stuff here
            }
            return false;
        }
    };


    public void editTextBacked(){
        if (replyBox!=null)
            replyBox.clearFocus();
        if (commentReplyBox!=null)
            commentReplyBox.clearFocus();
        Log.d("dtube3","imei back");

        if (commentsAdapter!=null)
            commentsAdapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (commentReplyBox!=null) {
            assert imm != null;
            imm.hideSoftInputFromWindow(commentReplyBox.getWindowToken(), 0);
        }
        else {
            assert imm != null;
            imm.hideSoftInputFromWindow(replyBox.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed(){
        Log.d("dtube2", "onBackPressed");

        if (fullscreen) {
            makeFullscreen(null);
            return;
        }else if (!showPIPUpgrade()) {
            if (Preferences.hasUpgrade && Tools.deviceSupportsPIPMode(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    enterPictureInPictureMode();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public boolean showPIPUpgrade(){

        //check if upgrade compatible for Picture-in-picture upgrade
        if (!Preferences.hasUpgrade && Tools.deviceSupportsPIPMode(this)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            //dont show on first launch of app
            boolean firstLaunch = prefs.getBoolean("firstlaunch",true);
            if (!firstLaunch) {

                //Only show popup max once per day

                int dayLastShown = prefs.getInt("lastdayshownupgrade", -1);

                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                if (dayLastShown!= day){
                    prefs.edit().putInt("lastdayshownupgrade", day).apply();
                    Intent pipUpgradeIntent = new Intent(VideoPlayActivity.this,PictureInPictureUpgradeActivity.class);
                    startActivityForResult(pipUpgradeIntent, REQUEST_UPGRADE);
                    return true;
                }

            }else {
                prefs.edit().putBoolean("firstlaunch",false).apply();
            }
        }
        return false;

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("dtube2", "onPause");
        //MediaPlayerSingleton.getInstance(this).startPlayer();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
            // Continue playback
            Log.d("dtube","onPause:isInPictureInPictureMode");
        } else {
            // Use existing playback logic for paused Activity behavior.
            if (!isFinishing())
                MediaPlayerSingleton.getInstance(this).pausePlayer();
        }



//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
//            if (isApplicationSentToBackground(this)){
//                enterPictureInPictureMode();
//            }
//        }
    }


    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void finish() {
        super.finish();
        Log.d("dtube2", "finish");
        //MediaPlayerSingleton.getInstance(this).disattachFromParent();
        MediaPlayerSingleton.getInstance(this).release();


        steemWebView.killWebView();
        steemWebView = null;
    }

    boolean onStopCalled;

    @Override
    protected void onStop() {
        Log.d("dtube2", "onStop");
        super.onStop();
        onStopCalled = true;
    }

    @Override
    protected void onDestroy() {
        Log.d("dtube2", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("dtube2", "onResume");
        onStopCalled = false;

        MediaPlayerSingleton.getInstance(this).startPlayer();

        if (steemWebView!=null)
            steemWebView.resumeTimers();

        if (onNewIntentCalled){

            onNewIntentCalled = false;
            playVideo(videoToPlay);
        }

        if (enterPIPModeOnResume){
            enterPIPModeOnResume = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                enterPictureInPictureMode();
            }
        }
    }

    boolean onNewIntentCalled;
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        // set the string passed from the service to the original intent
        setIntent(intent);

        Bundle videoBundle = getIntent().getBundleExtra("video");
        videoToPlay = ((Video)videoBundle.getSerializable("video"));
        Log.d("dtube2", "onNewIntent "+videoToPlay.title);
        onNewIntentCalled = true;

    }

    public void setNumberOfSubscribers(int count){
        subscribers = ""+count;
        updateUI();
    }

    //called from proxy
//    public void setReplies(CommentsList comments){
//
//        if (commentsAdapter == null) {
//            commentsAdapter = new CommentsAdapter(commentsList, this, accountName != null);
//            ((ListView) findViewById(R.id.comments_lv)).setAdapter(commentsAdapter);
//
//        }
//
//        commentsList = comments;
//        commentsAdapter.setCommentsList(commentsList);
//    }

    //called from proxy
    public void addReply(Comment comment){

        if (commentsAdapter == null) {
            commentsAdapter = new CommentsAdapter(commentsList, this, accountName != null, runningOnTV);
            commentsListView.setAdapter(commentsAdapter);
        }

        if (commentsList == null)
            commentsList = new CommentsList();


        //comment is a root comment
        if (comment.parent.equals(videoToPlay.permlink)) {
            //ensure we are not adding duplicate comments
            if (commentsList.getCommentByID(comment.permlink)==null) {
                commentsList.add(comment);

                class SortComments implements Comparator<Comment> {
                    public int compare(Comment a, Comment b) {
                        if (a.likes > b.likes || a.getDate() > b.getDate()) {
                            return -1;
                        } else if (a.likes < b.likes || a.getDate() < b.getDate()) {
                            return 1;
                        } else
                            return 0;
                    }
                }

                try {
                    Collections.sort(commentsList, new SortComments());
                }catch (IllegalArgumentException ia){
                    ia.printStackTrace();
                }

            }
        }else {
            //coment is a nested comment
            Comment rootComment = commentsList.getCommentByID(comment.parent);
            if (rootComment.childComments==null)
                rootComment.childComments = new CommentsList();
            Log.d("DT","Added child "+comment.commentHTML);
            rootComment.childComments.add(comment);
        }



        commentsAdapter.setCommentsList(commentsList);


    }

    //called from proxy
    public void setIsFollowing(boolean following){
        subscribeLoader.setVisibility(View.GONE);
        subscribed = following;
        if (accountName!=null)
            restrictClicks = false;
        updateUI();
    }

    //called from proxy
    public void setSuggestedVideos(VideoArrayList videos){
        suggestedVideos = videos;
        Log.d("dtube4",videos.size()+" suggested");

        if (suggestionAdapter == null) {
            suggestionAdapter = new SuggestionAdapter(suggestedVideos, this);
            suggestedVideosListView = findViewById(R.id.suggestions_lv);
            suggestedVideosListView.setAdapter(suggestionAdapter);
        }else
            suggestionAdapter.setVideos(suggestedVideos);
    }


    @SuppressLint("SetTextI18n")
    public void updateUI(){
        Log.d("dtube", "updateUI");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInPictureInPictureMode()) {
            //if in picture mode hide undervideo content and make view fullscreen
            findViewById(R.id.undervideo_contents).setVisibility(View.GONE);
            videoLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

//            Log.d("dtube", "updateUI - startPlayer");
//            super.onResume();
//            MediaPlayerSingleton.getInstance(this).startPlayer();

        } else {

            findViewById(R.id.undervideo_contents).setVisibility(View.VISIBLE);


            ((TextView) findViewById(R.id.item_title)).setText(videoToPlay.title);
            ((TextView) findViewById(R.id.item_value)).setText(videoToPlay.price);
            ((TextView) findViewById(R.id.item_user)).setText(videoToPlay.user);
            ((RelativeTimeTextView) findViewById(R.id.item_time)).setReferenceTime(videoToPlay.getDate());
            ((TextView) findViewById(R.id.provider_text)).setText("Provider: " + videoToPlay.getProvider());


            if (accountName != null && accountName.equals(videoToPlay.user)) {
                findViewById(R.id.item_subscribe).setEnabled(false);
            } else
                findViewById(R.id.item_subscribe).setEnabled(true);

            if (restrictClicks) {
                findViewById(R.id.item_subscribe).setClickable(false);
                findViewById(R.id.video_like).setClickable(false);
                findViewById(R.id.video_dislike).setClickable(false);
                replyBox.setFocusableInTouchMode(false);
            } else {
                replyBox.setFocusableInTouchMode(true);
                replyBox.setFocusable(true);
                findViewById(R.id.item_subscribe).setClickable(true);
                findViewById(R.id.video_like).setClickable(true);
                findViewById(R.id.video_dislike).setClickable(true);
            }

            if (subscribed) {
                ((Button) findViewById(R.id.item_subscribe)).setText(R.string.unsubscribe);
            } else
                ((Button) findViewById(R.id.item_subscribe)).setText(R.string.subscribe);

            ((TextView) findViewById(R.id.text_likes)).setText("" + videoToPlay.likes);
            ((TextView) findViewById(R.id.text_dislikes)).setText("" + videoToPlay.dislikes);

            if (Preferences.darkMode) {
                ((ImageView) findViewById(R.id.video_like)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                ((ImageView) findViewById(R.id.video_dislike)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageView) findViewById(R.id.video_like)).setColorFilter(null);
                ((ImageView) findViewById(R.id.video_dislike)).setColorFilter(null);
            }

            if (videoToPlay.voteType == 1) {
                ((ImageView) findViewById(R.id.video_like)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            } else if (videoToPlay.voteType == -1)
                ((ImageView) findViewById(R.id.video_dislike)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);

            if (videoToPlay.user != null) {
                Picasso.get().load(DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username", videoToPlay.user)).placeholder(R.drawable.login).transform(transformation)
                        .into((ImageView) findViewById(R.id.item_profileimage));
            }

            if (!setFullDescription) {
                ((TextView) findViewById(R.id.item_description)).setText(Tools.fromHtml(videoToPlay.longDescriptionHTML));
                //disable links if running on TV
                if (!runningOnTV)
                    ((TextView) findViewById(R.id.item_description)).setMovementMethod(LinkMovementMethod.getInstance());
                setFullDescription = true;
            }


            ((TextView) findViewById(R.id.subscribers)).setText(subscribers);


            ImageView fullscreenButton = findViewById(R.id.exo_fullscreen_button);
            if (fullscreenButton != null && !runningOnTV) {
                if (fullscreen) {
                    videoLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    fullscreenButton.setImageResource(R.drawable.exo_controls_fullscreen_exit);
                } else {
                    videoLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            Tools.numtodp(250, this)));
                    fullscreenButton.setImageResource(R.drawable.exo_controls_fullscreen_enter);
                }
            }
        }


    }

    public void subscribeButtonClicked(View v){
        subscribeLoader.setVisibility(View.VISIBLE);
        findViewById(R.id.item_subscribe).setClickable(false);

        if (subscribed)
            steemWebView.unfollowUser(videoToPlay.user, DtubeAPI.getAccountName(this), DtubeAPI.getUserPrivateKey(this));
        else
            steemWebView.followUser(videoToPlay.user, DtubeAPI.getAccountName(this), DtubeAPI.getUserPrivateKey(this));
    }

    //Called from proxy
    public void setVote(int weight, String permlink){
        likedislikeLoader.setVisibility(View.GONE);

        if (videoToPlay.permlink.equals(permlink)){
            if (weight>0){
                videoToPlay.voteType = 1;
                videoToPlay.likes++;
            }else {
                videoToPlay.voteType = -1;
                videoToPlay.dislikes ++;
            }
            updateUI();
        }else if (commentsList!=null && commentsList.getCommentByID(permlink)!=null){
            Comment c = commentsList.getCommentByID(permlink);
            if (weight>0){
                c.voteType = 1;
                c.likes++;
            }else {
                c.voteType = -1;
                c.dislikes ++;
            }
            commentsAdapter.notifyDataSetChanged();
        }

    }


    public void viewRepliesButtonClicked(View v){
        String permlink = v.getTag().toString();
        Comment c = commentsList.getCommentByID(permlink);
        steemWebView.getReplies(c.userName,c.permlink, DtubeAPI.getAccountName(this));

    }

    public void videoDislikeClicked(View v){
        likedislikeLoader.setVisibility(View.VISIBLE);
        steemWebView.votePost(videoToPlay.user, videoToPlay.permlink, DtubeAPI.getAccountName(this),
                DtubeAPI.getUserPrivateKey(this),-10000);
    }

    public void videoLikeClicked(View v){
        likedislikeLoader.setVisibility(View.VISIBLE);
        steemWebView.votePost(videoToPlay.user, videoToPlay.permlink, DtubeAPI.getAccountName(this),
                DtubeAPI.getUserPrivateKey(this),10000);
    }

    public void dislikeCommentButtonClicked(View v){
        String permlink = v.getTag().toString();
        steemWebView.votePost(commentsList.getCommentByID(permlink).userName, permlink, DtubeAPI.getAccountName(this),
                DtubeAPI.getUserPrivateKey(this),-10000);
    }

    public void likeCommentButtonClicked(View v){
        String permlink = v.getTag().toString();
        steemWebView.votePost(commentsList.getCommentByID(permlink).userName, permlink, DtubeAPI.getAccountName(this),
                DtubeAPI.getUserPrivateKey(this),10000);
    }

    public void replyToCommentButtonClicked(View v){
        FrameLayout replyHolder = ((LinearLayout)v.getParent().getParent()).findViewById(R.id.comment_reply_holder);

        replyHolder.setVisibility(View.VISIBLE);

        if (clientProfileImageURL!=null) {
            Picasso.get().load(clientProfileImageURL).placeholder(R.drawable.login).transform(transformation)
                    .into(((ImageView) replyHolder.findViewById(R.id.item_account_comment_image)));
        }

        commentReplyBox = replyHolder.findViewById(R.id.item_comment_reply_edittext);
        commentReplyBox.setTag(v.getTag());
        commentReplyBox.setOnEditorActionListener(editorActionListener);

        commentReplyBox.postDelayed(() -> commentReplyBox.requestFocus(),100);
    }


    public void onItemClick(int pos){
        if (steemWebView!=null)
            steemWebView.getVideoInfo(suggestedVideos.get(pos).user, suggestedVideos.get(pos).permlink, DtubeAPI.getAccountName(this));
    }

    public final boolean useEmbeded = false;
    boolean hadErrorLoading;
    public void setupVideoView(){
        hadErrorLoading = false;

        videoLayoutHolder.removeView(MediaPlayerSingleton.getInstance(this).getEmbeddedPlayerView());
        videoLayoutHolder.removeView(MediaPlayerSingleton.getInstance(this).getIPFSPlayerView());

        if (useEmbeded || runningOnTV){
            MediaPlayerSingleton.getInstance(this).playVideo(videoToPlay,this);

            if (videoLayoutHolder.findViewById(R.id.exo_content_frame)==null) {
                videoLayoutHolder.addView(MediaPlayerSingleton.getInstance(this).getRightPlayerView(), 0);
            }
        }else {
            MediaPlayerSingleton.getInstance(this).playVideo(videoToPlay,this);

            findViewById(R.id.video_content).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (oldBottom!=bottom) {
                    updateUI();
                    MediaPlayerSingleton.getInstance(VideoPlayActivity.this).getIPFSPlayerView().showController();
                }
                Log.d("dtube", "onLayoutChange");
            });


            if (videoLayoutHolder.findViewById(R.id.exo_content_frame)==null)
                videoLayoutHolder.addView(MediaPlayerSingleton.getInstance(this).getRightPlayerView());

        }
    }

    public void goToChannelClicked(View v){
        Intent channelIntent = new Intent(VideoPlayActivity.this,ChannelActivity.class);
        channelIntent.putExtra("subscribers",subscribers);
        channelIntent.putExtra("username", videoToPlay.user);
        channelIntent.putExtra("userurl", "/c/"+videoToPlay.user);
        channelIntent.putExtra("profileimage", profileImageURL);
        startActivityForResult(channelIntent, REQUEST_CHANNEL);
    }


    public void playVideo(Video v){
        videoToPlay = v;

        Log.d("dtube3", "to play andr "+videoToPlay.title+","+videoToPlay.permlink);
        setupVideoView();

        if (commentsAdapter!=null) {
            commentsList.clear();
            commentsAdapter.notifyDataSetChanged();
        }

        if (suggestionAdapter!=null) {
            suggestedVideos.clear();
            suggestionAdapter.notifyDataSetChanged();
        }


        setFullDescription = false;
        restrictClicks = true;
        replyBox.setText("");

        ((TextView) findViewById(R.id.item_description)).setText(videoToPlay.longDescriptionHTML);

        updateUI();

        steemWebView.getReplies(videoToPlay.user, videoToPlay.permlink, DtubeAPI.getAccountName(VideoPlayActivity.this));
        steemWebView.getIsFollowing(videoToPlay.user, DtubeAPI.getAccountName(VideoPlayActivity.this));
        steemWebView.getSuggestedVideos(videoToPlay.user);
        steemWebView.getSubscriberCount(videoToPlay.user);

    }

    public void makeFullscreen(View v){
        fullscreen = !fullscreen;
        updateUI();

        if (fullscreen)
        {
            WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            this.getWindow().setAttributes(attrs);
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
        else
        {
            WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            this.getWindow().setAttributes(attrs);
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    boolean enterPIPModeOnResume = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CHANNEL:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    if (v!=null)
                        steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }

            case REQUEST_UPGRADE:
                if (resultCode == RESULT_OK){
                    //ativity must be resmed beforeentering PIP mod
                    enterPIPModeOnResume = true;
                }else if (resultCode == RESULT_CANCELED){
                    finish();
                }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("dtube", "key");

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (runningOnTV) {
                    if (findViewById(R.id.undervideo_contents).getScrollY() == 0) {
                        wakeMediaControls();
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (runningOnTV) {
                    if (findViewById(R.id.undervideo_contents).getScrollY() == 0) {
                        MediaPlayerSingleton.getInstance(this).showControls();
                        MediaPlayerSingleton.getInstance(this).focusControls();
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (runningOnTV) {
                    if (findViewById(R.id.undervideo_contents).getScrollY() == 0) {
                        if (findViewById(R.id.exo_progress).isFocused())
                            MediaPlayerSingleton.getInstance(this).hideControls();
                    }
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:

                if (runningOnTV) {
                    if (findViewById(R.id.undervideo_contents).getScrollY() == 0) {
                        MediaPlayerSingleton.getInstance(this).showControls();
                    }
                }

                if (getCurrentFocus()!=null){

                    switch (getCurrentFocus().getId()){
                        case R.id.comment_et_holder:
                            replyBox.setEnabled(true);
                            replyBox.requestFocus();
                            break;

                        default:
                            break;
                    }
                }

                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void wakeMediaControls(){
        MediaPlayerSingleton.getInstance(this).showControls();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d("dtube","Key event "+event.getKeyCode()+"VS"+KeyEvent.KEYCODE_MEDIA_REWIND);
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (getCurrentFocus() != null && getCurrentFocus() instanceof  ViewGroup) {
                        //Open a dialog for options on a comment
                        if (getCurrentFocus().getId() == R.id.comments_lv) {

                            final View commentView = commentsListView.getSelectedView();
                            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(VideoPlayActivity.this);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(VideoPlayActivity.this, android.R.layout.select_dialog_item);
                            arrayAdapter.add(getResources().getString(R.string.like_comment));
                            arrayAdapter.add(getResources().getString(R.string.dislike_comment));

                            builderSingle.setNegativeButton("cancel", null);

                            builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                    commentView.findViewById(R.id.comment_like).performClick();
                                    break;

                                    case 1:
                                        commentView.findViewById(R.id.comment_dislike).performClick();
                                        break;

                                    case 2:
                                        commentView.findViewById(R.id.comment_reply).performClick();
                                        break;
                                }
                            });

                            //dialog display is delayed to prevent misfocus
                            commentView.postDelayed(builderSingle::show,10);

                            Log.d("dtube", "dispatch" + ((ViewGroup) getCurrentFocus()).getChildAt(0).getId() + "VS" + R.id.comment_item);
                        }else if (getCurrentFocus().getId() == R.id.suggestions_lv) {
                            //select the suggested video
                            onItemClick(suggestedVideosListView.getSelectedItemPosition());
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    wakeMediaControls();
                    MediaPlayerSingleton.getInstance(this).togglePlayPause();
                    break;

                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD:
                case KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD:
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    wakeMediaControls();
                    MediaPlayerSingleton.getInstance(this).rewind();
                    break;

                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_STEP_FORWARD:
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    wakeMediaControls();
                    MediaPlayerSingleton.getInstance(this).fastForward();
                    break;

            }
        }
        return super.dispatchKeyEvent(event);
    }

}
