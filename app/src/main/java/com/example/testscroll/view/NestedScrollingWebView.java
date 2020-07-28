package com.example.testscroll.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Scroller;

import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import com.example.testscroll.util.DimenHelper;

public class NestedScrollingWebView extends WebView implements NestedScrollingChild2 {
    private boolean mIsSelfFling;
    private boolean mHasFling;

    private final int TOUCH_SLOP;
    private int mMaximumVelocity;
    private int mFirstY;
    private int mLastY;
    private int mMaxScrollY;
    private int mWebViewContentHeight;
    private int mJsCallWebViewContentHeight;

    private final int[] mScrollConsumed = new int[2];

    private final float DENSITY;

    private NestedScrollingChildHelper mChildHelper;
    private NestedScrollingDetailContainer mParentView;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private boolean canScroll;

    public NestedScrollingWebView(Context context) {
        this(context, null);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollingWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        TOUCH_SLOP = configuration.getScaledTouchSlop();
        DENSITY = context.getResources().getDisplayMetrics().density;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    /**
     * set web content height from js callback, resolve problem that inner computing result is not accurate
     */
    public void setJsCallWebViewContentHeight(int webViewContentHeight) {
        if (webViewContentHeight > 0 && webViewContentHeight != mJsCallWebViewContentHeight) {
            mJsCallWebViewContentHeight = webViewContentHeight;
            if (mJsCallWebViewContentHeight < getHeight()) {
                // if inner height < container height, adjust container height to inner height
                DimenHelper.updateLayout(this, DimenHelper.NOT_CHANGE, mJsCallWebViewContentHeight);
            }
        }
    }

    public int getWebViewContentHeight() {
        if (mWebViewContentHeight == 0) {
            mWebViewContentHeight = mJsCallWebViewContentHeight;
        }

        if (mWebViewContentHeight == 0) {
            mWebViewContentHeight = (int) (getContentHeight() * DENSITY);
        }

        if (!canScroll) {
            // 有 ReadMore 的时候只能滚动 2 屏
            mWebViewContentHeight = (int) (computeVerticalScrollExtent() * 2);
        } else {
            mWebViewContentHeight = (int) (getContentHeight() * DENSITY);
        }
        return mWebViewContentHeight;
    }

    public boolean canScrollDown() {
        final int range = getWebViewContentHeight() - getHeight();
        if (range <= 0) {
            return false;
        }

        final int offset = getScrollY();
        return offset < range - TOUCH_SLOP;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mWebViewContentHeight = 0;
                mLastY = (int) event.getRawY();
                mFirstY = mLastY;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                initOrResetVelocityTracker();
                mIsSelfFling = false;
                mHasFling = false;
                mMaxScrollY = getWebViewContentHeight() - getHeight();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(event);
                int y = (int) event.getRawY();
                int dy = y - mLastY;
                mLastY = y;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (!dispatchNestedPreScroll(0, -dy, mScrollConsumed, null)) {
                    scrollBy(0, -dy);
                }
                if (Math.abs(mFirstY - y) > TOUCH_SLOP) {
                    // shielding scroll of web view self, fling event deal by itself
                    event.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int yVelocity = (int) -mVelocityTracker.getYVelocity();
                    if(isParentResetScroll()) {
                        recycleVelocityTracker();
                        mIsSelfFling = true;
                        flingScroll(0, yVelocity);
                    }
                }
                break;
        }
        super.onTouchEvent(event);
        return true;
    }

    @Override
    public void flingScroll(int vx, int vy) {
        mScroller.fling(0, getScrollY(), 0, vy, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recycleVelocityTracker();
        stopScroll();
        mChildHelper = null;
        mScroller = null;
        mParentView = null;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int currY = mScroller.getCurrY();
            if (!mIsSelfFling) {
                // parent fling
                scrollTo(0, currY);
                invalidate();
                return;
            }

            if (isWebViewCanScroll()) {
                scrollTo(0, currY);
                invalidate();
            }
            if (!mHasFling && mScroller.getStartY() < currY && !canScrollDown()
                    && startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                    && !dispatchNestedPreFling(0, mScroller.getCurrVelocity())) {
                // when fling reach web view bottom, pass this event to parent and recycler
                mHasFling = true;
                dispatchNestedFling(0, mScroller.getCurrVelocity(), false);
            }
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        // update max scrollY for read more scroll
        mMaxScrollY = getWebViewContentHeight() - getHeight();

        if (y < 0) {
            y = 0;
        }
        if (mMaxScrollY != 0 && y > mMaxScrollY) {
            y = mMaxScrollY;
        }
        if (isParentResetScroll()) {
            super.scrollTo(x, y);
        }
    }

    void scrollToBottom() {
        int y = getWebViewContentHeight();
        super.scrollTo(0, y - getHeight());
    }

    private NestedScrollingChildHelper getNestedScrollingHelper() {
        if (mChildHelper == null) {
            mChildHelper = new NestedScrollingChildHelper(this) {

            };
        }
        return mChildHelper;
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void initWebViewParent() {
        if (this.mParentView != null) {
            return;
        }
        View parent = (View) getParent();
        while (parent != null) {
            if (parent instanceof NestedScrollingDetailContainer) {
                this.mParentView = (NestedScrollingDetailContainer) parent;
                break;
            } else {
                parent = (View) parent.getParent();
            }
        }
    }

    private boolean isParentResetScroll() {
        if (mParentView == null) {
            initWebViewParent();
        }
        if (mParentView != null) {
            return mParentView.getScrollY() == 0;
        }
        return true;
    }

    private void stopScroll() {
        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    private boolean isWebViewCanScroll() {
        return getWebViewContentHeight() > getHeight();
    }

    /****** NestedScrollingChild2 BEGIN ******/

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getNestedScrollingHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getNestedScrollingHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getNestedScrollingHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getNestedScrollingHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getNestedScrollingHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return getNestedScrollingHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return getNestedScrollingHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getNestedScrollingHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getNestedScrollingHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getNestedScrollingHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        getNestedScrollingHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getNestedScrollingHelper().hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return getNestedScrollingHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return getNestedScrollingHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }
}
