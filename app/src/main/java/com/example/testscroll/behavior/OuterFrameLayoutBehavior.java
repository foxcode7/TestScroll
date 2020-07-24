package com.example.testscroll.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

/**
 * @author foxcoder
 * @since 2020-07-24
 */
public class OuterFrameLayoutBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    private boolean isReach;

    public OuterFrameLayoutBehavior() {
    }

    public OuterFrameLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull FrameLayout child, @NonNull View dependency) {
//        return dependency instanceof AppBarLayout;
//    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, FrameLayout child, MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                if(isReach) {
//                    return true;
//                }
//                break;
//        }
        return super.onInterceptTouchEvent(parent, child, ev);
//        return true;
    }

//    @Override
//    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FrameLayout child,
//                                       @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
//        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
//    }
}
