package com.lg.base.ui;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.lg.base.bus.LogUtil;
import com.lg.base.ui.viewpager.FixedSpeedScroller;
import com.lg.base.utils.ScreenUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * 支持wrap_content及设置滑动间隔
 * @author liguo
 *
 */
public class BaseViewPager extends ViewPager {

	protected final String TAG = BaseViewPager.class.getSimpleName();

	FixedSpeedScroller mScroller = null;

	public BaseViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		initScroller(context);
	}

	public BaseViewPager(Context context) {
		super(context);
		initScroller(context);
	}

	private void initScroller(Context ctx) {
		try {
			mScroller = new FixedSpeedScroller(ctx, new AccelerateInterpolator());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置滑动过程的持续时间，单位毫秒
	 * 
	 * @param millons
	 */
	public void setDuration(int millons) {
		try {
			Field mField = ViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);
			mScroller.setmDuration(millons);
			mField.set(this, mScroller);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据内容的宽高来 动态计算viewpager的高度
	 */
	public void setViewPagerHeight(int imgWidth, int imgHeight) {
		Display dm = ScreenUtil.getDisplay(getContext());
		ViewGroup.LayoutParams lp = this.getLayoutParams();
		double scal = (dm.getWidth() * imgHeight * 1.0) / imgWidth;
		int height = new BigDecimal(scal).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		lp.height = height;
		lp.width = dm.getWidth();
		LogUtil.d(TAG, "initViewpagerHeight() viewPager.wxh=" + dm.getWidth() + "x" + height);
		this.setLayoutParams(lp);
	}

	/**
	 * 当viewpager用作ListView的HeaderView的时候，请用些方法解决滑动上的冲突
	 */
	public void setSupportListView(boolean support) {
		if (support) {
			this.setOnTouchListener(touchListener);
		} else {
			this.setOnTouchListener(null);
		}
	}

	private static final OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			PointF downP = new PointF();
			PointF curP = new PointF();
			int act = event.getAction();
			if (act == MotionEvent.ACTION_DOWN || act == MotionEvent.ACTION_MOVE || act == MotionEvent.ACTION_UP) {
				if(v instanceof ViewGroup) {
					((ViewGroup) v).requestDisallowInterceptTouchEvent(true);
				}
				if (downP.x == curP.x && downP.y == curP.y) {
					return false;
				}
			}
			return false;
		}
	};

}
