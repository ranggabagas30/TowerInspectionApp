package com.sap.inspection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.event.UploadProgressEvent;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.manager.ItemUploadManager;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.ItemUpdateResultViewModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.model.value.Pair;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.util.Utility;
import com.sap.inspection.view.FormItem;
import com.sap.inspection.view.PhotoItemRadio;
import com.sap.inspection.views.adapter.FormFillAdapter;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;

public class FormFillActivity extends BaseActivity implements FormTextChange{

	public static final int REQUEST_CODE = 100;
	private static final int MenuShootImage = 101;
	private LinearLayout root;
	private RowModel rowModel;
	private ArrayList<ColumnModel> column;
	private int workFormGroupId;
    private String scheduleId;
    private String workFormGroupName;
	private int rowId;
	private ScheduleBaseModel schedule;
	private ItemValueModel itemValueForShare;
	private Uri mImageUri;
	private HashMap<Integer, ItemUpdateResultViewModel> itemValuesProgressView;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private ArrayList<ItemFormRenderModel> formModels;
	private PhotoItemRadio photoItem;
	private ItemUploadManager itemUploadManager;
	private ScrollView scroll;
	private AutoCompleteTextView search;
	private ListView list;
	private View searchView;
	private File photo;
	private FormFillAdapter adapter;

	private LatLng currentGeoPoint;
	private int accuracy;
	private String make;
	private String model;
	private String imei;
	private TextView title;
	private String pageTitle;
	private boolean finishInflate;

	private ProgressDialog progressDialog;

	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;

	private Button mBtnSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("");
		if (indexes == null)
			indexes = new ArrayList<Integer>();
		indexes.add(0);
		if (labels == null)
			labels = new ArrayList<String>();
		if (formItems == null)
			formItems = new ArrayList<FormItem>();
		if (formModels == null)
			formModels = new ArrayList<ItemFormRenderModel>();

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(connectionCallbacks)
				.addOnConnectionFailedListener(onConnectionFailedListener)
				.build();

		setCurrentGeoPoint(new LatLng(0, 0));
		setContentView(R.layout.activity_form_fill);

		searchView = findViewById(R.id.layout_search);
		list = (ListView) findViewById(R.id.list);
		list.setOnItemSelectedListener(itemSelected);
		list.setOnScrollListener(onScrollListener);
		adapter = new FormFillAdapter(this);
		adapter.setPhotoListener(photoClickListener);
        adapter.setUploadListener(uploadClickListener);
		list.setAdapter(adapter);
		progressDialog = new ProgressDialog(activity);
		Bundle bundle = getIntent().getExtras();
		rowId = bundle.getInt("rowId");
		workFormGroupId = bundle.getInt("workFormGroupId");
        workFormGroupName = bundle.getString("workFormGroupName");
        scheduleId = bundle.getString("scheduleId");

		/*DbRepository.getInstance().open(activity);
		DbRepositoryValue.getInstance().open(activity);*/

		DebugLog.d("rowId="+rowId+" workFormGroupId="+workFormGroupId+" scheduleId="+scheduleId);
		schedule = new ScheduleGeneral();
		schedule = schedule.getScheduleById(scheduleId);
		adapter.setWorkType(schedule.work_type.name);
		adapter.setWorkFormGroupId(workFormGroupId);
		DebugLog.d("workFormGroupName : " + workFormGroupName);
        adapter.setWorkFormGroupName(workFormGroupName);
		scroll = (ScrollView) findViewById(R.id.scroll);
		search = (AutoCompleteTextView) findViewById(R.id.search);
		search.setOnItemClickListener(searchClickListener);
		root = (LinearLayout) findViewById(R.id.root);
		title = (TextView) findViewById(R.id.header_title);
		mBtnSettings = (Button) findViewById(R.id.btnsettings);
		mBtnSettings.setOnClickListener(view -> {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
		});

