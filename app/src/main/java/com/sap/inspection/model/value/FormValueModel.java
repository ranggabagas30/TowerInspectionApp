package com.sap.inspection.model.value;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.view.ui.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.greenrobot.event.EventBus;

import static com.crashlytics.android.Crashlytics.log;

//import static com.sap.inspection.model.value.DbManagerValue.material_request;

//import static com.sap.inspection.model.value.DbManagerValue.material_request;


public class FormValueModel extends BaseModel {
	public static final int UPLOAD_NONE = 0;
	public static final int UPLOAD_ONGOING = 1;
	public static final int UPLOAD_DONE = 2;
	public static final int UPLOAD_FAIL = 3;

	public static final int UNSPECIFIED = -1;

	public String scheduleId;
	public int operatorId;
	public int itemId;
	public int siteId;
	public int work_form_group_id;
	public String photoDate;
	public String createdAt;
	public String value;
	public int rowId;
	public int gpsAccuracy;
	public String remark;
	public String latitude;
	public String longitude;
	public String photoStatus;
	public int uploadStatus = UPLOAD_NONE;
	public boolean typePhoto;
	public String picture;
	public boolean disable;

	// STP only
	public String wargaId;
	public String barangId;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public static void deleteAll(){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();

		MyApplication.getInstance().toast("Sukses delete seluruh data", Toast.LENGTH_SHORT);
	}

	public static void deleteAllBy(String scheduleId) {

		deleteAllBy(scheduleId, null, null);

		MyApplication.getInstance().toast("Sukses delete seluruh data schedule " + scheduleId, Toast.LENGTH_SHORT);
	}

	public static void deleteAllBy(String scheduleId, String wargaId, String barangId) {

		delete(scheduleId, UNSPECIFIED, UNSPECIFIED, wargaId, barangId);

	}

	public static void deleteAllBy(String scheduleId, int itemId) {

		deleteAllBy(scheduleId, itemId, null, null);

	}

	public static void deleteAllBy(String scheduleId, int itemId, String wargaId, String barangId) {

		delete(scheduleId, itemId, UNSPECIFIED, wargaId, barangId);

	}

	public static void delete(String scheduleId, int itemId, int operatorId){

		delete(scheduleId, itemId, operatorId, null, null);

	}

	public static void delete(String scheduleId, int itemId, int operatorId, String wargaId, String barangId) {

		String whereScheduleId = scheduleId != null ? DbManagerValue.colScheduleId + "=" + scheduleId : "";
		String whereItemId = itemId != UNSPECIFIED ? " AND " + DbManagerValue.colItemId + "=" + itemId : "";				// if itemid is unspecified
		String whereOperatorId = operatorId != UNSPECIFIED ? " AND " + DbManagerValue.colOperatorId + "=" + operatorId : ""; // if operatorid is unspecified
		String whereWarga = StringUtil.isNotNullAndNotEmpty(wargaId) ? " AND " + DbManagerValue.colWargaId + "= '" + wargaId + "'" : "";
		String whereBarang = StringUtil.isNotNullAndNotEmpty(barangId)? " AND " + DbManagerValue.colBarangId + "= '" + barangId + "'" : "";

		DebugLog.d("delete item(s) with scheduleid = " + scheduleId + whereItemId + whereOperatorId + whereWarga + whereBarang);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManagerValue.mFormValue + " WHERE " + whereScheduleId + whereItemId + whereOperatorId + whereWarga + whereBarang;
		SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepositoryValue.getInstance().close();
	}

	public static ArrayList<FormValueModel> getAllItemValueByScheduleId (String scheduleId) {

		ArrayList<FormValueModel> listModel = null;
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = DbManagerValue.colScheduleId+"=?";
		String[] args = new String[]{scheduleId};
		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(false, table, columns, where, args, null, null,null, null);

		if (cursor.moveToFirst()) {
			do {
				FormValueModel model;
				model = getSiteFromCursor(cursor);
				listModel.add(model);
			} while (cursor.moveToNext());
		}

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return listModel;
	}

	public static int countTaskDone(String scheduleId, int rowId){

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor mCount= DbRepositoryValue.getInstance().getDB().rawQuery("select count(*) from "+DbManagerValue.mFormValue+" where "+DbManagerValue.colScheduleId+"='" + scheduleId + "' and "+DbManagerValue.colRowId+"='" + rowId +"'", null);
		mCount.moveToFirst();
		int count= mCount.getInt(0);
		mCount.close();
		DbRepositoryValue.getInstance().close();
		return count;
	}

