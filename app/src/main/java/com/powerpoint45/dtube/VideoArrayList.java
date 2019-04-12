package com.powerpoint45.dtube;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by michael on 5/11/17.
 */

class VideoArrayList extends ArrayList<Video>{



    public int getNumberOfCategories(){
        ArrayList<Integer> categories = new ArrayList<>();
        for (int i =0; i<size(); i++){
            if (!categories.contains(get(i).categoryId))
                categories.add(get(i).categoryId);

        }

        return categories.size();
    }

    VideoArrayList getCategorizedVideos(int category){
        VideoArrayList categoryList = new VideoArrayList();
        for (int i =0; i<size(); i++){
            if (get(i).categoryId == category)
                categoryList.add(get(i));
        }
        return categoryList;
    }

    Video findVideo(String permlink){
        for (Video v: this){
            if (v.permlink.equals(permlink))
                return v;
        }
        return null;
    }

    Video findVideo(String permlink, int category){
        for (Video v: this){
            if (v.permlink.equals(permlink) && v.categoryId == category)
                return v;
        }
        return null;
    }

    boolean hasNewContent(VideoArrayList videos){
        for (Video v: videos){
            if (!containsVideo(v)) {
                Log.d("dtube","does not contain "+v.title);
                return true;
            }
        }
        return false;
    }

    boolean containsVideo(Video videoToCheck){
        for (Video v: this){
            if (v.permlink.equals(videoToCheck.permlink) && v.categoryId == videoToCheck.categoryId){
                return true;
            }
        }

        return false;
    }


}
