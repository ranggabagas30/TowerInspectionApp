package com.sap.inspection.views.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.inspection.BaseActivity;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.CheckInActivity;
import com.sap.inspection.FormFillActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class NavigationAdapter extends MyBaseAdapter {

	private Context context;
	private RowModel model;
	private Vector<RowModel> shown;
	private String scheduleId;
	private ScheduleBaseModel scheduleBaseModel;
    private int positionAncestry;

    public void setSchedule(ScheduleBaseModel scheduleBaseModel) {
    	this.scheduleBaseModel = scheduleBaseModel;
	}
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}
	public String getScheduleId() {
		return scheduleId;
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
        int position = 0;
        for (RowModel rowModel : shown) {
            DebugLog.d("id : " + rowModel.id);
			DebugLog.d("workFormGroupId : " + rowModel.work_form_group_id);
            DebugLog.d("name : " + rowModel.text);
            DebugLog.d("ancestry name : " + shown.get(position).text);
            DebugLog.d("ancestry : " + rowModel.ancestry);
            DebugLog.d("parentId : " + rowModel.parent_id);
            if (rowModel.children != null) {
                DebugLog.d("children size : " + rowModel.children.size());
                for (RowModel child : rowModel.children) {
                    DebugLog.d("--- child id : " + child.id);
					DebugLog.d("--- child workFormGroupId : " + child.work_form_group_id);
                    DebugLog.d("--- child name : " + child.text);
                    DebugLog.d("--- child ancestry : " + child.ancestry);
                    DebugLog.d("--- child parentId : " + child.parent_id);
                }
            }
            DebugLog.d("\n\n");
            position++;
        }
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
			holder.uploadWorkFormGroup = (ImageView) view.findViewById(R.id.workformgroup_upload);
			holder.uploadWorkFormGroup.setOnClickListener(uploadWorkFormGroupListener);
			holder.title = (TextView) view.findViewById(R.id.title);
			holder.title.setOnClickListener(ItemClickListener);
			if (getItemViewType(position) == 0){
				holder.uploadWorkFormGroup.setVisibility(View.VISIBLE);
			}

			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		holder.expandCollapse.setTag(position);
		holder.uploadWorkFormGroup.setTag(position);
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

		return view; 
	}

	OnClickListener expandCollapseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			toogleExpand(position);
		}
	};

	OnClickListener uploadWorkFormGroupListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!GlobalVar.getInstance().anyNetwork(context)){
				MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.checkConnection), Toast.LENGTH_SHORT);
			} else {

				if (!ItemUploadManager.getInstance().isRunning()) {
					String scheduleId = getScheduleId();
					int position = (int) v.getTag();
					RowModel rowModel = getItem(position);
					DebugLog.d("rowModel.work_form_group_id : " + rowModel.work_form_group_id);

					DbRepositoryValue.getInstance().open(context);
					DbRepository.getInstance().open(context);
					ItemValueModel itemValueModel = new ItemValueModel();
					ArrayList<ItemValueModel> listItemUploadByWorkFormGroupId = new ArrayList<>();
					ArrayList<ItemValueModel> listItemValue = itemValueModel.getItemValuesForUpload(scheduleId);
					for (ItemValueModel model : listItemValue) {

						WorkFormItemModel workFormItemModel = new WorkFormItemModel();
						workFormItemModel = workFormItemModel.getItemById(model.itemId, rowModel.work_form_group_id);
						if (rowModel.work_form_group_id == workFormItemModel.work_form_group_id) {
							listItemUploadByWorkFormGroupId.add(model);
							DebugLog.d("t1.workFormGroupId : " + workFormItemModel.work_form_group_id);
							DebugLog.d("t1.scheduleId : " + model.scheduleId);
							DebugLog.d("t1.operatorId : " + model.operatorId);
							DebugLog.d("t1.itemId : " + model.itemId);
							DebugLog.d("t1.remark : " + model.remark);
							DebugLog.d("t1.value : "  + model.value);
						}
						DebugLog.d("\n\n");
					}

					if (listItemUploadByWorkFormGroupId.size()!= 0) {
						ItemUploadManager.getInstance().addItemValues(listItemUploadByWorkFormGroupId);
					} else {
						MyApplication.getInstance().toast(context.getResources().getString(R.string.tidakadaitem), Toast.LENGTH_SHORT);
					}

					DbRepositoryValue.getInstance().close();
					DbRepository.getInstance().close();
				} else {
					MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.uploadProses), Toast.LENGTH_SHORT);
				}
			}

		}
	};

	OnClickListener ItemClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
            if (getItem(position).id == 0) {
                positionAncestry = position;
                DebugLog.d("positionAncestry : " + positionAncestry);
            }
			if (getItem(position).text.equalsIgnoreCase("others form")){
				Intent intent = new Intent(context, FormFillActivity.class);
				intent.putExtra("rowId", getItem(position).id);
				intent.putExtra("workFormGroupId", getItem(position).work_form_group_id);
				intent.putExtra("scheduleId", scheduleId);
                intent.putExtra("workFormGroupName", shown.get(positionAncestry).text);
				DebugLog.d("----ini others form lho----- "+scheduleId);
//				context.startActivity(intent);
			}
			else if (getItem(position).hasForm){
				DebugLog.d("----schedule id----- "+scheduleId);

				String workFormGroupName = shown.get(positionAncestry).text;
				Intent intent;
				intent = new Intent(context, FormFillActivity.class);
				intent.putExtra("rowId", getItem(position).id);
				intent.putExtra("workFormGroupId", getItem(position).work_form_group_id);
				intent.putExtra("scheduleId", scheduleId);
				intent.putExtra("workFormGroupName", workFormGroupName);
				intent.putExtra("scheduleBaseModel", scheduleBaseModel);
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
		ImageView uploadWorkFormGroup;
		TextView title;
	}


}
