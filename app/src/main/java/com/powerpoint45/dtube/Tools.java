package com.powerpoint45.dtube;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by michael on 20/11/17.
 */

public class Tools {

    @SuppressWarnings("deprecation")
    static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static int numtodp(int in, Activity activity) {
        int out = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, in, activity.getResources()
                        .getDisplayMetrics());
        return out;
    }

    public static float dptopx(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

//    static String getFormattedText(String unformatted){
//
//
//        Pattern httpLinkPattern = Pattern.compile("(http[s]?)://(www\\.)?([\\S&&[^.@]]+)(\\.[\\S&&[^@]]+)");
//
//        Pattern wwwLinkPattern = Pattern.compile("(?<!http[s]?://)(www\\.+)([\\S&&[^.@]]+)(\\.[\\S&&[^@]]+)");
//
//        Pattern mailAddressPattern = Pattern.compile("[\\S&&[^@]]+@([\\S&&[^.@]]+)(\\.[\\S&&[^@]]+)");
//
//
//        if (unformatted!=null) {
//            if (unformatted.startsWith("<center><a")){
//                unformatted = unformatted.substring(unformatted.indexOf("</center>")+9, unformatted.length());
//            }
//            if (unformatted.endsWith("(IPFS)</a>")){
//                unformatted = unformatted.substring(0,unformatted.lastIndexOf("<a href='https://d.tube/#!/v/"));
//            }
//
//
//            unformatted = unformatted.replaceAll("\n"," <br />");
//
//            Matcher httpLinksMatcher = httpLinkPattern.matcher(unformatted);
//            unformatted = httpLinksMatcher.replaceAll("<a href=\"$0\" target=\"_blank\">$0</a>");
//
//            final Matcher wwwLinksMatcher = wwwLinkPattern.matcher(unformatted);
//            unformatted = wwwLinksMatcher.replaceAll("<a href=\"http://$0\" target=\"_blank\">$0</a>");
//
//            final Matcher mailLinksMatcher = mailAddressPattern.matcher(unformatted);
//            unformatted = mailLinksMatcher.replaceAll("<a href=\"mailto:$0\">$0</a>");
//        }
//
//        return unformatted;
//    }



}
