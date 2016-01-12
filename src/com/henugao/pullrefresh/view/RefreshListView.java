package com.henugao.pullrefresh.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.henugao.pullrefresh.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView {
	
	
	private View headerView;  //ͷ����ˢ����ͼ	
	private ImageView ivArrow;
	private ProgressBar pbRoate;
	private TextView tvState;
	private TextView tvDate;
	private RotateAnimation upAnimation,downAnimation; //������ת��������ת����
	
	private int headerHeight; //ͷ���ĸ߶�
	private int downY; // 
	
	private final int PULL_REFRESH = 0;   //����ˢ��
	private final int RELEASE_RAEFRESH = 1; //�ɿ�ˢ��
	private final int REFRESHING = 2; //����ˢ��
	private int currentLevel = PULL_REFRESH; // ��ǰ��״̬




	public RefreshListView(Context context) {
		super(context);
		init();
	}



	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		initHeaderView();
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
		tvState.setText("����ˢ��");
		currentLevel = PULL_REFRESH;
		tvDate.setText("���ˢ�£�" + getCurrentTime());
		pbRoate.setVisibility(View.INVISIBLE);
		ivArrow.setVisibility(View.VISIBLE);
		headerView.setPadding(0, -headerHeight, 0, 0);
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
	}
}
