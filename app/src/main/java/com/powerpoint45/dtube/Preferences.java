package com.powerpoint45.dtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    public static boolean darkMode;


    public static void loadPreferences(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (!prefs.contains("dark_mode"))
            prefs.edit().putBoolean("dark_mode",false).apply();
        else
            darkMode = PreferenceManager.getDefaultSharedPreferences(c).getBoolean("dark_mode",false);
    }

}
