package com.lg.base.ui.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lg.base.ui.holder.BaseViewHolder;
import com.lg.base.ui.holder.IViewHolder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("javadoc")
public abstract class MyBaseAdapter<T,STATE> extends BaseAdapter {

	protected final String TAG = this.getClass().getSimpleName();
	private final ArrayList<AdapterItem<T, STATE>> mData = new ArrayList<AdapterItem<T, STATE>>();
	private HashMap<String, Integer> mViewTypeMap = new HashMap<String, Integer>();
	private SparseArray<IViewHolder<T, STATE>> mHolderList = new SparseArray<IViewHolder<T, STATE>>();
	private Context context = null;
	private LayoutInflater mInflater = null;
	private final ConcurrentHashMap<String, IViewHolder<T, STATE>> viewMap = new ConcurrentHashMap<String, IViewHolder<T, STATE>>();
	
	protected abstract void onInitViewType();

	protected abstract BaseViewHolder<T, STATE> onCreateViewHolder(View view,Context act);

	protected abstract void onDestory();

	public MyBaseAdapter(Context act) {
		super();
		this.context = act;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		onInitViewType();
	}
	
	protected LayoutInflater getLayoutInflater(){
		if(mInflater == null){
			mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		return mInflater;
	}

	public void addViewType(Class<?> c, int layout) {
		String key = c.getName();
		mViewTypeMap.put(key,layout);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public AdapterItem<T, STATE> getItem(int arg0) {
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHolder<T, STATE> holder = null;
		AdapterItem<T, STATE> item = getItem(position);
		Type dataType = item.getData().getClass();
		if (convertView == null) {
			convertView = inflateView(position, null, parent);
			if(convertView == null){
				return null;
			}
			holder = createViewHolder(convertView,position,dataType);
			viewMap.put(holder.toString(), holder);
		} else {
			holder = (BaseViewHolder<T, STATE>) convertView.getTag();
			holder.setPosIndex(position);
			holder.onLayout();
		}
        convertView.setTag(holder);
		holder.setItem(item);
		mHolderList.put(position, holder);
		return convertView;
	}
	
	private AdapterItem<T, STATE> convertDataToAdapterItem(T data, STATE state){
		if (data == null)
			return null;
		return new AdapterItem<T, STATE>(data, state);
	}

	public AdapterItem<T, STATE> addItem(T data, STATE state) {
		return addItem(mData.size(),data,state);
	}
	
	public AdapterItem<T, STATE> addItem(int index,T data, STATE state) {
		AdapterItem<T, STATE> adapterItem = convertDataToAdapterItem(data,state);
		if(adapterItem != null)
			mData.add(index,adapterItem);
		return adapterItem;
	}
	
	public AdapterItem<T, STATE> addItem(AdapterItem<T, STATE> ai) {
		mData.add(ai);
		return ai;
	}
	
	public AdapterItem<T, STATE> addItem(T data, STATE state, OnAdapterItemStateChangeListener<T, STATE> listener) {
		AdapterItem<T, STATE> item = addItem(data, state);
		if (item != null)
			item.setOnAdapterItemStateChangeListener(listener);
		return item;
	}
	
	public List<AdapterItem<T, STATE>> addItems(List<T> datas,List<STATE> states){
		if(datas == null || datas.size() == 0){
			return null;
		}
		List<AdapterItem<T, STATE>> items = new ArrayList<>();
		for (int i = 0; i < datas.size() ; i++){
			STATE st = states != null && states.size() > i ? states.get(i) : null;
			AdapterItem<T, STATE> item = addItem(datas.get(i),st);
			if(item != null){
				items.add(item);
			}
		}
		return items;
	}

	public void delItem(AdapterItem<T, STATE> item) {
		if (item == null || mData == null || mData.size() == 0)
			return;
		mData.remove(item);
	}

	public AdapterItem<T, STATE> delItem(int position) {
		if(mData == null)
			return null;
		if (position >= mData.size())
			return null;
		AdapterItem<T, STATE> item = mData.remove(position);
		return item;
	}

	public void clearItems() {
		if(mData != null) {
			mData.clear();
		}
	}

	private BaseViewHolder<T, STATE> createViewHolder(View view,int posIndex,Type dataType) {
		BaseViewHolder<T, STATE> holder = onCreateViewHolder(view, this.context);
        holder.setPosIndex(posIndex);
		holder.setDataType(dataType);
		holder.initViews();
		return holder;
	}

	private View inflateView(int position, View convertView, ViewGroup parent) {
		int typeCount = getViewTypeCount();
		if(typeCount == 1) {
			int resource = getResourceId(position);
			View v = convertView;
			if (convertView == null) {
				v = mInflater.inflate(resource, parent, false);
			}
			return v;
		}else if(typeCount > 1){
			int resource = getResourceIdByItemViewType(position);
			return mInflater.inflate(resource, parent, false);
		}
		return null;
	}

	protected Integer getResourceIdByItemViewType(int position){
		return getResourceId(position);
	}

	private Integer getResourceId(int position) {
		AdapterItem<T, STATE> item = getItem(position);
		return getResourceId(item.getData());
	}

	private int getResourceId(T data) {
        final String className = data.getClass().getName();
        if(mViewTypeMap.containsKey(className)){
            return mViewTypeMap.get(className);
        }
        String parentClassName = data.getClass().getSuperclass().getName();
        if(mViewTypeMap.containsKey(parentClassName)){
            return mViewTypeMap.get(parentClassName);
        }
		throw new RuntimeException("can't find layout for className:"+className);
	}

	protected Context getMyContext() {
		return this.context;
	}

	/**
	 * 在Activity销毁的时候，必须要手动调用
	 */
	public void destory() {
		try {
			onDestory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (IViewHolder<T, STATE> h : viewMap.values()) {
			h.destroy();
		}
		mHolderList.clear();
		viewMap.clear();
		mViewTypeMap.clear();
		System.gc();
	}

	public ArrayList<AdapterItem<T, STATE>> getItems() {
		ArrayList<AdapterItem<T, STATE>> items = new ArrayList<AdapterItem<T, STATE>>();
		items.addAll(mData);
		return items;
	}

	public void onPrepareNotifyDataSetChanged() {
	}

	public void onPostNotifyDataSetChanged() {
	}

	@Override
	public void notifyDataSetChanged() {
		onPrepareNotifyDataSetChanged();
		super.notifyDataSetChanged();
		onPostNotifyDataSetChanged();
	}
	//获取Holder
	public IViewHolder<T, STATE> getViewHolder(int position){
		return mHolderList.get(position, null);
	}
	
	@Deprecated
	public static final void printStatck(){
		   Throwable ex = new Throwable();
	        StackTraceElement[] es = ex.getStackTrace();
	        if (es != null) {
	            for (int i = 0; i < es.length; i++) {
	                StackTraceElement item = es[i];
	                String gg = item.getClassName()+"->"+item.getMethodName()+" line:"+item.getLineNumber();
	                System.out.println(gg);
	            }
	        }
	}
	Map<String,String> extraMap = null;
	public void putParam(String key,String value){
		if(extraMap == null){
			extraMap = new HashMap<>();
		}
		extraMap.put(key,value);
	}
	public String getParam(String key){
		if(extraMap == null || extraMap.size() == 0){
			return null;
		}
		return extraMap.get(key);
	}
}
