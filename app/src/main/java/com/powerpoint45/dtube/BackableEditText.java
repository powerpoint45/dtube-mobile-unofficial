package com.powerpoint45.dtube;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by michael on 13/11/17.
 */

public class BackableEditText extends AppCompatEditText {
    public BackableEditText(Context context) {
        super(context);
    }

    public BackableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BackableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            ((VideoPlayActivity)getContext()).editTextBacked();
            return true;
        }
        return false;
    }


}
