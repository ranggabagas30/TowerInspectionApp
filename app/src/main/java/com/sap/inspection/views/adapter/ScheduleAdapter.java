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
import com.sap.inspection.TowerApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.AsyncDeleteAllFiles;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.DeleteAllDataDialog;
import com.sap.inspection.view.dialog.DialogUtil;

import java.util.Vector;

public class ScheduleAdapter extends MyBaseAdapter {

	private Context context;
	private Vector<ScheduleBaseModel> models = new Vector<>();

	public ScheduleAdapter(Context context) {
		this.context = context;
	}

	public void setItems(Vector<ScheduleBaseModel> models) {
		this.models = models;
		notifyDataSetChanged();
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
				holder.deleteAndUpdateSchedule = view.findViewById(R.id.item_schedule_delete);
				holder.deleteAndUpdateSchedule.setOnClickListener(delete);

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
            holder.upload.setVisibility(View.VISIBLE);
            holder.deleteAndUpdateSchedule.setTag(position);
			holder.deleteAndUpdateSchedule.setVisibility(View.VISIBLE);
			if (itemModel.work_type.name.matches(Constants.regexIMBASPETIR)) {

				holder.upload.setVisibility(View.GONE);
				holder.deleteAndUpdateSchedule.setVisibility(View.GONE);

			}

			break;

		default:
			break;
		}

		if (view != null && getItem(position).isAnimated == true) {
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
        public ImageView deleteAndUpdateSchedule;
	}

    View.OnClickListener upload = v -> {
		if (!GlobalVar.getInstance().anyNetwork(TowerApplication.getContext())) {
			TowerApplication.getInstance().toast("Tidak ada koneksi internet, periksa kembali jaringan anda.", Toast.LENGTH_SHORT);
		} else {
			String scheduleId = (String) v.getTag();

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, FormValueModel.UNSPECIFIED, Constants.EMPTY, Constants.EMPTY).execute();
			else
				new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, FormValueModel.UNSPECIFIED, null, null).execute();
		}
	};

	View.OnClickListener delete = v -> {
		int deletedSchedulePosition = (int) v.getTag();
		ScheduleBaseModel deletedScheduleItem = getItem(deletedSchedulePosition);
		DebugLog.d("start deleting schedule " + deletedScheduleItem.id + " with pos " + deletedSchedulePosition);
		DialogUtil.deleteAllDataDialog(context, deletedScheduleItem.id)
				.setOnPositiveClickListener(scheduleId -> {
					DebugLog.d("delete all files by scheduleid " + scheduleId);
					AsyncDeleteAllFiles task = new AsyncDeleteAllFiles(scheduleId);
					task.execute();
				}).show();
	};
}
