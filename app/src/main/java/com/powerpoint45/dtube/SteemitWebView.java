package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by michael on 11/11/17.
 */

@SuppressLint("ViewConstructor")
public class SteemitWebView extends WebView {

    boolean loadedPage;

    @SuppressLint("SetJavaScriptEnabled")
    public SteemitWebView(Activity context) {
        super(context);

        WebView.setWebContentsDebuggingEnabled(true);
        setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                loadedPage = true;
            }
        });

        getSettings().setJavaScriptEnabled(true);
        loadUrl("file:///android_res/raw/steemit.html");

        //the javascript interface helps me get results from steemit javascript library
        addJavascriptInterface(new AppJavaScriptProxy(context), "androidAppProxy");
    }

    public void commentPost(String author, String permlink, String accountName, String privateKey, String comment,
                            String parentPermlink, String parentAuthor){
        queURL("javascript:commentPost('"+author+"','"+permlink+"','"+accountName+"','"+privateKey+"','"+comment+"','"+parentPermlink+"','"+parentAuthor+"');");
    }

    public void votePost(String author, String permlink, String accountName, String privateKey, int weight) {
        queURL("javascript:votePost('"+author+"','"+permlink+"','"+accountName+"','"+privateKey+"',"+weight+");");
    }

    public void followUser(String author, String accountName, String authentification){
        queURL("javascript:followAuthor('"+author+"','"+accountName+"','"+authentification+"');");
    }

    public void unfollowUser(String author, String accountName, String authentification){
        queURL("javascript:unfollowAuthor('"+author+"','"+accountName+"','"+authentification+"');");
    }

    public void getIsFollowing(String author, String accountName){
        queURL("javascript:getIsFollowing('"+author+"','"+accountName+"');");
    }

    public void login(String username, String password){
        queURL("javascript:login('"+username+"','"+password+"');");
    }

    public void getSubscriberCount(String userName){
        queURL("javascript:getSubscriberCount('"+userName+"');");
    }

    public void getSubscriptions(String userName){
        queURL("javascript:getSubscriptions('"+userName+"');");
    }

    //loads a set of videos at a time and sent to proxy
    public void getSubscriptionFeed(String username){
        queURL("javascript:getSubscriptionFeed('"+username+"');");
    }

    public void getHotVideosFeed(){
        queURL("javascript:getHotVideosFeed();");
    }


    public void getTrendingVideosFeed(){
        queURL("javascript:getTrendingVideosFeed();");
    }


    public void getNewVideosFeed(){
        queURL("javascript:getNewVideosFeed();");
    }


    public void getVideoInfo(String author, String permlink, String accountName){
        loadUrl("javascript:getVideoInfo('"+author+"','"+permlink+"','"+accountName+"');");
    }

    public void getReplies(String author, String permlink, String accountName){
        queURL("javascript:getReplies('"+author+"','"+permlink+"','"+accountName+"');");
    }

//    public void getAllReplies(String author, String permlink, String accountName){
//        queURL("javascript:getAllReplies('"+author+"','"+permlink+"','"+accountName+"');");
//    }

    public void getChannelVideos(String author, String lastPermlink){
        queURL("javascript:getAuthorVideos('"+author+"','"+lastPermlink+"');");
    }

    public void getSuggestedVideos(String username){
        queURL("javascript:getSuggestedVideos('"+username+"');");
    }


    public void killWebView(){
        loadUrl("about:blank");
        removeJavascriptInterface("androidAppProxy");
    }

    public void queURL(String url){
        if (loadedPage) {
            loadUrl(url);
            Log.d("dtube4", "open url");
        }else {
            class LoadRunner implements Runnable{

                private String url;
                private LoadRunner(String url){
                    this.url = url;
                }

                @Override
                public void run() {
                    do{
                        Log.d("dtube4","waiting to run url");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }while (!loadedPage);

                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadUrl(url);
                        }
                    });
                }
            }

            new Thread(new LoadRunner(url)).start();

        }
    }

}
