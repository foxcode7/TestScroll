package com.example.testscroll;

import android.os.Bundle;
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

import com.example.testscroll.view.NestedScrollDetailContainer;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, null, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NestedScrollDetailContainer container = rootView.findViewById(R.id.nested_container);
        if(getActivity() instanceof MainActivity) {
            container.setOnYChangedListener((MainActivity) getActivity());
        }

        initViewPager();
        initRecyclerView();
    }

    private List<NestedScrollWebView> initWebViews() {
        List<NestedScrollWebView> webViews = new ArrayList<>();
        webViews.add(createWebView(NestedScrollDetailContainer.TAG_NESTED_SCROLL_WEB_VIEW_ONE,
                "http://www.wbiw.com/2020/05/13/bedford-man-arrested-after-traffic-stop/"));
        webViews.add(createWebView(NestedScrollDetailContainer.TAG_NESTED_SCROLL_WEB_VIEW_TWO,
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
        RecyclerView rvList = rootView.findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvList.setLayoutManager(layoutManager);
        HeaderBottomAdapter rvAdapter = new HeaderBottomAdapter(getActivity());
        rvList.setAdapter(rvAdapter);
    }
}
