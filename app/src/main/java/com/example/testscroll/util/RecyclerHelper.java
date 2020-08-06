package com.example.testscroll.util;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author foxcoder
 * @since 2020-08-05
 */
public class RecyclerHelper {
    private int lastStart = -1;
    private int lastEnd;

    private OnReachedListener onReachedListener;

    public void setOnReachedListener(OnReachedListener onReachedListener) {
        this.onReachedListener = onReachedListener;
    }

    public interface OnReachedListener {
        void hasRvReachedItem(int index);
    }

    private void dealScrollEvent(int firstVisible, int lastVisible) {
        int visibleItemCount = lastVisible - firstVisible;
        if (visibleItemCount > 0) {
            if (lastStart == -1) {
                lastStart = firstVisible;
                lastEnd = lastVisible;
            } else {
                if (firstVisible != lastStart) {
                    lastStart = firstVisible;
                }
                if (lastVisible != lastEnd) {
                    if (lastVisible > lastEnd) {// scroll up
                        for (int i = lastEnd; i < lastVisible; i++) {
                            if (onReachedListener !=null){
                                onReachedListener.hasRvReachedItem(i + 1);
                            }
                        }
                    }
                    lastEnd = lastVisible;
                }
            }
        }
    }

    public void setRecyclerScrollListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(recyclerScrollListener);
    }

    private RecyclerView.OnScrollListener recyclerScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager layoutManager= (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager!=null){
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                int visibleItemCount = lastVisible - firstVisible;
                if (lastVisible == 0) {
                    visibleItemCount = 0;
                }
                if (visibleItemCount != 0) {
                    dealScrollEvent(firstVisible, lastVisible);
                }
            }
        }
    };
}
