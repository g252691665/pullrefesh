package com.henugao.pullrefresh;

import java.util.ArrayList;
import java.util.List;

import com.henugao.pullrefresh.view.RefreshListView;
import com.henugao.pullrefresh.view.RefreshListView.RefreshListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	
	private RefreshListView refreshListView;
	private List<String> list;
	private MyAdapter adapter;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//更新listview
			adapter.notifyDataSetChanged();
			//完成下拉刷新
			refreshListView.completeRefresh();
		}
	};
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUI();
		initData();

	}
	//初始化界面
	private void initUI() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
	}
	//初始化数据
	private void initData() {
		list = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			list.add("原来的数据：" + i);
		}
		adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);
		refreshListView.setOnRefreshListener(new RefreshListener() {
			
			@Override
			public void onPullRefresh() {
				//执行下拉刷新的操作：(可以定义一些自己想要的操作)比如说从远端服务器请求数据,
				requestData();
			}
		});

	}
	
	/**
	 * 模拟向服务器请求数据
	 */
	public void requestData() {
		new Thread(){
			public void run() {
				SystemClock.sleep(3000);
				list.add(0,"从服务器请求的数据");
				handler.sendEmptyMessage(0);
			};
		}.start();
	}
	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView textView = new TextView(MainActivity.this);
			textView.setText(list.get(position));
			textView.setTextSize(12);
			textView.setPadding(20, 20, 20, 20);
			return textView;
		}
		
	}


}
