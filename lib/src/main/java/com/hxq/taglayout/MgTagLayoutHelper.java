package com.hxq.taglayout;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.hxq.taglayout.MgTagContainer.NOT_SET;

/**
 * helper
 * @author xinghai.qi
 * @since 2023/11/1 11:09
 */
public class MgTagLayoutHelper {

    private final MgTagContainer mTagContainer;

    MgTagLayoutHelper(MgTagContainer tagContainer) {
        mTagContainer = tagContainer;
    }

    /**
     * measure tags
     * @param result
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    public void calculateLines(MgTagLinesResult result, int widthMeasureSpec, int heightMeasureSpec) {
        List<MgTagLine> tagLineList = new ArrayList<>();
        result.tagLines = tagLineList;

        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int mainWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int childState = 0;

        int startPadding = mTagContainer.getPaddingStart();
        int endPadding = mTagContainer.getPaddingEnd();
        int topPadding = mTagContainer.getPaddingTop();
        int bottomPadding = mTagContainer.getPaddingBottom();

        MgTagLine tagLine = new MgTagLine();
        tagLine.mWidth = startPadding + endPadding;
        tagLine.mFirstIndex = 0;

        // The index of the view in the tag line.
        int indexInTagLine = 0;

        int childCount = mTagContainer.getTagItemCount();
        for (int i = 0; i < childCount; i++) {
            View child = mTagContainer.getTagItemAt(i);
            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }

            final MgTagItem tagItem = (MgTagItem) child.getLayoutParams();
            int childWidthMeasureSpec;
            int childHeightMeasureSpec;

            childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                    startPadding + endPadding + tagItem.getMarginLeft() + tagItem.getMarginRight(),
                    tagItem.getWidth());
            childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                    topPadding + bottomPadding + tagItem.getMarginTop() + tagItem.getMarginBottom(),
                    tagItem.getHeight());
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            checkSizeConstraints(child);

            childState = View.combineMeasuredStates(childState, child.getMeasuredState());

            int maxLines = hasValidExpandView() ? mTagContainer.getMaxLineBeforeExpand() : mTagContainer.getMaxLines();
            if (isLineBreakRequired(mainWidth, widthMode, tagLine.mWidth, mTagContainer.getWidthTagNeeded(child, indexInTagLine))) {
                if (i == tagLine.mFirstIndex) {
                    // 当前行的第一个item，没有换行的必要，限制宽度为剩余宽度
                    childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mainWidth - tagLine.mWidth, View.MeasureSpec.EXACTLY);
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                    tagLine.mItemCount++;
                    indexInTagLine++;
                } else {
                    if (maxLines != NOT_SET && maxLines == tagLineList.size() + 1) {
                        // 已经是最后一行了，剩余的item都摆放不下，放弃继续摆放
                        if (mTagContainer.getThresholdWidth() != NOT_SET && mainWidth - tagLine.mWidth >= mTagContainer.getThresholdWidth()) {
                            childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mainWidth - tagLine.mWidth, View.MeasureSpec.EXACTLY);
                            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                            tagLine.mItemCount++;
                            indexInTagLine++;
                        } else {
                            // 后面的item都要舍弃了
                            addTagLine(tagLineList, tagLine, i == 0 ? 0 : i - 1);
                            break;
                        }
                    } else {
                        // 需要换行, 添加上一个line，new出新的line
                        addTagLine(tagLineList, tagLine, i == 0 ? 0 : i - 1);
                        tagLine = new MgTagLine();
                        tagLine.mItemCount = 1;
                        tagLine.mWidth = startPadding + endPadding;
                        tagLine.mFirstIndex = i;
                        indexInTagLine = 0;
                    }
                }
            } else {
                // 不需要换行
                tagLine.mItemCount++;
                indexInTagLine++;
            }

            tagLine.mWidth += mTagContainer.getWidthTagNeeded(child, indexInTagLine);
            tagLine.mHeight = Math.max(tagLine.mHeight, mTagContainer.getHeightTagNeeded(child));

            if (i == childCount - 1) {
                // 最后一个item了
                addTagLine(tagLineList, tagLine, i == 0 ? 0 : i - 1);
            }
        }

        result.shouldShowExpandView = determineIfShowExpandView(tagLineList);

        // 最后处理展开View的测量
        if (result.shouldShowExpandView) {
            View expandView = mTagContainer.getExpandView();
            final MgTagItem tagItem = (MgTagItem) expandView.getLayoutParams();
            int childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec,
                    startPadding + endPadding + tagItem.getMarginLeft() + tagItem.getMarginRight(),
                    tagItem.getWidth());
            int childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec,
                    topPadding + bottomPadding + tagItem.getMarginTop() + tagItem.getMarginBottom(),
                    tagItem.getHeight());
            expandView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            checkSizeConstraints(expandView);
            childState = View.combineMeasuredStates(childState, expandView.getMeasuredState());

            MgTagLine lastTagLine = tagLineList.get(tagLineList.size() - 1);
            int expandViewWidth = mTagContainer.getWidthTagNeeded(expandView, 0);

            int newItemCount = 0;
            int newTagLineWidth = startPadding + endPadding + expandViewWidth;
            int expandViewIndex = -1;
            for (int i = 0; i < lastTagLine.mItemCount; i++) {
                View childView = mTagContainer.getTagItemAt(lastTagLine.mFirstIndex + i);
                int childNeedWidth = mTagContainer.getWidthTagNeeded(childView, i);
                if (newTagLineWidth + childNeedWidth > mainWidth) {
                    // 当前的i摆不下了，break
                    if (i == 0) {
                        childWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mainWidth - newTagLineWidth, View.MeasureSpec.EXACTLY);
                        childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
                        newItemCount++;
                        newTagLineWidth += mTagContainer.getWidthTagNeeded(childView, i);
                        expandViewIndex = i + 1;
                    }
                    break;
                }
                newItemCount++;
                newTagLineWidth += childNeedWidth;
                expandViewIndex = i + 1;
            }

            lastTagLine.mItemCount = newItemCount + 1;
            lastTagLine.mWidth = newTagLineWidth;
            lastTagLine.mHeight = Math.max(expandView.getMeasuredHeight(), lastTagLine.mHeight);
            lastTagLine.mExpandIndexInLine = expandViewIndex;
        }

        result.childState = childState;
    }

    /**
     * layout tags
     * @param result
     */
    public void layout(MgTagLinesResult result) {
        List<MgTagLine> tagLineList = result.tagLines;
        if (tagLineList == null || tagLineList.size() == 0) {
            return;
        }
        int lineTop = mTagContainer.getPaddingTop();
        for (int lineIndex = 0; lineIndex < tagLineList.size(); lineIndex++) {
            MgTagLine tagLine = tagLineList.get(lineIndex);
            int lineLeft = mTagContainer.getPaddingStart();
            for (int index = 0; index < tagLine.mItemCount; index++) {
                View child;
                if (result.shouldShowExpandView && tagLine.mExpandIndexInLine > 0 && index == tagLine.mExpandIndexInLine) {
                    child = mTagContainer.getExpandView();
                } else {
                    child = mTagContainer.getTagItemAt(index + tagLine.mFirstIndex);
                }

                if (!result.shouldShowExpandView && child == mTagContainer.getExpandView()) {
                    continue;
                }

                MgTagItem tagItem = (MgTagItem) child.getLayoutParams();
                int left = lineLeft + tagItem.getMarginLeft();
                // 底部对齐
                int top = lineTop + tagItem.getMarginTop() + (tagLine.mHeight - child.getMeasuredHeight());
                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();
                child.layout(left, top, right, bottom);

                lineLeft = right + tagItem.getMarginRight() + mTagContainer.getHorizontalSpacing();
            }
            lineTop += tagLine.mHeight + mTagContainer.getVerticalSpacing();
        }
    }

