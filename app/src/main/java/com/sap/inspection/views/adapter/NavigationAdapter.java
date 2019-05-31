package com.sap.inspection.views.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rindang.zconfig.APIList;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.CheckInActivity;
import com.sap.inspection.FormActivity;
import com.sap.inspection.FormActivityWarga;
import com.sap.inspection.FormFillActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.config.formimbaspetir.Warga;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.CheckApprovalResponseModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

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
	private String workTypeName;
	private ScheduleBaseModel scheduleBaseModel;
    private int positionAncestry;
	int dataIndex = -1;

    public void setSchedule(ScheduleBaseModel scheduleBaseModel) {
    	this.scheduleBaseModel = scheduleBaseModel;
	}
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
		dataIndex = FormImbasPetirConfig.getDataIndex(scheduleId);
	}
	public String getScheduleId() {
		return scheduleId;
	}

	public void setWorkTypeName(String workTypeName) {
        this.workTypeName = workTypeName;
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
		DebugLog.d("model size : " + model.children.size());
		notifyDataSetChanged();
	}

	public void removeItem(RowModel removeItem) {

    	RowModel newRowItems = model;
		Vector<RowModel> dummyRowItems = new Vector<>();

    	DebugLog.d("initial new row items size = " + newRowItems.children.size());
		DebugLog.d("shown size = " + shown.size());

		DebugLog.d("\n\n === start iterating === ");
		for (RowModel navItem : newRowItems.children) { // prefer using newRowItems.children not model.children
														// avoid ConcurrentModificationException while looping over Java ArrayList

			DebugLog.d("nav label : " + navItem.text);
			if (!navItem.text.equalsIgnoreCase(removeItem.text)) {

				if (navItem.children != null && !navItem.children.isEmpty()) {

					Vector<RowModel> newSubNavItems = new Vector<>();
					for (RowModel subNavItem : navItem.children) {

						DebugLog.d("== sub nav label : " + subNavItem.text);
						if (!subNavItem.text.equalsIgnoreCase(removeItem.text)) {

							newSubNavItems.add(subNavItem);

						} else {

							DebugLog.d("--> sub nav excluded");
						}
					}
					navItem.children = newSubNavItems;
				}

				dummyRowItems.add(navItem);

			} else {

				DebugLog.d("-> nav exluded");
			}
		}
		DebugLog.d("\n\n === stop iterating === ");

		newRowItems.children = dummyRowItems;
		DebugLog.d("new row items size : " + newRowItems.children.size());

		setItems(newRowItems);

		// hit delete warga API
		String wargaId = StringUtil.getIdFromLabel(removeItem.text);
		wargaId = StringUtil.getRegisteredWargaId(scheduleId, wargaId);

		APIHelper.deleteWarga(context, new Handler(), wargaId);
	}

	@Override
	public void notifyDataSetChanged() {
		shown = model.getModels();
		DebugLog.d("shown size = "+shown.size());

        int position = 0;
        for (RowModel rowModel : shown) {

        	/*if (rowModel.text.contains(Constants.regexId) && !rowModel.text.matches("\\s*\\([A-Za-z]+\\)")) {

        		rowModel.text = StringUtil.getIdWithName(scheduleId, rowModel.text, rowModel.work_form_group_id);
			}*/
			DebugLog.d("id : " + rowModel.id);
			DebugLog.d("workFormGroupId : " + rowModel.work_form_group_id);
			DebugLog.d("name : " + rowModel.text);
			DebugLog.d("ancestry : " + rowModel.ancestry);
			DebugLog.d("parentId : " + rowModel.parent_id);
            if (rowModel.children != null) {
                DebugLog.d("children size : " + rowModel.children.size());
                int childPosition = 0;
                for (RowModel child : rowModel.children) {
                    DebugLog.d("--- child id : " + child.id);
					DebugLog.d("--- child workFormGroupId : " + child.work_form_group_id);
                    DebugLog.d("--- child name : " + child.text);
                    DebugLog.d("--- child ancestry : " + child.ancestry);
                    DebugLog.d("--- child parentId : " + child.parent_id);

                    childPosition++;
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
				holder.removeSubMenu = view.findViewById(R.id.removesubmenu);
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

			view.setTag(holder);

		} else
			holder = (ViewHolder) view.getTag();

		holder.expandCollapse.setTag(position);
		holder.uploadWorkFormGroup.setTag(position);
		holder.title.setText(getItem(position).text);
		holder.title.setTag(position);

		switch (getItemViewType(position)) {
		case 0:
			holder.uploadWorkFormGroup.setVisibility(View.VISIBLE);

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && workTypeName.matches(Constants.regexIMBASPETIR) && getItem(position).text.equalsIgnoreCase("Warga"))
				holder.uploadWorkFormGroup.setVisibility(View.INVISIBLE);

			holder.expandCollapse.setImageResource(getItem(position).isOpen ? R.drawable.ic_collapse : R.drawable.ic_expand);
			break;
		case 1:
			holder.expandCollapse.setImageResource(getItem(position).isOpen ? R.drawable.ic_collapse_2 : R.drawable.ic_expand_2);

			RowModel rowModel = getItem(position);

			DebugLog.d("view type = 1");
			DebugLog.d("label name = " + rowModel.text);

			holder.removeSubMenu.setVisibility(View.INVISIBLE);

			if (rowModel.text.contains(Constants.regexId)) {

				DebugLog.d("remove submenu visibility is VISIBLE");

				holder.removeSubMenu.setVisibility(View.VISIBLE);
				holder.removeSubMenu.setTag(rowModel);
				holder.removeSubMenu.setOnClickListener(removeSubMenuClickListener);

			}

			break;

		default:
			break;
		}

		return view; 
	}

	OnClickListener expandCollapseListener = v -> {
		int position = (Integer) v.getTag();
		toggleExpand(position);
	};

	OnClickListener uploadWorkFormGroupListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!GlobalVar.getInstance().anyNetwork(context)){
				MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.checkConnection), Toast.LENGTH_SHORT);
			} else {

				if (!ItemUploadManager.getInstance().isRunning()) {

                    int position = (int) v.getTag();

                    RowModel rowModel = getItem(position);

					String scheduleId = getScheduleId();
					int workFormGroupId = rowModel.work_form_group_id;

					DebugLog.d("scheduleId : " + scheduleId);
					DebugLog.d("workFormGroupId : " + workFormGroupId);

                    if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
						new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, Constants.EMPTY, Constants.EMPTY).execute();
                    else
                    	new ItemValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, null, null).execute();

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
				intent.putExtra(Constants.KEY_ROWID, getItem(position).id);
				intent.putExtra(Constants.KEY_WORKFORMGROUPID, getItem(position).work_form_group_id);
				intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
                intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, shown.get(positionAncestry).text);
				intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
				DebugLog.d("----ini others form lho----- "+scheduleId);

			} else if (getItem(position).hasForm){

                DebugLog.d("----schedule id----- "+scheduleId);

                String workFormGroupName = shown.get(positionAncestry).text;
                Intent intent;

                // if the navigation item is "Warga Ke-"
                if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && workTypeName.matches(Constants.regexIMBASPETIR)) {

                    if (getItem(position).text.contains(Constants.regexId)) {

						DebugLog.d("click warga id");
                        int parentId = getItem(position).id;

                        intent = new Intent(context, FormActivityWarga.class);
                        intent.putExtra(Constants.KEY_DATAINDEX, dataIndex);
                        intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
                        intent.putExtra(Constants.KEY_PARENTID, String.valueOf(parentId));
                        intent.putExtra(Constants.KEY_ROWID, getItem(position).id);
                        intent.putExtra(Constants.KEY_WORKFORMGROUPID, String.valueOf(getItem(position).work_form_group_id));
                        intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
                        intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);

                        ArrayList<Warga> wargas = FormImbasPetirConfig.getDataWarga(dataIndex);

                        if (wargas != null && !wargas.isEmpty()) {

                            String wargaIdFromLabel = StringUtil.getIdFromLabel(getItem(position).text);
                            Warga warga = FormImbasPetirConfig.getWarga(wargas, wargaIdFromLabel);
                            String message = "warga is null";

                            if (warga != null) {

                                message = "warga is not null";
                                String wargaId = warga.getWargaid();
                                intent.putExtra(Constants.KEY_WARGAID, wargaId);
                                context.startActivity(intent);
                            }
                            DebugLog.e(message);
                        }

                    } else if (getItem(position).text.contains(Constants.regexBeritaAcaraClosing) ||
                               getItem(position).text.contains(Constants.regexBeritaAcaraPenghancuran)) {

                       proceedApprovalCheckingFirst(scheduleId, workFormGroupName, workTypeName, getItem(position).id, getItem(position).work_form_group_id);

                    } else {

						intent = new Intent(context, FormFillActivity.class);
						intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
						intent.putExtra(Constants.KEY_ROWID, getItem(position).id);
						intent.putExtra(Constants.KEY_WORKFORMGROUPID, getItem(position).work_form_group_id);
						intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
						intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
						context.startActivity(intent);

					}
                } else {

					intent = new Intent(context, FormFillActivity.class);
					intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
					intent.putExtra(Constants.KEY_ROWID, getItem(position).id);
					intent.putExtra(Constants.KEY_WORKFORMGROUPID, getItem(position).work_form_group_id);
					intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
					intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
					context.startActivity(intent);
				}
			} else {

				if (getItem(position).text.contains(Constants.regexTambah)) { // action tambah warga

					((FormActivity) context).showInputAmountWargaDialog(dataIndex);

				} else
					toggleExpand(position);
			}
		}
	};

	OnClickListener removeSubMenuClickListener = v -> {

		RowModel removeRowModel = (RowModel) v.getTag();

		DebugLog.d("remove item with id " + removeRowModel.id + " and label " + removeRowModel.text);

		String scheduleDeleteId = scheduleId;
		String wargaDeleteId = StringUtil.getIdFromLabel(removeRowModel.text);

		boolean isSuccessful  = FormImbasPetirConfig.removeDataWarga(scheduleDeleteId, wargaDeleteId);

		String successfulMessage = "Sukses hapus data wargaid (" + wargaDeleteId + ")";
		String failedMessage	 = "Gagal hapus data wargaid (" + wargaDeleteId + "). Item telah terhapus atau tidak ditemukan";

		if (isSuccessful) {
			removeItem(removeRowModel);
			MyApplication.getInstance().toast(successfulMessage, Toast.LENGTH_LONG);
		} else {
			MyApplication.getInstance().toast(failedMessage, Toast.LENGTH_LONG);
		}
	};

	private void toggleExpand(int position){
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

	private void proceedApprovalCheckingFirst(String scheduleId, String workFormGroupName, String workTypeName, int rowId, int workFormGroupId) {

		DebugLog.d("proceed approval checking ... ");

		CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(context, scheduleId, workFormGroupName, workTypeName, rowId, workFormGroupId);
		APIHelper.getCheckApproval(context, checkApprovalHandler, scheduleId);

		/*if (!FormImbasPetirConfig.isScheduleApproved(scheduleId)) {

			CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(context, scheduleId, workFormGroupName, rowId, workFormGroupId);
			APIHelper.getCheckApproval(context, checkApprovalHandler, scheduleId);

		} else {

			Intent intent = new Intent(context, FormFillActivity.class);
			intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
			intent.putExtra(Constants.KEY_ROWID, rowId);
			intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
			intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
			context.startActivity(intent);
		}*/

	}

	private static class CheckApprovalHandler extends Handler {

	    private Context context;
        private String scheduleId;
        private String workFormGroupName;
        private String workTypeName;
        private int rowId;
        private int workFormGroupId;

	    public CheckApprovalHandler(Context context, String scheduleId, String workFormGroupName, String workTypeName, int rowId, int workFormGroupId) {
	        this.context = context;
	        this.scheduleId = scheduleId;
	        this.workFormGroupName = workFormGroupName;
	        this.workTypeName = workTypeName;
	        this.rowId = rowId;
	        this.workFormGroupId = workFormGroupId;
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");
			Gson gson = new Gson();

            if (isResponseOK) {

                if (bundle.getString("json") != null){

					CheckApprovalResponseModel checkApprovalResponseModel = gson.fromJson(bundle.getString("json"), CheckApprovalResponseModel.class);
					checkApprovalResponseModel.toString();

					if (!checkApprovalResponseModel.messages.equalsIgnoreCase("failed")) {

						DebugLog.d("check approval success");

						FormImbasPetirConfig.setScheduleApproval(scheduleId, true);

						Intent intent = new Intent(context, FormFillActivity.class);
						intent.putExtra(Constants.KEY_SCHEDULEID, scheduleId);
						intent.putExtra(Constants.KEY_ROWID, rowId);
						intent.putExtra(Constants.KEY_WORKFORMGROUPID, workFormGroupId);
						intent.putExtra(Constants.KEY_WORKFORMGROUPNAME, workFormGroupName);
						intent.putExtra(Constants.KEY_WORKTYPENAME, workTypeName);
						context.startActivity(intent);
						return;
					}

					DebugLog.d("belum ada approval dari STP");
					MyApplication.getInstance().toast("Schedule menunggu approval dari STP", Toast.LENGTH_LONG);

                } else {

                    MyApplication.getInstance().toast("Gagal mengecek approval. Response json = null", Toast.LENGTH_LONG);

                }
            } else {

            	MyApplication.getInstance().toast("Gagal mengecek approval. Response not OK dari server", Toast.LENGTH_LONG);
                DebugLog.d("response not ok");
            }
        }
    }

	private class ViewHolder {
		ImageView expandCollapse;
		ImageView uploadWorkFormGroup;
		ImageView removeSubMenu;
		TextView title;
	}
}
