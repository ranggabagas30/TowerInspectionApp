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
import android.util.Log;
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

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.model.value.Pair;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.DialogUtil;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.adapter.FormFillAdapter;
import com.sap.inspection.view.customview.FormItem;
import com.sap.inspection.view.customview.PhotoItemRadio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FormFillActivity extends BaseActivity implements FormTextChange{

	// bundle data
	private String scheduleId;
	private String workFormGroupName;
	private String workTypeName;
	private int workFormGroupId;
	private int rowId;

	private ScheduleBaseModel schedule;
	private WorkFormRowModel parentRow;
	private ArrayList<ColumnModel> column;
	private FormValueModel itemValueForShare;
	private Uri mImageUri;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private ArrayList<ItemFormRenderModel> formModels;
	private File photoFile;
	private FormFillAdapter adapter;


	private String pageTitle;
	private LatLng currentGeoPoint;
	private int accuracy;
	private boolean finishInflate;
	private boolean isChecklistOrSiteInformation;
	private boolean isMandatoryCheckingActive = false;

	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;

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
        setCurrentGeoPoint(new LatLng(0, 0));
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

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();

        // init schedule
        schedule = new ScheduleGeneral();
        schedule = schedule.getScheduleById(scheduleId);

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

        new FormLoader().execute();
	}
	
	OnItemSelectedListener itemSelected = new OnItemSelectedListener() {

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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		DebugLog.d("onStop");
		googleApiClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onStart() {
		DebugLog.d("onStart");
		super.onStart();
		// Connect the client.
		googleApiClient.connect();
	}

	OnClickListener photoClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (CommonUtil.checkGpsStatus(FormFillActivity.this) || CommonUtil.checkNetworkStatus(FormFillActivity.this)) {
				photoItem = (PhotoItemRadio) v.getTag();
				takePicture(photoItem.getItemId());
			} else
				DialogUtil.gpsDialog(FormFillActivity.this).show();
		}
	};

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
			if (!GlobalVar.getInstance().anyNetwork(activity)) {
				MyApplication.getInstance().toast(getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT);
				return;
			}
			int pos = (int)v.getTag(); DebugLog.d("pos = "+pos);
			ItemFormRenderModel itemFormRenderModel = adapter.getItem(pos);
			FormValueModel uploadItem = itemFormRenderModel.itemValue;
			if (uploadItem != null)
				new FormValueModel.AsyncCollectItemValuesForUpload(scheduleId, workFormGroupId, uploadItem.itemId, null, null).execute();
			else
				MyApplication.getInstance().toast(getString(R.string.error_no_item), Toast.LENGTH_LONG);
        }
    };

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
				mImageUri = FileUtil.getUriFromFile(this, photoFile);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				intent.putExtra("outputX", 480);
				intent.putExtra("outputY", 480);
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("scale", true);
				intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivityForResult(intent, Constants.RC_TAKE_PHOTO);
				return;
			} catch (NullPointerException npe) {
				DebugLog.e("take picture: " + npe.getMessage());
			} catch (IOException e) {
				DebugLog.e("take picture: " + e.getMessage());
			} catch (IllegalArgumentException ilae) {
				DebugLog.e("take picture: " + ilae.getMessage());
			}
		}

		// if failed, then show toast with failed message
		Toast.makeText(activity, getString(R.string.error_take_picture), Toast.LENGTH_SHORT).show();

    }

	//called after camera intent finished
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String siteLatitude = String.valueOf(currentGeoPoint.latitude);
		String siteLongitude = String.valueOf(currentGeoPoint.longitude);
		Pair<String, String> photoLocation;

		if(requestCode == Constants.RC_TAKE_PHOTO && resultCode == RESULT_OK) {

			if (photoItem != null && mImageUri != null){
				if (MyApplication.getInstance().isScheduleNeedCheckIn()) {
					photoLocation = CommonUtil.getPersistentLocation(scheduleId);
					if (photoLocation != null) {
						siteLatitude  = photoLocation.first();
						siteLongitude = photoLocation.second();
					} else {
						DebugLog.e("Persistent photo location error (null)");
					}
				}

				String[] textMarks = new String[3];
				String photoDate = DateTools.getCurrentDate();
				String latitude = siteLatitude;
				String longitude = siteLongitude;

				textMarks[0] = "Lat. : "+  latitude + ", Long. : "+ longitude;
				textMarks[1] = "Distance to site : " + MyApplication.getInstance().checkinDataModel.getDistance() + " meters";
				textMarks[2] = "Photo date : "+photoDate;

				ImageUtil.resizeAndSaveImageCheckExifWithMark(this, photoFile.toString(), textMarks);

				File filePhotoResult = new File(photoFile.toString());

				if (!CommonUtil.isCurrentLocationError(latitude, longitude)) {
					photoItem.deletePhoto();
					photoItem.setImage(filePhotoResult, latitude, longitude, accuracy);
				} else {
					DebugLog.e("location error : " + this.getResources().getString(R.string.sitelocationisnotaccurate));
					MyApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_SHORT);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	public void setCurrentGeoPoint(LatLng currentGeoPoint) {
		this.currentGeoPoint = currentGeoPoint;
	}

	public LatLng getCurrentGeoPoint() {
		return currentGeoPoint;
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
			column = ColumnModel.getAllItemByWorkFormGroupId(workFormGroupId);

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
					form.setColumn(column);
					form.setWorkFormGroupName(workFormGroupName);
					form.setWorkTypeName(workTypeName);
					form.setRowColumnModels(parentRow.row_columns, null);
					DebugLog.d("has input ? " + form.hasInput);
					DebugLog.d("has picture ? " + form.hasPicture);
					if (form.hasInput){
						indexes.add(indexes.get(indexes.size()-1) + form.getCount());
						String label = form.getLabel();
						if (TextUtils.isEmpty(label)) {
							label = "item with no label";
						}
						DebugLog.d("label added : " + label + " has input");
						labels.add(label);
						formModels.add(form);
					} else if (form.hasPicture){
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
				form.setColumn(column);
				form.setWorkTypeName(workTypeName);
				form.setWorkFormGroupName(workFormGroupName);
				form.setWorkFormGroupId(workFormGroupId);
				form.setRowColumnModels(rowChildren.row_columns, parentLabel);
				DebugLog.d("has input ? " + form.hasInput);
				if (form.hasInput){
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
					if (item.workItemModel!=null&&!item.workItemModel.search) {
						DebugLog.d("search="+item.workItemModel.search);
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
	}

	private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnected(@Nullable Bundle bundle) {
			DebugLog.d("");
			locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(5000); // Update location every second
			locationRequest.setFastestInterval(3000);
			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,locationListener);
		}

		@Override
		public void onConnectionSuspended(int i) {
			DebugLog.d("i="+i);
		}

	};

	private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
			MyApplication.getInstance().toast("Koneksi google api client gagal", Toast.LENGTH_LONG);
			DebugLog.d("connectionResult="+connectionResult.toString());
		}
	};

	private com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			accuracy = (int)location.getAccuracy();
			setCurrentGeoPoint(new LatLng(location.getLatitude(), location.getLongitude()));
			DebugLog.d(getCurrentGeoPoint().latitude +" || "+ getCurrentGeoPoint().longitude);
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

		if (adapter!=null && !adapter.isEmpty()) {
			if ((MyApplication.getInstance().IS_CHECKING_HASIL_PM() && isChecklistOrSiteInformation) || !MyApplication.getInstance().IS_CHECKING_HASIL_PM()){
				isMandatoryCheckingActive = true;
			}
		}

		if (isMandatoryCheckingActive) {

			DebugLog.d("\n\n ==== ON BACK PRESSED ====");
			DebugLog.d("scheduleId = " + scheduleId);
			DebugLog.d("workFormGroupName = " + workFormGroupName);
			DebugLog.d("is in check hasil pm ? " + MyApplication.getInstance().IS_CHECKING_HASIL_PM());
			DebugLog.d("Jumlah item adapter : " + adapter.getCount());

			String mandatoryLabel = "";
			boolean mandatoryFound = false;

			for (int i = 0; i < adapter.getCount(); i++) {

				ItemFormRenderModel item = adapter.getItem(i);

				DebugLog.d("no. " + i);
				DebugLog.d("\titem type = " + item.type);
				if (item.workItemModel != null) {
					DebugLog.d("\titem label = " + item.workItemModel.label);
					DebugLog.d("\titem isMandatory = " + item.workItemModel.mandatory);
					DebugLog.d("\titem isDisabled = " + item.workItemModel.disable);
				} else
					DebugLog.d("\titem workitemmodel = null");

				if (item.itemValue != null && !TextUtils.isEmpty(item.itemValue.value))
					DebugLog.d("\titem value = " + item.itemValue.value);

				if (list.contains(item.type) && item.workItemModel != null) {
					if (!FormValueModel.isItemValueValidated(item.workItemModel, item.itemValue)) {
						mandatoryLabel = item.workItemModel.label;
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
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
				getCurrentFocus().clearFocus();
			}
		}

		@Override
		public void onScroll(AbsListView absListView, int i, int i1, int i2) {

		}
	};

}