package com.powerpoint45.dtube;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    boolean tvMode;
    static SharedPreferences pref;

    //variables to help deal with resetting the theme when darkmode is changed
    //This is a difficult task due to all the fragments etc
    static Activity uiActivity;
    static boolean inUIActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Preferences.darkMode)
            setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);

        Log.d("dtube","pref oncreate" +pref);


        if (pref == null) {
            pref = PreferenceManager.getDefaultSharedPreferences(this);
            pref.registerOnSharedPreferenceChangeListener(this);
        }

    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;
        if (uiModeManager.getCurrentModeType()== Configuration.UI_MODE_TYPE_TELEVISION)
            tvMode = true;

        if (tvMode){
            loadHeadersFromResource(R.xml.preference_header_tv, target);
        }else
            loadHeadersFromResource(R.xml.preference_header, target);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("dtube","pref changed "+key);
        switch (key){
            case "dark_mode":
                if (pref!=null)
                    pref.unregisterOnSharedPreferenceChangeListener(this);
                Preferences.darkMode = sharedPreferences.getBoolean("dark_mode", false);
                MainActivity.changedDarkMode = true;
                if (uiActivity!=null)
                    uiActivity.finish();
                finish();
                startActivity(new Intent(PreferencesActivity.this, PreferencesActivity.class));
                break;
            default:
                break;
        }
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs1_fragment);
            inUIActivity = true;
            uiActivity = getActivity();
        }
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class Prefs1FragmentInner extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs2_fragment);
        }

    }

    /**
     * This fragment shows the preferences for the second header.
     */
    public static class Prefs2Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from headers XML.
            Log.i("args", "Arguments: " + getArguments());

            // Load the preferences from an XML resource
           addPreferencesFromResource(R.xml.prefs2_fragment);
        }
    }

    private final String[] FRAGMENTS = {
            "com.powerpoint45.dtube.PreferencesActivity$Prefs1Fragment",
            "com.powerpoint45.dtube.PreferencesActivity$Prefs2Fragment",
            "com.powerpoint45.dtube.PreferencesActivity$Prefs1FragmentInner",
            "com.powerpoint45.dtube.PreferencesActivity$Prefs2FragmentInner",
    };


    @Override
    protected boolean isValidFragment(String fragmentName) {
        Log.d("dtube",fragmentName);

        for (String FRAGMENT : FRAGMENTS) {
            if (FRAGMENT.equals(fragmentName)) {
                return true;
            }
        }
        return super.isValidFragment(fragmentName);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
    }

    @Override
    public void finish(){
        if (!inUIActivity) {
            if (pref != null)
                pref.unregisterOnSharedPreferenceChangeListener(this);
            pref = null;
        }else
            inUIActivity = false;
        super.finish();
    }

}
