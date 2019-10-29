package com.sap.inspection.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.AsyncDeleteAllFiles;
import com.sap.inspection.model.RejectionModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.DialogUtil;

import java.util.Vector;

public class ScheduleAdapter extends MyBaseAdapter {

	private Context context;
	private Vector<ScheduleBaseModel> models = new Vector<>();
	private AdapterView.OnItemClickListener onItemClickListener;

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

		ScheduleBaseModel schedule = getItem(position);

		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case 0:
				view = LayoutInflater.from(context).inflate(R.layout.item_schedule_separator,null);
				holder.title = view.findViewById(R.id.item_schedule_title_separator);
				break;

			case 1:
				view = LayoutInflater.from(context).inflate(R.layout.item_schedule2,null);
				holder.rejectedTitle = view.findViewById(R.id.item_schedule_message_rejection);
				holder.percent = view.findViewById(R.id.item_schedule_percent);
				holder.percent.setTypeface(null, Typeface.BOLD);
				holder.status = view.findViewById(R.id.item_schedule_status);
				holder.statusLayout = view.findViewById(R.id.item_schedule_statuslayout);
				holder.title = view.findViewById(R.id.item_schedule_title);
				holder.task = view.findViewById(R.id.item_schedule_task);
				holder.upload = view.findViewById(R.id.item_schedule_upload);
				holder.deleteAndUpdateSchedule = view.findViewById(R.id.item_schedule_delete);

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
			holder.upload.setOnClickListener(onUploadClickListener);
            holder.upload.setTag(getItem(position).id);
            holder.upload.setVisibility(View.VISIBLE);
			holder.deleteAndUpdateSchedule.setOnClickListener(onDeleteClickListener);
            holder.deleteAndUpdateSchedule.setTag(position);
			holder.deleteAndUpdateSchedule.setVisibility(View.VISIBLE);
			if (schedule.work_type.name.matches(Constants.regexIMBASPETIR)) {
				holder.upload.setVisibility(View.GONE);
				holder.deleteAndUpdateSchedule.setVisibility(View.GONE);
			}
			if (schedule.rejection != null) {
				holder.rejectedTitle.setVisibility(View.VISIBLE);
				holder.rejectedTitle.setText(schedule.rejection.getTitle());
				holder.rejectedTitle.setTag(schedule.rejection);
				holder.rejectedTitle.setOnClickListener(onRejectedTitleClickListener);
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
		public TextView rejectedTitle;
		public TextView percent;
		public TextView status;
		public View statusLayout;
		public TextView title;
		public TextView task;
		public ImageView arrow;
        public View upload;
        public ImageView deleteAndUpdateSchedule;
	}

    View.OnClickListener onUploadClickListener = v -> {
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

	View.OnClickListener onDeleteClickListener = v -> {
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

	View.OnClickListener onRejectedTitleClickListener = v -> {
		RejectionModel rejection = (RejectionModel) v.getTag();
		DialogUtil.showRejectionDialog(context, rejection.getTitle(), rejection.getMessages());
	};
}
