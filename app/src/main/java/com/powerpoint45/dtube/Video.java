package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

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
    private long time = 0;
    int categoryId;
    String hash;
    String snapHash;
    String imageURL;
    private String duration;
    String provider;//such as YouTube or FaceBook Videos

    String longDescriptionHTML;
    String subscribers;
    private String gateway;
    private String priorityGateway;
    private boolean triedLoadingBackupGateway;
    private String platform;

    int blockchain = DtubeAPI.NET_SELECT_AVION;


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
            "https://player.d.tube/ipfs/",
            "https://ipfs.d.tube/ipfs/",
            "https://video.oneloveipfs.com/ipfs/",
            "https://ipfs.infura.io/ipfs/",
            "https://gateway.temporal.cloud/ipfs/",
            "https://gateway.pinata.cloud/ipfs/",
            "https://ipfs.eternum.io/ipfs/",
            "https://ipfs.io/ipfs/"
    };

    void setGateway(String gw){
        if (gw.startsWith("https://") || gw.startsWith("http://"))
            gateway = gw;
        else
            gateway = "https://" + gw;
    }

    void setPriorityGateway(String gw){
        priorityGateway = gw;
    }

    private String getPriorityGateway(){
        return priorityGateway;
    }

    private String getGateway(){
        if (gateway!=null) {
            return gateway;
        }else {
            int g = hash.charAt(hash.length() - 1) % GATEWAYS.length;
            return GATEWAYS[g];
        }
    }

    String getVideoFrameUrl(){
        return VIDEO_FRAME_URL.replace("AUTHOR",user).replace("PERMLINK",permlink);
    }

//    String getVideoStreamURL(int attempt){
//        if (getPriorityGateway()==null)
//            attempt++;
//
//        switch (attempt){
//            case 0:
//                return getPriorityGateway()+"/ipfs/" +hash;
//            case 1:
//                break;
//            case 2:
//                break;
//            case 3:
//                break;
//            case 4:
//                break;
//            case 5:
//                break;
//        }
//
//        return null;
//    }


    String getVideoStreamURL() {
        Log.d("dtsgd", "getVideoStreamURL");
        if (getProvider().equals(DtubeAPI.PROVIDER_SKYNET)){
            return  "https://siasky.net/" + hash;
        }else if (getProvider().equals(DtubeAPI.PROVIDER_BTFS)) {
            return "https://player.d.tube/btfs/" + hash;
        }else

        if (getPriorityGateway()!=null)
            return getPriorityGateway()+"/ipfs/"+hash;

        if (gateway!=null)
            return gateway + "/ipfs/" + hash;

        else if (getDate()>=1544653245000L && !getGateway().equals("video.oneloveipfs.com")) {
            //Ater about Dec 12 dtube started using the new video.dtube.top gateway
            return getPrimaryIPFSURL() + "/ipfs/" + hash;
        }else
            return getBackupVideoStreamURL();
    }

    String getPrimaryIPFSURL(){
        Log.d("dtsgd", "getPrimaryIPFSURL");
        return "https://player.d.tube"+hash;
    }

    String getBackupVideoStreamURL(){
        Log.d("dtsgd", "getBackupVideoStreamURL");
        triedLoadingBackupGateway = true;
        return getGateway() + "/ipfs/" + hash;
    }

    public boolean hasTriedLoadingBackupGateway(){
        return triedLoadingBackupGateway;
    }


    String getDuration() {
        return duration;
    }

    void setDuration(String duration) {
        if (duration.startsWith("00:"))
            duration = duration.substring(3);
        this.duration = duration;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setTimeLong(long timeUnformatted){
        time = timeUnformatted;
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

    public String getProvider() {
        if (provider == null)
            return DtubeAPI.PROVIDER_IPFS;
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


    public void setImageURL(String url) {
        imageURL = url;
    }

    String getImageURL(){

        if (imageURL!=null) {
            return imageURL;
        }else if (getProvider().equals(DtubeAPI.PROVIDER_YOUTUBE)) {
            return "https://img.youtube.com/vi/" + hash + "/mqdefault.jpg";
        }else if (gateway!=null) {
            return gateway + "/ipfs/" + snapHash;
        }else {
            return DtubeAPI.CONTENT_IMAGE_URL + snapHash;
        }
    }

    String getBackupImageURL(){
        return DtubeAPI.CONTENT_IMAGE_URL_BAK + snapHash;
    }


    static void removeVideoFromRecents(String permlink, Context c){
        VideoArrayList v = getRecentVideos(c);
        if (v!=null){
            v.remove(v.findVideo(permlink));
            Video.saveRecentsList(v,c);
        }
    }

    public static void saveRecentsList(VideoArrayList videos, Context c){
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
        Log.d("dtube4","saving video to recents: "+this.title);
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