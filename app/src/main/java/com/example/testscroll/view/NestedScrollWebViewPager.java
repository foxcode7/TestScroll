package com.example.testscroll.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.example.testscroll.ContentGestureDetector;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class NestedScrollWebViewPager extends ViewPager {
    private NestedScrollDetailContainer mParentView;
    private ContentGestureDetector detector;

    private boolean isHorizontalGestureEffect = false;

    public boolean isHorizontalGestureEffect() {
        return isHorizontalGestureEffect;
    }

    public void setHorizontalGestureEffect(boolean horizontalGestureEffect) {
        isHorizontalGestureEffect = horizontalGestureEffect;
    }

    public NestedScrollWebViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

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
                    changeCurrentItem(1);
                }

                @Override
                public void onScrollRight() {
                    changeCurrentItem(0);
                }
            });
        }
        detector.onTouchEvent(ev);
        return result;
    }

    private void changeCurrentItem(int position) {
        isHorizontalGestureEffect = true;
        setCurrentItem(position);
        if(getAdapter() instanceof NestedScrollWebViewPagerAdapter) {
            NestedScrollWebView currentWebView = ((NestedScrollWebViewPagerAdapter) getAdapter()).getNestedScrollWebViews().get(position);
            if(!currentWebView.isCanScroll()) {
                if(mParentView == null) {
                    initWebViewParent();
                }
                mParentView.scrollTo(0, 0);
                currentWebView.scrollTo(0, 0);
            } else {
                if(!currentWebView.canScrollDown()) {
                    currentWebView.scrollToBottom();
                }
            }
        }
    }

    private void initWebViewParent() {
        if (this.mParentView != null) {
            return;
        }
        View parent = (View) getParent();
        while (parent != null) {
            if (parent instanceof NestedScrollDetailContainer) {
                this.mParentView = (NestedScrollDetailContainer) parent;
                break;
            } else {
                parent = (View) parent.getParent();
            }
        }
    }
}
