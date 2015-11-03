package com.lg.base.ui.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lg.base.R;
import com.lg.base.ui.holder.IOnScrollListener;
import com.lg.base.utils.DateUtil;

import java.util.List;

public class PullToRefreshListView extends ListView {

	@SuppressWarnings("unused")
	private static final String TAG = PullToRefreshListView.class.getSimpleName();

	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	private final static int RATIO = 3;

	private LayoutInflater inflater;
	private LinearLayout headView;
	private LinearLayout footerView;

	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	private boolean isRecored;
	private int headContentHeight;
	private int bottomContentHeight;
	private int startY;
	private int firstItemIndex;
	private int state;
	private boolean isBack;
	private OnRefreshListener refreshListener;
	private boolean isRefreshable = false;

	public PullToRefreshListView(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setCacheColorHint(context.getResources().getColor(R.color.transparent));
		inflater = LayoutInflater.from(context);
		headView = (LinearLayout) inflater.inflate(R.layout.refresh_head, null);
		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		addHeaderView(headView, null, false);
		// setOnScrollListener(this);

		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		state = DONE;

		footerView = (LinearLayout) inflater.inflate(R.layout.refresh_footer, null);
		measureView(footerView);
		bottomContentHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, 0, 0, -1 * bottomContentHeight);
		footerView.invalidate();
		addFooterView(footerView, null, false);
	}

	private int lastItemCount = 0;

	public boolean onTouchEvent(MotionEvent event) {
		if (!isRefreshable)
			return super.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstItemIndex == 0 && !isRecored) {
				isRecored = true;
				startY = (int) event.getY();
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
			}
			isRecored = false;
			isBack = false;
			break;
		case MotionEvent.ACTION_MOVE:
			int tempY = (int) event.getY();
			if (!isRecored && firstItemIndex == 0) {
				isRecored = true;
				startY = tempY;
			}
			if (state != REFRESHING && isRecored && state != LOADING) {
				if (state == RELEASE_To_REFRESH) {
					setSelection(0);
					if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
					} else if (tempY - startY <= 0) {
						state = DONE;
						changeHeaderViewByState();
					}
				}
				if (state == PULL_To_REFRESH) {
					setSelection(0);
					if ((tempY - startY) / RATIO >= headContentHeight) {
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
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			tipsTextview.setText(TEXT_RELEASE_To_REFRESH);
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
			}
			tipsTextview.setText(TEXT_PULL_TO_REFRESH);
			break;
		case REFRESHING:
			headView.setPadding(0, 0, 0, 0);
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText(TEXT_REFRESHING);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);
			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.ic_arrow_refresh);
			tipsTextview.setText(TEXT_PULL_TO_REFRESH);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			break;
		}
	}

	private final OnScrollListener mOnScrollListener = new OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisiableItem, int visiableItemCount, int totalItemCount) {
			firstItemIndex = firstVisiableItem;
			lastItemCount = firstVisiableItem + visiableItemCount;// - 1;
			if(mStateChangeListenerList != null && mStateChangeListenerList.size() > 0){
				for (IOnScrollListener listener : mStateChangeListenerList) {
					listener.onScroll(view, firstVisiableItem, visiableItemCount, totalItemCount);
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(mStateChangeListenerList != null && mStateChangeListenerList.size() > 0){
				for (IOnScrollListener listener : mStateChangeListenerList) {
					listener.onScrollStateChanged(view, scrollState);
				}
			}
			if (lastItemCount == getAdapter().getCount() && scrollState == SCROLL_STATE_IDLE){
				onBottomRefresh();
			}
		}
	};

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		setOnScrollListener(mOnScrollListener);
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onTopRefresh();

		public boolean onBottomRefresh();
	}

	public void setTopRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText(TEXT_LAST_UPDATE + DateUtil.formatDate(System.currentTimeMillis()));
		changeHeaderViewByState();
	}

	public void setBottomRefreshComplete() {
		changeBottomViewByState(DONE);
	}

	private void onTopRefresh() {
		if (refreshListener == null)
			return;
		refreshListener.onTopRefresh();
	}

	private void onBottomRefresh() {
		if (refreshListener == null)
			return;
		if (refreshListener.onBottomRefresh()) {
			changeBottomViewByState(REFRESHING);
		}
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
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		lastUpdatedTextView.setText(TEXT_LAST_UPDATE + DateUtil.formatDate(System.currentTimeMillis()));
		super.setAdapter(adapter);
	}

	private void changeBottomViewByState(int status) {
		if (status == REFRESHING) {
			footerView.setPadding(0, 0, 0, 0);
		} else if (status == DONE) {
			footerView.setPadding(0, 0, 0, -1 * bottomContentHeight);
		}
	}

	private static final String TEXT_REFRESHING = "正在刷新...";
	private static final String TEXT_PULL_TO_REFRESH = "下拉刷新";
	private static final String TEXT_RELEASE_To_REFRESH = "松开刷新";
	private static final String TEXT_LAST_UPDATE = "最近更新：";

	// ======================滑动状态改变Listener=====================
	private List<IOnScrollListener> mStateChangeListenerList = null;
	public void setOnScrollStateChangedListenerList(List<IOnScrollListener> listeners) {
		mStateChangeListenerList = listeners;
	}
}
