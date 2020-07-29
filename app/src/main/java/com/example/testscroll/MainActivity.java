package com.example.testscroll;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.testscroll.util.DisplayUtils;
import com.example.testscroll.view.NestedScrollDetailContainer;

public class MainActivity extends AppCompatActivity implements NestedScrollDetailContainer.OnYChangedListener {
    public LinearLayout containerLayout;
    private boolean hasAnimatorStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerLayout = findViewById(R.id.container_layout);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_layout, new MainFragment(), "")
                .commitAllowingStateLoss();
    }

    @Override
    public void onScrollYChanged(int yDiff) {
        useThresholdAndDirectionChangeTitleBar(yDiff, DisplayUtils.dp2px(50) / 2);
    }

    @Override
    public void onFlingYChanged(int yVelocity) {
        useThresholdAndDirectionChangeTitleBar(yVelocity, 3000);
    }

    private void useThresholdAndDirectionChangeTitleBar(int value, int threshold) {
        if(Math.abs(value) > threshold) {
            if(value > 0) {
                dealAnimatorOfTitleBar(-DisplayUtils.dp2px(50), 0);
            } else {
                dealAnimatorOfTitleBar(0, -DisplayUtils.dp2px(50));
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