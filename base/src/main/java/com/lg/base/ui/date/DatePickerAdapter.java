package com.lg.base.ui.date;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.ui.adapter.AdapterItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatePickerAdapter extends PagerAdapter implements OnItemClickListener {

	protected static final String TAG = DatePickerAdapter.class.getSimpleName();

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, position);
		View view = LayoutInflater.from(container.getContext()).inflate(R.layout.layout_date_picker, null);
		initView(view, cal, position);
		((ViewPager) container).addView(view, 0);
		return view;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}

	private void initView(View v, Calendar cal, final int pos) {
		TextView monthTv = (TextView) v.findViewById(R.id.date_picker_month_tv);
		String monthStr = DateUtil.getMonthOfYear(cal.get(Calendar.MONTH));
		String yearStr = cal.get(Calendar.YEAR) + "年";
		monthTv.setText(monthStr + " " + yearStr);

		CellAdapter cellAdapter = new CellAdapter(v.getContext());
		GridView gv = (GridView) v.findViewById(R.id.date_picker_grid_view);
		gv.setAdapter(cellAdapter);
		gv.setOnItemClickListener(this);
		List<CellItem> itemList1 = DateUtil.getBeforeDaysOfMonth(cal);
		addData(cellAdapter, itemList1, false);
		List<CellItem> itemList2 = DateUtil.getCurrentDaysOfMonth(cal);
		addData(cellAdapter, itemList2, true);
		List<CellItem> itemList3 = DateUtil.getAfterDaysOfMonth2(cal, itemList1.size());
		addData(cellAdapter, itemList3, false);

		cellAdapter.notifyDataSetChanged();

	}

	private void addData(CellAdapter cellAdapter, List<CellItem> itemList, boolean enable) {
		for (CellItem cellItem : itemList) {
			cellAdapter.addItem(cellItem, new CellStatus(enable));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CellAdapter adpter = (CellAdapter) parent.getAdapter();
		CellStatus status = adpter.getItem(position).getState();
		if (!status.isEnable()) { // 不可选的
			return;
		}
		final CellItem item = adpter.getItem(position).getData();
		ArrayList<AdapterItem<CellItem, CellStatus>> items = adpter.getItems();
		for (AdapterItem<CellItem, CellStatus> adapterItem : items) {
			boolean checked = adapterItem.getData().getDay() == item.getDay();
			adapterItem.getState().setChecked(checked);
		}
		adpter.notifyDataSetChanged();
		if (itemClickistener != null) {
			itemClickistener.onItemClick(item);
		}
	}

	private OnCellItemClickListener itemClickistener = null;

	void setOnCellItemClickListener(OnCellItemClickListener listener) {
		this.itemClickistener = listener;
	}

	@Override
	public int getItemPosition(Object object) {
		return PagerAdapter.POSITION_NONE;
	}
}
