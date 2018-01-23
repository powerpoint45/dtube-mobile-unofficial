package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Collections;
import java.util.Comparator;

import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by michael on 5/11/17.
 */

public class VideoPlayActivity extends AppCompatActivity {
    static final int REQUEST_CHANNEL = 0;

    JZVideoPlayerStandard videoView;
    WebViewVideoView webViewVideoView;
    LinearLayout videoLayoutHolder;

    SteemitWebView steemWebView;
    EditText replyBox;
    EditText commentReplyBox;
    TextView descriptionBox;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        setContentView(R.layout.activity_videoplay);
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
        Log.d("dtube","PLAYING ON PLAYER:"+videoToPlay.getVideoStreamURL());

        replyBox = ((EditText)findViewById(R.id.item_comment_edittext));
        videoLayoutHolder = (LinearLayout) findViewById(R.id.player_holder);
        descriptionBox = (TextView) findViewById(R.id.item_description);
        descriptionBox.setBackgroundColor(Color.TRANSPARENT);

        transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();

        if (clientProfileImageURL!=null) {
            Picasso.with(this).load(clientProfileImageURL).placeholder(R.drawable.ic_account_circle).transform(transformation)
                    .into(((ImageView) findViewById(R.id.item_account_comment_image)));
        }

        descriptionBox.setText(Tools.fromHtml(videoToPlay.longDescriptionHTML));
        descriptionBox.setMovementMethod(LinkMovementMethod.getInstance());

        if (accountName!=null)
            replyBox.setOnEditorActionListener(editorActionListener);
        else
            replyBox.setText(R.string.login_comment);

