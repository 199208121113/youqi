package cn.iwgang.familiarrecyclerview.refresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lg.base.R;
import com.lg.base.utils.ScreenUtil;

/**
 * Created by Syehunter on 2015/11/20.
 */
public class RefreshFooterLayout extends FrameLayout {

    public RefreshFooterLayout(Context context) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_refresh_bottom, this, false);
        int height = ScreenUtil.dip2px(context, 40);
        int width = ScreenUtil.getDisplay(context).getWidth();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width,height);
        view.setLayoutParams(lp);
        this.addView(view);
    }
}
