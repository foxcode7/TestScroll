package com.example.testscroll;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testscroll.util.RecyclerHelper;
import com.example.testscroll.view.NestedScrollContainer;
import com.example.testscroll.view.NestedScrollWebView;
import com.example.testscroll.view.NestedScrollWebViewPager;
import com.example.testscroll.view.NestedScrollWebViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author foxcoder
 * @since 2020-07-28
 */
public class MainFragment extends Fragment {
    private View rootView;
    private RecyclerView rvList;
    private HeaderBottomAdapter rvAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, null, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NestedScrollContainer container = rootView.findViewById(R.id.nested_container);
        if(getActivity() instanceof MainActivity) {
            container.setOnYChangedListener((MainActivity) getActivity());
        }
        container.setOnReachedListener(new NestedScrollContainer.OnReachedListener() {
            @Override
            public void hasWebReachedBottom() {
                Log.d("fox--->", "hasWebReachedBottom");
                if(rvList.getAdapter() == null) {
                    rvList.setAdapter(rvAdapter);
                }
            }

            @Override
            public void hasRvReachedItem(int index) {
                scrollRvReached(index);
            }
        });

        initViewPager();
        initRecyclerView();
    }

    private List<NestedScrollWebView> initWebViews() {
        List<NestedScrollWebView> webViews = new ArrayList<>();
        webViews.add(createWebView(NestedScrollContainer.TAG_NESTED_SCROLL_WEB_VIEW_ONE,
                "http://www.wbiw.com/2020/05/13/bedford-man-arrested-after-traffic-stop/"));
        webViews.add(createWebView(NestedScrollContainer.TAG_NESTED_SCROLL_WEB_VIEW_TWO,
                "http://www.wbiw.com/2020/05/13/bedford-man-arrested-after-traffic-stop/"));
        return webViews;
    }

    private NestedScrollWebView createWebView(String tag, String url) {
        NestedScrollWebView webContainer = new NestedScrollWebView(getActivity());
        webContainer.setTag(tag);
        webContainer.getSettings().setJavaScriptEnabled(true);
        webContainer.setWebViewClient(new WebViewClient());
        webContainer.setWebChromeClient(new WebChromeClient());
        webContainer.loadUrl(url);
        return webContainer;
    }

    private void initViewPager() {
        NestedScrollWebViewPager vp = rootView.findViewById(R.id.view_pager);
        vp.setAdapter(new NestedScrollWebViewPagerAdapter(initWebViews()));
    }

    private void initRecyclerView() {
        rvList = rootView.findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(layoutManager);
        rvAdapter = new HeaderBottomAdapter(getActivity());
        rvList.setAdapter(null);

        RecyclerHelper helper=new RecyclerHelper();

        helper.setRecyclerScrollListener(rvList);
        helper.setOnReachedListener(new RecyclerHelper.OnReachedListener() {
            @Override
            public void hasRvReachedItem(int index) {
                scrollRvReached(index);
            }
        });
    }

    /**
     * todo Jinseok load d2d ads here
     */
    private void scrollRvReached(int position) {
        Log.d("fox--->","hasRvReachedItem " + position);
    }
}
