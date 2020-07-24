package com.example.testscroll;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class WebViewPagerAdapter extends PagerAdapter {
    List<WebView> webViews = new ArrayList<>();

    public WebViewPagerAdapter(List<WebView> webViews) {
        this.webViews = webViews;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        WebView webView = webViews.get(position);
        container.addView(webView);
        return webView;
    }

    @Override
    public int getCount() {
        return webViews.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
