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
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.WorkFormColumnModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.model.value.Pair;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FormFillActivity extends BaseActivity implements FormTextChange, EasyPermissions.RationaleCallbacks{

	// bundle data
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
	private boolean isMandatoryCheckingActive = false;

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

            DebugLog.d("received bundle : ");
            DebugLog.d("rowId = " + rowId);
            DebugLog.d("workFormGroupId = " + workFormGroupId);
            DebugLog.d("workFormGroupName = " + workFormGroupName);
			DebugLog.d("workTypeName = " + workTypeName);
            DebugLog.d("scheduleId = " + scheduleId);

			isChecklistOrSiteInformation =  workFormGroupName.equalsIgnoreCase("checklist") ||
											workFormGroupName.equalsIgnoreCase("site information");
        }

        if (indexes == null)
            indexes = new ArrayList<Integer>();
        indexes.add(0);
        if (labels == null)
            labels = new ArrayList<String>();
        if (formItems == null)
            formItems = new ArrayList<FormItem>();
        if (formModels == null)
            formModels = new ArrayList<>();

        // init schedule
        schedule = ScheduleBaseModel.getScheduleById(scheduleId);

        adapter = new FormFillAdapter(this);
        adapter.setScheduleId(scheduleId);
        adapter.setPhotoListener(photoClickListener);
        adapter.setUploadListener(uploadClickListener);
        adapter.setWorkTypeName(schedule.work_type.name);
        adapter.setWorkFormGroupId(workFormGroupId);
        adapter.setWorkFormGroupName(workFormGroupName);

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

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(connectionCallbacks)
				.addOnConnectionFailedListener(onConnectionFailedListener)
				.build();

        new FormLoader().execute();
	}

	@Override
	protected void onStart() {
		super.onStart();
		DebugLog.d("onStart");
		connectGoogleApi();
	}

	@Override
	protected void onResume() {
		super.onResume();
		DebugLog.d("onResume");
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DebugLog.d("onDestroy");
		stopLocationUpdates();
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

				Pair<String, String> photoLocation;
				String currentLat  = String.valueOf(currentLocation.getLatitude());
				String currentLong = String.valueOf(currentLocation.getLongitude());
				int accuracy	   = (int) currentLocation.getAccuracy();

				if (mImageUri != null && !TextUtils.isEmpty(photoFile.toString())) {
					File filePhotoResult = new File(photoFile.toString());
					if (filePhotoResult.exists()) {
						if (TowerApplication.getInstance().isScheduleNeedCheckIn()) {
							photoLocation = CommonUtil.getPersistentLocation(scheduleId);
							if (photoLocation != null) {
								currentLat = photoLocation.first();
								currentLong = photoLocation.second();
							} else {
								DebugLog.e("Persistent photo location error (null)");
							}
						}

						String[] textMarks = new String[3];
						String photoDate = DateUtil.getCurrentDate();

						textMarks[0] = "Lat. : " + currentLat + ", Long. : " + currentLong;
						textMarks[1] = "Distance to site : " + TowerApplication.getInstance().checkinDataModel.getDistance() + " meters";
						textMarks[2] = "Photo date : " + photoDate;

						try {
							ImageUtil.resizeAndSaveImageCheckExifWithMark(this, photoFile.toString(), textMarks);
							if (!CommonUtil.isCurrentLocationError(currentLat, currentLong)) {
								photoItem.deletePhoto();
								photoItem.setImage(filePhotoResult, currentLat, currentLong, accuracy);
							} else {
								String errorMessage = this.getResources().getString(R.string.sitelocationisnotaccurate);
								DebugLog.e("location error : " + errorMessage);
								TowerApplication.getInstance().toast(errorMessage, Toast.LENGTH_LONG);
							}
						} catch (IOException e) {
							Toast.makeText(activity, getString(R.string.error_resize_and_save_image), Toast.LENGTH_LONG).show();
							DebugLog.e(getString(R.string.error_resize_and_save_image), e);
						}
					}
				} else Toast.makeText(activity, getString(R.string.error_imageuri_or_photofile_empty), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(activity, getString(R.string.error_using_manual_date_time), Toast.LENGTH_LONG).show();
				DateUtil.openDateTimeSetting(FormFillActivity.this, 0);
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

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

				DebugLog.d("no. " + i);
				DebugLog.d("\titem type = " + item.getType());
				if (item.getWorkItemModel() != null) {
					DebugLog.d("\titem label = " + item.getWorkItemModel().label);
					DebugLog.d("\titem isMandatory = " + item.getWorkItemModel().mandatory);
					DebugLog.d("\titem isDisabled = " + item.getWorkItemModel().disable);
				} else
					DebugLog.d("\titem workitemmodel = null");

				if (item.getItemValue() != null && !TextUtils.isEmpty(item.getItemValue().value))
					DebugLog.d("\titem value = " + item.getItemValue().value);

				if (list.contains(item.getType()) && item.getWorkItemModel() != null) {
					if (!FormValueModel.isItemValueValidated(item.getWorkItemModel(), item.getItemValue())) {
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

	@Override
	public void onTextChange(String string, View view) {
		if (view.getTag() != null){
			DebugLog.d((String)view.getTag());
			String[] split = ((String)view.getTag()).split("[|]");
			split[3] = string;
			for (int i = 0; i < split.length; i++) {
				DebugLog.d("=== "+split[i]);
			}
			saveValue(split, !string.equalsIgnoreCase(""),false);
		}
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
	
	private void saveValue(String[] itemProperties,boolean isAdding,boolean isCompundButton){

		if (itemProperties.length < 5){
			DebugLog.d("invalid component to saved");
			return;
		}
		//
		if (itemValueForShare == null) {
			itemValueForShare = new FormValueModel();
		}

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
					FormValueModel.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
				else{
					itemValueForShare.uploadStatus = FormValueModel.UPLOAD_NONE;
					itemValueForShare.save();
				}
			}
		}
		else{
			if (!isAdding)
				FormValueModel.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
			else{
				itemValueForShare.value = itemProperties[3];
				itemValueForShare.uploadStatus = FormValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}
		}
		DebugLog.d("===== value : "+itemValueForShare.value);
		DebugLog.d("row id : "+ itemValueForShare.rowId);
		DebugLog.d("task done : "+ FormValueModel.countTaskDone(schedule.id, itemValueForShare.rowId));
		//setPercentage(itemValueForShare.rowId);
	}

    public void takePicture(int itemId){
    	trackEvent(getString(R.string.event_take_picture));
    	DebugLog.d(getString(R.string.event_take_picture));
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(this.getPackageManager()) != null && FileUtil.isStorageAvailableAndWriteable(this)) {
			photoFile = null;
			try {
				String photoFileName = StringUtil.getNewPhotoFileName(schedule.id, itemId);
				String savedPath = Constants.DIR_PHOTOS + File.separator + schedule.id;
				photoFile = FileUtil.createTemporaryPhotoFile(photoFileName, ".jpg", savedPath);
				if (photoFile != null && photoFile.exists()) {
					photoFileName = photoFile.getPath().replaceFirst("/file:", "");
					photoFileName = photoFileName.replaceFirst("file://", "");
					photoFile = new File(photoFileName);
					mImageUri = FileUtil.getUriFromFile(this, photoFile);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
					intent.putExtra("outputX", 480);
					intent.putExtra("outputY", 480);
					intent.putExtra("aspectX", 1);
					intent.putExtra("aspectY", 1);
					intent.putExtra("scale", true);
					intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
					intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
					startActivityForResult(intent, Constants.RC_TAKE_PHOTO);
				} else {
					DebugLog.e("take picture: photo file not created");
					Toast.makeText(activity, getString(R.string.error_photo_file_not_created), Toast.LENGTH_LONG).show();
				}
				return;
			} catch (NullPointerException | IOException | IllegalArgumentException e) {
				DebugLog.e("take picture: " + e.getMessage());
			}
		}

		// if failed, then show toast with failed message
		Toast.makeText(activity, getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();
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
			parentRow = parentRow.getAllItemsByRowId(workFormGroupId, rowId);
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
					form.setWorkFormGroupName(workFormGroupName);
					form.setWorkTypeName(workTypeName);
					form.setRowColumnModels(parentRow.row_columns, null);
					DebugLog.d("has input ? " + form.isHasInput());
					DebugLog.d("has picture ? " + form.isHasPicture());
					if (form.isHasInput()){
						indexes.add(indexes.get(indexes.size()-1) + form.getCount());
						String label = form.getLabel();
						if (TextUtils.isEmpty(label)) {
							label = "item with no label";
						}
						DebugLog.d("label added : " + label + " has input");
						labels.add(label);
						formModels.add(form);
					} else if (form.isHasPicture()){
						String label = form.getLabel();
						if (TextUtils.isEmpty(label)) {
							label = "item with no label";
						}
						DebugLog.d("label added : " + label + " has picture");
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
				form.setWorkTypeName(workTypeName);
				form.setWorkFormGroupName(workFormGroupName);
				form.setWorkFormGroupId(workFormGroupId);
				form.setRowColumnModels(rowChildren.row_columns, parentLabel);
				DebugLog.d("has input ? " + form.isHasInput());
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
		
		private void checkHeaderName(WorkFormRowModel rowModel){
			if (rowModel.row_columns != null && 
					rowModel.row_columns.size() > 0 && 
					rowModel.row_columns.get(0).items != null &&
					rowModel.row_columns.get(0).items.size() > 0){
				DebugLog.d("========================= head row label : "+rowModel.row_columns.get(0).items.get(0).label);
				if (rowModel.row_columns.get(0).items.get(0).label != null && !rowModel.row_columns.get(0).items.get(0).label.equalsIgnoreCase(""))
					this.lastLable = rowModel.row_columns.get(0).items.get(0).label;
					rowModel.row_columns.get(0).items.get(0).labelHeader = this.lastLable;
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
				DebugLog.d("form kosong");
				layoutEmpty.setVisibility(View.VISIBLE);
				list.setVisibility(View.GONE);

			}
			super.onPostExecute(result);
		}
	}
	
	private void setPageTitle(){
		if (parentRow.row_columns.size() > 0 && parentRow.row_columns.get(0).items.size() > 0)
			pageTitle = parentRow.row_columns.get(0).items.get(0).label;
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

	private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView absListView, int i) {
			InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (getCurrentFocus() != null) {
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				getCurrentFocus().clearFocus();
			}
		}

		@Override
		public void onScroll(AbsListView absListView, int i, int i1, int i2) {

		}
	};

	private OnItemSelectedListener itemSelected = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> listView, View view, int position, long id)
		{
			DebugLog.d("==================== on item selected");
			FormFillAdapter adapter = (FormFillAdapter) listView.getAdapter();
			if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_TEXT_INPUT) {
				DebugLog.d("here is the text input");
				listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
				view.findViewById(R.id.item_form_input).requestFocus();
			}
			else if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_PICTURE_RADIO){
				DebugLog.d("here is the picture");
				listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
				RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
				if (radioGroup.getCheckedRadioButtonId() == R.id.radioNOK)
					view.findViewById(R.id.remark).requestFocus();
				else {
					if (!listView.isFocused()) {
						// listView.setItemsCanFocus(false);
						// Use beforeDescendants so that the EditText doesn't re-take focus
						listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
						listView.requestFocus();
					}
				}

			} else {
				if (!listView.isFocused()) {
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

	private OnItemClickListener searchClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			list.setSelection(indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
			DebugLog.d("===== selected : "+parent.getItemAtPosition(position)+" | "+indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
		}
	};

	private OnClickListener photoClickListener = new OnClickListener() {
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

	private OnClickListener uploadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!GlobalVar.getInstance().anyNetwork(activity)) {
				TowerApplication.getInstance().toast(getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT);
				return;
			}
			int pos = (int)v.getTag(); DebugLog.d("pos = "+pos);
			ItemFormRenderModel itemFormRenderModel = adapter.getItem(pos);
			FormValueModel uploadItem = itemFormRenderModel.getItemValue();
			if (uploadItem != null)
				new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, uploadItem.itemId, null, null).execute();
			else
				TowerApplication.getInstance().toast(getString(R.string.error_no_item), Toast.LENGTH_LONG);
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