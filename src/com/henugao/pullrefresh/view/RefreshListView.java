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
	
	
	private View headerView;  //ͷ����ˢ����ͼ	
	private View footerView;  //�ײ����ظ������ͼ
	private ImageView ivArrow;
	private ProgressBar pbRoate;
	private TextView tvState;
	private TextView tvDate;
	private RotateAnimation upAnimation,downAnimation; //������ת��������ת����
	
	private int headerHeight; //ͷ���ĸ߶�
	private int footerHeight; //�ײ��ĸ߶�
	private int downY; // 
	
	
	private final int PULL_REFRESH = 0;   //����ˢ��
	private final int RELEASE_RAEFRESH = 1; //�ɿ�ˢ��
	private final int REFRESHING = 2; //����ˢ��
	private int currentLevel = PULL_REFRESH; // ��ǰ��״̬
	
	private boolean isLodingMore = false;  //�ж��Ƿ����ڼ��ظ�������

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
	 * ��ʼ��������ת��������ת�Ķ���
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
	 * ��ʼ��ͷ����ˢ�²���
	 */
	public void initHeaderView(){
		headerView = View.inflate(getContext(), R.layout.layout_header, null);
		ivArrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
		pbRoate = (ProgressBar) headerView.findViewById(R.id.pb_roate);
		tvState = (TextView) headerView.findViewById(R.id.tv_state);
		tvDate = (TextView) headerView.findViewById(R.id.tv_date);
		headerView.measure(0, 0);//����֪ͨϵͳȥ����
		headerHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerHeight, 0, 0); //ͨ������padding����headerview
		//������setAdapter֮ǰ����
		addHeaderView(headerView);
	}
	
	/**
	 * ��ʼ���ײ��ļ��ظ���Ĳ���
	 */
	public void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.layout_footer, null);
		footerView.measure(0, 0);//����֪ͨϵͳȥ����
		footerHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerHeight, 0, 0); //ͨ������padding����headerview
		//������setAdapter֮ǰ����
		addFooterView(footerView);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			//�жϵ�ǰ�Ƿ�������ˢ��״̬���ǵĻ��Ͳ���������ˢ����
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
				return true; //����TouchMove,����listview����ô�move�¼��������listview�޷�����
			}
			
			break;
		case MotionEvent.ACTION_UP:
			//���������
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
	 * ���ݵ�ǰ��״̬ˢ��headerView
	 */
	public void refreshHeader() {
		switch (currentLevel) {
		case PULL_REFRESH:
			tvState.setText("����ˢ��");
			ivArrow.setAnimation(downAnimation);
			break;
		case RELEASE_RAEFRESH:
			tvState.setText("�ɿ�ˢ��");
			ivArrow.setAnimation(upAnimation);
			break;
		case REFRESHING:
			tvState.setText("����ˢ��...");
			ivArrow.clearAnimation(); //��Ϊ���ϵ���ת��������û��ִ����
			ivArrow.setVisibility(View.INVISIBLE);
			pbRoate.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}
	
	/**
	 * ���ˢ�º���ã�����״̬,�����ȡ�����ݸ�����adapter֮����UI�߳��е��ø÷���
	 */
	public void completeRefresh() {
		if (isLodingMore) {
			//���ü��ظ�����ͼΪ���ɼ�
			footerView.setPadding(0, -footerHeight, 0, 0);
			isLodingMore = false;
		}else {
			tvState.setText("����ˢ��");
			currentLevel = PULL_REFRESH;
			tvDate.setText("���ˢ�£�" + getCurrentTime());
			pbRoate.setVisibility(View.INVISIBLE);
			ivArrow.setVisibility(View.VISIBLE);
			headerView.setPadding(0, -headerHeight, 0, 0);
		}

	}
	
	/**
	 * ��ȡ��ǰ��ʱ��
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
	/**scroolState�����ֿ���ȡֵ
	 * SCROLL_STATE_IDLE //��ʾ��ָ�ɿ�
	 * SCROLL_STATE_TOUCH_SCROLL //��ʾ��ָ��ס����
	 * SCROLL_STATE_FLING //��ʾ���Թ���
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && 
				getLastVisiblePosition() == (getCount() - 1) &&
				!isLodingMore) {
			isLodingMore = true;
			footerView.setPadding(0, 0, 0, 0); //���ü��ظ�����ͼ�ɼ�
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
