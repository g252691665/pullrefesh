package com.henugao.pullrefresh.view;

import com.henugao.pullrefresh.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class RefreshListView extends ListView {
	
	
	private View headerView;  //ͷ����ˢ����ͼ	
	private int headerHeight; //ͷ���ĸ߶�

	public RefreshListView(Context context) {
		super(context);
		init();
	}



	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	private void init() {
		initHeaderView();
		
	}
	
	/**
	 * ��ʼ��ͷ����ˢ�²���
	 */
	public void initHeaderView(){
		headerView = View.inflate(getContext(), R.layout.layout_header, null);
		headerView.measure(0, 0);//����֪ͨϵͳȥ����
		headerHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerHeight, 0, 0); //ͨ������padding����headerview
		//������setAdapter֮ǰ����
		addHeaderView(headerView);
	}
}
