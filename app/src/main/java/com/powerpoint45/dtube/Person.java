package com.powerpoint45.dtube;

/**
 * Created by michael on 8/11/17.
 */

public class Person {
    String userName;

    public String getImageURL(){
        return DtubeAPI.PROFILE_IMAGE_SMALL_URL.replace("username",userName);
    }

    public String getURL(){
        return "https://d.tube/#!/c/" + userName+"/";
    }

}
