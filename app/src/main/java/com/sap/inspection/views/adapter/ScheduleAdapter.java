package com.sap.inspection.views.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;

import java.util.ArrayList;
import java.util.Vector;

public class ScheduleAdapter extends MyBaseAdapter {

	private Context context;
	private Vector<ScheduleBaseModel> models;
	

	public void setItems(Vector<ScheduleBaseModel> models) {
		this.models = models;
		notifyDataSetChanged();
	}

	public ScheduleAdapter(Context context) {
		this.context = context;
		models = new Vector<>();
	}
	
	@Override
	public int getCount() {
		return models.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public ScheduleBaseModel getItem(int position) {
		return models.get(position);
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isSeparator ? 0 : 1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return !getItem(position).isSeparator;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;

		ScheduleBaseModel itemModel = getItem(position);

		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case 0:
				view = LayoutInflater.from(context).inflate(R.layout.item_schedule_separator,null);
				holder.title = (TextView) view.findViewById(R.id.item_schedule_title);
				break;

			case 1:
				view = LayoutInflater.from(context).inflate(R.layout.item_schedule,null);
				holder.percent = (TextView) view.findViewById(R.id.item_schedule_percent);
				holder.percent.setTypeface(null, Typeface.BOLD);
				holder.status = (TextView) view.findViewById(R.id.item_schedule_status);
				holder.statusLayout = view.findViewById(R.id.item_schedule_statuslayout);
				holder.title = (TextView) view.findViewById(R.id.item_schedule_title);
				holder.task = (TextView) view.findViewById(R.id.item_schedule_task);
				holder.place = (TextView) view.findViewById(R.id.item_schedule_place);
				holder.upload = view.findViewById(R.id.item_schedule_upload);
				holder.upload.setOnClickListener(upload);
				holder.upload.setVisibility(View.VISIBLE);

				break;
			default:
				break;
			}
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		switch (getItemViewType(position)) {
		case 0:
			String[] date = getItem(position).day_date.split("[-]",3);
			
			holder.title.setText(Constants.MONTHS[Integer.parseInt(date[1]) - 1 ] +" "+ date[2] +","+ date[0]);
			break;

		case 1:
			holder.percent.setText(getItem(position).getPercent());
			holder.status.setText(getItem(position).getStatus());
			holder.statusLayout.setBackgroundColor(Color.parseColor(getItem(position).getPercentColor()));
			holder.title.setText(getItem(position).getTitle());
			holder.task.setText(getItem(position).getTask());
			holder.task.setTextColor(Color.parseColor(getItem(position).getTaskColor()));
			holder.place.setText(getItem(position).getPlace());
            holder.upload.setTag(getItem(position).id);
			if (itemModel.work_type.name.matches(Constants.regexIMBASPETIR))
				holder.upload.setVisibility(View.INVISIBLE);
			break;

		default:
			break;
		}

		if (view != null && getItem(position).isAnimated == true){
			getItem(position).isAnimated = false;
			Animation animation = new ScaleAnimation((float)0, (float)1.0 ,(float)1.0, (float)1.0);
			animation.setDuration(1000);
			view.startAnimation(animation);
			animation = null;
		}

		return view; 
	}

	private class ViewHolder {
		public TextView percent;
		public TextView status;
		public View statusLayout;
		public TextView title;
		public TextView task;
		public TextView place;
		public ImageView arrow;
        public View upload;
	}

    View.OnClickListener upload = v -> {
		if (!GlobalVar.getInstance().anyNetwork(MyApplication.getContext())) {
			MyApplication.getInstance().toast("Tidak ada koneksi internet, periksa kembali jaringan anda.", Toast.LENGTH_SHORT);
		} else {
			String scheduleId = (String) v.getTag();

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId, Constants.EMPTY, Constants.EMPTY).execute();
			else
				new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId).execute();
		}
	};

}
