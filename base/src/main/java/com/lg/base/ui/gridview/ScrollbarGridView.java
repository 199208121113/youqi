package com.lg.base.ui.gridview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

@SuppressWarnings("javadoc")
public class ScrollbarGridView extends GridView {
    private boolean haveScrollbar = false;

    public ScrollbarGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollbarGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollbarGridView(Context context) {
        super(context);
    }

    /**
     * 设置是否有ScrollBar，当要在ScollView中显示时，应当设置为false。 默认为 false
     *
     * @param haveScrollbars
     */
    public void setHaveScrollbar(boolean haveScrollbar) {
        this.haveScrollbar = haveScrollbar;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (haveScrollbar == false) {
            int expandSpec = MeasureSpec.makeMeasureSpec( Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
//    	int expandSpec = MeasureSpec.makeMeasureSpec( Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
//      super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
