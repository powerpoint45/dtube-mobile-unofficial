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
        if (activity instanceof LoginActivity)
            ((LoginActivity)activity).gotLoginResult(sucess);
        else
            Log.d("dtube4", sucess ? "logged in":"login failed");
    }

    @JavascriptInterface
    public void getHotVideosFeedCallback(String jsonVideos){
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
    public void getAuthorVideosCallback(String jsonVideos, final String lastPermlink){

        if (jsonVideos.equals("last") && lastPermlink.equals("last")){
            if (activity instanceof ChannelActivity){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ChannelActivity)activity).addVideos(null, lastPermlink);
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
                        ((ChannelActivity) activity).addVideos(videos, lastPermlink);
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
    public void getIsFollowingCallback(final boolean following, String author){

        if (activity instanceof VideoPlayActivity){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((VideoPlayActivity)activity).setIsFollowing(following);
                }
            });
        }

    }

    @JavascriptInterface
    public void getVideoInfoCallback(String json) {
        try {
            if (activity instanceof MainActivity) {
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
    public void getRepliesCallback(String jsonComments){
        try {
            JSONObject jo = new JSONObject(jsonComments);
            final Comment comment = new Comment();
            comment.indent = jo.getInt("indent");
            comment.permlink = jo.getString("permlink");
            comment.likes = jo.getInt("likes");
            comment.dislikes = jo.getInt("dislikes");
            comment.commentHTML = jo.getString("comment");
            //comment.commentHTML = Tools.getFormattedText(comment.commentHTML);
            comment.setTime(jo.getString("date"));
            comment.voteType = jo.getInt("voteType");
            comment.price = jo.getString("price");
            comment.userName = jo.getString("author");
            comment.children = jo.getInt("children");
            comment.parent = jo.getString("parent");


            if (activity instanceof VideoPlayActivity){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
        }
    }


    @JavascriptInterface
    public void getSubscriptionsCallback(String[] usernames){
        ArrayList<Person> persons = new ArrayList<>();
        for (String username: usernames) {
            Person p = new Person();
            p.userName = username;
            persons.add(p);
        }

        ((MainActivity)activity).setSubscriptions(persons);
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


            if (((MainActivity)activity).addVideos(videos)) {
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
            video.price = jo.getString("price");
            video.setTime(jo.getString("date"));
            video.hash = jo.getString("hash");
            video.snapHash = jo.getString("snaphash");
            video.permlink = jo.getString("permlink");
            if (jo.has("gateway")){
                video.setGateway(jo.getString("gateway"));
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