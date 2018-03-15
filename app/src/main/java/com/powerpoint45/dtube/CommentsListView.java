package com.powerpoint45.dtube;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created by michael on 14/03/18.
 */

public class CommentsListView extends ListView {
    public CommentsListView(Context context) {
        super(context);
    }

    public CommentsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    float lastTouch = -1;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if ((lastTouch!=-1 && (lastTouch-ev.getY())<0 && computeVerticalScrollOffset() == 0)
                ||(lastTouch!=-1 && (lastTouch-ev.getY())>0 && !canScrollVertically(1))){
            getParent().requestDisallowInterceptTouchEvent(false);
        }else {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL)
            lastTouch = -1;
        else
            lastTouch = ev.getY();
        return super.onInterceptTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((lastTouch!=-1 && (lastTouch-ev.getY())<0 && computeVerticalScrollOffset() == 0)
                ||(lastTouch!=-1 && (lastTouch-ev.getY())>0 && !canScrollVertically(1))){
            getParent().requestDisallowInterceptTouchEvent(false);

        }else {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL)
            lastTouch = -1;
        else
            lastTouch = ev.getY();
        return super.onTouchEvent(ev);
    }


}
