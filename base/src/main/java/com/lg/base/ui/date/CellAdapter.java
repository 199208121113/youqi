package com.lg.base.ui.date;

import android.content.Context;
import android.view.View;

import com.lg.base.R;
import com.lg.base.ui.adapter.MyBaseAdapter;
import com.lg.base.ui.holder.BaseViewHolder;

public class CellAdapter extends MyBaseAdapter<CellItem, CellStatus> {

	
	public CellAdapter(Context act) {
		super(act);
	}

	@Override
	protected void onInitViewType() {
		addViewType(CellItem.class, R.layout.item_date_picker);
	}

	@Override
	protected BaseViewHolder<CellItem, CellStatus> onCreateViewHolder(View view, Context ctx) {
		return new CellHolder(view, ctx);
	}

	@Override
	protected void onDestory() {
		
	}
	

}
