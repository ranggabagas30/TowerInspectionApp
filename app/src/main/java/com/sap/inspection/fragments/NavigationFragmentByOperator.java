//package com.sap.inspection.fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.sap.inspection.FormFillActivity;
//import com.sap.inspection.R;
//import com.sap.inspection.listener.FormActivityListener;
//import com.sap.inspection.model.ScheduleBaseModel;
//import com.sap.inspection.model.form.RowModel;
//import com.sap.inspection.views.adapter.NavigationAdapter;
//
//public class NavigationFragmentByOperator extends BaseFragment{
//	private NavigationAdapter adapter;
//	private ListView list;
//	private View back, mainmenu;
//	private TextView title, subTitle;
//	private RowModel navigationModel;
//	private ScheduleBaseModel scheduleModel;
//	private String workFormGroupId;
//	
//	private FormActivityListener backPressedListener;
//
//	public static NavigationFragmentByOperator newInstance() {
//		NavigationFragmentByOperator fragment = new NavigationFragmentByOperator();
//		return fragment;
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		adapter = new NavigationAdapter(activity);
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
//		View root = inflater.inflate(R.layout.fragment_navigation, null);
//		this.adapter.setScheduleBaseModel(scheduleModel);
//		this.adapter.setWorkFormGroupId(workFormGroupId);
//		list = (ListView) root.findViewById(R.id.list);
//		title = (TextView) root.findViewById(R.id.header_title);
//		title.setText(scheduleModel.work_type.name.toUpperCase());
//		subTitle = (TextView) root.findViewById(R.id.header_subtitle);
//		subTitle.setText(scheduleModel.site.name + " - "+scheduleModel.operator.name);
//		back = root.findViewById(R.id.action_left);
//		back.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (null != backPressedListener)
//					backPressedListener.myOnBackPressed();
//			}
//		});
//		mainmenu = root.findViewById(R.id.action_right);
//		mainmenu.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				getActivity().finish();
//			}
//		});
//		list.setAdapter(adapter);
//		adapter.setItems(navigationModel);
//		return root;
//	}
//
//	public void setFormActivityListener(FormActivityListener backPressedListener) {
//		this.backPressedListener = backPressedListener;
//	}
//	
//	public void setNavigationModel(RowModel navigationModel) {
//		this.navigationModel = navigationModel;
//	}
//	
//	public void setScheduleModel(ScheduleBaseModel scheduleModel) {
//		this.scheduleModel = scheduleModel;
//	}
//
//	public void setWorkFormGroupId(String workFormGroupId) {
//		this.workFormGroupId = workFormGroupId;
//	}
//	
//}