	public static FormValueModel getItemValue(String scheduleId, int itemId, int operatorId) {

		return getItemValue(scheduleId, itemId, operatorId, null, null);

	}
	public static FormValueModel getItemValue(String scheduleId, int itemId, int operatorId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = DbManagerValue.colScheduleId + "=?";
		String whereitemid   	= itemId != UNSPECIFIED ? " AND " + DbManagerValue.colItemId + "=?" : "";
		String whereoperatorid  = operatorId != UNSPECIFIED ? " AND " + DbManagerValue.colOperatorId + "=?" : "";
		String wherewargaid  	= StringUtil.isNotNullAndNotEmpty(wargaId) ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= StringUtil.isNotNullAndNotEmpty(barangId) ? " AND " + DbManagerValue.colBarangId + "=?" : "";

		String where = wherescheduleid + whereitemid + whereoperatorid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + "," + itemId + "," + operatorId + "," + wargaId + "," + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (itemId != UNSPECIFIED)
			argsList.add(String.valueOf(itemId));
		if (operatorId != UNSPECIFIED)
			argsList.add(String.valueOf(operatorId));
		if (StringUtil.isNotNullAndNotEmpty(wargaId))
			argsList.add(wargaId);
		if (StringUtil.isNotNullAndNotEmpty(barangId))
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

        if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		FormValueModel model = getSiteFromCursor(cursor);

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static ArrayList<FormValueModel> getItemValues(String scheduleId, int itemId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = DbManagerValue.colScheduleId + "=?";
		String whereitemid   	= itemId != UNSPECIFIED ? " AND " + DbManagerValue.colItemId + "=?" : "";
		String wherewargaid  	= StringUtil.isNotNullAndNotEmpty(wargaId) ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= StringUtil.isNotNullAndNotEmpty(barangId) ? " AND " + DbManagerValue.colBarangId + "=?" : "";
		String order 			= DbManagerValue.colItemId + " ASC";

		String where = wherescheduleid + whereitemid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + "," + itemId + "," + wargaId + "," + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (itemId != UNSPECIFIED)
			argsList.add(String.valueOf(itemId));
		if (StringUtil.isNotNullAndNotEmpty(wargaId))
			argsList.add(wargaId);
		if (StringUtil.isNotNullAndNotEmpty(barangId))
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null, order, null);

        if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		ArrayList<FormValueModel> results = new ArrayList<>();

		do {

			FormValueModel model = getSiteFromCursor(cursor);
			results.add(model);

		} while (cursor.moveToNext());

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return results;
	}

	public static FormValueModel getItemValue(String scheduleId, int rowId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = DbManagerValue.colScheduleId + "=?";
		String whererowid   	= rowId != UNSPECIFIED ? " AND " + DbManagerValue.colRowId + "=?" : "";
		String wherewargaid  	= StringUtil.isNotNullAndNotEmpty(wargaId) ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= StringUtil.isNotNullAndNotEmpty(barangId) ? " AND " + DbManagerValue.colBarangId + "=?" : "";

		String where = wherescheduleid + whererowid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + "," + rowId + "," + wargaId + "," + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (rowId != UNSPECIFIED)
			argsList.add(String.valueOf(rowId));
		if (StringUtil.isNotNullAndNotEmpty(wargaId))
			argsList.add(wargaId);
		if (StringUtil.isNotNullAndNotEmpty(barangId))
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		Cursor cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

        if (!cursor.moveToFirst()) {

			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		FormValueModel model = getSiteFromCursor(cursor);

		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}

	public static ArrayList<FormValueModel> getItemValuesForUpload() {


		ArrayList<FormValueModel> model = new ArrayList<FormValueModel>();
		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String where = null;
		String[] args = null;

		if (ItemUploadManager.getInstance().isRunning()){
			where =DbManagerValue.colUploadStatus+"!=? AND "+DbManagerValue.colUploadStatus+"!=?";
			args = new String[]{String.valueOf(UPLOAD_ONGOING),String.valueOf(UPLOAD_DONE)};
		}else{
			where =DbManagerValue.colUploadStatus+"!=?";
			args = new String[]{String.valueOf(UPLOAD_DONE)};
		}

		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return model;
		}
		do{
			model.add(getSiteFromCursor(cursor));
		}
		while(cursor.moveToNext());
		cursor.close();
		DbRepositoryValue.getInstance().close();
		return model;
	}


	public static ArrayList<FormValueModel> getItemValuesForUpload(String scheduleId, String wargaId, String barangId) {

		String table = DbManagerValue.mFormValue;
		String[] columns = null;
		String wherescheduleid  = scheduleId != null ? DbManagerValue.colScheduleId + "=?" : "";
		String wherewargaid  	= StringUtil.isNotNullAndNotEmpty(wargaId) ? " AND " + DbManagerValue.colWargaId + "=?" : "";
		String wherebarangid 	= StringUtil.isNotNullAndNotEmpty(barangId) ? " AND " + DbManagerValue.colBarangId + "=?" : "";

		String where = wherescheduleid + wherewargaid + wherebarangid;
		DebugLog.d("Get item(s) by (" + scheduleId + ", " + wargaId + ", " + barangId +")");

		List<String> argsList = new ArrayList<>();

		if (scheduleId != null)
			argsList.add(scheduleId);
		if (StringUtil.isNotNullAndNotEmpty(wargaId))
			argsList.add(wargaId);
		if (StringUtil.isNotNullAndNotEmpty(barangId))
			argsList.add(barangId);

		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);

