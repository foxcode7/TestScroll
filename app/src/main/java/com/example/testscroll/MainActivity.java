package com.example.testscroll;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.testscroll.view.NestedScrollContainer;

public class MainActivity extends AppCompatActivity implements NestedScrollContainer.OnYChangedListener {
    public LinearLayout containerLayout;
    public Toolbar toolbar;

    private boolean hasAnimatorStarted;
    private int toolbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerLayout = findViewById(R.id.container_layout);
        toolbar = findViewById(R.id.toolbar);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbarHeight = toolbar.getHeight();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_layout, new MainFragment(), "")
                .commitAllowingStateLoss();
    }

    @Override
    public void onScrollYChanged(int yDiff) {
        useThresholdAndDirectionChangeTitleBar(yDiff, toolbarHeight / 2);
    }

    @Override
    public void onFlingYChanged(int yVelocity) {
        useThresholdAndDirectionChangeTitleBar(yVelocity, 3000);
    }

    private void useThresholdAndDirectionChangeTitleBar(int value, int threshold) {
        if(Math.abs(value) > threshold) {
            if(value > 0) {
                dealAnimatorOfTitleBar(-toolbarHeight, 0);
            } else {
                dealAnimatorOfTitleBar(0, -toolbarHeight);
            }
        }
    }

    private void dealAnimatorOfTitleBar(int startY, int endY) {
        if(containerLayout.getTranslationY() == endY || hasAnimatorStarted) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(containerLayout,
                "translationY", startY, endY).setDuration(100L);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                hasAnimatorStarted = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                hasAnimatorStarted = false;
            }
        });
        animator.start();
    }
}