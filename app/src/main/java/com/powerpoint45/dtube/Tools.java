package com.powerpoint45.dtube;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static android.content.Context.UI_MODE_SERVICE;

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

    static int numtodp(int in, Activity activity) {
        int out = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, in, activity.getResources()
                        .getDisplayMetrics());
        return out;
    }

    static float dptopx(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;
        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found = false;
        }
        return found;
    }

    //check if device can run in Picture In Picture mode
    static boolean deviceSupportsPIPMode(Context c){
        PackageManager packageManager = c.getApplicationContext().getPackageManager();
        boolean supportsPIP = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            supportsPIP = packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE);
        }

        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(UI_MODE_SERVICE);
        boolean runningOnTV = uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION;

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !runningOnTV && supportsPIP;
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