		progressDialog.setMessage("Generating form...");
		progressDialog.setCancelable(false);
		try {
			progressDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FormLoader loader = new FormLoader();
		loader.execute();
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

		    }
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
		}

		public void onNothingSelected(AdapterView<?> listView)
		{
			DebugLog.d("==================== on nothing selected");
		    // This happens when you start scrolling, so we need to prevent it from staying
		    // in the afterDescendants mode if the EditText was focused 
		    listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	};


	private void setPercentage(int rowId){
		if (null != itemValuesProgressView.get(rowId) && finishInflate){
			int taskDone = itemValueForShare.countTaskDone(schedule.id, rowId);
			if (taskDone != 0){
				itemValuesProgressView.get(rowId).colored.setText(String.valueOf(
						itemValuesProgressView.get(rowId).getPercentage(
								taskDone)+"%"));
				//TODO change to match the database
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				itemValuesProgressView.get(rowId).plain.setText(" on "+df.format(Calendar.getInstance().getTime().getTime()));
			}else{
				itemValuesProgressView.get(rowId).colored.setText("");
				itemValuesProgressView.get(rowId).plain.setText("No action yet");
			}

		}
	}

	OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getTag() != null){
				DebugLog.d((String)buttonView.getTag());
				String[] split = ((String)buttonView.getTag()).split("[|]");
				for (int i = 0; i < split.length; i++) {
					DebugLog.d("=== "+split[i]);
				}
				saveValue(split, isChecked, true);
			}
		}
	};

	private void saveValue(String[] itemProperties,boolean isAdding,boolean isCompundButton){


		if (itemProperties.length < 5){
			DebugLog.d("invalid component to saved");
			return;
		}
		//
		if (itemValueForShare == null)
			itemValueForShare = new ItemValueModel();

		itemValueForShare = itemValueForShare.getItemValue(schedule.id, Integer.parseInt(itemProperties[1]), Integer.parseInt(itemProperties[2]));
		if (itemValueForShare == null){
			itemValueForShare = new ItemValueModel();
			itemValueForShare.scheduleId = schedule.id;
			itemValueForShare.rowId = Integer.parseInt(itemProperties[0]);
			itemValueForShare.itemId = Integer.parseInt(itemProperties[1]);
			itemValueForShare.operatorId = Integer.parseInt(itemProperties[2]);
			itemValueForShare.value = "";
			itemValueForShare.typePhoto = itemProperties[4].equalsIgnoreCase("1");
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
				itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
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
					itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
					itemValueForShare.save();
				}
			}
		}
		else{
			if (!isAdding)
				itemValueForShare.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
			else{
				itemValueForShare.value = itemProperties[3];
				itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}
		}
		DebugLog.d("===== value : "+itemValueForShare.value);
		DebugLog.d("row id : "+ itemValueForShare.rowId);
		DebugLog.d("task done : "+itemValueForShare.countTaskDone(schedule.id, itemValueForShare.rowId));
		setPercentage(itemValueForShare.rowId);
	}

	private int getTaskDone(int rowId,String scheduleId){
		ItemValueModel valueModel = new ItemValueModel();
		return valueModel.countTaskDone(scheduleId, rowId);

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

	public void onEvent(UploadProgressEvent event) {
		DebugLog.d("event="+new Gson().toJson(event));
	}

	@Override
	protected void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*DbRepository.getInstance().open(activity);
		DbRepositoryValue.getInstance().open(activity);*/
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		googleApiClient.disconnect();
		/*DbRepository.getInstance().close();
		DbRepositoryValue.getInstance().close();*/
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		googleApiClient.connect();
	}

	OnClickListener photoClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (Utility.checkGpsStatus(FormFillActivity.this) || Utility.checkNetworkStatus(FormFillActivity.this)) {
				photoItem = (PhotoItemRadio) v.getTag();

				if (Utility.isReadWriteStoragePermissionGranted(FormFillActivity.this)) {

					takePicture(photoItem.getItemId());

				} else {

					if (ActivityCompat.shouldShowRequestPermissionRationale(FormFillActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
							&& ActivityCompat.shouldShowRequestPermissionRationale(FormFillActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

						// Show an explanation to the user *asynchronously* -- don't block
						// this thread waiting for the user's response! After the user
						// sees the explanation, try again to request the permission.

					} else {

						ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.RC_STORAGE_PERMISSION);
					}
				}

			} else {
				new LovelyStandardDialog(FormFillActivity.this,R.style.CheckBoxTintTheme)
						.setTopColor(color(R.color.theme_color))
						.setButtonsColor(color(R.color.theme_color))
						.setIcon(R.drawable.logo_app)
						//String informasi GPS
						.setTitle(getString(R.string.informationGPS))
						.setMessage(getString(R.string.enableGPS))
						.setPositiveButton(android.R.string.yes, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent gpsOptionsIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(gpsOptionsIntent);
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.show();
			}
		}
	};

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
			DebugLog.d("");
			if (!GlobalVar.getInstance().anyNetwork(activity)) {
				//string checkConnection
				MyApplication.getInstance().toast(getString(R.string.checkConnection), Toast.LENGTH_SHORT);
				return;
			}
			int pos = (int)v.getTag();
			DebugLog.d("pos="+pos);
			ItemFormRenderModel itemFormRenderModel = adapter.getItem(pos);
			if (itemFormRenderModel.workItemModel.disable) {
				//item is disable
				Toast.makeText(activity, "Item di kunci", Toast.LENGTH_LONG).show();
			}
			//else if (itemFormRenderModel.itemValue!=null && itemFormRenderModel.itemValue.value!=null) {
			else if (itemFormRenderModel.itemValue!=null) {
				DebugLog.d("itemId=" + itemFormRenderModel.itemValue.itemId+" pos=" + pos + " hasPicture=" + itemFormRenderModel.hasPicture +
						" value=" + itemFormRenderModel.itemValue.value + " picture=" +
						itemFormRenderModel.itemValue.picture + " photoStatus=" + itemFormRenderModel.itemValue.photoStatus);
				ItemUploadManager.getInstance().addItemValue(itemFormRenderModel.itemValue);
			} else {
				Toast.makeText(activity, "Tidak ada foto", Toast.LENGTH_LONG).show();
			}
        }
    };


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == Constants.RC_STORAGE_PERMISSION) {

			boolean isStoragePermissionAllowed = true;

			for (int i = 0; i < permissions.length; i++) {

				String permission = permissions[i];
				int grantResult = grantResults[i];

				if (
						(permission.equalsIgnoreCase(Manifest.permission.READ_EXTERNAL_STORAGE)
						&& grantResult == PackageManager.PERMISSION_DENIED) ||

						(permission.equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)
						&& grantResult == PackageManager.PERMISSION_DENIED)
					) {

					isStoragePermissionAllowed = false;
					break;
				}
			}

			if (isStoragePermissionAllowed)

				Toast.makeText(this, "Silahkan ambil gambar (foto)", Toast.LENGTH_LONG).show();

			else {

				DebugLog.e("Permission storage failed");
				Crashlytics.log("Permissiong storage failed");
			}
		}
	}

	public boolean takePicture(int itemId){
		//		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		//		startActivityForResult(intent,CAMERA);

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		try
		{
			// place where to store camera taken picture
			photo = this.createTemporaryFile("picture-"+schedule.id+"-"+itemId+"-"+Calendar.getInstance().getTimeInMillis()+"-", ".jpg");
			mImageUri = Uri.fromFile(photo);
			photo.delete();
			DebugLog.d("photo url : "+photo.getName());
			DebugLog.d("mimage url : "+mImageUri.getPath());
		}
		catch(Exception e)
		{
			Crashlytics.logException(e);
			DebugLog.d(e.getMessage());
			DebugLog.d("Can't create file to take picture!");
//			Toast.makeText(activity, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT).show();
			return false;
		}

		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		//        intent.putExtra("crop", "true");
		intent.putExtra("outputX", 480);
		intent.putExtra("outputY", 480);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
		//start camera intent
		//	    activity.startActivityForResult(this, intent, MenuShootImage);
		activity.startActivityForResult(intent, MenuShootImage);
		return true;
	}

	private File createTemporaryFile(String part, String ext) throws Exception
	{
		File tempDir;
		String createDirectory;

		boolean createDirStatus;

		if (Utility.isExternalStorageReadOnly()) {

			DebugLog.d("external storage is read only");
			Crashlytics.log("storage is read only");

			MyApplication.getInstance().toast("Storage is read-only. Make sure it is writeble", Toast.LENGTH_LONG);

			return null;
		} else {

			if (Utility.isExternalStorageAvailable()) {

				DebugLog.d("external storage available");
				tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
				if (!tempDir.exists()) {
					DebugLog.d("using legacy path");
					tempDir = new File( "/storage/emulated/legacy/" + Environment.DIRECTORY_DCIM + "/Camera/");
				}

				createDirectory = tempDir.getAbsolutePath() + "/TowerInspection";
				tempDir = new File(createDirectory); // create temp folder

				if (!tempDir.exists()) {
					createDirStatus = tempDir.mkdir();
					if (!createDirStatus) {
						createDirStatus = tempDir.mkdirs();
						if (!createDirStatus) {
							DebugLog.e("failed to create dir : " + createDirectory);
							Crashlytics.log("fail to create dir : " + createDirectory);
						} else {
							DebugLog.d("create dir success");
						}
					}
				}

				createDirectory = tempDir.getAbsolutePath() + "/" + schedule.id + "/";
				tempDir = new File(createDirectory); // create schedule folder

				if (!tempDir.exists()) {
					createDirStatus = tempDir.mkdir();
					if (!createDirStatus) {
						createDirStatus = tempDir.mkdirs();
						if (!createDirStatus) {
							DebugLog.e("failed to create dir : " + createDirectory);
							Crashlytics.log("failed to create dir : " + createDirectory);
						} else {
							DebugLog.d("create dir success");
						}
					}
				}

				DebugLog.d("tempDir path : " + tempDir.getAbsolutePath());
				return File.createTempFile(part, ext, tempDir);

			} else {

				Crashlytics.log("storage is not available");
				MyApplication.getInstance().toast("Storage is not available", Toast.LENGTH_LONG);
				return null;
			}

		}

	}

	//called after camera intent finished
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		String siteLatitude = String.valueOf(currentGeoPoint.latitude);;
		String siteLongitude = String.valueOf(currentGeoPoint.longitude);
		Pair<String, String> photoLocation;
		if(requestCode==MenuShootImage && resultCode==RESULT_OK)
		{
			if (photoItem != null && mImageUri != null){
				photoItem.initValue();
				photoItem.deletePhoto();

				if (MyApplication.getInstance().isScheduleNeedCheckIn()) {
					photoLocation = Utility.getPersistentLocation(scheduleId);
					if (photoLocation != null) {
						siteLatitude  = photoLocation.first();
						siteLongitude = photoLocation.second();
					} else {
						Crashlytics.log(Log.ERROR, "photolocation", "Persistent photo location error (null)");
					}
				}

				String[] textMarks = new String[3];
				String photoDate = photoItem.setPhotoDate();
				String latitude = siteLatitude;
				String longitude = siteLongitude;

				textMarks[0] = "Lat. : "+  latitude + ", Long. : "+ longitude;
				//textMarks[1] = "Accurate up to : "+accuracy+" meters";
				textMarks[1] = "Distance to site : " + MyApplication.getInstance().checkinDataModel.getDistance() + " meters";
				textMarks[2] = "Photo date : "+photoDate;

				File file = ImageUtil.resizeAndSaveImageCheckExifWithMark(this, photo.getName(), schedule.id, textMarks);

				if (null != file) {

					if (Utility.isExternalStorageAvailable()) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
							Intent mediaScanIntent = new Intent(
									Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
							Uri contentUri = Uri.fromFile(file);
							mediaScanIntent.setData(contentUri);
							this.sendBroadcast(mediaScanIntent);
						} else {
							sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
									Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/TowerInspection/")));
						}
					}

					DebugLog.d( latitude+" || "+longitude);
					if (!Utility.isCurrentLocationError(latitude, longitude)) {
						photoItem.setPhotoDate();
						photoItem.setImage(mImageUri.toString(),latitude,longitude,accuracy);
					} else {
						MyApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_SHORT);
					}
				} else {
					MyApplication.getInstance().toast("Pengambilan foto gagal. Silahkan ulangi kembali", Toast.LENGTH_SHORT);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/*
	 * called when image is stored
	 */
	public boolean storeByteImage(byte[] data){
		// Create the <timestamp>.jpg file and modify the exif data
		String filename = "/sdcard"+String.format("/%d.jpg", System.currentTimeMillis());
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			try {
				fileOutputStream.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			ExifInterface exif = new ExifInterface(filename);
			createExifData(exif);
			exif.saveAttributes();
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * called when exif data profile is created
	 */
	public void createExifData(ExifInterface exif){
		// create a reference for Latitude and Longitude
		double lat = currentGeoPoint.latitude;
		if (lat < 0) {
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
			lat = -lat;
		} else {
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
		}

		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
				formatLatLongString(lat));

		double lon = currentGeoPoint.longitude;
		if (lon < 0) {
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
			lon = -lon;
		} else {
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
		}
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
				formatLatLongString(lon));
		try {
			exif.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		make = android.os.Build.MANUFACTURER; // get the make of the device
		model = android.os.Build.MODEL; // get the model of the divice

		exif.setAttribute(ExifInterface.TAG_MAKE, make);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		exif.setAttribute(ExifInterface.TAG_MODEL, model+" - "+imei);

		exif.setAttribute(ExifInterface.TAG_DATETIME, (new Date(System.currentTimeMillis())).toString()); // set the date & time

	}

	/*
	 * format the Lat Long values according to standard exif format
	 */
	private static String formatLatLongString(double d) {
		// format latitude and longitude according to exif format
		StringBuilder b = new StringBuilder();
		b.append((int) d);
		b.append("/1,");
		d = (d - (int) d) * 60;
		b.append((int) d);
		b.append("/1,");
		d = (d - (int) d) * 60000;
		b.append((int) d);
		b.append("/1000");
		return b.toString();
	}

	/*
	public int initiateLocation(){
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
//			setCurrentGeoPoint(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.GPS_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.GPS_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy();
		}
		else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
//			setCurrentGeoPoint(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.NETWORK_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.NETWORK_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getAccuracy();
		}
		setCurrentGeoPoint(new LatLng(0,0));
		return 0;

	}*/

	public void setCurrentGeoPoint(LatLng currentGeoPoint) {
		this.currentGeoPoint = currentGeoPoint;
	}

	public LatLng getCurrentGeoPoint() {
		return currentGeoPoint;
	}

	private class FormLoader extends AsyncTask<Void, Integer, Void>{
		String lastLable = null;

		@Override
		protected Void doInBackground(Void... params) {
			rowModel = new RowModel(FormFillActivity.this);
			rowModel = rowModel.getItemById(workFormGroupId, rowId);
			ColumnModel colModel = new ColumnModel();
			column = colModel.getAllItemByWorkFormGroupId(workFormGroupId);
			ItemFormRenderModel form;
			setPageTitle();
			//check if the head has a form
			for(int i = 0; i < rowModel.row_columns.size(); i++){
				if (rowModel.row_columns.get(i).items.size() > 0){
					finishInflate = false;
					DebugLog.d("-----------------------------------------------");
					DebugLog.d("========================= head row id : "+rowModel.id);
					DebugLog.d("========================= head row ancestry : "+rowModel.ancestry);
					checkHeaderName(rowModel);
					DebugLog.d("-----------------------------------------------");
					form = new ItemFormRenderModel();
					form.setSchedule(schedule);
					form.setColumn(column);
					form.setRowColumnModels(rowModel.row_columns, null);
					if (form.hasInput){
						DebugLog.d("========================= head row has input : ");
						indexes.add(indexes.get(indexes.size()-1) + form.getCount());
						labels.add(form.getLabel());
						formModels.add(form);
					}
					else if (form.hasPicture){
						DebugLog.d("========================= head row has picture : ");
						labels.add(form.getLabel());
						formModels.add(form);
					}
					break;
				}
			}

			int x = 0;
			//check if the child has a form
			String parentLabel = null;
			String ancestry = null;
			DebugLog.d("looping row childrens : ");
			DebugLog.d("\n\n========================= children size : " + rowModel.children.size());

			for (RowModel model : rowModel.children) {
				x++;
				DebugLog.d("\nchildren ke-" + x);
				DebugLog.d("checking header name ...");
				checkHeaderName(model);
				publishProgress(x*100/rowModel.children.size());
				finishInflate = false;
				DebugLog.d("-----------------------------------------------");
				DebugLog.d("========================= child row id : "+model.id);
				DebugLog.d("========================= child row ancestry : "+rowModel.ancestry);
				DebugLog.d("-----------------------------------------------");
				form = new ItemFormRenderModel();
				form.setSchedule(schedule);
				form.setColumn(column);
                form.setWorkFormGroupName(workFormGroupName);
				form.setRowColumnModels(model.row_columns,parentLabel);
				if (form.hasInput){
					indexes.add(indexes.get(indexes.size()-1) + form.getCount());
					String label = form.getLabel();
					while (labels.indexOf(label) != -1){
						label = label+".";
					}
					labels.add(label);
					formModels.add(form);
					DebugLog.d("indexes : " + indexes.get(indexes.size()-1));
					DebugLog.d("label : " + label);
					DebugLog.d("lables now : " + labels.get(labels.size()-1));
				}else
					parentLabel = form.getLabel();
//				setPercentage(model.id);
			}
			return null;
		}
		
		private void checkHeaderName(RowModel rowModel){
			if (rowModel.row_columns != null && 
					rowModel.row_columns.size() > 0 && 
					rowModel.row_columns.get(0).items != null &&
					rowModel.row_columns.get(0).items.size() > 0){
				DebugLog.d("========================= head row label : "+rowModel.row_columns.get(0).items.get(0).label);
				if (rowModel.row_columns.get(0).items.get(0).label != null && !rowModel.row_columns.get(0).items.get(0).label.equalsIgnoreCase(""))
					this.lastLable = rowModel.row_columns.get(0).items.get(0).label;
				rowModel.row_columns.get(0).items.get(0).labelHeader = this.lastLable;
				DebugLog.d("item label : " + rowModel.row_columns.get(0).items.get(0).label + ", item id : " + rowModel.row_columns.get(0).items.get(0).id);
				DebugLog.d("labelHeader : " + this.lastLable);
			}
		}

		private void checkUploadStatus(RowModel rowModel) {

		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setMessage("Generating form "+values[0]+" % complete");
		}

		@Override
		protected void onPostExecute(Void result) {
			title.setText(pageTitle);
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
			adapter.setItems(formModels);
			boolean ada = false;
			DebugLog.d("total formModels items : " + formModels.size());
			for (ItemFormRenderModel item : formModels) {
				DebugLog.d("search="+item.workItemModel.search);
				if (item.workItemModel!=null&&!item.workItemModel.search) {
					ada = true;
					break;
				}
			}
			if (ada)
				searchView.setVisibility(View.GONE);
//			SearchAdapter searchAdapter = new SearchAdapter(activity, android.R.layout.select_dialog_item, android.R.id.text1, indexes);
			ArrayAdapter<String> searchAdapter =
					new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, labels){

				public View getView(int position, View convertView, ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					int i = 10;
					((TextView) v).setTextSize(14);

//					Typeface Type = getFont () ;  // custom method to get a font from "assets" folder
//					((TextView) v).setTypeface(Type);
//					((TextView) v).setTextColor(Color.RED);
					((TextView) v) .setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);

					return v;
				}
			};

			search.setAdapter(searchAdapter);
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
		if (rowModel.row_columns.size() > 0 && rowModel.row_columns.get(0).items.size() > 0)
			pageTitle = rowModel.row_columns.get(0).items.get(0).label;
	}

	private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnected(@Nullable Bundle bundle) {
			DebugLog.d("");
			locationRequest = LocationRequest.create();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setInterval(5000); // Update location every second
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
			DebugLog.d("connectionResult="+connectionResult.toString());
		}
	};

	private com.google.android.gms.location.LocationListener locationListener = new com.google.android.gms.location.LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			accuracy = (int)location.getAccuracy();
			setCurrentGeoPoint(new LatLng(location.getLatitude(), location.getLongitude()));
			DebugLog.d(String.valueOf(getCurrentGeoPoint().latitude)+" || "+String.valueOf(getCurrentGeoPoint().longitude));
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
			String mandatoryLabel = "";
			boolean mandatoryFound = false;
			DebugLog.d("adapter size "+adapter.getCount());

			for (int i = 0; i < adapter.getCount(); i++) {

				ItemFormRenderModel item = adapter.getItem(i);

				DebugLog.d("count "+i);
				if (item.workItemModel!=null) {
					DebugLog.d("type="+item.type+" mandatory="+item.workItemModel.mandatory+
							" disable="+item.workItemModel.disable);
				}

				if (!MyApplication.getInstance().isInCheckHasilPm()) {

					if (item.itemValue!=null) {
						DebugLog.d("is in hasil pm : " + MyApplication.getInstance().isInCheckHasilPm());
						DebugLog.d("item label : " + item.workItemModel.label);
						DebugLog.d("itemValue.value="+item.itemValue.value); // belum ada foto
						DebugLog.d("scheduleId=" + item.itemValue.scheduleId);
						DebugLog.d("workFormGroupName : " + workFormGroupName);


						if (workFormGroupName.equalsIgnoreCase("Photograph") && item.type == 2) {
							DebugLog.d("photoStatus : " + item.itemValue.photoStatus);
							DebugLog.d("remark : " + item.itemValue.remark);
							if (item.workItemModel.mandatory && !item.workItemModel.disable) {
								if (!TextUtils.isEmpty(item.itemValue.photoStatus) && item.itemValue.photoStatus.equalsIgnoreCase("nok")) {
									if (item.itemValue.remark == null) {
										mandatoryLabel = item.workItemModel.label;
										mandatoryFound = true;
										break;
									} else {
										if (item.itemValue.remark.isEmpty()) {
											mandatoryLabel = item.workItemModel.label;
											mandatoryFound = true;
											break;
										}
									}
								}
							}
						}

						if (workFormGroupName.equalsIgnoreCase("Equipment Checklist")) {

							DebugLog.d("isian data : " + item.itemValue.value);

							if (item.workItemModel.mandatory && !item.workItemModel.disable) {
								if (item.itemValue.value == null || item.itemValue.value.isEmpty()) {
									mandatoryLabel = item.workItemModel.label;
									mandatoryFound = true;
									break;
								}
							}
						}
					}

					if (list.contains(item.type) && !workFormGroupName.equalsIgnoreCase("Photograph")) {
						if (item.itemValue == null || item.itemValue.value == null || item.itemValue.value.isEmpty()) {
							if (item.workItemModel != null && item.workItemModel.mandatory && !item.workItemModel.disable) {
								mandatoryLabel = item.workItemModel.label;
								mandatoryFound = true;
								break;
							}
						}
					}
				}

			}

			DebugLog.d("mandatoryFound="+mandatoryFound);

			if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {

				if (!mandatoryFound)
					super.onBackPressed();
				else {
					Toast.makeText(activity, mandatoryLabel + " wajib diisi", Toast.LENGTH_SHORT).show();
				}

			} else {
				super.onBackPressed();
			}
		} else
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