package com.lg.base.ui.recycle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lg.base.ui.adapter.AdapterItem;
import com.lg.base.ui.adapter.OnAdapterItemStateChangeListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by root on 16-1-28.
 */
public abstract class RecyclerViewAdapter<T,STATE,VH extends RecyclerViewHolder<T,STATE>> extends RecyclerView.Adapter<VH> {
    private final String TAG = "BaseRecyclerAdapter";
    private Context ctx;
    protected LayoutInflater inflater;
    private ArrayList<AdapterItem<T, STATE>> mDataList = new ArrayList<>();

    public RecyclerViewAdapter(Context ctx) {
        this.ctx = ctx;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH vh = startCreateViewHolder(parent,viewType);
        vh.initViews();
        return vh;
    }

    protected abstract VH startCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if(itemClickListener != null) {
            holder.getRootView().setOnClickListener(new TempClickListener(position, itemClickListener));
        }
        if(itemLongClickListener != null) {
            holder.getRootView().setOnLongClickListener(new TempLongClickListener(position, itemLongClickListener));
        }
        startBindViewHolder(holder,position);
    }
    protected abstract void startBindViewHolder(VH holder, int position);

    @Override
    public final int getItemCount() {
        return mDataList.size()+headerViewList.size();
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    private RecyclerItemClickListener itemClickListener = null;
    private RecyclerItemLongClickListener itemLongClickListener = null;

    public final void setItemClickListener(RecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public final void setItemLongClickListener(RecyclerItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    class TempClickListener implements View.OnClickListener{
        int pos;
        private RecyclerItemClickListener listener = null;
        public TempClickListener(int pos,RecyclerItemClickListener listener) {
            this.pos = pos;
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(v,pos);
        }
    }

    class TempLongClickListener implements View.OnLongClickListener{
        int pos ;
        RecyclerItemLongClickListener listener;
        public TempLongClickListener(int pos,RecyclerItemLongClickListener listener) {
            this.pos = pos;
            this.listener = listener;
        }

        @Override
        public boolean onLongClick(View v) {
            return listener.onItemLongClick(v,pos);
        }
    }

    //=============================================================================================
    public void destroy(){
        mDataList.clear();
        headerViewList.clear();
    }

    public  final int getDataCount(){
        return mDataList.size();
    }

    public  final void clearItems(){
        mDataList.clear();
    }

    private AdapterItem<T, STATE> convertDataToAdapterItem(T data, STATE state){
        if (data == null)
            return null;
        return new AdapterItem<>(data, state);
    }

    public  final AdapterItem<T, STATE> addItem(T data, STATE state) {
        return addItem(mDataList.size(),data,state);
    }

    public  final AdapterItem<T, STATE> addItem(int index,T data, STATE state) {
        AdapterItem<T, STATE> adapterItem = convertDataToAdapterItem(data, state);
        if(adapterItem != null)
            mDataList.add(index,adapterItem);
        return adapterItem;
    }

    public  final AdapterItem<T, STATE> addItem(AdapterItem<T, STATE> ai) {
        mDataList.add(ai);
        return ai;
    }

    public  final AdapterItem<T, STATE> addItem(T data, STATE state, OnAdapterItemStateChangeListener<T, STATE> listener) {
        AdapterItem<T, STATE> item = addItem(data, state);
        if (item != null)
            item.setOnAdapterItemStateChangeListener(listener);
        return item;
    }

    public  final AdapterItem<T, STATE> getItem(int location){
        return mDataList.get(location);
    }

    public  final void delItem(AdapterItem<T, STATE> item) {
        if (item == null || mDataList == null || mDataList.size() == 0)
            return;
        mDataList.remove(item);
    }

    public  final AdapterItem<T, STATE> delItem(int position) {
        if(mDataList == null)
            return null;
        if (position >= mDataList.size())
            return null;
        AdapterItem<T, STATE> item = mDataList.remove(position);
        return item;
    }

    public  final ArrayList<AdapterItem<T, STATE>> getItems() {
        ArrayList<AdapterItem<T, STATE>> items = new ArrayList<>();
        items.addAll(mDataList);
        return items;
    }

    protected  final Context getCtx() {
        return ctx;
    }

    //====================================================
    private List<View> headerViewList = new ArrayList<>();
    public final  void addHeaderView(View headerView){
        if(headerView == null)
            return;
        headerViewList.add(headerView);
    }

    public final  boolean removeHeaderView(View headerView){
        if(headerView == null)
            return false;
        return headerViewList.remove(headerView);
    }

    public final int getHeaderViewCount(){
        if(headerViewList == null){
            return 0;
        }
        return headerViewList.size();
    }

    public  final boolean isHeader(int position){
        return position < getHeaderViewCount();
    }

    public  final View getFirstHeaderView(){
        if(headerViewList.size() == 0){
            return null;
        }
        return headerViewList.get(0);
    }
}
