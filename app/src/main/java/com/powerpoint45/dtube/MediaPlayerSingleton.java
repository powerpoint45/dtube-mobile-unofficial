package com.powerpoint45.dtube;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
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

import static android.content.Context.UI_MODE_SERVICE;
import static com.google.android.exoplayer2.ui.PlayerView.SHOW_BUFFERING_ALWAYS;

public class MediaPlayerSingleton {

    private static MediaPlayerSingleton mediaPlayer;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private Video videoToPlay;
    private Context c;

    WebViewVideoView embeddedPlayer;

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
                if (!videoToPlay.hasTriedLoadingBackupGateway()){
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
        if(videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_TWITCH)
                ||videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_YOUTUBE))
            return  embeddedPlayer;
        else
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
        Log.d("dtube","provider:  "+ videoToPlay.getProvider());

        if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_YOUTUBE)) {
            Log.d("dtube2", "loading stream: " + videoToPlay.hash);
            String url = "https://www.youtube-nocookie.com/embed/"+videoToPlay.hash+"?autoplay=1&modestbranding=1&rel=0&showinfo=0";
            String html = "<html><body><iframe frameborder=0 allowfullscreen width=100% height=100% src=\"" + url + "\"  frameborder=0 allowfullscreen></iframe></body></html>";
            embeddedPlayer.loadData(html, "text/html", "utf-8");
            pauseNativePlayer();
            startEmbeddedPlayer();
        }else if (videoToPlay.getProvider().equals(DtubeAPI.PROVIDER_TWITCH)) {
            embeddedPlayer.loadUrl("https://player.twitch.tv/?video="+videoToPlay.hash);
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
    

    void togglePlayPause(){
        if (playerView.findViewById(R.id.exo_play).getVisibility()==View.VISIBLE)
            playerView.findViewById(R.id.exo_play).performClick();
        else if (playerView.findViewById(R.id.exo_pause).getVisibility()==View.VISIBLE)
            playerView.findViewById(R.id.exo_pause).performClick();
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
