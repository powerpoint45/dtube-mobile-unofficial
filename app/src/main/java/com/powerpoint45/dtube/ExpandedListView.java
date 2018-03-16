package com.powerpoint45.dtube;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ExpandedListView extends ListView{

	public ExpandedListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public ExpandedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ExpandedListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
    }
	
	float startY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e)
    {
        onTouchEvent(e);
        if (e.getAction() == MotionEvent.ACTION_DOWN) startY = e.getY();
        return (e.getAction() == MotionEvent.ACTION_MOVE) && (Math.abs(startY - e.getY()) > 50);
    }

}
