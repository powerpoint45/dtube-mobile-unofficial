package com.powerpoint45.dtube;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

/**
 * Created by michael on 18/11/17.
 */
class CustomMenuTarget implements Target {

    Context c;
    MenuItem m;
    private List<CustomMenuTarget> targets;

    CustomMenuTarget(MenuItem menuItem, Context c, List<CustomMenuTarget> targets){
        this.c = c;
        this.m = menuItem;
        this.targets = targets;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        Log.d("DEBUG", "onBitmapLoaded");
        BitmapDrawable mBitmapDrawable = new BitmapDrawable(c.getResources(), bitmap);
        //                                mBitmapDrawable.setBounds(0,0,24,24);
        // setting icon of Menu Item or Navigation View's Menu Item
        m.setIcon(mBitmapDrawable);
        targets.remove(this);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        Log.d("DEBUG", "onBitmapFailed");
        targets.remove(this);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        Log.d("DEBUG", "onPrepareLoad");
    }

}
