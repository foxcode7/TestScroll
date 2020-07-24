package com.example.testscroll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class WebViewPager extends ViewPager {
    private ContentGestureDetector detector;

    public WebViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * todo 临时方案
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = super.dispatchTouchEvent(ev);
        if (getContext() == null) {
            return result;
        }
        if (detector == null) {
            detector = new ContentGestureDetector(getContext(), new ContentGestureDetector.Callback() {
                @Override
                public void onScrollLeft() {
                    setCurrentItem(1);
                }

                @Override
                public void onScrollRight() {
                    setCurrentItem(0);
                }
            });
        }
        detector.onTouchEvent(ev);
        return result;
    }
}
