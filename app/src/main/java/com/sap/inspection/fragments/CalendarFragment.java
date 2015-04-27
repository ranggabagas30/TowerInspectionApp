package com.sap.inspection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import com.sap.inspection.R;
import com.sap.inspection.manager.ScreenManager;
import com.sap.inspection.views.adapter.CalendarAdapter;

public class CalendarFragment extends BaseTitleFragment{
	private GridView grid;
	private CalendarAdapter adapter;
	
	public static CalendarFragment newInstance() {
		CalendarFragment fragment = new CalendarFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new CalendarAdapter(activity);
	}
 
	@Override
	public View onGetLayout(LayoutInflater inflater) {
		View root = inflater.inflate(R.layout.fragment_calendar, null, false);
		grid = (GridView) root.findViewById(R.id.grid);
		grid.setColumnWidth((int) ((ScreenManager.getInstance().getMin() - 6) / 4));
		grid.setAdapter(adapter);
		return root;
	}

	@Override
	public String getTitle() {
		return "Main Menu";
	}
}
