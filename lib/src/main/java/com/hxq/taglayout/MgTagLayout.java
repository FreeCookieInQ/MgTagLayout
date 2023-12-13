package com.hxq.taglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import com.hxq.tablayout.R;

import java.util.ArrayList;
import java.util.List;

public class MgTagLayout extends ViewGroup implements MgTagContainer{

    private final MgTagLayoutHelper.MgTagLinesResult mMgTagLinesResult = new MgTagLayoutHelper.MgTagLinesResult();

    private List<MgTagLine> mTagLines = new ArrayList<>();

    private final MgTagLayoutHelper mLayoutHelper = new MgTagLayoutHelper(this);

    /** 最大可展示的行数*/
    int mMaxLine;
    /** 可摆放的剩余宽度阈值*/
    int mThresholdWidth;
    /** tag之间的横向间距*/
    int mHorizontalSpacing;
    /** tag之间的竖向间距*/
    int mVerticalSpacing;
    /** 展开之前可展示的最大行数*/
    int mMaxLineBeforeExpand = NOT_SET;
    /** 用于点击展开的按钮View*/
    View mExpandView;

    public MgTagLayout(Context context) {
        this(context, null);
    }

    public MgTagLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MgTagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MgTagLayout, defStyleAttr, 0);
        mHorizontalSpacing = ta.getDimensionPixelSize(R.styleable.MgTagLayout_tagHorizontalSpacing, 0);
        mVerticalSpacing = ta.getDimensionPixelSize(R.styleable.MgTagLayout_tagVerticalSpacing, 0);
        mMaxLine = ta.getInt(R.styleable.MgTagLayout_tagMaxLine, NOT_SET);
        mThresholdWidth = ta.getDimensionPixelSize(R.styleable.MgTagLayout_tagThresholdWidth, NOT_SET);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMgTagLinesResult.reset();
        mLayoutHelper.calculateLines(mMgTagLinesResult, widthMeasureSpec, heightMeasureSpec);
        mTagLines = mMgTagLinesResult.tagLines;

        int childState = mMgTagLinesResult.childState;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 根据行数计算结果，来决定宽高
        int calculatedMaxWidth = calculateMaxWidth();
        int calculatedMaxHeight = calculateTotalHeight();

        int widthSizeAndState;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                if (widthSize < calculatedMaxWidth) {
                    childState = View.combineMeasuredStates(childState, View.MEASURED_STATE_TOO_SMALL);
                }
                widthSizeAndState = View.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
                break;
            case MeasureSpec.UNSPECIFIED:
                widthSizeAndState = View.resolveSizeAndState(calculatedMaxWidth, widthMeasureSpec, childState);
                break;
            case MeasureSpec.AT_MOST:
                if (widthSize < calculatedMaxWidth) {
                    childState = View.combineMeasuredStates(childState, View.MEASURED_STATE_TOO_SMALL);
                } else {
                    widthSize = calculatedMaxWidth;
                }
                widthSizeAndState = View.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
                break;
            default:
                throw new IllegalStateException("Unknown width mode is set: " + widthMode);
        }

        int heightSizeAndState;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                if (heightSize < calculatedMaxHeight) {
                    childState = View.combineMeasuredStates(childState,
                            View.MEASURED_STATE_TOO_SMALL
                                    >> View.MEASURED_HEIGHT_STATE_SHIFT);
                }
                heightSizeAndState = View.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
                break;
            case MeasureSpec.AT_MOST: {
                if (heightSize < calculatedMaxHeight) {
                    childState = View.combineMeasuredStates(childState,
                            View.MEASURED_STATE_TOO_SMALL
                                    >> View.MEASURED_HEIGHT_STATE_SHIFT);
                } else {
                    heightSize = calculatedMaxHeight;
                }
                heightSizeAndState = View.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                heightSizeAndState = View.resolveSizeAndState(calculatedMaxHeight, heightMeasureSpec, childState);
                break;
            }
            default:
                throw new IllegalStateException("Unknown height mode is set: " + heightMode);
        }
        setMeasuredDimension(widthSizeAndState, heightSizeAndState);
    }

    /**
     * 获取所有tagLine的最大宽度
     * @return
     */
    private int calculateMaxWidth() {
        int maxLineWidth = 0;
        for (MgTagLine tagLine : mTagLines) {
            maxLineWidth = Math.max(maxLineWidth, tagLine.mWidth);
        }
        return maxLineWidth;
    }

    /**
     * 获取整个tagLine的高度总和
     * @return
     */
    private int calculateTotalHeight() {
        int maxLineHeight = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < mTagLines.size(); i++) {
            MgTagLine tagLine = mTagLines.get(i);
            maxLineHeight += tagLine.mHeight;
            if (i > 0) {
                maxLineHeight += getVerticalSpacing();
            }
        }
        return maxLineHeight;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        LayoutParams tabLayoutParams;
        if ((params instanceof LayoutParams)) {
            tabLayoutParams = (LayoutParams) params;
        } else {
            tabLayoutParams = new LayoutParams(params);
        }
        // 保证mExpandView在child列表的最后一个，不妨碍正常tag的测量和layout顺序
        if (mExpandView != null && child != mExpandView) {
            int expandIndex = indexOfChild(mExpandView);
            if (index < 0) {
                index = getChildCount();
            }
            if (index > expandIndex) {
                index = expandIndex;
            }
        }
        super.addView(child, index, tabLayoutParams);
    }

    /**
     * 添加展开View
     * @param expandView
     * @param params
     */
    public void setExpandView(View expandView, ViewGroup.LayoutParams params, int maxLineBeforeExpand) {
        mExpandView = expandView;
        mExpandView.setOnClickListener(v -> {
            removeView(mExpandView);
            mExpandView = null;
        });
        mMaxLineBeforeExpand = maxLineBeforeExpand;
        addView(expandView, getChildCount(), params);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLayoutHelper.layout(mMgTagLinesResult);
    }

    @Override
    public View getTagItemAt(int index) {
        return getChildAt(index);
    }

    @Override
    public int getTagItemCount() {
        return getChildCount();
    }

    @Override
    public int getMaxLines() {
        return mMaxLine;
    }

    @Override
    public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
        return getChildMeasureSpec(widthSpec, padding, childDimension);
    }

    @Override
    public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
        return getChildMeasureSpec(heightSpec, padding, childDimension);
    }

    @Override
    public int getWidthTagNeeded(View tagView, int tagIndexInLine) {
        int horizontalSpacing = tagIndexInLine == 0 ? 0 : getHorizontalSpacing();
        if (mExpandView != null && tagView == mExpandView) {
            horizontalSpacing = getHorizontalSpacing();
        }
        LayoutParams params = (LayoutParams) tagView.getLayoutParams();
        return tagView.getMeasuredWidth() + params.leftMargin + params.rightMargin + horizontalSpacing;
    }

    @Override
    public int getHeightTagNeeded(View tagView) {
        LayoutParams params = (LayoutParams) tagView.getLayoutParams();
        return tagView.getMeasuredHeight() + params.topMargin + params.bottomMargin;
    }

    @Override
    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    @Override
    public int getVerticalSpacing() {
        return mVerticalSpacing;
    }

    @Override
    public int getThresholdWidth() {
        return mThresholdWidth;
    }

    @Override
    public View getExpandView() {
        return mExpandView;
    }

    @Override
    public int getMaxLineBeforeExpand() {
        return mMaxLineBeforeExpand;
    }

    public static class LayoutParams extends MarginLayoutParams implements MgTagItem {

        private int mMinWidth = NOT_SET;

        private int mMinHeight = NOT_SET;

        private int mMaxWidth = MAX_SIZE;

        private int mMaxHeight = MAX_SIZE;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MgTagLayout_Layout);
            mMinWidth = a.getDimensionPixelSize(R.styleable.MgTagLayout_Layout_layout_minWidth, NOT_SET);
            mMinHeight = a.getDimensionPixelSize(R.styleable.MgTagLayout_Layout_layout_minHeight, NOT_SET);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.MgTagLayout_Layout_layout_maxWidth, MAX_SIZE);
            mMaxHeight = a.getDimensionPixelSize(R.styleable.MgTagLayout_Layout_layout_maxHeight, MAX_SIZE);
            a.recycle();
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            mMinWidth = source.mMinWidth;
            mMinHeight = source.mMinHeight;
            mMaxWidth = source.mMaxWidth;
            mMaxHeight = source.mMaxHeight;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(new ViewGroup.LayoutParams(width, height));
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        public int getMinWidth() {
            return mMinWidth;
        }

        public int getMinHeight() {
            return mMinHeight;
        }

        public int getMaxWidth() {
            return mMaxWidth;
        }

        public int getMaxHeight() {
            return mMaxHeight;
        }

        @Override
        public int getMarginLeft() {
            return 0;
        }

        @Override
        public int getMarginTop() {
            return 0;
        }

        @Override
        public int getMarginRight() {
            return 0;
        }

        @Override
        public int getMarginBottom() {
            return 0;
        }
    }


}
