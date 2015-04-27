package com.sap.inspection.views.adapter;

import java.util.Vector;


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
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.RowModel;

public class NavigationAdapterByOperator extends MyBaseAdapter {

	private Context context;
	private RowModel model;
	private Vector<RowModel> shown;
	private ScheduleBaseModel scheduleModel;
	private String workFormGroupId;
	
	public void setWorkFormGroupId(String workFormGroupId) {
		this.workFormGroupId = workFormGroupId;
	}
	
	public void setScheduleBaseModel(ScheduleBaseModel scheduleBaseModel) {
		this.scheduleModel = scheduleBaseModel;
	}

	public NavigationAdapterByOperator(Context context) {
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
		return getItem(position).level - 1;
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
				log("============== get default view : "+getItemViewType(position));
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
			if (getItem(position).hasForm){
				Intent intent = new Intent(context, FormFillActivity.class);
				intent.putExtra("rowId", getItem(position).id);
				intent.putExtra("scheduleId", scheduleModel.id);
				intent.putExtra("workFormGroupId", workFormGroupId);
				context.startActivity(intent);
			}else
				toogleExpand(position);
//			Toast.makeText(context, getItem(position).position, Toast.LENGTH_SHORT).show();
		}
	};
	
	private void toogleExpand(int position){
		if (getItem(position).isOpen)
			getItem(position).isOpen = false;
		else if (getItem(position).children != null && getItem(position).children.size() > 0){
			getItem(position).isOpen = true;
		}
		notifyDataSetChanged();
	}

	private class ViewHolder {
		ImageView expandCollapse;
		TextView title;
	}


}
