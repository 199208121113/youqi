package com.lg.base.ui.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lg.base.ui.adapter.AdapterItem;
import com.lg.base.ui.holder.IViewHolder;

/**
 * Created by root on 16-1-28.
 */
public abstract class RecyclerViewHolder<DATA, STATE> extends RecyclerView.ViewHolder implements IViewHolder<DATA, STATE> {
    protected final String TAG = this.getClass().getSimpleName();
    protected AdapterItem<DATA, STATE> item = null;
    private View rootView = null;
    private final Context context;

    public RecyclerViewHolder(View rootView, Context ctx) {
        super(rootView);
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
}
