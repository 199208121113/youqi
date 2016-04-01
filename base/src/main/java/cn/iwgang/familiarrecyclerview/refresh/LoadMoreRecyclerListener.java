package cn.iwgang.familiarrecyclerview.refresh;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import cn.iwgang.familiarrecyclerview.FamiliarRecyclerView;


/**
 * Created by Syehunter on 2015/11/21.
 */
public class LoadMoreRecyclerListener extends RecyclerView.OnScrollListener {

    private Context mContext;

    public int firstVisibleItemPosition;
    private int lastVisibleItemPosition;

    private int[] mPositions;
    private int mScrollState;
    private OnLoadMoreListener mOnLoadMoreListener;
    private RefreshFooterLayout mFooterLoadingLayout;

    /**
     * 是否是正则加载状态
     */
    private boolean isLoading = false;
    /**
     * 通过滚动方向判断是否允许上拉加载
     */
    public boolean isLoadingMoreEnabled = true;
    /**
     * 加载更多之前RecyclerView的item数量
     */
    private int mOldItemCount;

    private boolean hasCompleted = false;

    public LoadMoreRecyclerListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        hasCompleted = false;

        isLoadingMoreEnabled = dy > 0;

        RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();

        //初始化firstVisibleItemPosition和lastVisibleItemPosition
        if (null != mLayoutManager) {
            if (mLayoutManager instanceof LinearLayoutManager) {
                firstVisibleItemPosition = ((LinearLayoutManager) mLayoutManager)
                        .findFirstVisibleItemPosition();
                lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager)
                        .findLastVisibleItemPosition();
            } else if (mLayoutManager instanceof GridLayoutManager) {
                firstVisibleItemPosition = ((GridLayoutManager) mLayoutManager)
                        .findFirstVisibleItemPosition();
                lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager)
                        .findLastVisibleItemPosition();
            } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager mStaggeredGridLayoutManager =
                        (StaggeredGridLayoutManager) mLayoutManager;
                if (null == mPositions) {
                    mPositions = new int[mStaggeredGridLayoutManager.getSpanCount()];
                }
                mStaggeredGridLayoutManager.findFirstVisibleItemPositions(mPositions);
                mStaggeredGridLayoutManager.findLastVisibleItemPositions(mPositions);
                firstVisibleItemPosition = getFirst(mPositions);
                lastVisibleItemPosition = getLast(mPositions);
            } else {
                throw new IllegalArgumentException(
                        "The layoutManager must be one of LinearLayoutManager, " +
                                "GridLayoutManager or StaggeredGridLayoutManager");
            }
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView rv, int newState) {
        super.onScrollStateChanged(rv, newState);
        FamiliarRecyclerView recyclerView = (FamiliarRecyclerView)rv;

        mScrollState = newState;
        RecyclerView.LayoutManager mLayoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();

        if ((visibleItemCount > 0
                && mScrollState == RecyclerView.SCROLL_STATE_IDLE
                && lastVisibleItemPosition >= totalItemCount - 1)
                && isLoadingMoreEnabled) {

            if (isLoading) {
                return;
            }

            if (hasCompleted) {
                hasCompleted = !hasCompleted;
                return;
            }

            if (null != mOnLoadMoreListener) {
                addFooterLoadingLayout(recyclerView);
                mOnLoadMoreListener.onLoadMore();
                return;
            }

        }
    }

    /**
     * 添加LoadMore布局
     */
    private void addFooterLoadingLayout(FamiliarRecyclerView recyclerView) {
        isLoading = true;
        if (null == mFooterLoadingLayout) {
            mFooterLoadingLayout = new RefreshFooterLayout(mContext);
        }
        recyclerView.addFooterView(mFooterLoadingLayout);
        mOldItemCount = recyclerView.getAdapter().getItemCount();
        if(mOldItemCount > 0) {
            recyclerView.smoothScrollToPosition(mOldItemCount - 1);
        }
        mFooterLoadingLayout.requestLayout();
        mFooterLoadingLayout.setVisibility(View.VISIBLE);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    /**
     * StaggeredGridLayoutManager firstVisibleItemPosition
     *
     * @param mPositions
     * @return
     */
    private int getFirst(int[] mPositions) {
        int first = mPositions[0];
        for (int value : mPositions) {
            if (value < first) {
                first = value;
            }
        }
        return first;
    }

    /**
     * StaggeredGridLayoutManager lastVisibleItemPosition
     *
     * @param mPositions
     * @return
     */
    private int getLast(int[] mPositions) {
        int last = mPositions[0];
        for (int value : mPositions) {
            if (value > last) {
                last = value;
            }
        }
        return last;
    }

    public void setRefreshComplete(FamiliarRecyclerView frv){
        if (null != frv && frv.getFooterViewsCount() > 0){
            isLoading = false;
            hasCompleted = true;
            frv.removeFooterView(mFooterLoadingLayout);
        }
    }

}
