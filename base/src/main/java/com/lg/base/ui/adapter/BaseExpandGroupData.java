package com.lg.base.ui.adapter;

import java.util.ArrayList;

public class BaseExpandGroupData<C, CS>{

	private final ArrayList<AdapterItem<C, CS>> childItemList = new ArrayList<AdapterItem<C, CS>>();

	public ArrayList<AdapterItem<C, CS>> getChildItemList() {
		return childItemList;
	}

	public void addItemChild(C c, CS cs) {
		AdapterItem<C, CS> item = new AdapterItem<C, CS>(c, cs);
		childItemList.add(item);
	}

	public void addItemChild(C c, CS cs,OnAdapterItemStateChangeListener<C,CS> listener) {
		AdapterItem<C, CS> item = new AdapterItem<C, CS>(c, cs);
		childItemList.add(item);
		if(listener != null) {
			item.setOnAdapterItemStateChangeListener(listener);
		}
	}

	public int getChildCount() {
		return childItemList.size();
	}

}
