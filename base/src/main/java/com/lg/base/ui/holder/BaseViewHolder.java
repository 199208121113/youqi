package com.lg.base.ui.holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;

import com.lg.base.ui.adapter.AdapterItem;

import java.lang.reflect.Type;

public abstract class BaseViewHolder<DATA, STATE> implements IViewHolder<DATA, STATE> {
    protected final String TAG = this.getClass().getSimpleName();
	protected AdapterItem<DATA, STATE> item = null;
	private View rootView = null;
	private final Context context;

	public BaseViewHolder(View rootView, Context ctx) {
		super();
		this.rootView = rootView;
		this.context = ctx;
	}

	protected abstract void onInitViews(View view);

	protected abstract void onBindItem();

	protected abstract void onRecycleItem();

	protected abstract void onRefreshView();

	protected abstract void onDestroy();

	@Override
	public void setItem(AdapterItem<DATA, STATE> item) {
		if (item == null)
			return;
		boolean changed = isChangedForCurrentEntity(item);
		if (!changed) {
			refreshView();
			return;
		}
		if (this.item != null) {
			recycleItem();
		}
		this.item = item;
		bindItem();
	}

    protected boolean isChangedForCurrentEntity(AdapterItem<DATA, STATE> newItem){
        return this.item != newItem;
	}

	@Override
	public final AdapterItem<DATA, STATE> getItem() {
		return this.item;
	}

	@Override
	public final void bindItem() {
		onBindItem();
	}

	@Override
	public final void refreshView() {
		onRefreshView();
	}

	@Override
	public final View getRootView() {
		return this.rootView;
	}

	@Override
	public final void initViews() {
		onInitViews(getRootView());
	}

	@Override
	public final void recycleItem() {
		onRecycleItem();
	}

	@Override
	public final void destroy() {
		onDestroy();
	}

	@SuppressWarnings("unchecked")
	protected final <V extends View> V find(int id) {
		return (V) this.rootView.findViewById(id);
	}

	public final Context getMyContext() {
		return context;
	}

	// ====================================================
	protected int posIndex = -1;

	public final int getPosIndex() {
		return posIndex;
	}

	public final void setPosIndex(int posIndex) {
		this.posIndex = posIndex;
	}

	protected int posGroupIndex = -1;

	public final int getPosGroupIndex() {
		return posGroupIndex;
	}

	public final void setPosGroupIndex(int posGroupIndex) {
		this.posGroupIndex = posGroupIndex;
	}
	// ============================================

	protected int scrollState = OnScrollListener.SCROLL_STATE_IDLE;
	protected int firstVisibleItem = 0;
	protected int visibleItemCount = 0;
	protected int totalItemCount = 0;

	@Override
	public void onScrollStateChanged(ViewGroup vg, int scrollState) {
		this.scrollState = scrollState;
	}

	@Override
	public void onScroll(ViewGroup vg, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
		this.visibleItemCount = visibleItemCount;
		this.totalItemCount = totalItemCount;
	}
	
    public void onLayout(){

    }
	private Type dataType;

	public Type getDataType() {
		return dataType;
	}

	public void setDataType(Type dataType) {
		this.dataType = dataType;
	}
}
