package com.powerpoint45.dtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    static final String PROVIDER_APPICS = "Appics";

    static final String PLATFORM_STEEMIT = "https://api.steemit.com";
    static final String PLATFORM_HIVE = "https://api.hive.blog";

    //public static String PROFILE_IMG_URL = "https://img.busy.org/@";//https://img.busy.org/@lukewearechange//too slow, useing steemitimages
    //static String PROFILE_IMAGE_SMALL_STEEM_URL = "https://steemitimages.com/u/username/avatar/small";
    //static String PROFILE_IMAGE_SMALL_AVALON_URL = "https://avalon.d.tube/image/avatar/username/medium";//replace username with actual username
    static String PROFILE_IMAGE_MEDIUM_AVALON_URL = "https://dtube.fso.ovh/image/avatar/username/medium";//replace username with actual username
    static String PROFILE_IMAGE_MEDIUM_STEEM_URL = "https://steemitimages.com/u/username/avatar/medium";//replace username with actual username
    static String PROFILE_IMAGE_MEDIUM_HIVE_URL = "https://images.hive.blog/u/username/avatar";//replace username with actual username

    public String PROFILE_IMAGE_LARGE_URL = "https://dtube.fso.ovh/image/avatar/username/large";//replace username with actual username
    static String CONTENT_IMAGE_URL_BAK = "https://steemitimages.com/0x0/https://ipfs.io/ipfs/"; // hash at end
    static String CONTENT_IMAGE_URL = "https://snap1.d.tube/ipfs/";
    static String DTUBE_LOGIN_URL = "https://d.tube/#!/login";
    static String DTUBE_HOME_URL = "https://d.tube/#!";
    static String DTUBE_UPLOAD_URL = "https://d.tube/#!/upload";
    static String DTUBE_PUBLISH_URL = "https://d.tube/#!/publish";
    static String DTUBE_VIDEO_URL = "https://d.tube/#!/v/";

    static final String PROVIDER_API_URL_AVALON = "file:///android_res/raw/avalon.html";
    static final String PROVIDER_API_URL_HIVE = "file:///android_res/raw/hivemobile.html";
    static final String PROVIDER_API_URL_STEEM = "file:///android_res/raw/steemit.html";


    public final static int NET_SELECT_AVION = 1;
    public final static int NET_SELECT_HIVE = 2;
    public final static int NET_SELECT_STEEM = 3;


    static String getUserPrivateKey(Context c, int networkNum){
        Encryption encryption = new Encryption(c);
        return encryption.decryptString("privateKeyWif"+networkNum, networkNum);
    }

    static String getAccountName(Context c){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        return sharedPref.getString("username",null);
    }

    public static int getNetworkNumber(String selectedNet){

        if (selectedNet.equals(DtubeAPI.PROVIDER_API_URL_STEEM))
            return DtubeAPI.NET_SELECT_STEEM;
        else if (selectedNet.equals(DtubeAPI.PROVIDER_API_URL_HIVE))
            return DtubeAPI.NET_SELECT_HIVE;
        else if (selectedNet.equals(DtubeAPI.PROVIDER_API_URL_AVALON))
            return DtubeAPI.NET_SELECT_AVION;

        return 0;
    }

    static String getAccountName(Context c, int network){

        String username = null;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);

        switch (network){
            case DtubeAPI.NET_SELECT_AVION:
            case DtubeAPI.NET_SELECT_HIVE:
                username =  sharedPref.getString("username"+network,null);
                break;

            case DtubeAPI.NET_SELECT_STEEM:
                username =  sharedPref.getString("username",null);

                if (username==null)
                    username = sharedPref.getString("username"+network,null);

                break;
        }




        return username;
    }

    static String getAPIString(int api){
        switch (api) {
            case DtubeAPI.NET_SELECT_AVION:
                return PROVIDER_API_URL_AVALON;
            case DtubeAPI.NET_SELECT_HIVE:
                return PROVIDER_API_URL_HIVE;
            case DtubeAPI.NET_SELECT_STEEM:
                return PROVIDER_API_URL_STEEM;
        }

        return null;
    }

    static String getSelectedAPI(Context c){
        String defaultUrl = DtubeAPI.PROVIDER_API_URL_AVALON;

        if (getAccountName(c)!=null)
            defaultUrl = DtubeAPI.PROVIDER_API_URL_STEEM;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        return prefs.getString("selectedapi", defaultUrl);
    }

    static String getProfileImage(String username){
        switch (Preferences.selectedAPI){
            case PROVIDER_API_URL_AVALON:
                return DtubeAPI.PROFILE_IMAGE_MEDIUM_AVALON_URL.replace("username", username);
            case PROVIDER_API_URL_HIVE:
                Log.d("ddd",DtubeAPI.PROFILE_IMAGE_MEDIUM_HIVE_URL.replace("username", username));
                return DtubeAPI.PROFILE_IMAGE_MEDIUM_HIVE_URL.replace("username", username);
            case PROVIDER_API_URL_STEEM:
                return DtubeAPI.PROFILE_IMAGE_MEDIUM_STEEM_URL.replace("username", username);
        }

        return DtubeAPI.PROFILE_IMAGE_MEDIUM_AVALON_URL.replace("username", username);
    }

    static void saveSelectedAPI(Context c, String url){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putString("selectedapi", url).apply();
    }

//    static void saveUserCredentials(String username, String privateKey, Context c){
//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
//        sharedPref.edit().putString("username", username).apply();
//
//        Encryption encryption = new Encryption(c);
//        encryption.encryptString("privateKeyWif", privateKey);
//    }

    static void saveUserCredentials(String username, String privateKey, int networkNum, Context c){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().putString("username"+networkNum, username).apply();
        Log.d("dtubez", username+"  ,  "+networkNum);

        Encryption encryption = new Encryption(c);
        encryption.encryptString("privateKeyWif", privateKey, networkNum);
    }

    static void logout(Context c, int network){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().remove("cypher"+network).remove("username"+network).apply();
    }

    static void logoutAll(Context c){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref.edit().remove("cypher").remove("cypher"+DtubeAPI.NET_SELECT_AVION).remove("cypher"+DtubeAPI.NET_SELECT_STEEM).remove("cypher"+DtubeAPI.NET_SELECT_HIVE)
                .remove("username").remove("username"+DtubeAPI.NET_SELECT_STEEM).remove("username"+DtubeAPI.NET_SELECT_HIVE).remove("username"+DtubeAPI.NET_SELECT_AVION).apply();
    }
    
}
