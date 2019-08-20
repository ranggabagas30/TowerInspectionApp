package com.sap.inspection.view.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.inspection.R;

public abstract class BaseListTitleFragment extends BaseFragment {
	protected ListView list;
	private TextView title;
	private RelativeLayout header;
	protected ImageView actionRight;
	protected ImageView actionLeft;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_list_with_titleheader, null, false);
		header = root.findViewById(R.id.header);
		View headerTitleLayout = inflater.inflate(R.layout.header_title, header, true);
		title = headerTitleLayout.findViewById(R.id.header_title);
		title.setText(getTitle());
		list = root.findViewById(R.id.list);
		actionRight = headerTitleLayout.findViewById(R.id.action_right);
		actionLeft = headerTitleLayout.findViewById(R.id.action_left);
		onCreateView(inflater);
		return root;
	}
	
	public void onCreateView(LayoutInflater inflater){
	}
	
	public abstract String getTitle(); 
	
}
