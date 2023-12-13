package com.hxq.taglayout;

/**
 * item的属性
 * @author xinghai.qi
 * @since 2023/12/13 10:27
 */
public interface MgTagItem {

    /**
     * @return the width attribute of the tag item.
     *
     * The attribute is about how wide the view wants to be. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    int getWidth();

    /**
     * @return the height attribute of the tag item.
     *
     * The attribute is about how wide the view wants to be. Can be one of the
     * constants MATCH_PARENT(-1) or WRAP_CONTENT(-2), or an exact size.
     */
    int getHeight();

    /**
     * @return the minimum width attribute of the tag item
     *
     * The attribute determines the minimum width the child can shrink to.
     */
    int getMinWidth();

    /**
     * @return the minimum height attribute of the tag item
     *
     * The attribute determines the minimum height the child can shrink to.
     */
    int getMinHeight();

    /**
     * @return the maximum width attribute of the tag item
     *
     * The attribute determines the maximum width the child can expand to.
     */
    int getMaxWidth();

    /**
     * @return the maximum height attribute of the tag item
     */
    int getMaxHeight();

    /**
     * @return the left margin of the tag item.
     */
    int getMarginLeft();

    /**
     * @return the top margin of the tag item.
     */
    int getMarginTop();

    /**
     * @return the right margin of the tag item.
     */
    int getMarginRight();

    /**
     * @return the bottom margin of the tag item.
     */
    int getMarginBottom();

    /**
     * @return the start margin of the tag item depending on its resolved layout direction.
     */
    int getMarginStart();

    /**
     * @return the end margin of the tag item depending on its resolved layout direction.
     */
    int getMarginEnd();
}
