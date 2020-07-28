package com.example.testscroll;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.testscroll.view.NestedScrollingDetailContainer;
import com.example.testscroll.view.NestedScrollingWebView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NestedScrollingDetailContainer container;
    private Toolbar toolbar;
    private int curToolbarY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.nested_container);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "toolbar click", Toast.LENGTH_SHORT).show();
            }
        });
        container.setOnYChangedListener(new NestedScrollingDetailContainer.OnYChangedListener() {
            @Override
            public void onScrollYChanged(boolean isMoveOver, int yDiff) {
                if(Math.abs(yDiff) <= NestedScrollingDetailContainer.DEFAULT_DISTANCE_PARENT_SCROLL) {
                    if(isMoveOver) {
                        curToolbarY = (int)toolbar.getTranslationY();
                    } else {
                        if (yDiff > 0) {
                            if(curToolbarY + yDiff <= 0) {
                                toolbar.setTranslationY(curToolbarY + yDiff);
                            }
                        } else {
                            if(curToolbarY + yDiff >= -NestedScrollingDetailContainer.DEFAULT_DISTANCE_PARENT_SCROLL) {
                                toolbar.setTranslationY(curToolbarY + yDiff);
                            }
                        }
                    }
                } else {
                    if(yDiff > 0) {
                        curToolbarY = 0;
                    } else {
                        curToolbarY = -NestedScrollingDetailContainer.DEFAULT_DISTANCE_PARENT_SCROLL;
                    }
                }
            }

            @Override
            public void onFlingYChanged(int yVelocity) {
                if(yVelocity > 0) {
                    curToolbarY = 0;
                } else {
                    curToolbarY = -NestedScrollingDetailContainer.DEFAULT_DISTANCE_PARENT_SCROLL;
                }
                toolbar.setTranslationY(curToolbarY);
            }
        });

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