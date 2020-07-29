package com.example.testscroll.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class NestedScrollWebViewPagerAdapter extends PagerAdapter {
    List<NestedScrollWebView> nestedScrollWebViews;

    public NestedScrollWebViewPagerAdapter(List<NestedScrollWebView> nestedScrollWebViews) {
        this.nestedScrollWebViews = nestedScrollWebViews;
    }

    public List<NestedScrollWebView> getNestedScrollWebViews() {
        return nestedScrollWebViews;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        NestedScrollWebView webView = nestedScrollWebViews.get(position);
        container.addView(webView);
        return webView;
    }

    @Override
    public int getCount() {
        return nestedScrollWebViews.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
