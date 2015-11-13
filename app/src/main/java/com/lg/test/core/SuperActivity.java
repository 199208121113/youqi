package com.lg.test.core;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.BaseActivity;
import com.lg.base.utils.ScreenUtil;
import com.lg.test.R;

/**
 *
 * Created by liguo on 2015/11/13.
 */
public abstract class SuperActivity extends BaseActivity {

    private static int leftIconPaddingSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (leftIconPaddingSize == 0) {
            leftIconPaddingSize = ScreenUtil.dip2px(this, 10);
        }
        ActionBarMenu bar = getActionBarMenu();
        processActionBarMenu(bar);
    }

    public static void processActionBarMenu(ActionBarMenu bar) {
        if (bar == null)
            return;
        if (bar.getIcon() <= 0) {
            ImageView iv = bar.getIconView();
            if (iv != null) {
                iv.setVisibility(View.VISIBLE);
                iv.setImageResource(R.drawable.back_arrow_ffffff);
                iv.setPadding(0, leftIconPaddingSize, 0, leftIconPaddingSize);
            }
        }
        /*
         * 如果设置ActionBar的背景及底部线的颜色可直接在color.xml中覆盖原有的值
         <color name="col_action_bar_bottom_line">#00FF00</color>
         <color name="col_action_bar_background">#FF0000</color>
         */
        bar.getTitleView().setTextColor(Color.parseColor("#565656"));
        bar.getTitleView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        // 设置整actionbar的view
        bar.getViewGroup().setBackgroundColor(Color.parseColor("#EEEEEE"));
        bar.getLeftLayout().setBackgroundResource(R.drawable.sl_back_bg);

    }
}