		Cursor cursor;

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		cursor = DbRepositoryValue.getInstance().getDB().query(true, table, columns, where, args, null, null,null, null);

		if (!cursor.moveToFirst()){
			cursor.close();
			DbRepositoryValue.getInstance().close();
			return null;
		}

		ArrayList<FormValueModel> model = new ArrayList<>();

		do{
			model.add(getSiteFromCursor(cursor));
		}
		while(cursor.moveToNext());

		cursor.close();
		DbRepositoryValue.getInstance().close();

		return model;
	}

	public static ArrayList<FormValueModel> getItemValuesForUploadWithMandatoryCheck(String workTypeName, String scheduleId, Vector<WorkFormGroupModel> groupModels) {

		return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, groupModels, null, null);
    }

	public static ArrayList<FormValueModel> getItemValuesForUploadWithMandatoryCheck(String workTypeName, String scheduleId, Vector<WorkFormGroupModel> groupModels, String wargaId, String barangId) {

		DebugLog.d("upload items by scheduleId = " + scheduleId);
		ArrayList<FormValueModel> uploadItems = new ArrayList<>();

		// obtain all items from each group
		for (WorkFormGroupModel perGroup : groupModels) {

			ArrayList<FormValueModel> uploadItemsPerGroup = getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, perGroup.id, wargaId, barangId);
			if (uploadItemsPerGroup == null)
				return null;

			uploadItems.addAll(uploadItemsPerGroup);

		}

		return uploadItems;
	}

	public static ArrayList<FormValueModel> getItemValuesForUploadWithMandatoryCheck(String workTypeName, String scheduleId, int work_form_group_id, String wargaId, String barangId) {
		return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, -1, work_form_group_id, wargaId, barangId);
	}

	public static ArrayList<FormValueModel> getItemValuesForUploadWithMandatoryCheck(String workTypeName, String scheduleId, int itemId, int work_form_group_id, String wargaId, String barangId) {

		DebugLog.d("upload items by scheduleid = " + scheduleId + " workFormGroupId = " + work_form_group_id);
		ArrayList<FormValueModel> results = new ArrayList<>();

		ArrayList<WorkFormItemModel> workFormItems = new ArrayList<>();

		if (itemId == FormValueModel.UNSPECIFIED) {
			workFormItems = WorkFormItemModel.getWorkFormItems(work_form_group_id, "label");
		} else {
			WorkFormItemModel workFormItem = WorkFormItemModel.getWorkFormItemById(itemId, work_form_group_id);
			workFormItems.add(workFormItem);
		}

		if (workFormItems != null) {

			for (WorkFormItemModel workFormItem : workFormItems) {

				boolean isMandatory = workFormItem.mandatory;
				boolean isDisabled  = workFormItem.disable;

				if (!isDisabled) {

					if (workFormItem.scope_type.equalsIgnoreCase("all")) {

						ArrayList<FormValueModel> itemValues = getItemValues(scheduleId, workFormItem.id, wargaId, barangId);

						if (itemValues == null && isMandatory) {

							// mandatory item is not filled
							DebugLog.e("Item " + workFormItem.label + " kosong, cancel upload");
							return null;

						} else if (itemValues != null) { // there are some filled items

							if (!isItemValueValidated(workFormItem, itemValues.get(0), workTypeName))
								return null;

							// otherwise just add all the items
							DebugLog.d("-> added");
							results.addAll(itemValues);
						}

					} else {

						ScheduleBaseModel schedule = new ScheduleGeneral();
						schedule = schedule.getScheduleById(scheduleId);

						for (OperatorModel operator : schedule.operators) {

							int operatorid = operator.id;
							FormValueModel itemValue = getItemValue(scheduleId, workFormItem.id, operatorid, wargaId, barangId);

							if (workFormItem.mandatory) {

								if (!isItemValueValidated(workFormItem, itemValue, workTypeName))
									return null;

								results.add(itemValue);

							} else {

								if (itemValue != null)
									results.add(itemValue);

							}
						}
					}
				}
			}

		} else {
			DebugLog.e("workitems is null");
			return null;
		}

		return results;
	}

	public static class AsyncCollectItemValuesForUpload extends AsyncTask<Void, String, ArrayList<FormValueModel>> {

		private final String DONEPREPARINGITEMS = "DONEPREPARINGITEMS";
		private String scheduleId;
		private String workTypeName;
		private String wargaId;
		private String barangId;
		private int workFormGroupId;
		private int itemId;

		private ScheduleBaseModel scheduleBaseModel;
		private WorkFormModel workFormModel;
		private Vector<WorkFormGroupModel> workFormGroupModels;

		public AsyncCollectItemValuesForUpload(String scheduleId, int workFormGroupId, String wargaId, String barangId) {
			this(scheduleId, workFormGroupId, -1, wargaId, barangId);
		}

		public AsyncCollectItemValuesForUpload(String scheduleId, int workFormGroupId, int itemId, String wargaId, String barangId) {
			this.scheduleId = scheduleId;
			this.workFormGroupId = workFormGroupId;
			this.itemId = itemId;
			this.wargaId = wargaId;
			this.barangId = barangId;
		}

		private void publish(String msg) {
			publishProgress(msg);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			publish("Menyiapkan item untuk diupload");

			scheduleBaseModel = new ScheduleGeneral();
			scheduleBaseModel = scheduleBaseModel.getScheduleById(scheduleId);
			this.workTypeName = scheduleBaseModel.work_type.name;

			DebugLog.d("scheduleBase id : " + scheduleBaseModel.id);
			DebugLog.d("scheduleBase worktype id : " + scheduleBaseModel.work_type.id);

			if (workFormGroupId == UNSPECIFIED) {

				workFormModel = new WorkFormModel();
				workFormModel = workFormModel.getItemByWorkTypeId(scheduleBaseModel.work_type.id);

				workFormGroupModels = WorkFormGroupModel.getAllItemByWorkFormId(workFormModel.id);
			}
		}

		@Override
		protected ArrayList<FormValueModel> doInBackground(Void... voids) {

		    if (workFormGroupId == UNSPECIFIED) {

		    	if (!workFormGroupModels.isEmpty()) {

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

						DebugLog.d("SAP upload by (" + scheduleId + ",  UNSPECIFIED, " + wargaId + ", " + barangId + ")");
						return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, workFormGroupModels, wargaId, wargaId);
					} else {

						DebugLog.d("STP upload by (" + scheduleId + ",  UNSPECIFIED)");
						return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, workFormGroupModels);
					}
				} else {

					Crashlytics.log(Log.ERROR, FormValueModel.class.getSimpleName(), "Schedule " + scheduleId + " tidak memiliki daftar workformgroup");
					MyApplication.getInstance().toast("Schedule " + scheduleId + " tidak memiliki daftar workformgroup", Toast.LENGTH_SHORT);

					return new ArrayList<>();
				}

			} else {

		    	if (itemId == UNSPECIFIED) {

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

						DebugLog.d("SAP upload by (" + scheduleId + ", " + workFormGroupId + ", " + wargaId + ", " + barangId + ")");
						return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, workFormGroupId, wargaId, barangId);
					} else {

						DebugLog.d("STP upload by (" + scheduleId + ", " + workFormGroupId + ")");
						return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, workFormGroupId, null, null);
					}

				} else {

		    		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
		    			return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, itemId, workFormGroupId, wargaId, barangId);
					} else {
		    			return getItemValuesForUploadWithMandatoryCheck(workTypeName, scheduleId, itemId, workFormGroupId, null, null);
					}
				}
			}

		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			UploadProgressEvent event = new UploadProgressEvent(values[0]);
			event.done = values[0].equalsIgnoreCase(DONEPREPARINGITEMS);
			EventBus.getDefault().post(event);
		}

		@Override
		protected void onPostExecute(ArrayList<FormValueModel> uploadItems) {
			super.onPostExecute(uploadItems);
			publish(DONEPREPARINGITEMS);
			if (uploadItems == null)
				MyApplication.getInstance().toast(MyApplication.getContext().getString(R.string.failed_upload_mandatory_items), Toast.LENGTH_LONG);
			else
				ItemUploadManager.getInstance().addItemValues(uploadItems);
		}
	}

	public static boolean isItemValueValidated(WorkFormItemModel workFormItem, FormValueModel filledItem) {
	    return isItemValueValidated(workFormItem, filledItem, null);
    }

    public static boolean isItemValueValidated(WorkFormItemModel workFormItem, FormValueModel filledItem, String workTypeName) {

        if (!workFormItem.disable) {

            if (filledItem == null && workFormItem.mandatory) {

                // mandatory item is not filled
                DebugLog.d("Mandatory item ada yang kosong");
                MyApplication.getInstance().toast("Mandatory item ada yang kosong", Toast.LENGTH_SHORT);
                return false;

            } else if (filledItem != null) {

                // if the filled items are mandatory, then apply strict rules
                if (workFormItem.mandatory) {

                    // if the value is empty
                    if (TextUtils.isEmpty(filledItem.value)) {

                        // if the item type is file "photoitem"
                        if (workFormItem.field_type.equalsIgnoreCase("file")) {

                            // and photo status is not null or empty and photo status not equal "NA"
                            if (!TextUtils.isEmpty(filledItem.photoStatus) && !filledItem.photoStatus.equalsIgnoreCase(Constants.NA)) {
                                DebugLog.e("Photo item" + workFormItem.label + " harus ada");
                                MyApplication.getInstance().toast("Photo item" + workFormItem.label + " harus ada", Toast.LENGTH_LONG);
                                return false;
                            }

                        } else {

                            DebugLog.d("Mandatory item ada yang kosong");
                            MyApplication.getInstance().toast("Mandatory item ada yang kosong", Toast.LENGTH_SHORT);
                            return false; // and not photo item radio, then return null
                        }

                    }

                }

                return !workFormItem.field_type.equalsIgnoreCase("file") || FormValueModel.isPictureRadioItemValidated(workFormItem, filledItem, workTypeName);
            }
        }

        return true;
    }

    public static boolean isPictureRadioItemValidated(WorkFormItemModel workFormItem, FormValueModel filledItem) {
        return isPictureRadioItemValidated(workFormItem, filledItem, null);
    }

    public static boolean isPictureRadioItemValidated(WorkFormItemModel workFormItem, FormValueModel filledItem, String workTypeName) {

        // checking for form's item type picture radio with mandatory applied only on "NOK" option
        if (TextUtils.isEmpty(filledItem.photoStatus)) {
            DebugLog.e("Photo status item " + workFormItem.label + " harus diisi");
            MyApplication.getInstance().toast("Photo status item " + workFormItem.label + " harus diisi", Toast.LENGTH_LONG);
            return false;
        } else {
            if (TextUtils.isEmpty(filledItem.remark)) {

                boolean isValidated = true;

                if (filledItem.photoStatus.equalsIgnoreCase(Constants.NOK))
                    isValidated = false;

                if (BuildConfig.FLAVOR.equalsIgnoreCase("sap")) {
                    if (!TextUtils.isEmpty(workTypeName)) {
                        boolean isRoutingSchedule = workTypeName.equalsIgnoreCase(MyApplication.getInstance().getString(R.string.routing_segment)) ||
                                                    workTypeName.equalsIgnoreCase(MyApplication.getInstance().getString(R.string.handhole)) ||
                                                    workTypeName.equalsIgnoreCase(MyApplication.getInstance().getString(R.string.hdpe));

                        if (isRoutingSchedule) {
                            if (filledItem.photoStatus.equalsIgnoreCase(Constants.NOK) || filledItem.photoStatus.equalsIgnoreCase(Constants.OK))
                                isValidated = false;
                        }
                    }
                }

                if (!isValidated) {
                    DebugLog.e("Remark item " + workFormItem.label + " harus diisi");
                    MyApplication.getInstance().toast("Remark item " + workFormItem.label + " harus diisi", Toast.LENGTH_LONG);
                }

                return isValidated;
            }
        }

        return true;
    }

	public void save(Context context){

		save();
	}

	public void save(){

		log("saving value on itemvaluemodel");
		log("row id : "+  rowId);
		log("schedule Id : "+  scheduleId);
		log("operator id : "+  operatorId);
		log("item id : " + itemId);
		log("warga id : " + wargaId);
		log("barang id : " + barangId);
		log("------------------------------------");
		save(scheduleId, photoStatus);
	}

	public void saveForCorrective(){
		save(scheduleId+"-"+photoStatus, photoStatus);
	}

	public void save(String scheduleId, String photoStatus){

		String sql = null;
		switch (DbManagerValue.schema_version) {
			case 1 : {
				break;
			}
			case 2 : {
				break;
			}
			case 3 : {
				break;
			}
			case 4 : {
				break;
			}
			case 5 : {
				break;
			}
			case 6 : {
				break;
			}
			case 7 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt);

				DbRepositoryValue.getInstance().open(MyApplication.getInstance());

				SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

				bindAndCheckNullString(stmt, 1, scheduleId);
				stmt.bindLong(2, itemId);
				bindAndCheckNullString(stmt, 3, value);
				bindBooleanToInteger(stmt, 4, typePhoto);
				stmt.bindLong(5, operatorId);
				stmt.bindLong(6, rowId);
				bindAndCheckNullString(stmt, 7, remark);
				bindAndCheckNullString(stmt, 8, latitude);
				bindAndCheckNullString(stmt, 9, longitude);
				bindAndCheckNullString(stmt, 10, photoStatus);
				stmt.bindLong(11, gpsAccuracy);
				stmt.bindLong(12, uploadStatus);
				bindAndCheckNullString(stmt, 13, getCurrentDate());
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();

				break;
			}
			case 8 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt,
						DbManagerValue.colPhotoDate);

				DbRepositoryValue.getInstance().open(MyApplication.getInstance());

				SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

				bindAndCheckNullString(stmt, 1, scheduleId);
				stmt.bindLong(2, itemId);
				bindAndCheckNullString(stmt, 3, value);
				bindBooleanToInteger(stmt, 4, typePhoto);
				stmt.bindLong(5, operatorId);
				stmt.bindLong(6, rowId);
				bindAndCheckNullString(stmt, 7, remark);
				bindAndCheckNullString(stmt, 8, latitude);
				bindAndCheckNullString(stmt, 9, longitude);
				bindAndCheckNullString(stmt, 10, photoStatus);
				stmt.bindLong(11, gpsAccuracy);
				stmt.bindLong(12, uploadStatus);
				bindAndCheckNullString(stmt, 13, getCurrentDate());
				bindAndCheckNullString(stmt, 14, photoDate);
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();

				break;
			}
			case 9 : {
				sql = String.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						DbManagerValue.mFormValue , DbManagerValue.colScheduleId,
						DbManagerValue.colItemId,DbManagerValue.colValue,
						DbManagerValue.colIsPhoto,DbManagerValue.colOperatorId,
						DbManagerValue.colRowId, DbManagerValue.colRemark,
						DbManagerValue.colLatitude, DbManagerValue.colLongitude,
						DbManagerValue.colPhotoStatus,DbManagerValue.colGPSAccuracy,
						DbManagerValue.colUploadStatus,DbManagerValue.colCreatedAt,
						DbManagerValue.colPhotoDate,DbManagerValue.colWargaId,
						DbManagerValue.colBarangId);

				DbRepositoryValue.getInstance().open(MyApplication.getInstance());

				SQLiteStatement stmt = DbRepositoryValue.getInstance().getDB().compileStatement(sql);

				bindAndCheckNullString(stmt, 1, scheduleId);
				stmt.bindLong(2, itemId);
				bindAndCheckNullString(stmt, 3, value);
				bindBooleanToInteger(stmt, 4, typePhoto);
				stmt.bindLong(5, operatorId);
				stmt.bindLong(6, rowId);
				bindAndCheckNullString(stmt, 7, remark);
				bindAndCheckNullString(stmt, 8, latitude);
				bindAndCheckNullString(stmt, 9, longitude);
				bindAndCheckNullString(stmt, 10, photoStatus);
				stmt.bindLong(11, gpsAccuracy);
				stmt.bindLong(12, uploadStatus);
				bindAndCheckNullString(stmt, 13, getCurrentDate());
				bindAndCheckNullString(stmt, 14, photoDate);
				bindAndCheckNullString(stmt, 15, wargaId);
				bindAndCheckNullString(stmt, 16, barangId);
				stmt.executeInsert();
				stmt.close();
				DbRepositoryValue.getInstance().close();
				break;
			}
			default:
				break;
		}

	}

	private boolean isColumnExist(String colName) {
		boolean isExist = false;
		Cursor res = null;
		try {

			res = DbRepositoryValue.getInstance().getDB().rawQuery("Select * from "+ DbManagerValue.mFormValue +" limit 1", null);

			int colIndex = res.getColumnIndex(colName);
			if (colIndex!=-1){
				isExist = true;
			}

		} catch (Exception e) {
		} finally {
			try { if (res !=null){ res.close(); } } catch (Exception e1) {}
		}
		return isExist;
	}

	private void AddColumn(String colName, String colType) {
		String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s",
				DbManagerValue.mFormValue, colName, colType);
		try {
			DbRepositoryValue.getInstance().getDB().execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			DebugLog.d("error add columns");
		}
	}

	private String getCurrentDate(){
		Date currentDate = Calendar.getInstance().getTime();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault());
		return simpleDateFormat.format(currentDate);
	}


	private static FormValueModel getSiteFromCursor(Cursor c) {
		FormValueModel FormValueModel = null;

		if (null == c)
			return FormValueModel;

		FormValueModel = new FormValueModel();
		switch (DbManagerValue.schema_version) {
			case 1 : {
				//continue 1 to 2
			}
			case 2 : {
				//continue 2 to 3
			}
			case 3 : {
				//continue 3 to 4
			}
			case 4 : {
				//continue 4 to 5
			}
			case 5 : {
				FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
				FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
				FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
				FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
				FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
				FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
				FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
				FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
				FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
				FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
				FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
				FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;
				break;
			}
			case 6 : {
				//continue 6 to 7
			}
			case 7 : {
				FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
				FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
				FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
				FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
				FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
				FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
				FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
				FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
				FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
				FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
				FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
				FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;
				FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
				break;
			}
			case 8 : {
				FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
				FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
				FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
				FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
				FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
				FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
				FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
				FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
				FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
				FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
				FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
				FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;
				FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
				FormValueModel.photoDate = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoDate)));
				break;
			}
			case 9 : {
				FormValueModel.scheduleId = (c.getString(c.getColumnIndex(DbManagerValue.colScheduleId)));
				FormValueModel.operatorId = (c.getInt(c.getColumnIndex(DbManagerValue.colOperatorId)));
				FormValueModel.itemId = (c.getInt(c.getColumnIndex(DbManagerValue.colItemId)));
				FormValueModel.rowId = (c.getInt(c.getColumnIndex(DbManagerValue.colRowId)));
				FormValueModel.gpsAccuracy = (c.getInt(c.getColumnIndex(DbManagerValue.colGPSAccuracy)));
				FormValueModel.remark = (c.getString(c.getColumnIndex(DbManagerValue.colRemark)));
				FormValueModel.latitude = (c.getString(c.getColumnIndex(DbManagerValue.colLatitude)));
				FormValueModel.longitude = (c.getString(c.getColumnIndex(DbManagerValue.colLongitude)));
				FormValueModel.photoStatus = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoStatus)));
				FormValueModel.value = (c.getString(c.getColumnIndex(DbManagerValue.colValue)));
				FormValueModel.uploadStatus = (c.getInt(c.getColumnIndex(DbManagerValue.colUploadStatus)));
				FormValueModel.typePhoto = c.getInt(c.getColumnIndex(DbManagerValue.colIsPhoto)) == 1;
				FormValueModel.createdAt = (c.getString(c.getColumnIndex(DbManagerValue.colCreatedAt)));
				FormValueModel.photoDate = (c.getString(c.getColumnIndex(DbManagerValue.colPhotoDate)));
				FormValueModel.wargaId	 = (c.getString(c.getColumnIndex(DbManagerValue.colWargaId)));
				FormValueModel.barangId	 = (c.getString(c.getColumnIndex(DbManagerValue.colBarangId)));
				break;
			}
			default:
				break;
		}
		return FormValueModel;
	}

	public static String createDB(){
		String createTable = null;
		switch (DbManagerValue.schema_version) {
			case 1 : {
				//continue 1 to 2
			}
			case 2 : {
				//continue 2 to 3
			}
			case 3 : {
				//continue 3 to 4
			}
			case 4 : {
				//continue 4 to 5
			}
			case 5 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
								+ " ("+ DbManagerValue.colScheduleId + " integer, "
								+ DbManagerValue.colOperatorId + " integer, "
								+ DbManagerValue.colItemId + " integer, "
								+ DbManagerValue.colSiteId + " integer, "
								+ DbManagerValue.colGPSAccuracy + " integer, "
								+ DbManagerValue.colRowId + " integer, "
								+ DbManagerValue.colRemark + " varchar, "
								+ DbManagerValue.colPhotoStatus + " varchar, "
								+ DbManagerValue.colLatitude + " varchar, "
								+ DbManagerValue.colLongitude + " varchar, "
								+ DbManagerValue.colValue + " varchar, "
								+ DbManagerValue.colUploadStatus + " integer, "
								+ DbManagerValue.colIsPhoto + " integer, "
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			case 6 : {
				//continue 6 to 7
			}
			case 7 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
								+ " ("+ DbManagerValue.colScheduleId + " integer, "
								+ DbManagerValue.colOperatorId + " integer, "
								+ DbManagerValue.colItemId + " integer, "
								+ DbManagerValue.colSiteId + " integer, "
								+ DbManagerValue.colGPSAccuracy + " integer, "
								+ DbManagerValue.colRowId + " integer, "
								+ DbManagerValue.colRemark + " varchar, "
								+ DbManagerValue.colPhotoStatus + " varchar, "
								+ DbManagerValue.colLatitude + " varchar, "
								+ DbManagerValue.colLongitude + " varchar, "
								+ DbManagerValue.colValue + " varchar, "
								+ DbManagerValue.colUploadStatus + " integer, "
								+ DbManagerValue.colIsPhoto + " integer, "
								+ DbManagerValue.colCreatedAt + " varchar, "
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			case 8 : {
				createTable =  "create table if not exists " + DbManagerValue.mFormValue
								+ " ("+ DbManagerValue.colScheduleId + " integer, "
								+ DbManagerValue.colOperatorId + " integer, "
								+ DbManagerValue.colItemId + " integer, "
								+ DbManagerValue.colSiteId + " integer, "
								+ DbManagerValue.colGPSAccuracy + " integer, "
								+ DbManagerValue.colRowId + " integer, "
								+ DbManagerValue.colRemark + " varchar, "
								+ DbManagerValue.colPhotoStatus + " varchar, "
								+ DbManagerValue.colLatitude + " varchar, "
								+ DbManagerValue.colLongitude + " varchar, "
								+ DbManagerValue.colValue + " varchar, "
								+ DbManagerValue.colUploadStatus + " integer, "
								+ DbManagerValue.colIsPhoto + " integer, "
								+ DbManagerValue.colCreatedAt + " varchar, "
								+ DbManagerValue.colPhotoDate + " varchar,"
								+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId + "))";
				break;
			}
			case 9 : {
				createTable = "create table if not exists " + DbManagerValue.mFormValue
						+ " ("+ DbManagerValue.colScheduleId + " integer, "
						+ DbManagerValue.colOperatorId + " integer, "
						+ DbManagerValue.colItemId + " integer, "
						+ DbManagerValue.colSiteId + " integer, "
						+ DbManagerValue.colGPSAccuracy + " integer, "
						+ DbManagerValue.colRowId + " integer, "
						+ DbManagerValue.colRemark + " varchar, "
						+ DbManagerValue.colPhotoStatus + " varchar, "
						+ DbManagerValue.colLatitude + " varchar, "
						+ DbManagerValue.colLongitude + " varchar, "
						+ DbManagerValue.colValue + " varchar, "
						+ DbManagerValue.colUploadStatus + " integer, "
						+ DbManagerValue.colIsPhoto + " integer, "
						+ DbManagerValue.colCreatedAt + " varchar, "
						+ DbManagerValue.colPhotoDate + " varchar, "
						+ DbManagerValue.colWargaId + " varchar, "
						+ DbManagerValue.colBarangId + " varchar,"
						+ "PRIMARY KEY (" + DbManagerValue.colScheduleId + ","+ DbManagerValue.colItemId + ","+ DbManagerValue.colOperatorId
						+ "," + DbManagerValue.colWargaId + "," + DbManagerValue.colBarangId
						+ "))";
				break;
			}
			default:
				break;
		}
		return createTable;
	}

	public static void resetAllUploadStatus(){
		DbRepositoryValue.getInstance().open(MyApplication.getInstance());
		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colUploadStatus, UPLOAD_NONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mFormValue, cv, null, null);
		DbRepositoryValue.getInstance().close();
	}

	// update wargaid by scheduleid
	public static void updateWargaId(String scheduleId, String oldWargaId, String newWargaId) {

		DebugLog.d("update values scheduleid (oldWargaId, NewWargaId) : " + scheduleId + "(" + oldWargaId + "," + newWargaId + ")");

		String where  = DbManagerValue.colScheduleId + "=?" + " AND " +  DbManagerValue.colWargaId + "=?";
		String[] args = new String[] {scheduleId, oldWargaId};

		DbRepositoryValue.getInstance().open(MyApplication.getInstance());

		ContentValues cv = new ContentValues();
		cv.put(DbManagerValue.colWargaId, newWargaId);
		cv.put(DbManagerValue.colUploadStatus, FormValueModel.UPLOAD_DONE);

		DbRepositoryValue.getInstance().getDB().update(DbManagerValue.mFormValue, cv, where, args);
		DbRepositoryValue.getInstance().close();
	}

	public static void updateWargaItems(String oldWargaId, String newWargaId, ArrayList<FormValueModel> itemValuesModified) {

		for (FormValueModel itemValueSaved : itemValuesModified) {

			DebugLog.d(String.format("saving (scheduleid, itemid, oldwargaid, newwargaid) : (%s, %s, %s, %s)", itemValueSaved.scheduleId, itemValueSaved.itemId, oldWargaId, newWargaId));
			itemValueSaved.wargaId = newWargaId;
			itemValueSaved.save();

			DebugLog.d("deleting old item...");
			FormValueModel.delete(itemValueSaved.scheduleId, itemValueSaved.itemId, itemValueSaved.operatorId, oldWargaId, Constants.EMPTY);
		}
	}

	public static void updateBarangItems(String wargaId, String oldBarangId, String newBarangId, ArrayList<FormValueModel> itemValueModified) {

		for (FormValueModel itemValueSaved : itemValueModified) {

			DebugLog.d(String.format("saving (scheduleid, itemid, oldwargaid, newwargaid) : (%s, %s, %s, %s)", itemValueSaved.scheduleId, itemValueSaved.itemId, oldBarangId, newBarangId));
			itemValueSaved.barangId = newBarangId;
			itemValueSaved.save();

			DebugLog.d("deleting old item...");
			FormValueModel.delete(itemValueSaved.scheduleId, itemValueSaved.itemId, itemValueSaved.operatorId, wargaId, oldBarangId);
		}
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
