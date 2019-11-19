package com.sap.inspection.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
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
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.DeleteAllProgressEvent;
import com.sap.inspection.manager.AsyncDeleteAllFiles;
import com.sap.inspection.model.RejectionModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DialogUtil;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.apache.http.HttpStatus;

import java.io.File;
import java.util.Vector;

import de.greenrobot.event.EventBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ScheduleAdapter extends MyBaseAdapter {

	private Context context;
	private Vector<ScheduleBaseModel> scheduleItems = new Vector<>();
	private AdapterView.OnItemClickListener onItemClickListener;

	public ScheduleAdapter(Context context) {
		this.context = context;
	}

	public void setItems(Vector<ScheduleBaseModel> scheduleItems) {
		this.scheduleItems = scheduleItems;
		notifyDataSetChanged();
	}

	public void addItem(ScheduleBaseModel model) {
		this.scheduleItems.add(model);
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		this.scheduleItems.remove(position);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return scheduleItems.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public ScheduleBaseModel getItem(int position) {
		return scheduleItems.get(position);
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

			holder.percent.setText(schedule.getPercent());
			holder.status.setText(schedule.getStatus());
			holder.statusLayout.setBackgroundColor(Color.parseColor(schedule.getPercentColor()));
			holder.title.setText(schedule.getTitle());
			holder.task.setText(schedule.getTask());
			holder.task.setTextColor(Color.parseColor(schedule.getTaskColor()));
			holder.task.setVisibility(View.VISIBLE);
			holder.upload.setOnClickListener(onUploadClickListener);
            holder.upload.setTag(schedule.id);
            holder.upload.setVisibility(View.VISIBLE);
			holder.deleteAndUpdateSchedule.setOnClickListener(onDeleteClickListener);
            holder.deleteAndUpdateSchedule.setTag(position);
			holder.deleteAndUpdateSchedule.setVisibility(View.VISIBLE);

			if (schedule.work_type.name.matches(Constants.regexIMBASPETIR)) {
				holder.upload.setVisibility(View.GONE);
				holder.deleteAndUpdateSchedule.setVisibility(View.GONE);
			} else if (schedule.work_type.name.matches(Constants.regexFOCUT)) {
				holder.title.setText(TextUtils.isEmpty(schedule.tt_number) ? "NULL" : schedule.tt_number);
				holder.task.setVisibility(View.GONE);
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

		if (view != null && schedule.isAnimated == true) {
			schedule.isAnimated = false;
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

	@SuppressLint("CheckResult")
	View.OnClickListener onDeleteClickListener = v -> {
		int deletedSchedulePosition = (int) v.getTag();
		ScheduleBaseModel deleteSchedule = getItem(deletedSchedulePosition);
		DialogUtil.deleteAllDataDialog(context, deleteSchedule)
				.setOnPositiveClickListener(schedule -> {
					DebugLog.d("delete all files by scheduleId " + schedule.id + " with position: " + deletedSchedulePosition);
					EventBus.getDefault().post(new DeleteAllProgressEvent("Deleting schedule on progress", false, false));
					String path = Constants.DIR_PHOTOS + File.separator + schedule.id + File.separator;
					CommonUtil.deleteAllData(schedule)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(
									() -> {
									    CommonUtil.deleteFiles(path);
                                        if (!TextUtils.isEmpty(schedule.id)) {
                                            if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && schedule.work_type.name.matches(Constants.regexFOCUT)) {
                                                TowerAPIHelper.deleteSchedule(schedule.id)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                response -> {
                                                                    if (response.status == HttpStatus.SC_OK) {
                                                                        ScheduleBaseModel.delete(schedule.id);
                                                                        removeItem(deletedSchedulePosition);
                                                                        EventBus.getDefault().post(new DeleteAllProgressEvent(response.messages, true, false));
                                                                    } else {
                                                                        EventBus.getDefault().post(new DeleteAllProgressEvent("Failed (error code: " + response.status + ")", true, false));
                                                                    }
                                                                }, error ->  {
                                                                    EventBus.getDefault().post(new DeleteAllProgressEvent("Failed delete schedule", true, false));
                                                                    DebugLog.e(error.getMessage(), error);
                                                                }
                                                        );
                                            } else {
                                                EventBus.getDefault().post(new DeleteAllProgressEvent("Success delete all " + schedule.id + " data", true, false));
                                            }
                                        } else {
                                            EventBus.getDefault().post(new DeleteAllProgressEvent("Failed delete schedule. Schedule id not found", true, false));
                                        }
									},
                                    error -> {
                                        EventBus.getDefault().post(new DeleteAllProgressEvent("Failed delete local data for schedule " + schedule.id, true, false));
                                        DebugLog.e(error.getMessage(), error);
                                    }
							);
				}).show();
	};

	View.OnClickListener onRejectedTitleClickListener = v -> {
		RejectionModel rejection = (RejectionModel) v.getTag();
		DialogUtil.showRejectionDialog(context, rejection.getTitle(), rejection.getMessages());
	};
}
