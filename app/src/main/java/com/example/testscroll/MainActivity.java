package com.example.testscroll;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testscroll.view.NestedScrollingDetailContainer;
import com.example.testscroll.view.NestedScrollingWebView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    NestedScrollingDetailContainer mNestedScrollingDetailContainer;
    NestedScrollingWebView webContainer1;
    NestedScrollingWebView webContainer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNestedScrollingDetailContainer = findViewById(R.id.nested_container);
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
        NestedScrollingWebView webContainer = new NestedScrollingWebView(this);
        webContainer.setTag(NestedScrollingDetailContainer.TAG_NESTED_SCROLL_WEB_VIEW);
        webContainer.setCanScroll(false);
        webContainer.getSettings().setJavaScriptEnabled(true);
        webContainer.setWebViewClient(new WebViewClient());
        webContainer.setWebChromeClient(new WebChromeClient());
        webContainer.loadUrl("http://www.wbiw.com/2020/05/13/bedford-man-arrested-after-traffic-stop/");
        return webContainer;
    }

    private void initViewPager() {
        WebViewPager vp = findViewById(R.id.view_pager);
        vp.setAdapter(new WebViewPagerAdapter(initWebViews()));
    }

    private void initRecyclerView() {
        RecyclerView rvList = findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvList.setLayoutManager(layoutManager);
        HeaderBottomAdapter rvAdapter = new HeaderBottomAdapter(this);
        rvList.setAdapter(rvAdapter);
    }

    public void onReadMore(View v) {
        final Rect rect = new Rect();
        v.getGlobalVisibleRect(rect);
        Log.e("CG", "Read more : " + rect + " " + getResources().getDisplayMetrics().heightPixels
                + " " + webContainer1.computeVerticalScrollRange()
                + " " + webContainer1.computeVerticalScrollOffset()
                + " " + webContainer1.computeVerticalScrollExtent());
        v.setVisibility(View.GONE);
        // 点击 ReadMore，外层容器滚动到屏幕顶部
        mNestedScrollingDetailContainer.scrollTo(0, 0);

        // 内部网页向下滚动 WebView 底部到 屏幕底部 的距离，模拟网页展开效果
        webContainer1.setCanScroll(true);
        webContainer1.scrollTo(0, webContainer1.computeVerticalScrollOffset() + (getResources().getDisplayMetrics().heightPixels - rect.top));
    }
}