package com.example.testscroll;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testscroll.util.DisplayUtils;
import com.example.testscroll.view.NestedScrollingDetailContainer;
import com.example.testscroll.view.NestedScrollingWebView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foxcoder
 * @since 2020-07-28
 */
public class MainFragment extends Fragment {

    private NestedScrollingDetailContainer container;
    private NestedScrollingWebView webContainer1;
    private NestedScrollingWebView webContainer2;

    private LinearLayout containerLayout;
    private int curToolbarY;

    private View rootView;
    private TextView readMore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, null, false);
        readMore = rootView.findViewById(R.id.read_more);
        readMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadMore(v);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerLayout = ((MainActivity)getActivity()).containerLayout;
        container = rootView.findViewById(R.id.nested_container);
        container.setOnYChangedListener(new NestedScrollingDetailContainer.OnYChangedListener() {
            @Override
            public void onScrollYChanged(int yDiff) {
                if(Math.abs(yDiff) > DisplayUtils.dp2px(50) / 2) {
                    if(yDiff > 0) {
                        if(containerLayout.getTranslationY() == 0) return;
                        ObjectAnimator.ofFloat(containerLayout, "translationY", -DisplayUtils.dp2px(50), 0).setDuration(200L).start();
                    } else {
                        if(containerLayout.getTranslationY() == -DisplayUtils.dp2px(50)) return;
                        ObjectAnimator.ofFloat(containerLayout, "translationY", 0, -DisplayUtils.dp2px(50)).setDuration(200L).start();
                    }
                }
            }

            @Override
            public void onFlingYChanged(int yVelocity) {
                if(Math.abs(yVelocity) > 3000) {
                    if(yVelocity > 0) {
                        if(containerLayout.getTranslationY() == 0) return;
                        ObjectAnimator.ofFloat(containerLayout, "translationY", -DisplayUtils.dp2px(50), 0).setDuration(200L).start();
                    } else {
                        if(containerLayout.getTranslationY() == -DisplayUtils.dp2px(50)) return;
                        ObjectAnimator.ofFloat(containerLayout, "translationY", 0, -DisplayUtils.dp2px(50)).setDuration(200L).start();
                    }
                }
            }
        });

        initViewPager();
        initRecyclerView();
    }

    private List<WebView> initWebViews() {
        List<WebView> webViews = new ArrayList<>();
        webContainer1 = createWebView();
        webViews.add(webContainer1);
        webContainer2 = createWebView();
        webViews.add(webContainer2);
        return webViews;
    }

    private NestedScrollingWebView createWebView() {
        NestedScrollingWebView webContainer = new NestedScrollingWebView(getActivity());
        webContainer.setTag(NestedScrollingDetailContainer.TAG_NESTED_SCROLL_WEB_VIEW);
        webContainer.getSettings().setJavaScriptEnabled(true);
        webContainer.setWebViewClient(new WebViewClient());
        webContainer.setWebChromeClient(new WebChromeClient());
        webContainer.loadUrl("http://www.wbiw.com/2020/05/13/bedford-man-arrested-after-traffic-stop/");
        return webContainer;
    }

    private void initViewPager() {
        WebViewPager vp = rootView.findViewById(R.id.view_pager);
        vp.setAdapter(new WebViewPagerAdapter(initWebViews()));
    }

    private void initRecyclerView() {
        RecyclerView rvList = rootView.findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(layoutManager);
        HeaderBottomAdapter rvAdapter = new HeaderBottomAdapter(getActivity());
        rvList.setAdapter(rvAdapter);
    }

    public void onReadMore(View v) {
        final Rect rect = new Rect();
        webContainer1.getGlobalVisibleRect(rect);

        final Rect rect2 = new Rect();
        container.getGlobalVisibleRect(rect2);
        v.setVisibility(View.GONE);
        // 点击 ReadMore，外层容器滚动到屏幕顶部
        container.scrollTo(0, 0);

        // 内部网页向下滚动 WebView 底部到 屏幕底部 的距离，模拟网页展开效果
        webContainer1.setCanScroll(true);
        webContainer1.scrollTo(0,webContainer1.computeVerticalScrollOffset() + rect2.bottom - rect.bottom);
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return -1;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.y;
    }
}
