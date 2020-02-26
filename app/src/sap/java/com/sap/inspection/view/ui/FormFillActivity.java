package com.sap.inspection.view.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.rest.TowerAPIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.WorkFormColumnModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DateUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.util.PrefUtil;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.adapter.FormFillAdapter;
import com.sap.inspection.view.customview.FormItem;
import com.sap.inspection.view.customview.PhotoItemRadio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FormFillActivity extends BaseActivity implements FormTextChange, EasyPermissions.RationaleCallbacks {

	// bundle data
	private String wargaId;
	private String barangId;
	private String scheduleId;
	private String workFormGroupName;
	private String workTypeName;
	private int workFormGroupId;
	private int rowId;

	private ScheduleGeneral schedule;
	private WorkFormRowModel parentRow;
	private ArrayList<WorkFormColumnModel> column;
	private FormValueModel itemValueForShare;
	private Uri mImageUri;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private ArrayList<ItemFormRenderModel> formModels;
	private File photoFile;
	private FormFillAdapter adapter;

	private String pageTitle;
	private boolean finishInflate;
	private boolean isChecklistOrSiteInformation;

	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private Location currentLocation;

	// view
	private ScrollView scroll;
	private LinearLayout root;
	private RelativeLayout layoutEmpty;
	private PhotoItemRadio photoItem;
	private AutoCompleteTextView search;
	private ListView list;
	private View searchView;
	private TextView title;
	private Button mBtnSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form_fill);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			rowId 				= bundle.getInt(Constants.KEY_ROWID);
			workFormGroupId 	= bundle.getInt(Constants.KEY_WORKFORMGROUPID);
			workFormGroupName 	= bundle.getString(Constants.KEY_WORKFORMGROUPNAME);
			workTypeName		= bundle.getString(Constants.KEY_WORKTYPENAME);
			scheduleId 			= bundle.getString(Constants.KEY_SCHEDULEID);
			wargaId 			= bundle.getString(Constants.KEY_WARGAID) != null ? bundle.getString(Constants.KEY_WARGAID) : Constants.EMPTY;
			barangId			= bundle.getString(Constants.KEY_BARANGID) != null ? bundle.getString(Constants.KEY_BARANGID) : Constants.EMPTY;

			DebugLog.d("received bundle : ");
            DebugLog.d("rowId = " + rowId);
            DebugLog.d("workFormGroupId = " + workFormGroupId);
            DebugLog.d("workFormGroupName = " + workFormGroupName);
            DebugLog.d("workTypeName = " + workTypeName);
            DebugLog.d("scheduleId = " + scheduleId);
            DebugLog.d("wargaId = " + wargaId);
            DebugLog.d("barangId = " + barangId);

			isChecklistOrSiteInformation =  workFormGroupName.equalsIgnoreCase("checklist") ||
											workFormGroupName.equalsIgnoreCase("site information");
		}

		if (indexes == null)
			indexes = new ArrayList<>();
		indexes.add(0);
		if (labels == null)
			labels = new ArrayList<>();
		if (formItems == null)
			formItems = new ArrayList<>();
		if (formModels == null)
			formModels = new ArrayList<>();

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(connectionCallbacks)
				.addOnConnectionFailedListener(onConnectionFailedListener)
				.build();

		// init schedule
		schedule = ScheduleBaseModel.getScheduleById(scheduleId);

		adapter = new FormFillAdapter(this);
		adapter.setScheduleId(scheduleId);
		adapter.setPhotoListener(photoClickListener);
		adapter.setUploadListener(uploadClickListener);
		adapter.setWorkTypeName(schedule.work_type.name);
		adapter.setWorkFormGroupId(workFormGroupId);
		adapter.setWorkFormGroupName(workFormGroupName);
		adapter.setWargaId(wargaId);
		adapter.setBarangId(barangId);

		list = findViewById(R.id.list);
		list.setOnItemSelectedListener(itemSelected);
		list.setOnScrollListener(onScrollListener);
		list.setAdapter(adapter);

		// init view
		scroll = findViewById(R.id.scroll);
		layoutEmpty = findViewById(R.id.item_form_empty_layout);
		searchView = findViewById(R.id.layout_search);
		search = findViewById(R.id.search);
		search.setOnItemClickListener(searchClickListener);
		root = findViewById(R.id.root);
		title = findViewById(R.id.header_title);
		mBtnSettings = findViewById(R.id.btnsettings);
		mBtnSettings.setOnClickListener(view -> {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
		});

		new FormLoader().execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		connectGoogleApi();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!CommonUtil.checkPlayServices(this)) {
			Toast.makeText(activity, getString(R.string.warning_check_play_service_message), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopLocationUpdates();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	OnItemSelectedListener itemSelected = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> listView, View view, int position, long id)
		{
			DebugLog.d("==================== on item selected");
			FormFillAdapter adapter = (FormFillAdapter) listView.getAdapter();
		    if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_TEXT_INPUT)
		    {
				DebugLog.d("here is the text input");
		        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		        view.findViewById(R.id.item_form_input).requestFocus();
		    }
		    else if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_PICTURE_RADIO){
				DebugLog.d("here is the picture");
		        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		        if (radioGroup.getCheckedRadioButtonId() == R.id.radioNOK)
		        	view.findViewById(R.id.remark).requestFocus();
		        else
			    {
			        if (!listView.isFocused())
			        {
			            // listView.setItemsCanFocus(false);
			            // Use beforeDescendants so that the EditText doesn't re-take focus
			            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			            listView.requestFocus();
			        }
			    }

		    } else {
		        if (!listView.isFocused())
		        {
		            // listView.setItemsCanFocus(false);
		            // Use beforeDescendants so that the EditText doesn't re-take focus
		            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		            listView.requestFocus();
		        }
		    }
		}

		public void onNothingSelected(AdapterView<?> listView)
		{
			DebugLog.d("==================== on nothing selected");
		    // This happens when you start scrolling, so we need to prevent it from staying
		    // in the afterDescendants mode if the EditText was focused 
		    listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	};

	private void saveValue(String[] itemProperties,boolean isAdding,boolean isCompundButton){

		if (itemProperties.length < 5){
			DebugLog.d("invalid component to saved");
			return;
		}
		//
		if (itemValueForShare == null)
			itemValueForShare = new FormValueModel();

		itemValueForShare = FormValueModel.getItemValue(schedule.id, Integer.parseInt(itemProperties[1]), Integer.parseInt(itemProperties[2]));
		if (itemValueForShare == null){
			itemValueForShare = new FormValueModel();
			itemValueForShare.scheduleId = schedule.id;
			itemValueForShare.rowId = Integer.parseInt(itemProperties[0]);
			itemValueForShare.itemId = Integer.parseInt(itemProperties[1]);
			itemValueForShare.operatorId = Integer.parseInt(itemProperties[2]);
			itemValueForShare.value = "";
			itemValueForShare.typePhoto = itemProperties[4].equalsIgnoreCase("1");

			DebugLog.d("item is null, initiate first");
		}
		DebugLog.d("=================================================================");
		DebugLog.d("===== value : "+itemValueForShare.value);
		if (isCompundButton){
			if (isAdding){ //adding value on check box
				DebugLog.d("goto adding");
				// value still null or blank
				if (itemValueForShare.value == null | itemValueForShare.value.equalsIgnoreCase(""))
					itemValueForShare.value = itemProperties[3];
				// any value apply before
				else{
					String[] chkBoxValue = itemValueForShare.value.split("[,]");
					for(int i = 0; i < chkBoxValue.length; i++){ 
						if (chkBoxValue[i].equalsIgnoreCase(itemProperties[3]))
							break;
						if (i == chkBoxValue.length - 1)
							itemValueForShare.value += ","+itemProperties[3];
					}
				}
				itemValueForShare.uploadStatus = FormValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}else{ // deleting on checkbox
				DebugLog.d("goto deleting");
				String[] chkBoxValue = itemValueForShare.value.split("[,]");
				itemValueForShare.value = "";
				//removing unchecked checkbox value
				for(int i = 0; i < chkBoxValue.length; i++){ 
					if (!chkBoxValue[i].equalsIgnoreCase(itemProperties[3]))
						if (i == chkBoxValue.length - 1 || chkBoxValue[chkBoxValue.length - 1].equalsIgnoreCase(itemProperties[3]))
							itemValueForShare.value += chkBoxValue[i];
						else
							itemValueForShare.value += chkBoxValue[i]+','; 
				}
				chkBoxValue = null;
				if (itemValueForShare.value.equalsIgnoreCase(""))
					itemValueForShare.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
				else{
					itemValueForShare.uploadStatus = FormValueModel.UPLOAD_NONE;
					itemValueForShare.save();
				}
			}
		}
		else{
			if (!isAdding) {
				DebugLog.d("deleting item row");
				itemValueForShare.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
			}
			else{
				DebugLog.d("saving update values");
				itemValueForShare.value = itemProperties[3];
				itemValueForShare.uploadStatus = FormValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}
		}
		DebugLog.d("task done : "+itemValueForShare.countTaskDone(schedule.id, itemValueForShare.rowId));
		//setPercentage(itemValueForShare.rowId);
	}

	@Override
	public void onTextChange(String string, View view) {
		if (view.getTag() != null){
			DebugLog.d((String)view.getTag());
			String[] split = ((String) view.getTag()).split("[|]");
			split[3] = string;
			for (int i = 0; i < split.length; i++) {
				DebugLog.d("=== "+split[i]);
			}
			saveValue(split, !string.equalsIgnoreCase(""),false);
		}
	}

	OnClickListener photoClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (CommonUtil.checkGpsStatus(FormFillActivity.this) || CommonUtil.checkNetworkStatus(FormFillActivity.this)) {
				photoItem = (PhotoItemRadio) v.getTag();
				if (photoItem == null) {
					Toast.makeText(activity, getString(R.string.error_photo_item_not_valid), Toast.LENGTH_LONG).show();
					return;
				}

				// if dialog has never shown once
				if (!PrefUtil.getBoolPref(R.string.key_should_not_show_take_picture_dialog, false)) {

					// show it
					DialogUtil.showTakePictureDialog(activity, (position, item) -> {

						// set initial value of dialog not shown as false
						PrefUtil.putBoolPref(R.string.key_should_not_show_take_picture_dialog, false);
						switch (position) {
							// set dialog not shown as true ("Don't show again") and proceed take picture
							case 0 : PrefUtil.putBoolPref(R.string.key_should_not_show_take_picture_dialog, true);

								// proceed take picture
							case 1 : takePicture(photoItem.getItemId()); break;

							// dismiss
							default: break;
						}
					});
				} else {
					// proceed take picture anyway
					takePicture(photoItem.getItemId());
				}
			} else
				DialogUtil.showGPSdialog(FormFillActivity.this);
		}
	};

    OnClickListener uploadClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
			if (!GlobalVar.getInstance().anyNetwork(activity)) {
				TowerApplication.getInstance().toast(getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT);
				return;
			}
			int pos = (int)v.getTag();
			DebugLog.d("pos = "+pos);
			ItemFormRenderModel itemFormRenderModel = adapter.getItem(pos);
			FormValueModel uploadItem = itemFormRenderModel.getItemValue();
			if (uploadItem != null)
				new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, uploadItem.itemId, uploadItem.wargaId, uploadItem.barangId).execute();
			else
				TowerApplication.getInstance().toast(getString(R.string.error_no_item), Toast.LENGTH_LONG);
        }
    };

	public boolean takePicture(int itemId){
		trackEvent(getString(R.string.event_take_picture));
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		if (!FileUtil.isStorageAvailableAndWritable()) {
			String storageStatus = FileUtil.getStorageStatus(this);
			Toast.makeText(this, getString(R.string.error_take_picture, storageStatus), Toast.LENGTH_LONG).show();
			return false;
		}

		if (intent.resolveActivity(this.getPackageManager()) == null) {
			String errorMessage = getString(R.string.error_no_camera_application);
			Toast.makeText(this, getString(R.string.error_take_picture, errorMessage), Toast.LENGTH_LONG).show();
			return false;
		}

		try {
			photoFile = null;
			String photoFileName = StringUtil.getNewPhotoFileName(schedule.id, itemId);
			/*ContextWrapper cw = new ContextWrapper(getApplicationContext());
			File savedDir = cw.getDir(Constants.TOWER_PHOTOS_DIR, MODE_PRIVATE); // path to /data/data/yourapp/app_data/{savedDir}*/
			//File savedDir = new File(getFilesDir(), Constants.TOWER_PHOTOS_DIR); // internal storage dir
			//File savedDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Constants.TOWER_PHOTOS_DIR); // path to /Android/data/app/{packagename}/files/pictures/...
			//File savedPath = new File(savedDir, scheduleId);
			String savedPath = Constants.TOWER_PHOTOS_PATH + File.separator + schedule.id;
			photoFile = FileUtil.createTemporaryPhotoFile(CommonUtil.getEncryptedMD5Hex(photoFileName), ".jpg", savedPath);
			mImageUri = FileUtil.getUriFromFile(this, photoFile);
			DebugLog.d("photoFile string path: " + photoFile.toString());
			DebugLog.d("photoFile absolute path: " + photoFile.getAbsolutePath());
			DebugLog.d("mImageUri : " + mImageUri.toString());
			if (mImageUri != null && photoFile != null && !TextUtils.isEmpty(photoFile.toString())) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				intent.putExtra("outputX", 480);
				intent.putExtra("outputY", 480);
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("scale", true);
				intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivityForResult(intent, Constants.RC_TAKE_PHOTO);
				return true;
			} else {
				throw new NullPointerException(getString(R.string.error_imageuri_or_photofile_empty));
			}
		} catch (NullPointerException | IOException | IllegalArgumentException e) {
			DebugLog.e("take picture: " + e.getMessage());
			Toast.makeText(this, getString(R.string.error_take_picture, e.getMessage()), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if(requestCode == Constants.RC_TAKE_PHOTO && resultCode == RESULT_OK) {
			if (DateUtil.isTimeAutomatic(this)) {

			    // anonymously send fake gps report to server
                String fakeGPSReport = CommonUtil.checkFakeGPS(this, currentLocation);
                if (!TextUtils.isEmpty(fakeGPSReport)) {
                    sendFakeGPSReport(fakeGPSReport, String.valueOf(schedule.site.id));
                }

				String currentLat  = String.valueOf(currentLocation.getLatitude());
				String currentLong = String.valueOf(currentLocation.getLongitude());
				int accuracy	   = (int) currentLocation.getAccuracy();

				String[] textMarks = new String[3];
				String photoDate = DateUtil.getCurrentDate();

				textMarks[0] = "Lat. : "+  currentLat + ", Long. : "+ currentLong;
				textMarks[1] = "Distance to site : " + TowerApplication.getInstance().checkinDataModel.getDistance() + " meters";
				textMarks[2] = "Photo date : "+photoDate;

				try {
					String photoAbsolutePath = photoFile.getAbsolutePath();
					ImageUtil.resizeAndSaveImageCheckExifWithMark(this, photoFile, textMarks);
					ImageUtil.addWaterMark(this, R.drawable.watermark_ipa_grayscale, photoAbsolutePath);
					CommonUtil.encryptFileBase64(photoFile, photoAbsolutePath);

					if (schedule.work_type.name.matches(Constants.regexIMBASPETIR)) {
						photoItem.deletePhoto();
						photoItem.setImage(photoFile, currentLat, currentLong, accuracy);
					} else {
						if (!CommonUtil.isCurrentLocationError(currentLat, currentLong)) {
							photoItem.deletePhoto();
							photoItem.setImage(photoFile, currentLat, currentLong, accuracy);
						} else {
							String errorMessage = this.getResources().getString(R.string.sitelocationisnotaccurate);
							DebugLog.e("location error : " + errorMessage);
							TowerApplication.getInstance().toast(errorMessage, Toast.LENGTH_LONG);
						}
					}
				} catch (NullPointerException | IOException e) {
					String errorMessage = getString(R.string.error_resize_and_save_image, e.getMessage());
					Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
					DebugLog.e(errorMessage, e);
				}
			} else {
				Toast.makeText(this, getString(R.string.error_using_manual_date_time), Toast.LENGTH_LONG).show();
				DateUtil.openDateTimeSetting(FormFillActivity.this, 0);
			}
		}

		super.onActivityResult(requestCode, resultCode, intent);
	}

    private void sendFakeGPSReport(String message, String siteId) {
        compositeDisposable.add(
                TowerAPIHelper.reportFakeGPS(String.valueOf(System.currentTimeMillis()), BuildConfig.VERSION_NAME, message,siteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> DebugLog.d("send fake GPS report complete"),
                                error -> DebugLog.e(error.getMessage(), error)
                        )
        );
    }

	private class FormLoader extends AsyncTask<Void, Integer, Void>{
		String lastLable = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showMessageDialog("Generating form...");
		}

		@Override
		protected Void doInBackground(Void... params) {

			parentRow = new WorkFormRowModel();

		    if (workTypeName.equalsIgnoreCase(getString(R.string.corrective))) {

				CorrectiveScheduleResponseModel.CorrectiveGroup correctiveGroup = CorrectiveScheduleConfig.getCorrectiveGroup(Integer.valueOf(scheduleId), workFormGroupId);

				if (correctiveGroup != null) {
					Set<Integer> uniqueChildRowIds = new HashSet<>();
					for (CorrectiveScheduleResponseModel.CorrectiveItem correctiveItem : correctiveGroup.getItems()) {
						uniqueChildRowIds.add(correctiveItem.getRow_id());
					}
					int[] childRowIds = ArrayUtils.toPrimitiveArray(uniqueChildRowIds);
					parentRow = parentRow.getAllItemsByRowId(workFormGroupId, rowId, childRowIds);

				}

            } else {

                parentRow = parentRow.getAllItemsByRowId(workFormGroupId, rowId);

			}

			column = WorkFormColumnModel.getAllItemByWorkFormGroupId(workFormGroupId);
			ItemFormRenderModel form;
			setPageTitle();

			//check if the head has a form
			DebugLog.d("\nGet header form fill and its children items...");
			for(int i = 0; i < parentRow.row_columns.size(); i++){

				RowColumnModel rowColHeader = parentRow.row_columns.get(i);
				DebugLog.d(i + ". row col id : " + rowColHeader.id);

				if (rowColHeader.items.size() > 0){
					finishInflate = false;
					checkHeaderName(parentRow);
					form = new ItemFormRenderModel();
					form.setSchedule(schedule);
					form.setColumns(column);
					form.setWargaId(wargaId);
					form.setBarangId(barangId);
					form.setWorkFormGroupId(workFormGroupId);
					form.setWorkTypeName(workTypeName);
					form.setRowColumnModels(parentRow.row_columns, null);
					if (form.isHasInput()){
						indexes.add(indexes.get(indexes.size()-1) + form.getCount());
						String label = form.getLabel();

						if (TextUtils.isEmpty(label)) {
							label = "item with no label";
						}
						labels.add(label);
						formModels.add(form);
					} else if (form.isHasPicture()){
						String label = form.getLabel();
						if (TextUtils.isEmpty(label)) {
							label = "item with no label";
						}
						labels.add(label);
						formModels.add(form);
					}
					break;
				}
			}

			int x = 0;
			//check if the child has a form
			String parentLabel = null;
			DebugLog.d("\n\nlooping children with size : " + parentRow.children.size());
			for (WorkFormRowModel rowChildren : parentRow.children) {
				x++;
				DebugLog.d("\nchildren ke-" + x + " with id " + rowChildren.id);
				DebugLog.d("checking item's header label..");
				checkHeaderName(rowChildren);
				publishProgress(x * 100 / parentRow.children.size());
				finishInflate = false;
				form = new ItemFormRenderModel();
				form.setSchedule(schedule);
				form.setColumns(column);
				form.setWargaId(wargaId);
				form.setBarangId(barangId);
				form.setWorkTypeName(workTypeName);
				form.setWorkFormGroupName(workFormGroupName);
				form.setWorkFormGroupId(workFormGroupId);
				form.setRowColumnModels(rowChildren.row_columns,parentLabel);
				if (form.isHasInput()){
					indexes.add(indexes.get(indexes.size()-1) + form.getCount());
					String label = form.getLabel();
					while (labels.indexOf(label) != -1){
						label = label+".";
					}

					if (TextUtils.isEmpty(label)) {
						label = "item with no label";
					}

					labels.add(label);
					DebugLog.d("indexes : " + indexes.get(indexes.size()-1));
					DebugLog.d("label : " + label);
					formModels.add(form);

				}else
					parentLabel = form.getLabel();
			}

			return null;
		}
		
		private void checkHeaderName(WorkFormRowModel parentRow){
			if (parentRow.row_columns != null &&
					parentRow.row_columns.size() > 0 &&
					parentRow.row_columns.get(0).items != null &&
					parentRow.row_columns.get(0).items.size() > 0){
				DebugLog.d("head row label : "+parentRow.row_columns.get(0).items.get(0).label);
				if (parentRow.row_columns.get(0).items.get(0).label != null && !parentRow.row_columns.get(0).items.get(0).label.equalsIgnoreCase(""))
					this.lastLable = parentRow.row_columns.get(0).items.get(0).label;
				parentRow.row_columns.get(0).items.get(0).labelHeader = this.lastLable;
				DebugLog.d("labelHeader : " + this.lastLable);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			showMessageDialog("Generating form "+values[0]+" % complete");
		}

		@Override
		protected void onPostExecute(Void result) {
			title.setText(pageTitle);
			hideDialog();

			if (!formModels.isEmpty()) {
                adapter.setItems(formModels);

                boolean ada = false;
                DebugLog.d("total formModels items : " + formModels.size());
                for (ItemFormRenderModel item : formModels) {
                    if (item.getWorkItemModel()!=null&&!item.getWorkItemModel().search) {
                        DebugLog.d("search="+item.getWorkItemModel().search);
                        ada = true;
                        break;
                    }
                }

                if (ada)
                    searchView.setVisibility(View.GONE);

                ArrayAdapter<String> searchAdapter =
                        new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, labels){

                            public View getView(int position, View convertView, ViewGroup parent) {
                                View v = super.getView(position, convertView, parent);
                                int i = 10;
                                ((TextView) v).setTextSize(14);
                                ((TextView) v).setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

                                return v;
                            }
                        };

                search.setAdapter(searchAdapter);
            } else {

			    layoutEmpty.setVisibility(View.VISIBLE);
			    list.setVisibility(View.GONE);

            }
			super.onPostExecute(result);
		}
	}
	
	OnItemClickListener searchClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			list.setSelection(indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
			DebugLog.d("===== selected : "+parent.getItemAtPosition(position)+" | "+indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
		}
	};
	
	private void setPageTitle(){

		if (parentRow.row_columns.size() > 0 && parentRow.row_columns.get(0).items.size() > 0)
			pageTitle = parentRow.row_columns.get(0).items.get(0).label;

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && pageTitle.contains(Constants.regexId)) {
			String barangLabel = pageTitle;
			String barangID = barangId;
			String barangName = StringUtil.getName(scheduleId, wargaId, barangId, workFormGroupId);
			StringBuilder barangLabelBuilder = new StringBuilder(barangLabel).append(barangID);
			if (!TextUtils.isEmpty(barangName))
				barangLabelBuilder.append(" (").append(barangName).append(")");

			pageTitle = new String(barangLabelBuilder);
		}
	}

	private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnected(@Nullable Bundle bundle) {
			if (!PermissionUtil.hasAllPermissions(FormFillActivity.this))
				return;

			startLocationUpdates();
		}

		@Override
		public void onConnectionSuspended(int i) {
			DebugLog.d("i="+i);
		}

	};

	private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = connectionResult -> {
		TowerApplication.getInstance().toast("Koneksi google api client gagal", Toast.LENGTH_LONG);
		DebugLog.d("connectionResult="+connectionResult.toString());
	};

	private com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			currentLocation = location;
			DebugLog.d("location(lat: " + currentLocation.getLatitude() + ", long: " + currentLocation.getLongitude() + ", acc: " + currentLocation.getAccuracy()+ ")");
		}
	};

	@Override
	public void onBackPressed() {
		ArrayList<Integer> list = new ArrayList<>();
		list.add(ItemFormRenderModel.TYPE_PICTURE_RADIO);
		list.add(ItemFormRenderModel.TYPE_CHECKBOX);
		list.add(ItemFormRenderModel.TYPE_RADIO);
		list.add(ItemFormRenderModel.TYPE_TEXT_INPUT);
		list.add(ItemFormRenderModel.TYPE_PICTURE);
		list.add(ItemFormRenderModel.TYPE_EXPAND);
		adapter.notifyDataSetChanged();

		boolean isMandatoryCheckingActive = false;

		if (adapter!=null && !adapter.isEmpty()) {
			if ((TowerApplication.getInstance().IS_CHECKING_HASIL_PM() && isChecklistOrSiteInformation) || !TowerApplication.getInstance().IS_CHECKING_HASIL_PM()){
				isMandatoryCheckingActive = true;
			}
		}

		if (isMandatoryCheckingActive) {

			DebugLog.d("\n\n ==== ON BACK PRESSED ====");
			DebugLog.d("scheduleId = " + scheduleId);
			DebugLog.d("workFormGroupName = " + workFormGroupName);
			DebugLog.d("is in check hasil pm ? " + TowerApplication.getInstance().IS_CHECKING_HASIL_PM());
			DebugLog.d("Jumlah item adapter : " + adapter.getCount());

			String mandatoryLabel = "";
			boolean mandatoryFound = false;

			for (int i = 0; i < adapter.getCount(); i++) {

				ItemFormRenderModel item = adapter.getItem(i);

				DebugLog.d("no. "+ i);
				DebugLog.d("\titem type = " + item.getType());
				if (item.getWorkItemModel()!=null) {
					DebugLog.d("\titem label = " + item.getWorkItemModel().label);
					DebugLog.d("\titem isMandatory = " + item.getWorkItemModel().mandatory);
					DebugLog.d("\titem isDisabled = " + item.getWorkItemModel().disable);
				} else
					DebugLog.d("\titem workitemmodel = null");

				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
					DebugLog.d("\titem wargaId = " + item.getWargaId());
					DebugLog.d("\titem barangId = " + item.getBarangId());
				}

				if (item.getItemValue() != null && !TextUtils.isEmpty(item.getItemValue().value)) DebugLog.d("\titem value = " +item.getItemValue().value);

				if (list.contains(item.getType()) && item.getWorkItemModel() != null) {
					if (!FormValueModel.isItemValueValidated(item.getWorkItemModel(), item.getItemValue(), workTypeName)) {
						mandatoryLabel = item.getWorkItemModel().label;
						mandatoryFound = true;
						break;
					}
				}
			}

			//if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
			if (mandatoryFound) {
				DebugLog.e("mandatoryFound with label : " + mandatoryLabel);
				return;
			}
		}

		super.onBackPressed();
	}

	private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView absListView, int i) {
			InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (getCurrentFocus() != null) {
				if (inputManager != null) {
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				}
				getCurrentFocus().clearFocus();
			}
		}

		@Override
		public void onScroll(AbsListView absListView, int i, int i1, int i2) {

		}
	};

	@AfterPermissionGranted(Constants.RC_ALL_PERMISSION)
	private void requestAllPermission() {
		PermissionUtil.requestAllPermissions(this, getString(R.string.rationale_allpermissions), Constants.RC_ALL_PERMISSION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == Constants.RC_ALL_PERMISSION) {
			connectGoogleApi();
		}
	}

	@Override
	public void onRationaleAccepted(int requestCode) {

	}

	@Override
	public void onRationaleDenied(int requestCode) {
		if (requestCode == Constants.RC_ALL_PERMISSION) {
			Toast.makeText(activity, getString(R.string.error_permission_denied), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void connectGoogleApi() {
		if (PermissionUtil.hasAllPermissions(this)) {
			if (googleApiClient != null) googleApiClient.connect();
		} else requestAllPermission();
	}

	private void startLocationUpdates() {
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5000); // Update location every second
		locationRequest.setFastestInterval(3000);
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,locationListener);
	}

	private void stopLocationUpdates() {
		if (googleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
			googleApiClient.disconnect();
		}
	}
}