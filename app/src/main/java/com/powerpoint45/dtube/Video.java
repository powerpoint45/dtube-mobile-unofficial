package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.content.Context;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class Video implements Serializable{
    String title;
    String user;
    String price;
    String permlink;
    private long time;
    int categoryId;
    String hash;
    String snapHash;
    private String duration;

    String longDescriptionHTML;
    String subscribers;
    private String gateway;


    //0=no vote
    //1=vote up
    //-1=vote down
    int voteType;

    int likes;
    int dislikes;


    //replace AUTHOR,PERMLINK,and GATEWAY with actual values
    //https://skzap.github.io/embedtube/#!/AUTHOR/PERMLINK/true/true/GATEWAY
    private static String VIDEO_FRAME_URL = "file:///android_res/raw/embed.html#!/AUTHOR/PERMLINK/true/true";
    //private static String VIDEO_FRAME_URL = "https://skzap.github.io/embedtube/#!/AUTHOR/PERMLINK/true/true/GATEWAY";

    private final String[] GATEWAYS = new String[]{
            "https://ipfs.infura.io",
            "https://ipfs.io",
            "https://gateway.ipfs.io",
            "https://scrappy.i.ipfs.io",
            "https://chappy.i.ipfs.io"
    };

    void setGateway(String gw){
        gateway = gw;
    }

    private String getGateway(){
        if (gateway!=null) {
            return gateway;
        }else {
            int g = hash.charAt(hash.length() - 1) % GATEWAYS.length;
            return GATEWAYS[g].split("://")[1];
        }
    }

    String getVideoFrameUrl(){
        return VIDEO_FRAME_URL.replace("AUTHOR",user).replace("PERMLINK",permlink);
    }

    String getVideoStreamURL() {
        return "https://" + getGateway() + "/ipfs/" + hash;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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

//    public void setFormattedTime(long time){
//            this.time = time;
//    }

    long getDate(){
            return time;
    }


    String getImageURL(){
            return DtubeAPI.CONTENT_IMAGE_URL + snapHash;
    }


    static void removeVideoFromRecents(String permlink, Context c){
        VideoArrayList v = getRecentVideos(c);
        if (v!=null){
            v.remove(v.findVideo(permlink));
            Video.saveRecentsList(v,c);
        }
    }

    private static void saveRecentsList(VideoArrayList videos, Context c){
        FileOutputStream fos;
        try {
            fos = c.openFileOutput("recentsVideos", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(videos);
            os.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveVideoToRecents(Context c){
        VideoArrayList videos = getRecentVideos(c);
        if (videos==null)
            videos = new VideoArrayList();

        if (!videos.containsVideo(this)) {
            videos.add(0, this);
            saveRecentsList(videos,c);
        }
    }

    static VideoArrayList getRecentVideos(Context context) {
        ObjectInputStream inputStream = null;

        try {
            // Construct the ObjectInputStream object
            inputStream = new ObjectInputStream(context.openFileInput("recentsVideos"));

            Object obj = inputStream.readObject();

            if (obj instanceof VideoArrayList) {
                VideoArrayList videos = (VideoArrayList) obj;
                for (Video v:videos) {
                    v.categoryId = DtubeAPI.CAT_HISTORY;
                    v.price = null;
                }
                return videos;
            }
            else
                return null;

        } catch (EOFException | ClassNotFoundException ex) { // This exception will be caught when EOF is
            ex.printStackTrace();
        } catch (FileNotFoundException ignored) {
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

//        public String getHash(){
//            if (videoStreamURL!=null)
//                return videoStreamURL.substring(videoStreamURL.indexOf("/ipfs/")+6);
//            return null;
//        }
}