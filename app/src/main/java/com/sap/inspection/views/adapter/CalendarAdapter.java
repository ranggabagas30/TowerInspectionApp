package com.sap.inspection.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sap.inspection.R;

public class CalendarAdapter extends MyBaseAdapter {
	
	private Context context;
	
	public CalendarAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return 100;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
//	@Override
//	public int getViewTypeCount() {
//		return 2;
//	}
//	
//	@Override
//	public int getItemViewType(int position) {
//		return position == 0 ? 0 : 1;
//	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
				view = LayoutInflater.from(context).inflate(R.layout.item_calendar,null);
				holder.month = (TextView) view.findViewById(R.id.item_calendar_month);
				holder.task = (TextView) view.findViewById(R.id.item_calendar_task);
				holder.day = (TextView) view.findViewById(R.id.item_calendar_day);
				holder.dayPhrase = (TextView) view.findViewById(R.id.item_calendar_day_phrase);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		
		return view; 
	}
	
	private class ViewHolder {
		public TextView month;
		public TextView task;
		public TextView day;
		public TextView dayPhrase;
	}


}