        updateUI();
        setupVideoView();
    }



    EditText.OnEditorActionListener editorActionListener = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
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
                if (commentReplyBox!=null)
                    imm.hideSoftInputFromWindow(commentReplyBox.getWindowToken(), 0);
                else
                    imm.hideSoftInputFromWindow(replyBox.getWindowToken(), 0);

                v.clearFocus();

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

        commentsAdapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (commentReplyBox!=null)
            imm.hideSoftInputFromWindow(commentReplyBox.getWindowToken(), 0);
        else
            imm.hideSoftInputFromWindow(replyBox.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed(){
        if (JZVideoPlayerStandard.backPress()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayerStandard.releaseAllVideos();
        if (webViewVideoView!=null) {
            webViewVideoView.pauseVideo();
            webViewVideoView.pauseTimers();
        }
        if (steemWebView!=null)
            steemWebView.pauseTimers();
    }

    @Override
    public void finish() {
        super.finish();
        JZVideoPlayerStandard.backPress();
        if (videoView!=null) {
            videoView.release();
            videoView = null;
        }
        if (webViewVideoView!=null){
            webViewVideoView.killWebView();
            webViewVideoView = null;
        }
        steemWebView.killWebView();
        steemWebView = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (webViewVideoView!=null)
            webViewVideoView.resumeTimers();
        if (steemWebView!=null)
            steemWebView.resumeTimers();
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
            commentsAdapter = new CommentsAdapter(commentsList, this, accountName != null);
            ((ListView) findViewById(R.id.comments_lv)).setAdapter(commentsAdapter);
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
            ((ListView) findViewById(R.id.suggestions_lv)).setAdapter(suggestionAdapter);
        }else
            suggestionAdapter.setVideos(suggestedVideos);
    }


    @SuppressLint("SetTextI18n")
    public void updateUI(){

        ((TextView)findViewById(R.id.item_title)).setText(videoToPlay.title);
        ((TextView)findViewById(R.id.item_value)).setText(videoToPlay.price);
        ((TextView)findViewById(R.id.item_user)).setText(videoToPlay.user);
        ((RelativeTimeTextView)findViewById(R.id.item_time)).setReferenceTime(videoToPlay.getDate());


        if (restrictClicks){
            findViewById(R.id.item_subscribe).setClickable(false);
            findViewById(R.id.video_like).setClickable(false);
            findViewById(R.id.video_dislike).setClickable(false);
            replyBox.setFocusableInTouchMode(false);
        }else {
            replyBox.setFocusableInTouchMode(true);
            findViewById(R.id.item_subscribe).setClickable(true);
            findViewById(R.id.video_like).setClickable(true);
            findViewById(R.id.video_dislike).setClickable(true);
        }

        if (subscribed){
            ((Button)findViewById(R.id.item_subscribe)).setText(R.string.unsubscribe);
        }else
            ((Button)findViewById(R.id.item_subscribe)).setText(R.string.subscribe);

        ((TextView) findViewById(R.id.text_likes)).setText("" + videoToPlay.likes);
        ((TextView) findViewById(R.id.text_dislikes)).setText("" + videoToPlay.dislikes);

        ((ImageView)findViewById(R.id.video_like)).setColorFilter(null);
        ((ImageView)findViewById(R.id.video_dislike)).setColorFilter(null);

        if (videoToPlay.voteType == 1){
            ((ImageView)findViewById(R.id.video_like)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }else if(videoToPlay.voteType == -1)
            ((ImageView)findViewById(R.id.video_dislike)).setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);

        if (videoToPlay.user!=null) {
            Picasso.with(this).load(DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username", videoToPlay.user)).placeholder(R.drawable.ic_account_circle).transform(transformation)
                    .into((ImageView) findViewById(R.id.item_profileimage));
        }

        if (!setFullDescription) {
            ((TextView) findViewById(R.id.item_description)).setText(Tools.fromHtml(videoToPlay.longDescriptionHTML));
            ((TextView) findViewById(R.id.item_description)).setMovementMethod(LinkMovementMethod.getInstance());
            setFullDescription = true;
        }


        ((TextView)findViewById(R.id.subscribers)).setText(subscribers);
    }

    public void subscribeButtonClicked(View v){
        if (subscribed)
            steemWebView.unfollowUser(videoToPlay.user, DtubeAPI.getAccountName(this), DtubeAPI.getUserPrivateKey(this));
        else
            steemWebView.followUser(videoToPlay.user, DtubeAPI.getAccountName(this), DtubeAPI.getUserPrivateKey(this));
    }

    //Called from proxy
    public void setVote(int weight, String permlink){
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
        steemWebView.votePost(videoToPlay.user, videoToPlay.permlink, DtubeAPI.getAccountName(this),
                DtubeAPI.getUserPrivateKey(this),-10000);
    }

    public void videoLikeClicked(View v){
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
        FrameLayout replyHolder = (FrameLayout)((LinearLayout)v.getParent().getParent()).findViewById(R.id.comment_reply_holder);

        replyHolder.setVisibility(View.VISIBLE);

        if (clientProfileImageURL!=null) {
            Picasso.with(this).load(clientProfileImageURL).placeholder(R.drawable.ic_account_circle).transform(transformation)
                    .into(((ImageView) replyHolder.findViewById(R.id.item_account_comment_image)));
        }

        commentReplyBox = (EditText)  replyHolder.findViewById(R.id.item_comment_reply_edittext);
        commentReplyBox.setTag(v.getTag());
        commentReplyBox.setOnEditorActionListener(editorActionListener);
        commentReplyBox.requestFocus();
    }


    public void onItemClick(int pos){
        if (steemWebView!=null)
            steemWebView.getVideoInfo(suggestedVideos.get(pos).user, suggestedVideos.get(pos).permlink, DtubeAPI.getAccountName(this));
    }

    public final boolean useEmbeded = true;
    public void setupVideoView(){
        if (useEmbeded){

            if (webViewVideoView != null) {
                videoLayoutHolder.removeView(webViewVideoView);
                webViewVideoView.killWebView();
            }

            webViewVideoView = new WebViewVideoView(this);

            webViewVideoView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webViewVideoView.setBackgroundColor(Color.BLACK);
            Log.d("dtube4","To load "+videoToPlay.getVideoFrameUrl());

            webViewVideoView.loadUrl(videoToPlay.getVideoFrameUrl());

            if (videoLayoutHolder.findViewById(R.id.embeded_video_view)==null)
                videoLayoutHolder.addView(webViewVideoView);
        }else {
            Log.d("dtube4","setupVideoView");
            if (videoView == null){
                videoView = new JZVideoPlayerStandard(this);
            }
            videoView.setUp(videoToPlay.getVideoStreamURL()
                    , JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, videoToPlay.title);

            Picasso.with(this).load(videoToPlay.getImageURL()).resize(720, 720).centerInside().into(videoView.thumbImageView);
            videoView.startButton.performClick();
            videoView.setId(R.id.native_video_view);

            if (videoLayoutHolder.findViewById(R.id.native_video_view)==null)
                videoLayoutHolder.addView(videoView);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CHANNEL:
                if (resultCode == RESULT_OK){
                    Video v = (Video)data.getBundleExtra("video").getSerializable("video");
                    steemWebView.getVideoInfo(v.user, v.permlink, DtubeAPI.getAccountName(this));
                }
        }

    }
}
