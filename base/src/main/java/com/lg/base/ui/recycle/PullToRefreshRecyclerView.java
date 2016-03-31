package com.lg.base.ui.recycle;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import com.lg.base.utils.ScreenUtil;


/**
 * Created by root on 16-1-29.
 */
public class PullToRefreshRecyclerView extends RecyclerView {
    @SuppressWarnings("unused")
    private static final String TAG = PullToRefreshRecyclerView.class.getSimpleName();

    public final static int RELEASE_To_REFRESH = 0;
    public final static int PULL_To_REFRESH = 1;
    public final static int REFRESHING = 2;
    public final static int DONE = 3;
    public final static int LOADING = 4;
    public final static int RATIO = 3;

    private LayoutInflater inflater;
    private LinearLayout headView;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    private boolean isRecored;
    private int headContentHeight;
    private int bottomPadding;
    private int startY;
    private int firstItemIndex;
    private int lastItemIndex = 0;
    private int state;
    private boolean isBack;
    private OnRefreshListener refreshListener;
    private boolean isRefreshable = false;

    public PullToRefreshRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void setHeaderView(LinearLayout headView){
//        View childView = headView.findViewById(childViewOfHeaderView);
//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)childView.getLayoutParams();
//        Display display = ScreenUtil.getDisplay(headView.getContext());
//        int bWidth = 640;
//        int bHeight = 260;
//        int newWidth = Math.round(display.getWidth() * 0.5f);
//        int newHeight = Math.round(bHeight * newWidth * 1f / bWidth);
//        if(lp == null){
//            lp = new LinearLayout.LayoutParams(newWidth,newHeight);
//        }else{
//            lp.width = newWidth;
//            lp.height = newHeight;
//        }
//        childView.setLayoutParams(lp);

        measureView(headView);
        bottomPadding = ScreenUtil.dip2px(headView.getContext(), 10);
        headContentHeight = headView.getMeasuredHeight();
        headView.setPadding(0, -1 * headContentHeight + bottomPadding, 0, 0);
        headView.invalidate();
    }
    private void init(Context context) {
        inflater = LayoutInflater.from(context);

        animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);
        state = DONE;
    }

    public View getHeaderView(){
        return this.headView;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isRefreshable || headView == null)
            return super.onTouchEvent(event);
        GridLayoutManager layoutManager = ((GridLayoutManager)this.getLayoutManager());
        firstItemIndex = layoutManager.findFirstVisibleItemPosition();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (firstItemIndex <= 1 && !isRecored) {
                    isRecored = true;
                    startY = (int) event.getY();
//                    LogUtil.e(TAG,"ACTION_DOWN,startY="+startY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (state != REFRESHING && state != LOADING) {
                    if (state == PULL_To_REFRESH) {
                        state = DONE;
                        changeHeaderViewByState();
                    }
                    if (state == RELEASE_To_REFRESH) {
                        state = REFRESHING;
                        changeHeaderViewByState();
                        onTopRefresh();
                    }
                } else if (state == REFRESHING) {
                    int tempY = (int) event.getY();
                    if ((tempY - startY) > 0) {
                        state = REFRESHING;
                    } else {
                        state = DONE;
                    }

                    changeHeaderViewByState();
                }
                lastItemIndex = firstItemIndex;
//                LogUtil.e(TAG, "ACTION_UP,startY=" + startY+",tmpY="+event.getY());
                isRecored = false;
                isBack = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int tempY = (int) event.getY();
                if (!isRecored && firstItemIndex <= 1) {
                    isRecored = true;
                    startY = tempY;
                }
                if (state != REFRESHING && isRecored && state != LOADING) {
                    if (state == RELEASE_To_REFRESH) {
//                        scrollToPosition(0);
                        if (((tempY - startY) / RATIO < headContentHeight/NEW_SIZE) && (tempY - startY) > 0) {
                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();
                        } else if (tempY - startY <= 0) {
                            state = DONE;
                            changeHeaderViewByState();
                        }
                    }
                    if (state == PULL_To_REFRESH) {
//                        scrollToPosition(0);
                        if ((tempY - startY) / RATIO >= headContentHeight/NEW_SIZE) {
                            state = RELEASE_To_REFRESH;
                            isBack = true;
                            changeHeaderViewByState();
                        } else if (tempY - startY <= 0) {
                            state = DONE;
                            changeHeaderViewByState();
                        }
                    }
                    if (state == DONE) {
                        if (tempY - startY > 0) {
                            state = PULL_To_REFRESH;
                            changeHeaderViewByState();
                        }
                    }
                    if (state == PULL_To_REFRESH) {
                        headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);
                    }
                    if (state == RELEASE_To_REFRESH) {
                        headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
                    }
                }else if(state == REFRESHING){
                    headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight+headContentHeight, 0, 0);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
    private final int NEW_SIZE = 2;
    private void changeHeaderViewByState() {
//        LogUtil.e(TAG,"st="+getStateStr(state));
        switch (state) {
            case RELEASE_To_REFRESH:
                break;
            case PULL_To_REFRESH:
                if (isBack) {
                    isBack = false;
                }
                break;
            case REFRESHING:
                headView.setPadding(0, 0, 0, 0);
                scrollToPosition(0);
                break;
            case DONE:
                headView.setPadding(0, -1 * headContentHeight + bottomPadding, 0, 0);
                scrollToPosition(1);
                break;
            default:
                break;
        }
    }

    private String getStateStr(int st){
        if(st == DONE){
            return "DONE";
        }
        if(st == RELEASE_To_REFRESH){
            return "RELEASE_To_REFRESH";
        }
        if(st == PULL_To_REFRESH){
            return "PULL_To_REFRESH";
        }
        if(st == REFRESHING){
            return "REFRESHING";
        }
        return "UNKNOWN";


    }


    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
        isRefreshable = refreshListener != null;
    }

    public interface OnRefreshListener {
        void onTopRefresh();
    }

    public void setTopRefreshComplete() {
        state = DONE;
        changeHeaderViewByState();
    }


    private void onTopRefresh() {
        if (refreshListener == null)
            return;
        refreshListener.onTopRefresh();
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, View.MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void showHeaderView(){
        state = REFRESHING;
        changeHeaderViewByState();
    }

    public void setAdapter(RecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
    }

}
