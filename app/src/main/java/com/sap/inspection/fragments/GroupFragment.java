package com.sap.inspection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.inspection.R;
import com.sap.inspection.listener.FormActivityListener;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.views.adapter.NavigationAdapter;

public class NavigationFragment extends BaseFragment {
	private NavigationAdapter adapter;
	private ListView list;
	private View back, mainmenu;
	private TextView title, subTitle;
	private RowModel navigationModel;
	private ScheduleBaseModel schedule;
    private String workTypeName;

	private FormActivityListener backPressedListener;

	public static NavigationFragment newInstance() {
		NavigationFragment fragment = new NavigationFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("onCreate");
		adapter = new NavigationAdapter(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		DebugLog.d("onCreateView");
		View root = inflater.inflate(R.layout.fragment_navigation, null);
		list = (ListView) root.findViewById(R.id.list);
		title = (TextView) root.findViewById(R.id.header_title);
		title.setText(schedule.site.name);
		subTitle = (TextView) root.findViewById(R.id.header_subtitle);
		subTitle.setText(schedule.work_type.name);
		back = root.findViewById(R.id.action_left);
		back.setOnClickListener(v -> {
			if (null != backPressedListener)
				backPressedListener.myOnBackPressed();
		});
		mainmenu = root.findViewById(R.id.action_right);
		mainmenu.setOnClickListener(v -> getActivity().finish());
		list.setAdapter(adapter);
		adapter.setItems(navigationModel);
		adapter.setScheduleId(schedule.id);
		adapter.setWorkTypeName(workTypeName);
		return root;
	}

	public void setSchedule(ScheduleBaseModel schedule) {
		this.schedule = schedule;
	}

	public void setFormActivityListener(FormActivityListener backPressedListener) {
		this.backPressedListener = backPressedListener;
	}
	
	public void setNavigationModel(RowModel navigationModel) {
		this.navigationModel = navigationModel;
	}

	public void setWorkTypeName(String workTypeName) {
	    this.workTypeName = workTypeName;
    }

    public void setItems(RowModel rowModel) {
		adapter.setItems(rowModel);
	}
	public void refreshItems() {
		adapter.notifyDataSetChanged();
	}
}
