package com.sap.inspection.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.view.ui.BaseActivity;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.view.ui.GroupActivity;
import com.sap.inspection.view.ui.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.config.formimbaspetir.Warga;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.responsemodel.BaseResponseModel;
import com.sap.inspection.model.responsemodel.CheckApprovalResponseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.dialog.DeleteWargaAndBarangDialog;
import com.sap.inspection.util.StringUtil;

import java.util.ArrayList;
import java.util.Vector;

public class GroupsAdapter extends MyBaseAdapter {

	private Context context;
	private WorkFormRowModel groupItems;
	private Vector<WorkFormRowModel> shown;
	private String scheduleId;
	private String workTypeName;
    private int positionAncestry;
	private int dataIndex = -1;

	public GroupsAdapter(Context context) {
		this.context = context;
		if (null == groupItems)
			groupItems = new WorkFormRowModel();
		if (null == shown)
			shown = new Vector<WorkFormRowModel>();
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

	public void setItems(WorkFormRowModel groupItems){
		this.groupItems = groupItems;
		DebugLog.d("groupItems size : " + groupItems.children.size());
		notifyDataSetChanged();
	}

	public void removeItem(WorkFormRowModel removeItem) {

    	WorkFormRowModel newGroupItems = groupItems;
		Vector<WorkFormRowModel> dummyGroupItems = new Vector<>();

    	DebugLog.d("initial new row items size = " + newGroupItems.children.size());
		DebugLog.d("shown size = " + shown.size());

		DebugLog.d("\n\n === start iterating === ");
		for (WorkFormRowModel groupItem : newGroupItems.children) { // prefer using newGroupItems.children not groupItems.children
														// avoid ConcurrentModificationException while looping over Java ArrayList

			DebugLog.d("nav label : " + groupItem.text);
			if (!groupItem.text.equalsIgnoreCase(removeItem.text)) {

				if (groupItem.children != null && !groupItem.children.isEmpty()) {

					Vector<WorkFormRowModel> subGroupItems = new Vector<>();
					for (WorkFormRowModel subGroupItem : groupItem.children) {

						DebugLog.d("== sub nav label : " + subGroupItem.text);
						if (!subGroupItem.text.equalsIgnoreCase(removeItem.text)) {

							subGroupItems.add(subGroupItem);

						} else {

							DebugLog.d("--> sub nav excluded");
						}
					}
					groupItem.children = subGroupItems;
				}

				dummyGroupItems.add(groupItem);

			} else {

				DebugLog.d("-> nav exluded");
			}
		}
		DebugLog.d("\n\n === stop iterating === ");

		newGroupItems.children = dummyGroupItems;
		DebugLog.d("new row items size : " + newGroupItems.children.size());

		setItems(newGroupItems);
	}

	@Override
	public void notifyDataSetChanged() {
		shown = groupItems.getModels();
		DebugLog.d("shown size = "+shown.size());

        int position = 0;
        for (WorkFormRowModel groupItem : shown) {

			DebugLog.d("id : " + groupItem.id);
			DebugLog.d("workFormGroupId : " + groupItem.work_form_group_id);
			DebugLog.d("name : " + groupItem.text);
			DebugLog.d("ancestry : " + groupItem.ancestry);
			DebugLog.d("parentId : " + groupItem.parent_id);
            if (groupItem.children != null) {
                DebugLog.d("children size : " + groupItem.children.size());
                int childPosition = 0;
                for (WorkFormRowModel child : groupItem.children) {
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
	public WorkFormRowModel getItem(int position) {
		return shown.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		WorkFormRowModel groupItem = getItem(position);
		DebugLog.d(groupItem.ancestry+"/"+groupItem.id+" | "+groupItem.text);

		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case 0:
				view = LayoutInflater.from(context).inflate(R.layout.item_navigation_1,null);
				break;
			case 1:
				view = LayoutInflater.from(context).inflate(R.layout.item_navigation_2,null);
				holder.removeGroup = view.findViewById(R.id.removesubmenu);
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
		holder.title.setText(groupItem.text);
		holder.title.setTag(position);

		switch (getItemViewType(position)) {
		case 0:
			holder.uploadWorkFormGroup.setVisibility(View.VISIBLE);

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && workTypeName.matches(Constants.regexIMBASPETIR) && groupItem.text.equalsIgnoreCase("Warga"))
				holder.uploadWorkFormGroup.setVisibility(View.INVISIBLE);

			holder.expandCollapse.setImageResource(groupItem.isOpen ? R.drawable.ic_collapse : R.drawable.ic_expand);
			break;
		case 1:
			holder.expandCollapse.setImageResource(groupItem.isOpen ? R.drawable.ic_collapse_2 : R.drawable.ic_expand_2);

			holder.removeGroup.setVisibility(View.INVISIBLE);

			if (groupItem.text.contains(Constants.regexId)) {

				DebugLog.d("remove submenu visibility is VISIBLE");
				holder.removeGroup.setVisibility(View.VISIBLE);
				holder.removeGroup.setTag(groupItem);
				holder.removeGroup.setOnClickListener(removeSubGroupItemsClickListener);

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
				MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.failed_nointernetconnection), Toast.LENGTH_SHORT);
			} else {

				if (!ItemUploadManager.getInstance().isRunning()) {

                    int position = (int) v.getTag();

                    WorkFormRowModel groupItem = getItem(position);

					String scheduleId = getScheduleId();
					int workFormGroupId = groupItem.work_form_group_id;

					DebugLog.d("scheduleId : " + scheduleId);
					DebugLog.d("workFormGroupId : " + workFormGroupId);

                    if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
						new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, Constants.EMPTY, Constants.EMPTY).execute();
                    else
                    	new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, null, null).execute();

				} else {
					MyApplication.getInstance().toast(MyApplication.getContext().getResources().getString(R.string.uploadProses), Toast.LENGTH_SHORT);
				}
			}

		}
	};

