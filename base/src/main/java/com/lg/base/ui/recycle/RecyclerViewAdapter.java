package com.lg.base.ui.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.lg.base.ui.adapter.AdapterItem;
import com.lg.base.ui.adapter.OnAdapterItemStateChangeListener;

import java.util.ArrayList;


/**
 * Created by root on 16-1-28.
 */
public abstract class RecyclerViewAdapter<T,STATE,VH extends RecyclerViewHolder<T,STATE>> extends RecyclerView.Adapter<VH> {
    @SuppressWarnings("unused")
    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();
    private Context ctx;
    protected LayoutInflater inflater;
    private ArrayList<AdapterItem<T, STATE>> mDataList = new ArrayList<>();

    public RecyclerViewAdapter(Context ctx) {
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }


    //=============================================================================================
    public void destroy(){
        mDataList.clear();
    }

    @Override
    public final int getItemCount() {
        return mDataList.size();
    }

    public final void clearItems(){
        mDataList.clear();
    }

    private AdapterItem<T, STATE> convertDataToAdapterItem(T data, STATE state){
        if (data == null)
            return null;
        return new AdapterItem<>(data, state);
    }

    public final AdapterItem<T, STATE> addItem(T data, STATE state) {
        return addItem(mDataList.size(),data,state);
    }

    public final AdapterItem<T, STATE> addItem(int index,T data, STATE state) {
        AdapterItem<T, STATE> adapterItem = convertDataToAdapterItem(data, state);
        if(adapterItem != null)
            mDataList.add(index,adapterItem);
        return adapterItem;
    }

    public final AdapterItem<T, STATE> addItem(AdapterItem<T, STATE> ai) {
        mDataList.add(ai);
        return ai;
    }

    public final AdapterItem<T, STATE> addItem(T data, STATE state, OnAdapterItemStateChangeListener<T, STATE> listener) {
        AdapterItem<T, STATE> item = addItem(data, state);
        if (item != null)
            item.setOnAdapterItemStateChangeListener(listener);
        return item;
    }

    public final AdapterItem<T, STATE> getItem(int location){
        return mDataList.get(location);
    }

    public final void delItem(AdapterItem<T, STATE> item) {
        if (item == null || mDataList == null || mDataList.size() == 0)
            return;
        mDataList.remove(item);
    }

    public final AdapterItem<T, STATE> delItem(int position) {
        if(mDataList == null)
            return null;
        if (position >= mDataList.size())
            return null;
        AdapterItem<T, STATE> item = mDataList.remove(position);
        return item;
    }

    public final ArrayList<AdapterItem<T, STATE>> getItems() {
        ArrayList<AdapterItem<T, STATE>> items = new ArrayList<>();
        items.addAll(mDataList);
        return items;
    }

    protected final Context getCtx() {
        return ctx;
    }
}
