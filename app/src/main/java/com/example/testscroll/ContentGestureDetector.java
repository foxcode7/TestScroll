package com.example.testscroll;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class ContentGestureDetector implements GestureDetector.OnGestureListener {

    public interface Callback {
        void onScrollLeft();

        void onScrollRight();
    }

    private float slop;
    private GestureDetector detector;
    private Callback callback;

    private float scrollX;
    private float scrollY;
    private boolean isListening = false;

    public ContentGestureDetector(Context context, Callback callback) {
        this.detector = new GestureDetector(context, this);
        this.callback = callback;
        this.slop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isListening = true;
                scrollX = 0;
                scrollY = 0;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isListening = false;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isListening) {
            return false;
        }
        scrollX += distanceX;
        scrollY += distanceY;

        // Angle between this and the horizontal is less than 30 degree
        if(Math.sqrt(3) * Math.abs(scrollY) > Math.abs(scrollX)) {
            isListening = false;
        } else {
            if (distanceX > 0 && Math.abs(scrollX) > slop) {
                notifyScroll(distanceX);
                isListening = false;
            } else if (distanceX < 0 && Math.abs(scrollX) > slop) {
                notifyScroll(distanceX);
                isListening = false;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private void notifyScroll(float x) {
        if (x > 0) {
            callback.onScrollLeft();
        } else {
            callback.onScrollRight();
        }
    }
}