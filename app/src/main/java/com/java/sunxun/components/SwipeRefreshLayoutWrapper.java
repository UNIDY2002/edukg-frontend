package com.java.sunxun.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class SwipeRefreshLayoutWrapper extends SwipeRefreshLayout {

    public static interface OnLoadListener {
        public void onLoad();
    }

    private View footer;
    private OnLoadListener onLoadListener;

    private RecyclerView content;

    private boolean isMove = false;
    private boolean isLoading = false;

    private int startX, startY;
    private int endX, endY;

    public SwipeRefreshLayoutWrapper(@NonNull Context context, @LayoutRes int footerRes) {
        super(context);
        footer = LayoutInflater.from(context).inflate(footerRes, null, false);
    }

    private boolean ableToLoad() {
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (content == null) getContentList();
    }

    private void getContentList() {
        if (getChildCount() > 0 && getChildAt(0) instanceof RecyclerView) {
            content = (RecyclerView) getChildAt(0);
            content.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                isMove = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                endX = (int) ev.getX();
                endY = (int) ev.getY();

                if (startX != endX || startY != endY) {
                    isMove = true;
                }
                break;
            }

            default: {
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
