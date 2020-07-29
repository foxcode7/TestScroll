package com.example.testscroll.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import com.example.testscroll.R;
import com.example.testscroll.util.DimenHelper;
import com.example.testscroll.util.DisplayUtils;

/**
 * @author foxcoder
 * @since 2020-07-29
 */
public class CollapsibleWebView extends WebView {
    private final int DEFAULT_SHOW_SCREEN_HEIGHT_TIMES = 2;
    protected int mWebViewContentHeight;
    private final float DENSITY;
    private int mJsCallWebViewContentHeight;

    private Paint bgPaint;
    private Paint textPaint;

    private boolean canScroll;
    private int showScreenHeightTimes;

    public boolean isCanScroll() {
        return canScroll;
    }

    public CollapsibleWebView(Context context) {
        this(context, null);
    }

    public CollapsibleWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsibleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DENSITY = context.getResources().getDisplayMetrics().density;
        showScreenHeightTimes = DEFAULT_SHOW_SCREEN_HEIGHT_TIMES;

        bgPaint = new Paint();
        bgPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(context, R.color.white));
        textPaint.setDither(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(DisplayUtils.dp2px(16));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawReadMore(canvas);
    }

    private void drawReadMore(Canvas canvas) {
        if(canScroll) return;
        canvas.save();
        Rect rect = new Rect(0, getBottom() * showScreenHeightTimes - DisplayUtils.dp2px(50),
                DisplayUtils.getScreenWidth(), getBottom() * showScreenHeightTimes);
        canvas.drawRect(rect, bgPaint);

        Paint.FontMetricsInt fontMetrics = textPaint.getFontMetricsInt();
        int baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        canvas.drawText("Read More", rect.centerX(), baseline, textPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(!canScroll) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    Rect readMoreRect = new Rect(0, getBottom() - DisplayUtils.dp2px(50), DisplayUtils.getScreenWidth(), getBottom());
                    if (readMoreRect.contains(x, y)) {
                        canScroll = true;
                        onReadMoreClick();
                        invalidate();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
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
            mWebViewContentHeight = (int) (computeVerticalScrollExtent() * showScreenHeightTimes);
        } else {
            mWebViewContentHeight = (int) (getContentHeight() * DENSITY);
        }
        return mWebViewContentHeight;
    }

    protected void onReadMoreClick() {

    }
}
