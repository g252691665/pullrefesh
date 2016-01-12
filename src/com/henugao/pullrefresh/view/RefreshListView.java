package com.henugao.pullrefresh.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.henugao.pullrefresh.R;
import android.widget.AbsListView.OnScrollListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView implements OnScrollListener {
	
	
	private View headerView;  //头部的刷新视图	
	private View footerView;  //底部加载更多的视图
	private ImageView ivArrow;
	private ProgressBar pbRoate;
	private TextView tvState;
	private TextView tvDate;
	private RotateAnimation upAnimation,downAnimation; //向上旋转和向下旋转动画
	
	private int headerHeight; //头部的高度
	private int footerHeight; //底部的高度
	private int downY; // 
	
	
	private final int PULL_REFRESH = 0;   //下拉刷新
	private final int RELEASE_RAEFRESH = 1; //松开刷新
	private final int REFRESHING = 2; //正在刷新
	private int currentLevel = PULL_REFRESH; // 当前的状态
	
	private boolean isLodingMore = false;  //判断是否正在加载更多数据

	public RefreshListView(Context context) {
		super(context);
		init();
	}



	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOnScrollListener(this);
		initHeaderView();
		initFooterView();
		initRotateAnimation();
		
	}
	
	/**
	 * 初始化向上旋转和向下旋转的动画
	 */
	private void initRotateAnimation() {
		upAnimation = new RotateAnimation(0, -180, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setFillAfter(true);
		upAnimation.setDuration(300);
		downAnimation = new RotateAnimation(-180, -360, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setFillAfter(true);
		downAnimation.setDuration(300);
	}



	/**
	 * 初始化头部的刷新布局
	 */
	public void initHeaderView(){
		headerView = View.inflate(getContext(), R.layout.layout_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
		pbRoate = (ProgressBar) headerView.findViewById(R.id.pb_roate);
		tvState = (TextView) headerView.findViewById(R.id.tv_state);
		tvDate = (TextView) headerView.findViewById(R.id.tv_date);
		headerView.measure(0, 0);//主动通知系统去测量
		headerHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerHeight, 0, 0); //通过设置padding隐藏headerview
		//必须在setAdapter之前调用
		addHeaderView(headerView);
	}
	
	/**
	 * 初始化底部的加载更多的布局
	 */
	public void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.layout_footer, null);
		footerView.measure(0, 0);//主动通知系统去测量
		footerHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerHeight, 0, 0); //通过设置padding隐藏headerview
		//必须在setAdapter之前调用
		addFooterView(footerView);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			//判断当前是否正处于刷新状态，是的话就不能再下拉刷新了
			if (currentLevel == REFRESHING) {
				break;
			}
			int deltaY = (int) (ev.getY() - downY);
			int pendingTop = -headerHeight + deltaY;
			if (pendingTop > -headerHeight && getFirstVisiblePosition() == 0) {
				headerView.setPadding(0, pendingTop, 0, 0);
				if (pendingTop >= 0 && currentLevel == PULL_REFRESH) {
					currentLevel = RELEASE_RAEFRESH;
					refreshHeader();
				} else if(pendingTop < 0 && currentLevel == RELEASE_RAEFRESH) {
					currentLevel = PULL_REFRESH;
					refreshHeader();
				}
				return true; //拦截TouchMove,不让listview处理该次move事件，会造成listview无法滑动
			}
			
			break;
		case MotionEvent.ACTION_UP:
			//有两种情况
			if (currentLevel == PULL_REFRESH) {
				headerView.setPadding(0, -headerHeight, 0, 0);
			}else if (currentLevel == RELEASE_RAEFRESH) {
				headerView.setPadding(0, 0, 0, 0);
				currentLevel = REFRESHING;
				refreshHeader();
				if (listener != null) {
					listener.onPullRefresh();
				}
				
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 根据当前的状态刷新headerView
	 */
	public void refreshHeader() {
		switch (currentLevel) {
		case PULL_REFRESH:
			tvState.setText("下拉刷新");
			ivArrow.setAnimation(downAnimation);
			break;
		case RELEASE_RAEFRESH:
			tvState.setText("松开刷新");
			ivArrow.setAnimation(upAnimation);
			break;
		case REFRESHING:
			tvState.setText("正在刷新...");
			ivArrow.clearAnimation(); //因为向上的旋转动画可能没有执行完
			ivArrow.setVisibility(View.INVISIBLE);
			pbRoate.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 完成刷新后调用，重置状态,在你获取完数据更新完adapter之后，在UI线程中调用该方法
	 */
	public void completeRefresh() {
		if (isLodingMore) {
			//重置加载更多视图为不可见
			footerView.setPadding(0, -footerHeight, 0, 0);
			isLodingMore = false;
		}else {
			tvState.setText("下拉刷新");
			currentLevel = PULL_REFRESH;
			tvDate.setText("最后刷新：" + getCurrentTime());
			pbRoate.setVisibility(View.INVISIBLE);
			ivArrow.setVisibility(View.VISIBLE);
			headerView.setPadding(0, -headerHeight, 0, 0);
		}

	}
	
	/**
	 * 获取当前的时间
	 * @return
	 */
	private String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String currenTime = sdf.format(new Date());
		return currenTime;
	}
	
	RefreshListener listener;

	public void setOnRefreshListener(RefreshListener listener){
		this.listener = listener;
	}
	public interface RefreshListener {
		void onPullRefresh();
		void onLoadMore();
	}
	/**scroolState的三种可能取值
	 * SCROLL_STATE_IDLE //表示手指松开
	 * SCROLL_STATE_TOUCH_SCROLL //表示手指按住滚动
	 * SCROLL_STATE_FLING //表示惯性滚动
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
				getLastVisiblePosition() == (getCount() - 1) &&
				!isLodingMore) {
			isLodingMore = true;
			footerView.setPadding(0, 0, 0, 0); //设置加载更多视图可见
			setSelection(getCount());
			if (listener != null) {
				listener.onLoadMore();
			}
		}
	}



	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}
}
