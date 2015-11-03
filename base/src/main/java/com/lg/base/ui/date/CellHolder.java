package com.lg.base.ui.date;

import android.content.Context;
import android.view.View;
import android.widget.CheckedTextView;

import com.lg.base.R;
import com.lg.base.ui.holder.BaseViewHolder;

public class CellHolder extends BaseViewHolder<CellItem, CellStatus> {

	public CellHolder(View rootView, Context act) {
		super(rootView, act);
	}

	CheckedTextView tv1;
	@Override
	protected void onInitViews(View view) {
		tv1 = find(R.id.item_date_picker_tv);
	}

	@Override
	protected void onBindItem() {
		bindText();
	}

	private void bindText() {
		String text = ""+getItem().getData().getDay();
		tv1.setText(text);
		CellStatus cs = getItem().getState();
		tv1.setEnabled(cs.isEnable());
		tv1.setChecked(cs.isChecked());
	}

	@Override
	protected void onResetViews() {

	}

	@Override
	protected void onRecycleItem() {
		tv1.setText("");
	}

	@Override
	protected void onRefreshView() {
		bindText();
	}

	@Override
	protected void onDestroy() {
		tv1 = null;
	}

}
