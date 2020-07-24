package com.example.testscroll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author foxcoder
 * @since 2020-07-23
 */
public class HeaderBottomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_HEADER = 0;
    public static final int ITEM_TYPE_CONTENT = 1;
    public static final int ITEM_TYPE_BOTTOM = 2;
    public String [] texts={"1", "2", "3", "4", "5", "1", "2", "3", "4", "5", "1", "2", "3", "4", "5", "1", "2", "3", "4", "5"};
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private int mHeaderCount=1;
    private int mBottomCount=1;

    public HeaderBottomAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public int getContentItemCount(){
        return texts.length;
    }

    public boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    public boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }


    @Override
    public int getItemViewType(int position) {
        int dataItemCount = getContentItemCount();
        if (mHeaderCount != 0 && position < mHeaderCount) {
            return ITEM_TYPE_HEADER;
        } else if (mBottomCount != 0 && position >= (mHeaderCount + dataItemCount)) {
            return ITEM_TYPE_BOTTOM;
        } else {
            return ITEM_TYPE_CONTENT;
        }
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ContentViewHolder(View itemView) {
            super(itemView);
            textView=(TextView)itemView.findViewById(R.id.tv_item_text);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class BottomViewHolder extends RecyclerView.ViewHolder {
        public BottomViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType ==ITEM_TYPE_HEADER) {
            return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.rv_header, parent, false));
        } else if (viewType == ITEM_TYPE_CONTENT) {
            return  new ContentViewHolder(mLayoutInflater.inflate(R.layout.rv_item, parent, false));
        } else if (viewType == ITEM_TYPE_BOTTOM) {
            return new BottomViewHolder(mLayoutInflater.inflate(R.layout.rv_footer, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {

        } else if (holder instanceof ContentViewHolder) {
            ((ContentViewHolder) holder).textView.setText(texts[position - mHeaderCount]);

        } else if (holder instanceof BottomViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return mHeaderCount + getContentItemCount() + mBottomCount;
    }
}
