package com.lg.test.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lg.base.core.ActionBarMenu;
import com.lg.base.core.InjectView;
import com.lg.base.utils.ImageUtil;
import com.lg.base.utils.ScreenUtil;
import com.lg.test.R;
import com.lg.test.core.SuperActivity;

/**
 * Created by liguo on 2015/11/25.
 */
public class TestRecyclerViewActivity extends SuperActivity {

    @InjectView(value = R.id.act_v7_rv)
    RecyclerView mRecyclerView;

    TestAdapter mAdapter;

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,TestRecyclerViewActivity.class);
        return it;
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

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TestAdapter(new String[]{"A","B","C","D"});
        mRecyclerView.setAdapter(mAdapter);
    }

    public class TestAdapter extends RecyclerView.Adapter<ViewHolder> {
        private String[] mDataList;
        public TestAdapter(String[] myDataset) {
            mDataList = myDataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindText(mDataList[position]);
        }

        @Override
        public int getItemCount() {
            return mDataList.length;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        TextView tv;
        public ViewHolder(View v) {
            super(v);
            this.view = v;
            tv = (TextView)v.findViewById(R.id.item_recycler_view_tv);
            Display display = ScreenUtil.getDisplay(view.getContext());
            ImageUtil.setLayoutParamsByPX(v, display.getWidth(), display.getWidth() / 5);
        }
        public void bindText(String text){
            tv.setText(text);
        }
    }
}
