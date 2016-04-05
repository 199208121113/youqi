package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lg.base.bus.EventBus;
import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.InjectView;
import com.lg.base.ui.recycle.RecyclerViewAdapter;
import com.lg.base.ui.recycle.RecyclerViewHolder;
import com.lg.base.utils.ScreenUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;

import cn.iwgang.familiarrecyclerview.FamiliarRecyclerView;
import cn.iwgang.familiarrecyclerview.refresh.OnLoadMoreListener;
import cn.iwgang.familiarrecyclerview.refresh.RefreshHeaderLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by liguo on 2015/11/25.
 */
public class TestRecyclerViewActivity extends SuperActivity implements FamiliarRecyclerView.OnItemClickListener,FamiliarRecyclerView.OnItemLongClickListener{

    @InjectView(value = R.id.act_v7_rv)
    FamiliarRecyclerView mRecyclerView;

    @InjectView(R.id.act_v7_ptr_frame_layout)
    PtrFrameLayout mPtrFramelayout;

    TestAdapter mAdapter;

    public static Intent createIntent(Context ctx){
        return new Intent(ctx,TestRecyclerViewActivity.class);
    }

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("RecyclerView测试");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        //(1)添加headerView
        int headerViewHeight = ScreenUtil.dip2px(this, 100);
        View headerView = layoutInflater.inflate(R.layout.item_recycler_header,null);
        headerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,headerViewHeight));
        mRecyclerView.addHeaderView(headerView);

        //(2)添加footerView
        View footerView = layoutInflater.inflate(R.layout.item_recycler_footer, null);
        footerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, headerViewHeight));
        mRecyclerView.addFooterView(footerView);

        //(3)设置分割线(也可以在布局文件中直接指定分割线Divider及分割线大小，当然你也可以使用自己的分割线实现),如果是网格或瀑布流视图，你甚至可以设置横竖不同的分割线Divider及分割线大小
        //mRecyclerView.setDivider();

        //(4) 设置数据空View（设置isRetainShowHeadOrFoot为true时，可以让显示EmptyView时不会清除掉添加的HeadView和FooterView）
        View emptyView =layoutInflater.inflate(R.layout.layout_empty_view, null);
        int width = ScreenUtil.getDisplay(this).getWidth();
        int height = ScreenUtil.dip2px(this,40);
        emptyView.setLayoutParams(new LinearLayout.LayoutParams(width,height));
        mRecyclerView.setEmptyView(emptyView,true);

        //(5)Item单击事件
        mRecyclerView.setOnItemClickListener(this);

        //(6)Item长按事件
        mRecyclerView.setOnItemLongClickListener(this);

        // 设置滚动到顶部或底部时的事件回调
        /*mRecyclerView.setOnScrollListener(new FamiliarRecyclerViewOnScrollListener(mRecyclerView.getLayoutManager()) {
            @Override
            public void onScrolledToTop() {
                ToastUtil.show(getApplication(),"Top");
            }

            @Override
            public void onScrolledToBottom() {
                ToastUtil.show(getApplication(),"Bottom");
            }
        });*/

        //(7)============下拉刷新,上拉加载更多=============
        mPtrFramelayout.setResistance(1.7f);
        mPtrFramelayout.setRatioOfHeaderHeightToRefresh(1.2f);
        mPtrFramelayout.setDurationToClose(200);
        mPtrFramelayout.setDurationToCloseHeader(1000);
        mPtrFramelayout.setPullToRefresh(false);
        mPtrFramelayout.disableWhenHorizontalMove(true);  //ViewPager滑动冲突
        mPtrFramelayout.setKeepHeaderWhenRefresh(true);

        RefreshHeaderLayout rh = new RefreshHeaderLayout(this);
        mPtrFramelayout.setHeaderView(rh); //设置刷新的View
        mPtrFramelayout.addPtrUIHandler(rh);

        //(8)设置下拉刷新的事件
        mPtrFramelayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                EventBus.get().sendEmptyMessageDelayed(getLocation(),1,3000);
            }
        });

        //(9)设置加载更多的事件
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                EventBus.get().sendEmptyMessageDelayed(getLocation(),2,3000);
            }
        });

        //(10)初始化Adapter的数据
        mAdapter = new TestAdapter(this);
        addDataToAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void executeMessage(Message msg) {
        super.executeMessage(msg);
        if(msg.what == 1){
            mPtrFramelayout.refreshComplete();
        }else if(msg.what == 2){
            addDataToAdapter();
            mRecyclerView.refreshBootomComplete();
        }
    }

    @Override
    public void onItemClick(FamiliarRecyclerView familiarRecyclerView, View view, int position) {
        String text = mAdapter.getItem(position).getData().getText();
        ToastUtil.show(this, "onItemClick(),text=" + text);
    }

    @Override
    public boolean onItemLongClick(FamiliarRecyclerView familiarRecyclerView, View view, int position) {
        String text = mAdapter.getItem(position).getData().getText();
        ToastUtil.show(this,"onItemLongClick(),text="+text);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAdapter != null) {
            mAdapter.destroy();
        }
    }

    private void addDataToAdapter(){
        int startIndex = mAdapter.getItemCount();
        int endIndex = startIndex+5;
        for (;startIndex < endIndex;startIndex++){
            mAdapter.addItem(new TestBean(""+(startIndex+1)),null);
        }
        mAdapter.notifyDataSetChanged();
    }

    public static class TestAdapter extends RecyclerViewAdapter<TestBean,Void,TestRecyclerViewHolder> {
        public TestAdapter(Context ctx) {
            super(ctx);
        }

        @Override
        public TestRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View vv = inflater.inflate(R.layout.item_recycler_view,parent,false);
            TestRecyclerViewHolder vh = new TestRecyclerViewHolder(vv,getCtx());
            vh.initViews();
            return vh;
        }

        @Override
        public void onBindViewHolder(TestRecyclerViewHolder holder, int position) {
            holder.setItem(getItem(position));
        }
    }

    public static class TestRecyclerViewHolder extends RecyclerViewHolder<TestBean,Void> {
        TextView tv;

        public TestRecyclerViewHolder(View rootView, Context ctx) {
            super(rootView, ctx);
        }

        @Override
        protected void onInitViews(View view) {
            tv = (TextView)view.findViewById(R.id.item_recycler_view_tv);
        }

        @Override
        protected void onBindItem() {
            tv.setText(getItem().getData().getText());
        }


        @Override
        protected void onRecycleItem() {

        }

        @Override
        protected void onRefreshView() {

        }

        @Override
        protected void onDestroy() {

        }
    }

    public static class TestBean{
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }


        public TestBean(String text) {
            this.text = text;
        }
    }
}
