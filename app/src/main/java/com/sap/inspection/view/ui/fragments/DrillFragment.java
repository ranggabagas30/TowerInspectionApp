package com.sap.inspection.view.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView.LayoutParams;

import com.sap.inspection.R;
import com.sap.inspection.listener.GroupActivityListener;
import com.sap.inspection.view.adapter.DrillAdapter;

public class DrillFragment extends BaseListTitleFragment{
	private DrillAdapter adapter;
	private GroupActivityListener groupActivityListener;
	
	public static DrillFragment newInstance() {
		DrillFragment fragment = new DrillFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new DrillAdapter(activity);
	}
	
	@Override
	public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState) {
		super.onCreateView(inflater, savedInstanceState);
		LayoutParams p = new LayoutParams(getResources().getDimensionPixelOffset(R.dimen.padding_medium), getResources().getDimensionPixelOffset(R.dimen.padding_medium));
		View header = new View(activity);
		header.setLayoutParams(p);
		View footer = new View(activity);
		footer.setLayoutParams(p);
		list.addFooterView(footer);
		list.addHeaderView(header);
		list.setAdapter(adapter);
		list.setPadding(getResources().getDimensionPixelOffset(R.dimen.padding_large), 0, getResources().getDimensionPixelOffset(R.dimen.padding_large), 0);
		actionLeft.setVisibility(View.VISIBLE);
		actionLeft.setOnClickListener(v -> {
			if (groupActivityListener != null)
				groupActivityListener.onShowNavigation();
		});
	}

	public void setGroupActivityListener(
			GroupActivityListener groupActivityListener) {
		this.groupActivityListener = groupActivityListener;
	}
 
	@Override
	public String getTitle() {
		return "Main Menu";
	}
}
