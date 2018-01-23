package com.powerpoint45.dtube;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class Comment implements Serializable {
    String commentHTML;
//    String authorURL;
    String userName;
    private String date;
    private long time;
    String price;
    int likes;
    int dislikes;
    int children;

    int indent;
    String permlink;
    String parent;

    //0=no vote
    //1=vote up
    //-1=vote down
    int voteType;

    CommentsList childComments;

    String getImageURL(){
        return DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username",userName);
    }

    @SuppressLint("SimpleDateFormat")
    public void setTime(String timeUnformatted){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //2011-07-27T06:41:11+00:00
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = formatter.parse(timeUnformatted);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        if (date!=null)
            time = date.getTime();
    }

    long getDate(){
        return time;
    }
}