	OnClickListener ItemClickListener = v -> {

		int position = (Integer) v.getTag();
		itemClickAction(position);
	};

	OnClickListener removeSubGroupItemsClickListener = v -> {

		WorkFormRowModel removedGroupRow = (WorkFormRowModel) v.getTag();
		showConfirmDeleteWargaDialog(removedGroupRow);

	};

	private void itemClickAction(int position) {

		WorkFormRowModel groupItem = getItem(position);

		if (groupItem.id == 0) {
			positionAncestry = position;
			DebugLog.d("positionAncestry : " + positionAncestry);
		}

		if (groupItem.text.equalsIgnoreCase("others form")){

			DebugLog.d("----ini others form lho----- "+scheduleId);
			BaseActivity.navigateToFormFillActivity(
					context,
					scheduleId,
					groupItem.id,
					groupItem.work_form_group_id,
					shown.get(positionAncestry).text,
					workTypeName);

		} else if (groupItem.hasForm){

			DebugLog.d("----schedule id----- "+scheduleId);
			WorkFormGroupModel workFormGroup = WorkFormGroupModel.getWorkFormGroupById(String.valueOf(shown.get(position).work_form_group_id));
			String workFormGroupName = workFormGroup.name;

			// if the navigation item is "Warga Ke-"
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && workTypeName.matches(Constants.regexIMBASPETIR)) {

				if (groupItem.text.contains(Constants.regexId)) {

					DebugLog.d("click warga id");
					int parentId = groupItem.id;

					ArrayList<Warga> wargas = FormImbasPetirConfig.getDataWarga(dataIndex);

					if (wargas != null && !wargas.isEmpty()) {

						String wargaIdFromLabel = StringUtil.getIdFromLabel(groupItem.text);
						Warga warga = FormImbasPetirConfig.getWarga(wargas, wargaIdFromLabel);
						String message = "warga is null";

						if (warga != null) {

							message = "warga is not null";
							String wargaId = warga.getWargaid();

							BaseActivity.navigateToGroupWargaActivity(
									context,
									dataIndex,
									scheduleId,
									String.valueOf(parentId),
									groupItem.id,
									String.valueOf(groupItem.work_form_group_id),
									workFormGroupName,
									workTypeName,
									wargaId
							);
						}
						DebugLog.e(message);
					}

				} else if (groupItem.text.contains(Constants.regexBeritaAcaraClosing) ||
						groupItem.text.contains(Constants.regexBeritaAcaraPenghancuran)) {

					proceedApprovalCheckingFirst(
							scheduleId,
							workFormGroupName,
							workTypeName,
							groupItem.id,
							groupItem.work_form_group_id);

				} else {

					BaseActivity.navigateToFormFillActivity(
							context,
							scheduleId,
							groupItem.id,
							groupItem.work_form_group_id,
							workFormGroupName,
							workTypeName);
				}
			} else {

				BaseActivity.navigateToFormFillActivity(
						context,
						scheduleId,
						groupItem.id,
						groupItem.work_form_group_id,
						workFormGroupName,
						workTypeName);

			}
		} else {

			if (groupItem.text.contains(Constants.regexTambah)) { // action tambah warga

				((GroupActivity) context).showInputAmountWargaDialog(dataIndex);

			} else
				toggleExpand(position);
		}
	}

	@SuppressLint("HandlerLeak")
	private void showConfirmDeleteWargaDialog(WorkFormRowModel removedWargaItem) {

		DebugLog.d("remove item with id " + removedWargaItem.id + " and label " + removedWargaItem.text);

		DeleteWargaAndBarangDialog deleteWargaDialog = new DeleteWargaAndBarangDialog(context, removedWargaItem);
		deleteWargaDialog.setOnPositiveClickListener(removedRowItem -> {

			String scheduleDeleteId = scheduleId;
			String wargaDeleteId = StringUtil.getIdFromLabel(removedWargaItem.text);
			String realWargaDeletedid = StringUtil.getRegisteredWargaId(scheduleDeleteId, wargaDeleteId);

			APIHelper.deleteWarga(context, new Handler() {
				@Override
				public void handleMessage(Message msg) {

					String successfulMessage = "Sukses hapus data wargaid (" + wargaDeleteId + ")";
					String failedMessage	 = "Gagal hapus data wargaid (" + wargaDeleteId + "). Item telah terhapus atau tidak ditemukan";

					Bundle bundle = msg.getData();
					Gson gson = new Gson();

					BaseResponseModel responseDeleteWargaModel = gson.fromJson(bundle.getString("json"), BaseResponseModel.class);

					boolean isResponseOK = bundle.getBoolean("isresponseok");

					if (isResponseOK) {

						boolean isSuccessful  = FormImbasPetirConfig.removeDataWarga(scheduleDeleteId, wargaDeleteId);

						if (isSuccessful) {

							FormValueModel.deleteAllBy(scheduleId, realWargaDeletedid, Constants.EMPTY);

							removeItem(removedWargaItem);
							DebugLog.d("remove wargaid berhasil dengan message : " + responseDeleteWargaModel.messages);
							MyApplication.getInstance().toast(successfulMessage, Toast.LENGTH_LONG);
						} else {
							MyApplication.getInstance().toast(failedMessage, Toast.LENGTH_LONG);
						}
					} else {

						MyApplication.getInstance().toast(failedMessage + ". Repsonse not OK from server", Toast.LENGTH_LONG);
						DebugLog.e("response not ok");
					}
				}
			}, realWargaDeletedid);
		});

		deleteWargaDialog.show();
	}

	private void toggleExpand(int position){
		WorkFormRowModel groupItem = getItem(position);
		if (groupItem.isOpen){
			groupItem.isOpen = false;
			DebugLog.d("closed");
		}
		else if (groupItem.children != null && groupItem.children.size() > 0){
			groupItem.isOpen = true;
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

						BaseActivity.navigateToFormFillActivity(
								context,
								scheduleId,
								rowId,
								workFormGroupId,
								workFormGroupName,
								workTypeName);

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
		ImageView removeGroup;
		TextView title;
	}
}
