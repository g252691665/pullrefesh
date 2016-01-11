package com.henugao.pullrefresh.view;

import com.henugao.pullrefresh.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class RefreshListView extends ListView {
	
	
	private View headerView;  //头部的刷新视图	
	private int headerHeight; //头部的高度

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
	 * 初始化头部的刷新布局
	 */
	public void initHeaderView(){
		headerView = View.inflate(getContext(), R.layout.layout_header, null);
		headerView.measure(0, 0);//主动通知系统去测量
		headerHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headerHeight, 0, 0); //通过设置padding隐藏headerview
		//必须在setAdapter之前调用
		addHeaderView(headerView);
	}
}
