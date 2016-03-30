package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.InjectView;
import com.lg.base.ui.recycle.ItemDecorationByGridLayout;
import com.lg.base.ui.recycle.ItemDecorationByLinearLayout;
import com.lg.base.ui.recycle.PullToRefreshRecyclerView;
import com.lg.base.ui.recycle.RecyclerItemClickListener;
import com.lg.base.ui.recycle.RecyclerViewAdapter;
import com.lg.base.ui.recycle.RecyclerViewHolder;
import com.lg.base.utils.ImageUtil;
import com.lg.base.utils.ScreenUtil;
import com.lg.base.utils.ToastUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;

import java.util.Random;

/**
 * Created by liguo on 2015/11/25.
 */
public class TestRecyclerViewActivity extends SuperActivity implements RecyclerItemClickListener {

    @InjectView(value = R.id.act_v7_rv)
    PullToRefreshRecyclerView mRecyclerView;

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

        int a = 3;
        //(1)设置LayoutManager
        RecyclerView.LayoutManager layoutManager = null;
        if(a == 1) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager = linearLayoutManager;
        }else if(a == 2){
            layoutManager = new GridLayoutManager(this,3);
        }else if(a == 3){
            layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        }
        mRecyclerView.setLayoutManager(layoutManager);

        //(2)设置分隔线,可在AppTheme中设置 <item name="android:listDivider">@drawable/sh_recycler_linear_hor</item>
        RecyclerView.ItemDecoration itemDecoration = null;
        if(a == 1) {
            itemDecoration = new ItemDecorationByLinearLayout(this, LinearLayoutManager.VERTICAL);
        }else if(a == 2 || a== 3){
            itemDecoration = new ItemDecorationByGridLayout(this);
        }
        mRecyclerView.addItemDecoration(itemDecoration);

        //(4)初始化Adapter的数据
        mAdapter = new TestAdapter(this);
        mAdapter.setItemClickListener(this);
        int dp10 = ScreenUtil.dip2px(this,10);
        for (int i = 1; i< 100;i++){
            int h = new Random().nextInt(10)+3;
            if(h < 3){
                h = 3;
            }
            h = h * dp10;
            mAdapter.addItem(new TestBean(""+i,h),null);
        }
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAdapter != null) {
            mAdapter.destroy();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        ToastUtil.show(this,"position="+position);
    }

    public static class TestAdapter extends RecyclerViewAdapter<TestBean,Void,TestRecyclerViewHolder> {
        public TestAdapter(Context ctx) {
            super(ctx);
        }

        @Override
        protected TestRecyclerViewHolder startCreateViewHolder(ViewGroup parent, int viewType) {
            TestRecyclerViewHolder tvh;
            if(viewType == 0){
                tvh = new TestRecyclerViewHolder(getFirstHeaderView(),getCtx());
                tvh.setNothingTodo(true);
            }else{
                View vv = inflater.inflate(R.layout.item_recycler_view,parent,false);
                tvh = new TestRecyclerViewHolder(vv,getCtx());
            }
            return tvh;
        }

        @Override
        public int getItemViewType(int position) {
            return isHeader(position) ? 0 : 1;
        }

        @Override
        protected void startBindViewHolder(TestRecyclerViewHolder holder, int position) {
            if(isHeader(position)){
                return;
            }
            int dataPos = position;
            if(getHeaderViewCount() > 0) {
                dataPos -= getHeaderViewCount();
            }
            holder.setItem(getItem(dataPos));
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
            ImageUtil.setLayoutParamsByPX(getRootView(), getRootView().getWidth(),getItem().getData().getHeight());
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
        private int height;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public TestBean(String text, int height) {
            this.text = text;
            this.height = height;
        }
    }
}
