package com.java.sunxun.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class RecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mData;

    public RecyclerViewAdapter(Context context, int layoutId, List<T> data) {
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.mData = data;
    }

    @Override
    public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(mLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: What is this?
        // holder.updatePosition(position);
        this.convert(holder, mData.get(position));
    }

    public void updateData(List<T> newData) {
        this.mData = newData;
        this.notifyDataSetChanged();
    }

    public abstract void convert(ViewHolder holder, T data);

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final private View mConvertView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mConvertView = itemView;
        }

        public <K extends View> K getViewById(int viewId) {
            return (K) mConvertView.findViewById(viewId);
        }
    }
}
