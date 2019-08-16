package com.sap.inspection.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.inspection.R;
import com.sap.inspection.listener.GroupActivityListener;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.adapter.GroupsAdapter;

public class GroupFragment extends BaseFragment {
	private GroupsAdapter adapter;
	private ListView list;
	private View back, mainmenu;
	private TextView title, subTitle;
	private WorkFormRowModel groupItems;
	private ScheduleBaseModel schedule;
    private String workTypeName;

	private GroupActivityListener backPressedListener;

	public static GroupFragment newInstance() {
		GroupFragment fragment = new GroupFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("onCreate");
		adapter = new GroupsAdapter(activity);
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
		adapter.setItems(groupItems);
		adapter.setScheduleId(schedule.id);
		adapter.setWorkTypeName(workTypeName);
		return root;
	}

	public void setSchedule(ScheduleBaseModel schedule) {
		this.schedule = schedule;
	}

	public void setGroupActivityListener(GroupActivityListener backPressedListener) {
		this.backPressedListener = backPressedListener;
	}
	
	public void setGroupItems(WorkFormRowModel groupItems) {
		this.groupItems = groupItems;
	}

	public void setWorkTypeName(String workTypeName) {
	    this.workTypeName = workTypeName;
    }

    public void setItems(WorkFormRowModel groupItems) {
		adapter.setItems(groupItems);
	}
	public void refreshItems() {
		adapter.notifyDataSetChanged();
	}
}
