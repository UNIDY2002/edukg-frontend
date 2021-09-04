package com.java.sunxun.components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ExpandLinearLayout extends LinearLayout {
    private boolean isExpanded = false;
    private int shrinkHeight = 0;
    private int expandHeight = 0;

    private float animationPercent = 0f;

    public ExpandLinearLayout(Context context) {
        super(context);
        this.initView();
    }

    public ExpandLinearLayout(Context context, AttributeSet attr) {
        super(context, attr);
        this.initView();
    }

    public ExpandLinearLayout(Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        this.initView();
    }

    /**
     * This function will initialize all the variables.
     * We set 'Shrink' & 'Animation has been over' by default.
     */
    private void initView() {
        this.isExpanded = false;
        this.animationPercent = 1f;
    }

    private static int getMarginedHeight(View view) {
        return ((MarginLayoutParams) view.getLayoutParams()).topMargin
                + ((MarginLayoutParams) view.getLayoutParams()).bottomMargin
                + view.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Init
        this.expandHeight = 0;
        this.shrinkHeight = 0;

        // Calculate the shrinkHeight & expandHeight
        for (int i = 0; i < getChildCount(); ++i) {
            if (i == 0) {
                this.shrinkHeight = getMarginedHeight(this.getChildAt(0))
                        + this.getPaddingTop() + this.getPaddingBottom();
                this.expandHeight = this.getPaddingTop() + this.getPaddingBottom();
            }

            expandHeight += getMarginedHeight(this.getChildAt(i));
        }

        int diff = this.expandHeight - this.shrinkHeight;

        // Final settings
        setMeasuredDimension(
                widthMeasureSpec,
                isExpanded
                        ? shrinkHeight + (int)(diff * animationPercent)
                        : expandHeight - (int)(diff * animationPercent)
        );
    }

    public boolean toggle() {
        isExpanded = !isExpanded;
        startAnimation();
        return isExpanded;
    }

    // The setter is designed for animator
    private void setAnimationPercent(float val) {
        this.animationPercent = val;
        requestLayout();
    }

    private void startAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "animationPercent", 0f, 1f);
        animator.setDuration(300);
        animator.start();
    }
}
