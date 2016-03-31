package com.lg.base.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.lg.base.ui.holder.BaseViewHolder;
import com.lg.base.ui.holder.IViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseExpandAdapter<G extends BaseExpandGroupData<C, CS>, GS, C, CS> extends BaseExpandableListAdapter {

	private final List<AdapterItem<G, GS>> mData = new ArrayList<AdapterItem<G, GS>>();
	final Context context;
	private final LayoutInflater mInflater;
	private final ConcurrentHashMap<String, IViewHolder<G, GS>> viewMap = new ConcurrentHashMap<String, IViewHolder<G, GS>>();
	private final ConcurrentHashMap<String, IViewHolder<C, CS>> viewMapChild = new ConcurrentHashMap<String, IViewHolder<C, CS>>();
	private int groupLayoutResId = -1;
	private int childLayoutResId = -1;

	public BaseExpandAdapter(Context context) {
		super();
		this.context = context;
		mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		onInitGroupViewType();
		onInitChildViewType();
	}

	@Override
	public int getGroupCount() {
		return mData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<AdapterItem<C, CS>> c = mData.get(groupPosition).getData().getChildItemList();
		return c == null ? 0 : c.size();
	}

	@Override
	public AdapterItem<G, GS> getGroup(int groupPosition) {
		return mData.get(groupPosition);
	}

	@Override
	public AdapterItem<C, CS> getChild(int groupPosition, int childPosition) {
		List<AdapterItem<C, CS>> c = mData.get(groupPosition).getData().getChildItemList();
		return c != null ? c.get(childPosition) : null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return (groupPosition + 1L) * childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		BaseViewHolder<G, GS> holder = null;
		if (convertView == null) {
			convertView = inflateView(null, parent, this.groupLayoutResId);
			holder = createGroupViewHolder(convertView);
			convertView.setTag(holder);
			viewMap.put(holder.toString(), holder);
		} else {
			holder = (BaseViewHolder<G, GS>) convertView.getTag();
		}
		holder.setPosGroupIndex(groupPosition);
		AdapterItem<G, GS> item = getGroup(groupPosition);
		holder.setItem(item);
		return convertView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		BaseViewHolder<C, CS> holder = null;
		if (convertView == null) {
			convertView = inflateView(null, parent, this.childLayoutResId);
			holder = createChildViewHolder(convertView);
			convertView.setTag(holder);
			viewMapChild.put(holder.toString(), holder);
		} else {
			holder = (BaseViewHolder<C, CS>) convertView.getTag();
		}
		holder.setPosIndex(childPosition);
		holder.setPosGroupIndex(groupPosition);
		AdapterItem<C, CS> item = getChild(groupPosition, childPosition);
		holder.setItem(item);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	//=========================================================================

	protected void addGroupViewType(int layout) {
		this.groupLayoutResId = layout;
	}

	protected void addChildViewType(int layout) {
		this.childLayoutResId = layout;
	}

	private BaseViewHolder<G, GS> createGroupViewHolder(View view) {
		BaseViewHolder<G, GS> holder = onCreateGroupViewHolder(view, this.context);
		holder.initViews();
		return holder;
	}

	private BaseViewHolder<C, CS> createChildViewHolder(View view) {
		BaseViewHolder<C, CS> holder = onCreateChildViewHolder(view, this.context);
		holder.initViews();
		return holder;
	}

	private View inflateView(View convertView, ViewGroup parent, int layoutId) {
		View v = convertView;
		if (convertView == null) {
			v = mInflater.inflate(layoutId, parent, false);
		}
		return v;
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
		for (IViewHolder<G, GS> h : viewMap.values()) {
			h.destroy();
		}
		viewMap.clear();
		
		for (IViewHolder<C, CS> h : viewMapChild.values()) {
			h.destroy();
		}
		viewMapChild.clear();
	}
	
	//==========================================================================
	public AdapterItem<G, GS> addItemGroup(G g,GS gs){
		if(g == null)
			return null;
		AdapterItem<G, GS> item = new AdapterItem<G, GS>(g, gs);
		mData.add(item);
		return item;
	}
	
	public AdapterItem<G, GS> addItemGroup(G g,GS gs,OnAdapterItemStateChangeListener<G, GS> listener){
		AdapterItem<G, GS> item = addItemGroup(g,gs);
		if(item != null)
			item.setOnAdapterItemStateChangeListener(listener);
		return item;
	}
	
	public AdapterItem<C, CS> addItemChild(G g,C c,CS cs){
		if(g == null)
			return null;
		if(c == null)
			return null;
		AdapterItem<C, CS> item = new AdapterItem<C, CS>(c,cs);
		g.getChildItemList().add(item);
		return item;
	}
	
	public void clear(){
		mData.clear();
		viewMap.clear();
		viewMapChild.clear();
	}
	

	protected abstract void onInitGroupViewType();

	protected abstract void onInitChildViewType();

	protected abstract BaseViewHolder<G, GS> onCreateGroupViewHolder(View view, Context act);

	protected abstract BaseViewHolder<C, CS> onCreateChildViewHolder(View view, Context act);

	protected abstract void onDestory();

}
