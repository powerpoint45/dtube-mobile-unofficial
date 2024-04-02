package com.powerpoint45.dtube;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class AppJavaScriptProxy {

    private Activity activity = null;

    AppJavaScriptProxy(Activity activity) {
        this.activity = activity;
    }
    //Remember you cannot make UI calls from this scope!!!


    @JavascriptInterface
    public void getSubscriptionFeedCallback(String jsonVideos){
        manageFeed(jsonVideos, DtubeAPI.CAT_SUBSCRIBED);
    }

    @JavascriptInterface
    public void loginCallback(boolean sucess){
        Log.d("dtube4", sucess ? "loginCallback logged in":" loginCallback login failed");
        if (activity instanceof LoginActivity)
            ((LoginActivity)activity).gotLoginResult(sucess);
        else
            Log.d("dtube4", sucess ? "logged in":"login failed");
    }

    @JavascriptInterface
    public void getHotVideosFeedCallback(String jsonVideos){
        Log.d("dtube5",jsonVideos);
        manageFeed(jsonVideos, DtubeAPI.CAT_HOT);
    }

    @JavascriptInterface
    public void getTrendingVideosFeedCallback(String jsonVideos){
        manageFeed(jsonVideos, DtubeAPI.CAT_TRENDING);
    }

    @JavascriptInterface
    public void getNewVideosFeedCallback(String jsonVideos){
        manageFeed(jsonVideos, DtubeAPI.CAT_NEW);
    }


    @JavascriptInterface
    public void getSuggestedVideosCallback(String jsonVideos){
        final VideoArrayList videos = new VideoArrayList();
        JSONArray ja;
        try {
            ja = new JSONArray(jsonVideos);
            for (int v = 0; v < ja.length(); v++) {
                JSONObject jo = ja.getJSONObject(v);
                Video video = getVideoFromJsonObject(jo);
                videos.add(video);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (activity instanceof  VideoPlayActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((VideoPlayActivity)activity).setSuggestedVideos(videos);
                }
            });
        }
    }



    @JavascriptInterface
    public void getAuthorVideosCallback(final String jsonVideos, final String lastPermlink, final String newLastAuthor){

        if (jsonVideos.equals("last") && lastPermlink.equals("last")){
            if (activity instanceof ChannelActivity){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ChannelActivity)activity).addVideos(null, lastPermlink, newLastAuthor);
                    }
                });
            }
        }else {
            final VideoArrayList videos = new VideoArrayList();
            JSONArray ja = null;
            try {
                ja = new JSONArray(jsonVideos);
                for (int v = 0; v < ja.length(); v++) {
                    JSONObject jo = ja.getJSONObject(v);
                    Video video = getVideoFromJsonObject(jo);
                    videos.add(video);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (activity instanceof ChannelActivity) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ChannelActivity) activity).addVideos(videos, lastPermlink, newLastAuthor);
                    }
                });
            }
        }
    }



    @JavascriptInterface
    public void votePostCallback(final int weight,final String permlink){
        if (activity instanceof VideoPlayActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((VideoPlayActivity)activity).setVote(weight, permlink);
                }
            });
        }
    }

    @JavascriptInterface
    public void getIsFollowingCallback(final boolean following, final String author){
        Log.d("following?","S:" +following);
        if (activity instanceof VideoPlayActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((VideoPlayActivity)activity).setIsFollowing(following);
                }
            });
        }else if (activity instanceof ChannelActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ChannelActivity)activity).setIsFollowing(following);
                }
            });
        }

    }

    @JavascriptInterface
    public void getVideoInfoCallback(String json) {
        try {
            if (activity instanceof MainActivity) {
                Log.d("dtube4", json);
                JSONObject o = new JSONObject(json);
                Video video = getVideoFromJsonObject(o);
                Log.d("dtube4", video.getVideoStreamURL());
                video.saveVideoToRecents(activity);
                ((MainActivity) activity).playVideo(video);
            }else if (activity instanceof VideoPlayActivity){
                JSONObject o = new JSONObject(json);
                final Video video = getVideoFromJsonObject(o);
                Log.d("dtube4", video.getVideoStreamURL());
                video.saveVideoToRecents(activity);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((VideoPlayActivity) activity).playVideo(video);
                    }
                });
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    @JavascriptInterface
    public void commentPostCallback(){
//        if (activity instanceof VideoPlayActivity){
//            ((VideoPlayActivity)activity).loadReplies();
//        }
    }

    @JavascriptInterface
    public void getRepliesCallback(String jsonComments){
        try {
            Log.d("KK", "getRepliesCallback:"+jsonComments);
            JSONObject jo = new JSONObject(jsonComments);
            final Comment comment = new Comment();
            comment.indent = jo.getInt("indent");
            comment.permlink = jo.getString("permlink");

            if (jo.has("likes"))
                comment.likes = jo.getInt("likes");
            if (jo.has("dislikes"))
                comment.dislikes = jo.getInt("dislikes");
            comment.commentHTML = jo.getString("comment");
            //comment.commentHTML = Tools.getFormattedText(comment.commentHTML);


            if (jo.get("date") instanceof Long){
                comment.setTimeLong(jo.getLong("date"));
            }else
                comment.setTime(jo.getString("date"));



            if (jo.has("voteType"))
                comment.voteType = jo.getInt("voteType");

            if (jo.has("price"))
                comment.price = jo.getString("price");

            comment.userName = jo.getString("author");
            //comment.children = jo.getInt("children");
            comment.parent = jo.getString("parent");


            if (activity instanceof VideoPlayActivity){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("COMz:",comment.commentHTML);
                        ((VideoPlayActivity)activity).addReply(comment);
                    }
                });
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

