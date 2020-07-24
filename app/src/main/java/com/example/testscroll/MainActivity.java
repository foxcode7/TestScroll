package com.example.testscroll;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.testscroll.view.NestedScrollingDetailContainer;
import com.example.testscroll.view.NestedScrollingWebView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewPager();
        initRecyclerView();
    }

    private List<WebView> initWebViews() {
        List<WebView> webViews = new ArrayList<>();

        webViews.add(createWebView());
        webViews.add(createWebView());
        return webViews;
    }

    private WebView createWebView() {
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
}