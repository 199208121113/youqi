package com.lg.base.ui.date;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class DatePicker extends ViewPager {

	private DatePickerAdapter datePickerAdapter = null;
	public DatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DatePicker(Context context) {
		super(context);
		init();
	}
	private void init(){
		datePickerAdapter = new DatePickerAdapter();
		this.setAdapter(datePickerAdapter);
	}

	public DatePickerAdapter getDatePickerAdapter() {
		return datePickerAdapter;
	}
	
	public void setOnCellItemClickListener(OnCellItemClickListener listener) {
		datePickerAdapter.setOnCellItemClickListener(listener);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = child.getMeasuredHeight();
			if (h > height)
				height = h;
		}
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
}