//    @JavascriptInterface
//    public void getAllRepliesCallback(String jsonComments){
//        try {
//            JSONArray jarr = new JSONArray(jsonComments);
//            final CommentsList commentsList = new CommentsList();
//            for (int i = 0; i<jarr.length(); i++){
//                JSONObject jo = jarr.getJSONObject(i);
//                Comment comment = new Comment();
//                comment.indent = jo.getInt("indent");
//                comment.permlink = jo.getString("permlink");
//                comment.likes = jo.getInt("likes");
//                comment.dislikes = jo.getInt("dislikes");
//                comment.commentHTML = jo.getString("comment");
//                //comment.commentHTML = Tools.getFormattedText(comment.commentHTML);
//                comment.setTime(jo.getString("date"));
//                comment.voteType = jo.getInt("voteType");
//                comment.price = jo.getString("price");
//                comment.userName = jo.getString("author");
//
//                commentsList.add(comment);
//            }
//
//            if (activity instanceof VideoPlayActivity){
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((VideoPlayActivity)activity).setReplies(commentsList);
//                    }
//                });
//            }
//
//        }catch (JSONException e){
//            e.printStackTrace();
//        }
//    }

    @JavascriptInterface
    public void getSubscriberCountCallback(String account, final int count){
        if (activity instanceof MainActivity)
            ((MainActivity)activity).setSubscribers(account, count);
        else if (activity instanceof VideoPlayActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((VideoPlayActivity)activity).setNumberOfSubscribers(count);
                }
            });
        }else if (activity instanceof ChannelActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ChannelActivity)activity).setNumberOfSubscribers(count);
                }
            });
        }
    }


    @JavascriptInterface
    public void getSubscriptionsCallback(String[] usernames){
        Log.d("dtube9", "getSubscriptionsCallback");
        ArrayList<Person> persons = new ArrayList<>();
        for (String username: usernames) {
            Person p = new Person();
            p.userName = username;
            persons.add(p);
        }

        ((MainActivity)activity).setSubscriptions(persons);
    }

    @JavascriptInterface
    public void getBalanceCallback(String accountName, String balance, String dollarBalance, String votingPower){
        if (activity instanceof ChannelActivity){
            ((ChannelActivity)activity).updateBalances(accountName, balance, dollarBalance, votingPower);
        }
    }



    public void manageFeed(String jsonFeed, int category){
        try {
            VideoArrayList videos = new VideoArrayList();
            JSONArray ja = new JSONArray(jsonFeed);

            for (int v = 0; v<ja.length(); v++){
                JSONObject jo = ja.getJSONObject(v);
                Video video = getVideoFromJsonObject(jo);
                video.categoryId = category;
                videos.add(video);
            }

            Log.d("dtube5", "manageFeed size:" + videos.size() +",category:" + category);

            if (((MainActivity)activity).addVideos(videos)) {
                Log.d("dtube5", "addVideos category:" + category);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) activity).initFeed();
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }





    public Video getVideoFromJsonObject(JSONObject jo){
        Video video = new Video();
        try {
            video.title = jo.getString("title");
            video.user = jo.getString("username");

            if (jo.has("datelong")){
                video.setTimeLong(jo.getLong("datelong"));
            }


            if (jo.has("date")) {
                video.setTime(jo.getString("date"));
            }

            if (jo.has("price")){
                video.price = jo.getString("price");
            }

            if (jo.has("hash")) {
                video.hash = jo.getString("hash");
            }

            if (jo.has("snaphash"))
                video.snapHash = jo.getString("snaphash");

            if (jo.has("thumbnailUrl"))
                video.setImageURL(jo.getString("thumbnailUrl"));

            if (jo.has("provider"))
                video.setProvider(jo.getString("provider"));

            video.permlink = jo.getString("permlink");

            if (jo.has("duration")){
                video.setDuration(jo.getString("duration"));
            }

            if (jo.has("platform")){
                video.setPlatform(jo.getString("platform"));
            }

            if (jo.has("blockchain"))
                video.blockchain = jo.getInt("blockchain");

            if (jo.has("gateway")){
                video.setGateway(jo.getString("gateway"));
            }

            if (jo.has("priority_gw")){
                video.setPriorityGateway(jo.getString("gateway"));
            }

            if (jo.has("description")) {
                video.longDescriptionHTML = jo.getString("description");
                //video.longDescriptionHTML = Tools.getFormattedText(video.longDescriptionHTML);
            }
            if (jo.has("voteType"))
                video.voteType = jo.getInt("voteType");
            if (jo.has("dislikes"))
                video.dislikes = jo.getInt("dislikes");
            if (jo.has("likes"))
                video.likes = jo.getInt("likes");
            return video;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}