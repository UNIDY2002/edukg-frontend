package com.java.sunxun.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.java.sunxun.R;

import java.util.List;

// TODO: I think this adapter had better extend from RecyclerViewAdapter<T>, update it later
public abstract class DraggableRecyclerViewAdapter<T>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    // View Types
    public static final int HEADER = 0;
    public static final int CONTENT = 1;

    // Some helpers
    final private LayoutInflater mInflater;
    final private ItemTouchHelper mHelper;

    final private int childNumBeforeContent;
    final private List<T> mData;

    private boolean isEditing = false;

    public interface BtnClickListener {
        void onItemClick(View view, int position);
    }

    private BtnClickListener mBtnListener;
    private BtnViewHolder mCurrentViewHolder;
    private ViewGroup parentView;

    private long touchStartTime;

    // TODO: The method is never universal, resolve it later
    final private int headerTitleId;
    final private int btnTextId;
    final private int btnIconId;

    final private int headerLayout;
    final private int contentLayout;

    public DraggableRecyclerViewAdapter(
            Context context, ItemTouchHelper helper, List<T> data, int childNumBeforeContent,
            int headerTitleId, int btnTextId, int btnIconId, int headerLayout, int contentLayout) {
        this.mInflater = LayoutInflater.from(context);
        this.mHelper = helper;
        this.mData = data;
        this.childNumBeforeContent = childNumBeforeContent;
        this.headerLayout = headerLayout;
        this.contentLayout = contentLayout;
        this.headerTitleId = headerTitleId;
        this.btnTextId = btnTextId;
        this.btnIconId = btnIconId;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? HEADER : CONTENT;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parentView = parent;
        if (viewType == HEADER) {
            final HeaderViewHolder holder = new HeaderViewHolder(
                    this.mInflater.inflate(headerLayout, parent, false));
            holder.title.setOnClickListener(v -> {
                if (isEditing) {
                    cancelEditMode((RecyclerView) parent);
                    holder.title.setText("EDIT");
                } else {
                    startEditMode((RecyclerView) parent);
                    holder.title.setText("OVER");
                }
            });
            return holder;
        } else {
            final BtnViewHolder holder = new BtnViewHolder(
                    this.mInflater.inflate(contentLayout, parent, false));
            // Cancel recycle
            holder.setIsRecyclable(false);
            holder.itemView.setOnClickListener(v -> {
                int position = holder.getAdapterPosition();
                if (isEditing) {
                    View currentView = ((RecyclerView) parent).getLayoutManager().findViewByPosition(position);
                    // TODO: How can you know where it will be moved to?
                    View targetView = ((RecyclerView) parent).getLayoutManager().findViewByPosition(childNumBeforeContent + mData.size());
                    startAnimation(
                            (RecyclerView) parent, currentView, targetView.getLeft(), targetView.getTop());
                } else {
                    mBtnListener.onItemClick(v, position - childNumBeforeContent);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (!isEditing) {
                    startEditMode((RecyclerView) parent);
                    // TODO: What is it?
                    View caption = parent.getChildAt(0);
                    if (caption == ((RecyclerView) parent).getLayoutManager().findViewByPosition(0)) {
                        TextView tvBtnEdit = caption.findViewById(R.id.entity_category);
                        tvBtnEdit.setText("OVER");
                    }
                }
                mHelper.startDrag(holder);
                return true;
            });
            holder.itemView.setOnTouchListener((v, event) -> {
                if (isEditing) {
                    mCurrentViewHolder = holder;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchStartTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // When touch time exceeds 100ms, we deem it as 'LongPress'
                            if (System.currentTimeMillis() - touchStartTime > 100)
                                mHelper.startDrag(holder);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            touchStartTime = 0;
                            break;
                    }
                }
                return false;
            });
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (holder instanceof DraggableRecyclerViewAdapter.HeaderViewHolder) {
            // TODO: Replace with resource string
            ((HeaderViewHolder) holder).title.setText(isEditing ? "OVER" : "EDIT");
        } else if (holder instanceof DraggableRecyclerViewAdapter.BtnViewHolder) {
            ((BtnViewHolder) holder).icon.setVisibility(isEditing ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + childNumBeforeContent;
    }

    /**
     * This function is used for resetting UI when reentering the page
     */
    public void reset() {
        if (isEditing) {
            isEditing = false;
            notifyDataSetChanged();
        }
    }

    private ImageView getCachedView(ViewGroup parent, RecyclerView recyclerView, View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);

        // Create an empty view
        final ImageView resView = new ImageView(recyclerView.getContext());

        // Construct the ImageView from the bitmap of the known view
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        resView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);

        // Get measures
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parentLocations = new int[2];
        recyclerView.getLocationOnScreen(parentLocations);

        // Set parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parentLocations[1], 0, 0);
        parent.addView(resView, params);

        return resView;
    }

    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation res = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY
        );
        res.setDuration(360);
        res.setFillAfter(true);
        return res;
    }

    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup parent = (ViewGroup) recyclerView.getParent();
        final ImageView copiedView = getCachedView(parent, recyclerView, currentView);

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        copiedView.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                parent.removeView(copiedView);
                if (currentView.getVisibility() == View.INVISIBLE) currentView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    @Override
    public void onItemMove(int from, int to) {
        T item = mData.get(from - childNumBeforeContent);
        mData.remove(item);
        mData.add(to - childNumBeforeContent, item);
        notifyItemMoved(from, to);
    }

    @Override
    public void onChangeItem() {
        int position = this.mCurrentViewHolder.getAdapterPosition();
        View currentView = ((RecyclerView) parentView).getLayoutManager().findViewByPosition(position);
        // TODO: How can you know where it will be moved to?
        View targetView = ((RecyclerView) parentView).getLayoutManager().findViewByPosition(childNumBeforeContent + mData.size());
        startAnimation(
                (RecyclerView) parentView, currentView, targetView.getLeft(), targetView.getTop());
    }

    private void startEditMode(RecyclerView parent) {
        isEditing = true;

        for (int i = 0; i < parent.getChildCount(); ++i) {
            View child = parent.getChildAt(i);
            // TODO: start shaking & change title & show icon
        }
    }

    private void cancelEditMode(RecyclerView parent) {
        isEditing = false;

        for (int i = 0; i < parent.getChildCount(); ++i) {
            View child = parent.getChildAt(i);
            // TODO: end shaking & change title & hide icon
        }
    }

    private void toggleShaking(View view, boolean shaking) {
        if (shaking) {
            if (view.getAnimation() != null) view.getAnimation().start();
            RotateAnimation rotation = new RotateAnimation(-1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator linear = new LinearInterpolator();
            rotation.setInterpolator(linear);
            rotation.setDuration(100);
            rotation.setRepeatCount(-1);
            rotation.setFillAfter(false);
            rotation.setStartOffset(10);
            rotation.setRepeatMode(Animation.REVERSE);
            view.startAnimation(rotation);
        } else {
            if (view.getAnimation() != null) view.getAnimation().cancel();
            view.clearAnimation();
        }
    }

    public void setBtnClickListener(BtnClickListener listener) {
        this.mBtnListener = listener;
    }

    // TODO: These ViewHolders is not designed to be in common use, update it later
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(headerTitleId);
        }
    }

    public class BtnViewHolder extends RecyclerView.ViewHolder {
        public TextView btnText;
        public ImageView icon;

        public BtnViewHolder(View itemView) {
            super(itemView);
            btnText = itemView.findViewById(btnTextId);
            icon = itemView.findViewById(btnIconId);
        }
    }
}
