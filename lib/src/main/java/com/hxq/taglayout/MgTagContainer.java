package com.hxq.taglayout;

import android.view.View;

public interface MgTagContainer {

    int NOT_SET = -1;

    int MAX_SIZE = Integer.MAX_VALUE & View.MEASURED_SIZE_MASK;

    View getTagItemAt(int index);

    /**
     * @return the number of tag items contained in the tag container.
     */
    int getTagItemCount();

    /**
     * @return the current value of the maximum number of tag lines. If not set, {@link #NOT_SET}
     * is returned.
     */
    int getMaxLines();

    /**
     * @return the top padding of the tag container.
     */
    int getPaddingTop();

    /**
     * @return the bottom padding of the tag container.
     */
    int getPaddingBottom();

    /**
     * @return the start padding of this view depending on its resolved layout direction.
     */
    int getPaddingStart();

    /**
     * @return the end padding of this view depending on its resolved layout direction.
     */
    int getPaddingEnd();

    /**
     * Returns the child measure spec for its width.
     *
     * @param widthSpec      the measure spec for the width imposed by the parent
     * @param padding        the padding along the width for the parent
     * @param childDimension the value of the child dimension
     */
    int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension);

    /**
     * Returns the child measure spec for its height.
     *
     * @param heightSpec     the measure spec for the height imposed by the parent
     * @param padding        the padding along the height for the parent
     * @param childDimension the value of the child dimension
     */
    int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension);

    int getWidthTagNeeded(View tagView, int tagIndexInLine);

    int getHeightTagNeeded(View tagView);

    int getHorizontalSpacing();

    int getVerticalSpacing();

    /**
     * 可摆放的阈值
     * @return
     */
    int getThresholdWidth();

    View getExpandView();

    int getMaxLineBeforeExpand();
}
