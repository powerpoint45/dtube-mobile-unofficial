package com.powerpoint45.dtube;

import static android.content.Context.UI_MODE_SERVICE;
import static com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_ALWAYS;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class MediaPlayerSingleton {

    private static MediaPlayerSingleton mediaPlayer;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private Video videoToPlay;

    boolean triedLoadingSecondarySource = false;

    WebViewVideoView embeddedPlayer;

    Activity c;

    private MediaPlayerSingleton(){

    }

    private MediaPlayerSingleton(Activity c){
        init(c);
    }

    private void init(Activity c) {
        embeddedPlayer = new WebViewVideoView(c,false);

        playerView = new PlayerView(c);
        player = ExoPlayerFactory.newSimpleInstance(c);
        playerView.setShowBuffering(SHOW_BUFFERING_ALWAYS);
        playerView.setPlayer(player);

        this.c = c;

        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(UI_MODE_SERVICE);

        if (uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION) {
            playerView.findViewById(R.id.exo_fullscreen_button).setVisibility(View.GONE);
            int extraPadding = Tools.numtodp(20, c);
            playerView.findViewById(R.id.control_bar_holder).setPadding(extraPadding,0,extraPadding,extraPadding);
        }

        player.addListener(new Player.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                //hadErrorLoading = true;
                if (triedLoadingSecondarySource== false) {
                    triedLoadingSecondarySource = true;
                    playVideo(videoToPlay, c);
                    Log.d("dtsgd", "loading secondary");
                }

                if (!videoToPlay.hasTriedLoadingBackupGateway()){
                    Log.d("dtsgd", "loading backup");
                    //try loading backup stream
                    Log.d("dtube3","BACKUP LOAD "+videoToPlay.getBackupVideoStreamURL());
                    player.prepare(getMediaSource(videoToPlay.getBackupVideoStreamURL()));
                    player.setPlayWhenReady(true);
                }else
                    Toast.makeText(c, R.string.video_error, Toast.LENGTH_LONG).show();
            }
        });

        player.addVideoListener(new VideoListener() {
            @Override
            public void onRenderedFirstFrame() {
            }
        });
    }

    public SimpleExoPlayer getPlayer(){
        return player;
    }

    /**
     *
     * @return playerView or youTubePlayerView based on provider
     */
    View getRightPlayerView(){
        if(videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_TWITCH) || videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_3SPEAK)
                ||videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_YOUTUBE)||videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_APPICS)) {
            return embeddedPlayer;
        }else
            return playerView;
    }

    PlayerView getIPFSPlayerView(){
        return playerView;
    }
    WebView getEmbeddedPlayerView(){return embeddedPlayer;}

    private MediaSource getMediaSource(String url){
        Uri uri = Uri.parse(url);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(c,
                Util.getUserAgent(c, "DtubeClient"));
        // This is the MediaSource representing the media to be played.
        return new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    void playVideo(Video videoToPlay, Context c){
        this.videoToPlay = videoToPlay;
        Log.d("dtube4","provider:"+ videoToPlay.getProvider());

        if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_YOUTUBE)) {
            Log.d("dtube2", "loading stream: " + videoToPlay.hash);
            String url = "https://www.youtube-nocookie.com/embed/"+videoToPlay.hash+"?autoplay=1&modestbranding=1&rel=0&showinfo=0";
            String html = "<html><body><iframe frameborder=0 allowfullscreen width=100% height=100% src=\"" + url + "\"  frameborder=0 allowfullscreen></iframe></body></html>";
            embeddedPlayer.loadData(html, "text/html", "utf-8");
            pauseNativePlayer();
            startEmbeddedPlayer();
        }else if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_3SPEAK)) {
            Log.d("dtube9","PROVIDER_3SPEAK");
            String url = "https://3speak.tv/embed?v="+videoToPlay.hash;
            Log.d("dtube9",url);
            String html = "<html><body><iframe src=\""+url+"\""+"  frameborder=\"0\" width=100% height=100% allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe></body></html>";
            Log.d("dtube9",html);
            //embeddedPlayer.loadUrl(url);
            embeddedPlayer.loadData(html, "text/html", "utf-8");
            embeddedPlayer.queClickCenter();
            pauseNativePlayer();
            startEmbeddedPlayer();
        } else if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_TWITCH)) {
            embeddedPlayer.loadUrl("https://player.twitch.tv/?video="+videoToPlay.hash);
            pauseNativePlayer();
            startEmbeddedPlayer();
        }else if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_APPICS)) {
            //embeddedPlayer.loadUrl(videoToPlay.hash);

            //embeddedPlayer.setBackgroundColor(Color.RED);
            //Log.d("dfegwaf","PROVIDER_APPICS " + videoToPlay.hash);

            String url = videoToPlay.hash;
            String html = "<html><body><iframe frameborder=0 allowfullscreen width=100% height=100% src=\"" + url + "\"  frameborder=0 allowfullscreen></iframe></body></html>";
            embeddedPlayer.loadData(html, "text/html", "utf-8");

            pauseNativePlayer();
            startEmbeddedPlayer();


        } else {
            Log.d("dtube", "loading stream: " + videoToPlay.getVideoStreamURL());

            player.prepare(getMediaSource(videoToPlay.getVideoStreamURL()));


            player.setPlayWhenReady(true);
            playerView.showController();

            pauseEmbeddedPlayer();

            Picasso.get().load(videoToPlay.getImageURL()).resize(720, 720).centerInside().into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    playerView.findViewById(R.id.exo_shutter).setBackground(new BitmapDrawable(bitmap));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    static MediaPlayerSingleton getInstance(Activity c){
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayerSingleton(c);

        return mediaPlayer;
    }

    void showControls(){
        playerView.showController();
    }

    void hideControls(){
        playerView.hideController();
    }

    void removeControls(){
        playerView.findViewById(R.id.control_bar_holder).setVisibility(View.GONE);
    }

    void restoreControls(){
        playerView.findViewById(R.id.control_bar_holder).setVisibility(View.VISIBLE);
    }

    void focusControls(){
        playerView.findViewById(R.id.exo_pause).requestFocus();
        playerView.findViewById(R.id.exo_play).requestFocus();
    }

    void pausePlayer(){
        pauseNativePlayer();
        pauseEmbeddedPlayer();
    }

    void pauseNativePlayer(){
        if (player!=null){
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    void pauseEmbeddedPlayer(){
        if (embeddedPlayer!=null){
            embeddedPlayer.onPause();
        }
    }

    void startPlayer(){
        if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_BTFS) || videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_IPFS)) {
            startNativePlayer();
        }else{
            startEmbeddedPlayer();
        }
    }

    public void resetVideoLoadFlags(){
        triedLoadingSecondarySource=false;
        Log.d("dtsgd","reset loadinfo flags");
    }

    void startNativePlayer(){
        if (player!=null){
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    void startEmbeddedPlayer(){
        if (embeddedPlayer!=null){
            embeddedPlayer.onResume();
        }
    }




    boolean playing = true;
    void togglePlayPause(){

        ViewGroup player = (ViewGroup) getRightPlayerView();

        if (player instanceof PlayerView){

            if (playerView.findViewById(R.id.exo_play).getVisibility()==View.VISIBLE) {
                playerView.findViewById(R.id.exo_play).performClick();
                playing = true;
            }
            else if (playerView.findViewById(R.id.exo_pause).getVisibility()==View.VISIBLE) {
                playerView.findViewById(R.id.exo_pause).performClick();
                playing = false;
            }

        }else if (player instanceof WebViewVideoView){
            ((WebViewVideoView)player).clickCenter();
        }




    }


    void rewind(){
        long pos = player.getCurrentPosition();
        pos -=5000;
        if (pos <0)
            pos = 0;
        player.seekTo(pos);
    }

    void fastForward(){
        long pos = player.getCurrentPosition();
        pos +=5000;
        player.seekTo(pos);
    }

    public void release(){
        player.release();
        mediaPlayer = null;
        playerView = null;

    }

}
