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
public class TestSupportV7 extends SuperActivity {

    @InjectView(value = R.id.act_v7_rv)
    RecyclerView mRecyclerView;

    TestAdapter mAdapter;

    public static Intent createIntent(Context ctx){
        Intent it = new Intent(ctx,TestSupportV7.class);
        return it;
    }

    @Override
    protected ActionBarMenu onActionBarCreate() {
        return new ActionBarMenu("support-v7测试");
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_v7;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new TestAdapter(new String[]{"A","B","C","D"});
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    public class TestAdapter extends RecyclerView.Adapter<ViewHolder> {
        private String[] mDataset;
        public TestAdapter(String[] myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindText(mDataset[position]);
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
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

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }

        }


        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                //android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }
    }
}
