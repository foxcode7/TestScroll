package com.example.testscroll.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NestedScrollingDetailContainer extends ViewGroup implements NestedScrollingParent2 {
    public static final String TAG_NESTED_SCROLL_WEB_VIEW = "nested_scroll_web_view";
    public static final String TAG_NESTED_SCROLL_RECYCLER_VIEW = "nested_scroll_recycler_view";

    private static final int FLYING_FROM_WEBVIEW_TO_PARENT = 0;
    private static final int FLYING_FROM_PARENT_TO_WEBVIEW = 1;
    private static final int FLYING_FROM_RVLIST_TO_PARENT = 2;

    private boolean mIsSetFlying;
    private boolean mIsRvFlyingDown;
    private boolean mIsBeingDragged;

    private int mMaximumVelocity;
    private int mCurFlyingType;
    private int mInnerScrollHeight; // outer container's max scroll distance
    private final int TOUCH_SLOP;
    private int mScreenWidth;
    private int mLastY;
    private int mLastMotionY;

    private NestedScrollingWebView mChildWebView;
    private RecyclerView mChildRecyclerView;

    private NestedScrollingParentHelper mParentHelper;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private OnYChangedListener onYChangedListener;

    public interface OnYChangedListener {
        void onScrollYChanged(int yDiff);

        void onFlingYChanged(int yVelocity);
    }

    public void setOnYChangedListener(OnYChangedListener onYChangedListener) {
        this.onYChangedListener = onYChangedListener;
    }

    public NestedScrollingDetailContainer(Context context) {
        this(context, null);
    }

    public NestedScrollingDetailContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollingDetailContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new Scroller(getContext());
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        TOUCH_SLOP = viewConfiguration.getScaledTouchSlop();
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);

        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = measureWidth;
        } else {
            width = mScreenWidth;
        }

        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = child.getLayoutParams();
            int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, left + right, params.width);
            int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, top + bottom, params.height);
            measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
        }
        setMeasuredDimension(width, measureHeight);

        findWebView(this);
        findRecyclerView(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTotalHeight = 0;
        mInnerScrollHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            // child is vertical layout
            child.layout(0, childTotalHeight, childWidth, childHeight + childTotalHeight);
            childTotalHeight += childHeight;
            mInnerScrollHeight += childHeight;
        }
        mInnerScrollHeight -= getMeasuredHeight();
    }

    private int lastDownY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int pointCount = ev.getPointerCount();
        if (pointCount > 1) {
            return true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastDownY = (int)ev.getY();
                mIsSetFlying = false;
                mIsRvFlyingDown = false;
                initOrResetVelocityTracker();
                resetScroller();
                dealWithError();
                break;
            case MotionEvent.ACTION_MOVE:
                if(onYChangedListener != null) {
                    onYChangedListener.onScrollYChanged((int)ev.getY() - lastDownY);
                }
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(onYChangedListener != null) {
                    onYChangedListener.onScrollYChanged((int)ev.getY() - lastDownY);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    if(onYChangedListener != null) {
                        onYChangedListener.onFlingYChanged((int)mVelocityTracker.getYVelocity());
                    }
                    if(isParentCenter()) {
                        // deal parent center fling
                        int yVelocity = (int) -mVelocityTracker.getYVelocity();
                        mCurFlyingType = yVelocity > 0 ? FLYING_FROM_WEBVIEW_TO_PARENT : FLYING_FROM_PARENT_TO_WEBVIEW;
                        recycleVelocityTracker();
                        parentFling(yVelocity);
                    }
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mLastY == 0) {
                    mLastY = (int) event.getY();
                    return true;
                }
                int y = (int) event.getY();
                int dy = y - mLastY;
                mLastY = y;
                scrollBy(0, -dy);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLastY = 0;
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                // intercept move event what fails in a non-sliding subview
                final int y = (int) ev.getY();
                final int yDiff = Math.abs(y - mLastMotionY);
                boolean isInNestedChildViewArea = isTouchNestedInnerView((int)ev.getRawX(), (int)ev.getRawY());
                if (yDiff > TOUCH_SLOP && !isInNestedChildViewArea) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) ev.getY();
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }

        return mIsBeingDragged;
    }

    public void scrollToTarget(View view) {
        if (view == null) {
            return;
        }
        if (mChildWebView != null) {
            mChildWebView.scrollToBottom();
        }
        scrollTo(0, view.getTop() - 100);
    }

    /**
     * judge whether (x,y) is in nested view
     */
    private boolean isTouchNestedInnerView(int x, int y) {
        List<View> innerView = new ArrayList<>();
        if (mChildWebView != null) {
            innerView.add(mChildWebView);
        }
        if (mChildRecyclerView != null) {
            innerView.add(mChildRecyclerView);
        }

        for (View nestedView : innerView) {
            if (nestedView.getVisibility() != View.VISIBLE) {
                continue;
            }
            int[] location = new int[2];
            nestedView.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + nestedView.getMeasuredWidth();
            int bottom = top + nestedView.getMeasuredHeight();
            if (y >= top && y <= bottom && x >= left && x <= right) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScroller != null) {
            mScroller.abortAnimation();
            mScroller = null;
        }
        mVelocityTracker = null;
        mChildRecyclerView = null;
        mChildWebView = null;
        mParentHelper = null;
    }

    @Override
    public void scrollTo(int x, int y) {
        // limit parent Y scroll range
        if (y < 0) {
            y = 0;
        }
        if (y > getInnerScrollHeight()) {
            y = getInnerScrollHeight();
        }
        super.scrollTo(x, y);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            switch (mCurFlyingType) {
                case FLYING_FROM_WEBVIEW_TO_PARENT:
                    if (mIsRvFlyingDown) {// recycler deal self's fling
                        break;
                    }
                    scrollTo(0, currY);
                    invalidate();
                    checkRvTop();
                    if (getScrollY() == getInnerScrollHeight() && !mIsSetFlying) {
                        // when reach parent bottom, pass scroll event to recycler
                        mIsSetFlying = true;
                        recyclerViewFling((int) mScroller.getCurrVelocity());
                    }
                    break;
                case FLYING_FROM_PARENT_TO_WEBVIEW:
                    scrollTo(0, currY);
                    invalidate();
                    if (currY <= 0 && !mIsSetFlying) {
                        // when reach parent top, pass scroll event to web view
                        mIsSetFlying = true;
                        webViewFling((int) -mScroller.getCurrVelocity());
                    }
                    break;
                case FLYING_FROM_RVLIST_TO_PARENT:
                    if (getScrollY() != 0) {
                        invalidate();
                    } else if (!mIsSetFlying) {
                        mIsSetFlying = true;
                        // when reach parent top, pass scroll event to web view
                        webViewFling((int) -mScroller.getCurrVelocity());
                    }
                    break;
            }
        }
    }

    private NestedScrollingParentHelper getNestedScrollingHelper() {
        if (mParentHelper == null) {
            mParentHelper = new NestedScrollingParentHelper(this);
        }
        return mParentHelper;
    }

    private void webViewFling(int v) {
        if (mChildWebView != null) {
            mChildWebView.flingScroll(0, v);
        }
    }

    private void recyclerViewFling(int v) {
        if (mChildRecyclerView != null) {
            mChildRecyclerView.fling(0, v);
        }
    }

    private void findRecyclerView(ViewGroup parent) {
        if (mChildRecyclerView != null) {
            return;
        }

        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof RecyclerView && TAG_NESTED_SCROLL_RECYCLER_VIEW.equals(child.getTag())) {
                mChildRecyclerView = (RecyclerView) child;
                break;
            }
            if (child instanceof ViewGroup) {
                findRecyclerView((ViewGroup) child);
            }
        }
    }

    private void findWebView(ViewGroup parent) {
        if (mChildWebView != null) {
            return;
        }
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof NestedScrollingWebView && TAG_NESTED_SCROLL_WEB_VIEW.equals(child.getTag())) {
                mChildWebView = (NestedScrollingWebView) child;
                break;
            }
            if (child instanceof ViewGroup) {
                findWebView((ViewGroup) child);
            }
        }
    }

    private boolean isParentCenter() {
        return getScrollY() > 0 && getScrollY() < getInnerScrollHeight();
    }

    private void scrollToWebViewBottom() {
        if (mChildWebView != null) {
            mChildWebView.scrollToBottom();
        }
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

    private void resetScroller() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        if (mChildRecyclerView != null) {
            mChildRecyclerView.stopScroll();
        }
    }

    /**
     * deal some unknown error situation
     */
    private void dealWithError() {
        // if parent has position offset and web view not reach bottom, this situation is exception.
        // two ways to fix: 1. move web view to bottom 2. scroll parent position to 0
        // This is defensive code
        if (isParentCenter() && canWebViewScrollDown()) {
            if (getScrollY() > getMeasuredHeight() / 4) {
                scrollToWebViewBottom();
            } else {
                scrollTo(0, 0);
            }
        }
    }

    private void parentFling(float velocityY) {
        mScroller.fling(0, getScrollY(), 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
    }

    private void checkRvTop() {
        if (isParentCenter() && !isRvTop()) {
            rvScrollToPosition(0);
        }
    }

    private void rvScrollToPosition(int position) {
        if (mChildRecyclerView == null) {
            return;
        }

        mChildRecyclerView.scrollToPosition(position);
        RecyclerView.LayoutManager manager = mChildRecyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, 0);
        }
    }

    private boolean isRvTop() {
        if (mChildRecyclerView == null) {
            return false;
        }
        return !mChildRecyclerView.canScrollVertically(-1);
    }

    private boolean canWebViewScrollDown() {
        return mChildWebView != null && mChildWebView.canScrollDown();
    }

    private int getInnerScrollHeight() {
        return mInnerScrollHeight;
    }

    /****** NestedScrollingParent2 BEGIN ******/

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public int getNestedScrollAxes() {
        return getNestedScrollingHelper().getNestedScrollAxes();
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        getNestedScrollingHelper().onNestedScrollAccepted(child, target, axes, type);
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        getNestedScrollingHelper().onStopNestedScroll(target);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (target instanceof NestedScrollingWebView) {
            // when web view reach bottom, continue scroll down parent and recycler
            mCurFlyingType = FLYING_FROM_WEBVIEW_TO_PARENT;
            parentFling(velocityY);
        } else if (target instanceof RecyclerView && velocityY < 0 && getScrollY() >= getInnerScrollHeight()) {
            // when recycler reach top, continue scroll parent and web view
            mCurFlyingType = FLYING_FROM_RVLIST_TO_PARENT;
            parentFling(velocityY);
        } else if (target instanceof RecyclerView && velocityY > 0) {
            mIsRvFlyingDown = true;
        }

        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (dyUnconsumed < 0) {
            // recycler view scroll to parent gap
            scrollBy(0, dyUnconsumed);
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @Nullable int[] consumed, int type) {
        boolean isWebViewBottom = !canWebViewScrollDown();
        boolean isCenter = isParentCenter();
        if (dy > 0 && isWebViewBottom && getScrollY() < getInnerScrollHeight()) {
            // when web view reach bottom, continue scroll down parent container
            scrollBy(0, dy);
            if (consumed != null) {
                consumed[1] = dy;
            }
        } else if (dy < 0 && isCenter) {
            // when recycler reach top, continue scroll up parent container
            scrollBy(0, dy);
            if (consumed != null) {
                consumed[1] = dy;
            }
        }
        if (isCenter && !isWebViewBottom) {
            // when occur some exception situation
            scrollToWebViewBottom();
        }
    }
}
