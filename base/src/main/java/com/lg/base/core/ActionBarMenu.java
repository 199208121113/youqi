package com.lg.base.core;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lg.base.R;

import java.util.List;

public class ActionBarMenu implements OnClickListener {
	private int icon = 0;
	private String title = null;
	private List<ItemView> items = null;

	public ActionBarMenu(int icon, String title) {
		super();
		this.icon = icon;
		this.title = title;
	}

	public ActionBarMenu(int icon, String title, List<ItemView> items) {
		this(icon, title);
		this.items = items;
	}

	public ActionBarMenu(String title) {
		super();
		this.title = title;
	}

	public ActionBarMenu(int icon) {
		super();
		this.icon = icon;
	}

	public int getIcon() {
		return icon;
	}

	public ActionBarMenu setIcon(int icon) {
		this.icon = icon;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public ActionBarMenu setTitle(String title) {
		this.title = title;
		return this;
	}

	public List<ItemView> getItems() {
		return items;
	}

	public ActionBarMenu setItems(List<ItemView> items) {
		this.items = items;
		return this;
	}

	public static class ItemView {
		private boolean clickable = false;
		private View view;

		public ItemView(View view) {
			super();
			this.view = view;
		}

		public View getView() {
			return view;
		}

		public void setView(View view) {
			this.view = view;
		}

		public boolean isClickable() {
			return clickable;
		}

		public void setClickable(boolean clickable) {
			this.clickable = clickable;
		}

		private boolean childrenClickable = true;

		public boolean isChildrenClickable() {
			return childrenClickable;
		}

		public void setChildrenClickable(boolean childrenClickable) {
			this.childrenClickable = childrenClickable;
		}

	}

	@Override
	public void onClick(View v) {
		if (this.listener != null) {
			this.listener.onActionBarClick(v);
		}
	}

	OnActionBarItemClickListener listener = null;

	ViewGroup viewGroup = null;
	private ImageView iconView;
	private TextView titleView;
	private LinearLayout itemLayout = null;
	private LinearLayout leftLayout = null;

	void setViewAndListener(View v, OnActionBarItemClickListener listener) {
		this.listener = listener;
		this.viewGroup = (ViewGroup) v;
		if (this.bgResId > 0) {
			this.viewGroup.setBackgroundResource(this.bgResId);
		}
		iconView = (ImageView) viewGroup.findViewById(R.id.action_bar_icon_left);
		titleView = (TextView) viewGroup.findViewById(R.id.action_bar_title);
		leftLayout = (LinearLayout) viewGroup.findViewById(R.id.action_bar_left_linear_layout);
		if (this.getIcon() > 0) {
			iconView.setImageResource(getIcon());
			iconView.setVisibility(View.VISIBLE);
			iconView.setOnClickListener(this);
			iconView.setAdjustViewBounds(true);
			iconView.setScaleType(ScaleType.FIT_CENTER);
		} else {
			iconView.setVisibility(View.GONE);
		}
		if (this.getTitle() != null && this.getTitle().trim().length() > 0) {
			titleView.setText(getTitle());
			titleView.setVisibility(View.VISIBLE);
			//titleView.setOnClickListener(this);
		} else {
			titleView.setVisibility(View.GONE);
		}

		List<ItemView> itemList = getItems();
		if (itemList != null && itemList.size() > 0) {
			itemLayout = (LinearLayout) viewGroup.findViewById(R.id.action_bar_layout);
			for (ItemView item : itemList) {
				addItemView(item);
			}
		}
	}

	public View getChildItemView(int index) {
		if (itemLayout == null)
			return null;
		return itemLayout.getChildAt(index);
	}

	public int getChildItemCount() {
		if (itemLayout == null)
			return 0;
		return itemLayout.getChildCount();
	}

	public void removeAllItems(){
		if(itemLayout == null || itemLayout.getChildCount() == 0){
			return;
		}
		itemLayout.removeAllViews();
	}

	public void addItemView(ItemView item){
		if(item == null){
			return;
		}
		View vv = item.getView();
		if (item.isClickable()) {
			vv.setOnClickListener(this);
		}
		if (vv instanceof ViewGroup) {
			if (item.isChildrenClickable()) {
				ViewGroup vg = (ViewGroup) vv;
				int n = vg.getChildCount();
				for (int i = 0; i < n; i++) {
					vg.getChildAt(i).setOnClickListener(this);
				}
			}
		}
		itemLayout.addView(vv);
	}

	public ImageView getIconView() {
		return iconView;
	}

	public TextView getTitleView() {
		return titleView;
	}

	/** 整个actionbar的view */
	public ViewGroup getViewGroup() {
		return viewGroup;
	}

	/** 标题+icon的ViewGropu,主要用于点击时设置返回的效果 */
	public LinearLayout getLeftLayout() {
		return leftLayout;
	}

	// =====================background===============
	private int bgResId = -1;

	public void setBackground(int bgResId) {
		if (bgResId <= 0)
			return;
		this.bgResId = bgResId;
		if (this.viewGroup != null) {
			this.viewGroup.setBackgroundResource(bgResId);
		}
	}
}
