package com.henugao.pullrefresh;

import java.util.ArrayList;
import java.util.List;

import com.henugao.pullrefresh.view.RefreshListView;

import android.os.Bundle;
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
		MyAdapter adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);

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
