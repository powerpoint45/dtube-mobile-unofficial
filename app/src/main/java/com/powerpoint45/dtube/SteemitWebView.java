package com.powerpoint45.dtube;

import static com.powerpoint45.dtube.Preferences.selectedAPI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by michael on 11/11/17.
 */

@SuppressLint("ViewConstructor")
public class SteemitWebView extends WebView {

    boolean loadedPage;
    public String api;

    @SuppressLint("SetJavaScriptEnabled")
    /**
     * context: Activity Context
     * api: API url selection. pass null for already selected login type
     */
    public SteemitWebView(Activity context, String api) {
        super(context);
        this.api = api;

        if (getResources().getBoolean(R.bool.debug)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        setWebViewClient(new WebViewClient() {

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d("dtube", "shouldInterceptRequest");
                request.getRequestHeaders().remove("Access-Control-Allow-Origin");
                request.getRequestHeaders().put("Access-Control-Allow-Origin", "*");
                return super.shouldInterceptRequest(view, request);
            }

            public void onPageFinished(WebView view, String url) {
                loadedPage = true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(context, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        setWebChromeClient(new WebChromeClient(){

            public void onProgressChanged(WebView view, int newProgress){

                if(newProgress == 100){
                    // Page loading finish
                    if (view.getUrl().equals(selectedAPI))
                        loadedPage = true;
                }
            }
        });



        getSettings().setJavaScriptEnabled(true);

        if (api == null)
            api = selectedAPI;
        this.api = api;
        loadUrl(api);


        //the javascript interface helps me get results from steemit javascript library
        addJavascriptInterface(new AppJavaScriptProxy(context), "androidAppProxy");
    }

    public void commentPost(String author, String permlink, String accountName, String privateKey, String comment, String parentPermlink, String parentAuthor, int indent){
        queURL("javascript:commentPost('"+author+"','"+permlink+"','"+accountName+"','"+privateKey+"','"+comment+"','"+parentPermlink+"','"+parentAuthor+"','"+indent+"');");
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

    public void login(String username, String password, boolean upvote, boolean follow){
        queURL("javascript:login('"+username+"','"+password+"',"+upvote+","+follow+");");
    }


    public void getSubscriberCount(String userName){
        queURL("javascript:getSubscriberCount('"+userName+"');");
    }

    public void getSubscriptions(String userName){
        Log.d("DT", "getSubscriptions0");
        queURL("javascript:getSubscriptions('"+userName+"');");
    }

    //loads a set of videos at a time and sent to proxy
    public void getSubscriptionFeed(String username, String StartAuthor, String StartPermlink){
        if (StartAuthor == null)
            StartAuthor = "";

        if (StartPermlink == null)
            StartPermlink = "";

        queURL("javascript:getSubscriptionFeed('"+username+"','"+StartAuthor+"','"+StartPermlink+"');");
    }

    public void getSubscriptionFeed(String userName){
        queURL("javascript:getSubscriptionFeed('"+userName+"', '', '');");
    }

    public void getHotVideosFeed(String StartAuthor, String StartPermlink){
        queURL("javascript:getHotVideosFeed('"+StartAuthor+"','"+StartPermlink+"');");
    }


    public void getTrendingVideosFeed(String StartAuthor, String StartPermlink){
        queURL("javascript:getTrendingVideosFeed('"+StartAuthor+"','"+StartPermlink+"');");
    }


    public void getNewVideosFeed(String StartAuthor, String StartPermlink){
        queURL("javascript:getNewVideosFeed('"+StartAuthor+"','"+StartPermlink+"');");
    }

    public void getHotVideosFeed(){
        queURL("javascript:getHotVideosFeed(null,null);");
    }


    public void getTrendingVideosFeed(){
        queURL("javascript:getTrendingVideosFeed(null,null);");
    }


    public void getNewVideosFeed(){
        queURL("javascript:getNewVideosFeed(null,null);");
    }


    public void getVideoInfo(String author, String permlink, String accountName){
        Log.d("dtubez","javascript:getVideoInfo('"+author+"','"+permlink+"','"+accountName+"');");
        queURL("javascript:getVideoInfo('"+author+"','"+permlink+"','"+accountName+"');");
    }

    public void getReplies(String author, String permlink, String accountName){
        queURL("javascript:getReplies('"+author+"','"+permlink+"','"+accountName+"');");
    }

//    public void getAllReplies(String author, String permlink, String accountName){
//        queURL("javascript:getAllReplies('"+author+"','"+permlink+"','"+accountName+"');");
//    }

    public void getChannelVideos(String author, String lastPermlink, String lastAuthor, String accountName){
        Log.d("dt","getChannelVideos");
        queURL("javascript:getAuthorVideos('"+author+"','"+lastPermlink+"','"+lastAuthor+"','"+accountName+"');");
    }

    public void getChannelVideos2(String author, String lastPermlink, String accountName){
        Log.d("dt","getChannelVideos2");
        queURL("javascript:getAuthorVideos('"+author+"','"+lastPermlink+"','"+accountName+"',1);");
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

    public void getBalance(String channelName) {
        queURL("javascript:getBalance('"+channelName+"');");
    }
}
