package com.example.testscroll.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author foxcoder
 * @since 2020-07-24
 */
public class OuterFrameLayout extends FrameLayout {

    public OuterFrameLayout(@NonNull Context context) {
        super(context);
    }

    public OuterFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OuterFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
        return true;
    }

    private float lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = event.getY() - lastY;
                setY(getY() - dy);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }
}
