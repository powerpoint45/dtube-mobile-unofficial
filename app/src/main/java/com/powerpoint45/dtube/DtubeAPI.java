package com.powerpoint45.dtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by michael on 5/11/17.
 */

public class DtubeAPI {

    static final int CAT_SUBSCRIBED = 0;
    static final int CAT_HOT = 1;
    static final int CAT_TRENDING = 2;
    static final int CAT_NEW = 3;
    static final int CAT_HISTORY = 4;

    static final String PROVIDER_IPFS = "IPFS";
    static final String PROVIDER_BTFS = "BTFS";
    static final String PROVIDER_YOUTUBE = "YouTube";
    static final String PROVIDER_TWITCH = "Twitch";
    static final String PROVIDER_SKYNET = "Sia Skynet";
    static final String PROVIDER_3SPEAK = "3speak";



    //public static String PROFILE_IMG_URL = "https://img.busy.org/@";//https://img.busy.org/@lukewearechange//too slow, useing steemitimages
    static String PROFILE_IMAGE_SMALL_URL = "https://steemitimages.com/u/username/avatar/small";//replace username with actual username
    static String PROFILE_IMAGE_MEDIUM_URL = "https://steemitimages.com/u/username/avatar/medium";//replace username with actual username
    public String PROFILE_IMAGE_LARGE_URL = "https://steemitimages.com/u/username/avatar/large";//replace username with actual username
    static String CONTENT_IMAGE_URL_BAK = "https://steemitimages.com/0x0/https://ipfs.io/ipfs/"; // hash at end
    static String CONTENT_IMAGE_URL = "https://snap1.d.tube/ipfs/";
    static String DTUBE_LOGIN_URL = "https://d.tube/#!/login";
    static String DTUBE_HOME_URL = "https://d.tube/#!";
    static String DTUBE_UPLOAD_URL = "https://d.tube/#!/upload";
    static String DTUBE_PUBLISH_URL = "https://d.tube/#!/publish";
    static String DTUBE_VIDEO_URL = "https://d.tube/#!/v/";


    static String getUserPrivateKey(Context c){
        Encryption encryption = new Encryption(c);
        return encryption.decryptString("privateKeyWif");
    }

    static String getAccountName(Context c){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString("username",null);
    }

    static void saveUserCredentials(String username, String privateKey, Context c){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().putString("username", username).apply();

        Encryption encryption = new Encryption(c);
        encryption.encryptString("privateKeyWif", privateKey);
    }

    static void logout(Context c){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().remove("cypher").remove("username").apply();
    }
    
}
