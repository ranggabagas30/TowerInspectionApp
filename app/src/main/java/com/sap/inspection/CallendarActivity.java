package com.sap.inspection;

import java.util.LinkedHashMap;
import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;

import com.appmogli.widget.Dataset;
import com.appmogli.widget.SectionedGridViewAdapter;
import com.appmogli.widget.SectionedGridViewAdapter.OnGridItemClickListener;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.CallendarModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.tools.DebugLog;

public class CallendarActivity extends BaseActivity implements OnGridItemClickListener {

	protected static final String TAG = "MainActivity";
	private ListView listView;
	private Dataset dataSet;
	private SectionedGridViewAdapter adapter = null;
	private LinkedHashMap<String, Vector<CallendarModel>> cursorMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");
		setContentView(R.layout.activity_sectioned_grid);
		Bundle bundle = getIntent().getExtras();
		int filterBy = bundle.getInt("filterBy");
		ScheduleGeneral scheduleGeneral = new ScheduleGeneral();
		switch (filterBy) {
		case R.string.schedule:
			log("schedule");
			cursorMap = scheduleGeneral.getListScheduleForCallendarAdapter(scheduleGeneral.getAllSchedule(activity));
			break;
		case R.string.preventive:
			log("preventive");
			cursorMap = scheduleGeneral.getListScheduleForCallendarAdapter(scheduleGeneral.getScheduleByWorktype(activity, getString(R.string.preventive)));
			break;
		case R.string.corrective:
			log("corrective");
			cursorMap = scheduleGeneral.getListScheduleForCallendarAdapter(scheduleGeneral.getScheduleByWorktype(activity, getString(R.string.corrective)));
			break;
		case R.string.newlocation:
			log("new location");
			cursorMap = scheduleGeneral.getListScheduleForCallendarAdapter(scheduleGeneral.getScheduleByWorktype(activity, getString(R.string.newlocation)));
			break;
		case R.string.colocation:
			log("colocation");
			cursorMap = scheduleGeneral.getListScheduleForCallendarAdapter(scheduleGeneral.getScheduleByWorktype(activity, getString(R.string.colocation)));
			break;
		default:
			break;
		}

		listView = (ListView) findViewById(R.id.listview);
		listView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

						// now check the width of the list view
						int width = listView.getWidth();
						adapter = new SectionedGridViewAdapter(CallendarActivity.this, cursorMap, listView.getWidth(), listView.getHeight(),getResources().getDimensionPixelSize(R.dimen.grid_item_size),getResources().getDimensionPixelSize(R.dimen.grid_item_size));
						adapter.setListener(CallendarActivity.this);
						listView.setAdapter(adapter);
//						listView.setDividerHeight(adapter.gapBetweenChildrenInRow());
					}
				});
	}

	@Override
	public void onGridItemClicked(String sectionName, int position, View v) {
		log(sectionName +" | "+ position+" | "+cursorMap.get(sectionName).get(position).date);
		Intent data = new Intent();
		data.putExtra("date", cursorMap.get(sectionName).get(position).date);
		setResult(Constants.CALLENDAR_ACTIVITY, data);
		finish();
	}

}
