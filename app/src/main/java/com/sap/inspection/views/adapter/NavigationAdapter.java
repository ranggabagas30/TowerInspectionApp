package com.sap.inspection.views.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.inspection.FormFillActivity;
import com.sap.inspection.R;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.tools.DebugLog;

import java.util.Vector;

public class NavigationAdapter extends MyBaseAdapter {

	private Context context;
	private RowModel model;
	private Vector<RowModel> shown;
	private String scheduleId;

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public NavigationAdapter(Context context) {
		this.context = context;
		if (null == model)
			model = new RowModel();
		if (null == shown)
			shown = new Vector<RowModel>();
	}

	public void setItems(RowModel model){
		this.model = model;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		shown = model.getModels();
		DebugLog.d("shown size = "+shown.size());
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return shown.size();
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).level;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public RowModel getItem(int position) {
		return shown.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		DebugLog.d(getItem(position).ancestry+"/"+getItem(position).id+" | "+getItem(position).text);
		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case 0:
				view = LayoutInflater.from(context).inflate(R.layout.item_navigation_1,null);
				break;
			case 1:
				view = LayoutInflater.from(context).inflate(R.layout.item_navigation_2,null);
				break;
			case 2:
				view = LayoutInflater.from(context).inflate(R.layout.item_navigation_3,null);
				break;

			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				break;
			}
			holder.expandCollapse = (ImageView) view.findViewById(R.id.expandCollapse);
			holder.expandCollapse.setOnClickListener(expandCollapseListener);
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.title.setOnClickListener(ItemClickListener);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		holder.expandCollapse.setTag(position);
		holder.title.setText(getItem(position).text);
		holder.title.setTag(position);
		switch (getItemViewType(position)) {
		case 0:
			holder.expandCollapse.setImageResource(getItem(position).isOpen ? R.drawable.ic_collapse : R.drawable.ic_expand);
			break;
		case 1:
			holder.expandCollapse.setImageResource(getItem(position).isOpen ? R.drawable.ic_collapse_2 : R.drawable.ic_expand_2);
			break;

		default:
			break;
		}
		//		if (view != null){
		//			Animation animation = new ScaleAnimation((float)1.0, (float)1.0 ,(float)0, (float)1.0);
		//			animation.setDuration(300);
		//			view.startAnimation(animation);
		//			animation = null;
		//		}

		return view; 
	}

	OnClickListener expandCollapseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			toogleExpand(position);
		}
	};

	OnClickListener ItemClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if (getItem(position).text.equalsIgnoreCase("others form")){
				Intent intent = new Intent(context, FormFillActivity.class);
				intent.putExtra("rowId", getItem(position).id);
				intent.putExtra("workFormGroupId", getItem(position).work_form_group_id);
				intent.putExtra("scheduleId", scheduleId);
				DebugLog.d("----ini others form lho----- "+scheduleId);
//				context.startActivity(intent);
			}
			else if (getItem(position).hasForm){
				Intent intent = new Intent(context, FormFillActivity.class);
				intent.putExtra("rowId", getItem(position).id);
				intent.putExtra("workFormGroupId", getItem(position).work_form_group_id);
				intent.putExtra("scheduleId", scheduleId);
				DebugLog.d("----schedule id----- "+scheduleId);
				context.startActivity(intent);
//				Toast.makeText(context, "tester", Toast.LENGTH_SHORT).show();
			}else
				toogleExpand(position);
//						Toast.makeText(context, "tester "+getItem(position).position, Toast.LENGTH_SHORT).show();
		}
	};

	private void toogleExpand(int position){
		if (getItem(position).isOpen){
			getItem(position).isOpen = false;
			DebugLog.d("closed");
		}
		else if (getItem(position).children != null && getItem(position).children.size() > 0){
			getItem(position).isOpen = true;
			DebugLog.d("open");
		}else{
			DebugLog.d("not open");
		}
		notifyDataSetChanged();
	}

	private class ViewHolder {
		ImageView expandCollapse;
		TextView title;
	}


}
