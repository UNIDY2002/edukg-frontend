package com.java.sunxun.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.java.sunxun.R;

import java.util.ArrayList;

public class DraggableRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {
    // Touch-related time variables
    private long startTime;
    private static final long SPACE_TIME = 100;

    private final LayoutInflater mInflater;
    private final ItemTouchHelper mItemTouchHelper;

    private boolean isEditMode;
    private final ArrayList<T> mData;
    private int availableNum;

    public interface LongPressCallback {
        void start();
    }

    public interface ListChangeCallback<T> {
        void start(ArrayList<T> mData, int availableNum);
    }

    final private LongPressCallback longPressCallback;
    final private ListChangeCallback<T> listChangeCallback;

    public DraggableRecyclerViewAdapter(Context context, ItemTouchHelper helper, ArrayList<T> mData, int availableNum, LongPressCallback longPressCallback, ListChangeCallback<T> listChangeCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        this.longPressCallback = longPressCallback;
        this.listChangeCallback = listChangeCallback;
        this.mData = new ArrayList<>();
        this.mData.addAll(mData);
        this.availableNum = availableNum;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, int viewType) {
        final ContentViewHolder holder = new ContentViewHolder(
                mInflater.inflate(R.layout.item_home_subject_btn, parent, false)
        );

        holder.textView.setOnLongClickListener(v -> {
            if (holder.getAdapterPosition() >= availableNum) return true;

            if (!isEditMode) {
                RecyclerView recyclerView = ((RecyclerView) parent);
                startEditMode(recyclerView);
            }

            // Whenever user long press the btn, start dragging
            mItemTouchHelper.startDrag(holder);
            this.longPressCallback.start();
            return true;
        });

        // When user touch the btn in edit mode, we need to record time and position
        holder.textView.setOnTouchListener((v, event) -> {
            if (isEditMode && holder.getAdapterPosition() < availableNum) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        startTime = System.currentTimeMillis();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (System.currentTimeMillis() - startTime > SPACE_TIME)
                            mItemTouchHelper.startDrag(holder);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        startTime = 0;
                        break;
                    }
                }
            }
            return false;
        });

        // When user touch the delete btn, move the item to the tail
        holder.imgEdit.setOnClickListener(v -> {
            if (!isEditMode) return;

            if (holder.getAdapterPosition() < availableNum) {
                if (this.availableNum == 1) {
                    Snackbar.make(v, "应用至少需要展示一个学科", Snackbar.LENGTH_SHORT).show();
                    return;
                } else --this.availableNum;

                onItemMove(holder.getAdapterPosition(), mData.size() - 1, true);
                holder.textView.setBackgroundResource(R.drawable.bg_subject_deleted);
                shakeView(holder.itemView, false);
                holder.imgEdit.setImageResource(R.drawable.ic_reset_back);
            } else {
                ++this.availableNum;
                onItemMove(holder.getAdapterPosition(), availableNum - 1, true);
                holder.textView.setBackgroundResource(R.drawable.bg_subject_plain);
                shakeView(holder.itemView, true);
                holder.imgEdit.setImageResource(R.drawable.ic_close);
            }
        });

        // Start shaking
        shakeView(holder.itemView, isEditMode);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentViewHolder mHolder = (ContentViewHolder) holder;
        mHolder.textView.setText(mData.get(position).toString());

        if (isEditMode) mHolder.imgEdit.setVisibility(View.VISIBLE);
        else mHolder.imgEdit.setVisibility(View.INVISIBLE);

        mHolder.textView.setBackgroundResource(position < availableNum ? R.drawable.bg_subject_plain : R.drawable.bg_subject_deleted);
        mHolder.imgEdit.setImageResource(position < availableNum ? R.drawable.ic_close : R.drawable.ic_reset_back);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition, boolean isSpecialMove) {
        if (!isSpecialMove && toPosition >= availableNum) return;
        T item = mData.get(fromPosition);
        mData.remove(fromPosition);
        mData.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
        this.listChangeCallback.start(mData, availableNum);
    }

    public boolean switchMode(RecyclerView parent) {
        if (isEditMode) cancelEditMode(parent);
        else startEditMode(parent);
        return isEditMode;
    }

    private void startEditMode(RecyclerView parent) {
        isEditMode = true;

        for (int i = 0; i < mData.size(); i++) {
            ImageView imgEdit = (ImageView) parent.getChildAt(i).findViewById(R.id.subject_btn_icon);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.VISIBLE);
            }
            shakeView(parent.getChildAt(i), i < availableNum);
        }
    }

    public void cancelEditMode(RecyclerView parent) {
        isEditMode = false;

        for (int i = 0; i < parent.getChildCount(); i++) {
            ImageView imgEdit = (ImageView) parent.getChildAt(i).findViewById(R.id.subject_btn_icon);
            if (imgEdit != null) {
                imgEdit.setVisibility(View.INVISIBLE);
            }
            shakeView(parent.getChildAt(i), false);
        }
    }

    private void shakeView(View view, boolean shake) {
        if (shake) {
            if (view.getAnimation() != null) {
                view.getAnimation().start();
            }
            RotateAnimation rotate = new RotateAnimation(-1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator linear = new LinearInterpolator();
            rotate.setInterpolator(linear);
            rotate.setDuration(100);
            rotate.setRepeatCount(-1);
            rotate.setFillAfter(false);
            rotate.setStartOffset(10);
            rotate.setRepeatMode(Animation.REVERSE);
            view.startAnimation(rotate);
        } else {
            if (view.getAnimation() != null) {
                view.getAnimation().cancel();
            }
            view.clearAnimation();
        }
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private final TextView textView;
        private final ImageView imgEdit;

        public ContentViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.subject_btn_text);
            imgEdit = (ImageView) itemView.findViewById(R.id.subject_btn_icon);
            textView.setBackgroundResource(R.drawable.bg_subject_plain);
            imgEdit.setImageResource(R.drawable.ic_close);
        }

        @Override
        public void onItemSelected() { }

        @Override
        public void onItemFinish() { }
    }
}