    /**
     * 决定是否需要展示展开View
     * @param tagLineList
     * @return
     */
    private boolean determineIfShowExpandView(List<MgTagLine> tagLineList) {
        if (tagLineList == null || !hasValidExpandView()) {
            return false;
        }
        if (tagLineList.size() < mTagContainer.getMaxLineBeforeExpand()) {
            return false;
        }

        int totalItemInLine = 0;
        for (MgTagLine tagLine : tagLineList) {
            totalItemInLine += tagLine.mItemCount;
        }
        return totalItemInLine < mTagContainer.getTagItemCount() - 1;
    }

    /**
     * 是否需要展示展开按钮
     * @return
     */
    private boolean hasValidExpandView() {
        View expandView = mTagContainer.getExpandView();
        if (expandView == null || expandView.getVisibility() == View.GONE) {
            return false;
        }
        if (mTagContainer.getMaxLineBeforeExpand() <= 0 || mTagContainer.getMaxLines() != NOT_SET && mTagContainer.getMaxLineBeforeExpand() >= mTagContainer.getMaxLines()) {
            return false;
        }
        return true;
    }

    /**
     * 是否需要换行
     * @param mainWidth
     * @param mainMode
     * @param currentWidth
     * @param childWidth
     * @return
     */
    private boolean isLineBreakRequired(int mainWidth, int mainMode, int currentWidth, int childWidth) {
        if (mainMode == View.MeasureSpec.UNSPECIFIED) {
            // 父view未限制宽度
            return false;
        }
        return mainWidth < currentWidth + childWidth;
    }

    private void addTagLine(List<MgTagLine> tagLineList, MgTagLine tagLine, int viewIndex) {
        tagLine.mLastIndex = viewIndex;
        tagLineList.add(tagLine);
    }

    /**
     * 检查view的宽高是否有限制
     * @param view
     */
    private void checkSizeConstraints(View view) {
        boolean needsMeasure = false;
        MgTagItem tagItem = (MgTagItem) view.getLayoutParams();
        int childWidth = view.getMeasuredWidth();
        int childHeight = view.getMeasuredHeight();

        if (childWidth < tagItem.getMinWidth()) {
            needsMeasure = true;
            childWidth = tagItem.getMinWidth();
        } else if (childWidth > tagItem.getMaxWidth()) {
            needsMeasure = true;
            childWidth = tagItem.getMaxWidth();
        }

        if (childHeight < tagItem.getMinHeight()) {
            needsMeasure = true;
            childHeight = tagItem.getMinHeight();
        } else if (childHeight > tagItem.getMaxHeight()) {
            needsMeasure = true;
            childHeight = tagItem.getMaxHeight();
        }
        if (needsMeasure) {
            int widthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec
                    .makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
        }
    }

    static class MgTagLinesResult {
        List<MgTagLine> tagLines;
        int childState;
        boolean shouldShowExpandView;

        void reset() {
            tagLines = null;
            childState = 0;
        }
    }
}
