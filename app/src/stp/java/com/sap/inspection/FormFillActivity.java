package com.sap.inspection;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
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
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.tools.PersistentLocation;
import com.sap.inspection.util.ExifUtil;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.PermissionUtil;
import com.sap.inspection.view.FormItem;
import com.sap.inspection.view.PhotoItemRadio;
import com.sap.inspection.views.adapter.FormFillAdapter;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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
	public ItemValueModel PhotographModel;
	private Uri mImageUri;
	private HashMap<Integer, ItemUpdateResultViewModel> itemValuesProgressView;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private ArrayList<ItemFormRenderModel> formModels;
	private PhotoItemRadio photoItem;
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

	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;

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
            scheduleId 			= bundle.getString(Constants.KEY_SCHEDULEID);

            DebugLog.d("received bundle : ");
            DebugLog.d("rowId = " + rowId);
            DebugLog.d("workFormGroupId = " + workFormGroupId);
            DebugLog.d("workFormGroupName = " + workFormGroupName);
            DebugLog.d("scheduleId = " + scheduleId);
        }

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
        scroll = (ScrollView) findViewById(R.id.scroll);
        searchView = findViewById(R.id.layout_search);
        search = (AutoCompleteTextView) findViewById(R.id.search);
        search.setOnItemClickListener(searchClickListener);
        root = (LinearLayout) findViewById(R.id.root);
        title = (TextView) findViewById(R.id.header_title);
        mBtnSettings = (Button) findViewById(R.id.btnsettings);
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
		        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		        if (radioGroup.getCheckedRadioButtonId() == R.id.radioNOK)
		        	view.findViewById(R.id.remark).requestFocus();
		        else {
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

	private void saveValue(String[] itemProperties,boolean isAdding,boolean isCompundButton){

		if (itemProperties.length < 5){
			DebugLog.d("invalid component to saved");
			return;
		}
		//
		if (itemValueForShare == null) {
			itemValueForShare = new ItemValueModel();
		}

		itemValueForShare = itemValueForShare.getItemValue(schedule.id, Integer.parseInt(itemProperties[1]), Integer.parseInt(itemProperties[2]));
		if (itemValueForShare == null){

			itemValueForShare = new ItemValueModel();
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

                if (CommonUtil.isReadWriteStoragePermissionGranted(FormFillActivity.this)) {

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
                        .setPositiveButton(android.R.string.yes, v1 -> {
                            Intent gpsOptionsIntent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsOptionsIntent);
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
				MyApplication.getInstance().toast(getResources().getString(R.string.checkConnection), Toast.LENGTH_SHORT);
				return;
			}
			int pos = (int)v.getTag();
			ItemFormRenderModel itemFormRenderModel = adapter.getItem(pos);
			if (itemFormRenderModel.workItemModel.disable){
				Toast.makeText(activity, "Item di kunci", Toast.LENGTH_LONG).show();
			}
			else if (itemFormRenderModel.itemValue!=null) {

				ItemValueModel itemUpload = itemFormRenderModel.itemValue;
				ItemUploadManager.getInstance().addItemValue(itemFormRenderModel.workItemModel, itemFormRenderModel.itemValue);

				DebugLog.d("isMandatory= " + itemFormRenderModel.workItemModel.mandatory + " itemId = " + itemUpload.itemId + " pos = " + pos + " hasPicture = " + itemFormRenderModel.hasPicture + " value = " + itemUpload.value + " picture = " + itemUpload.picture + " photoStatus = " + itemUpload.photoStatus);

			} else {
				Toast.makeText(activity, "Tidak ada foto", Toast.LENGTH_LONG).show();
			}
        }
    };


    public boolean takePicture(int itemId){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(this.getPackageManager()) != null) {

            photo = null;
            try
            {
                // place where to store camera taken picture
                photo = createTemporaryFile("picture-"+schedule.id+"-"+itemId+"-"+Calendar.getInstance().getTimeInMillis()+"-", ".jpg");

            }
            catch(Exception e)
            {
                Crashlytics.logException(e);
                DebugLog.d(e.getMessage());
                DebugLog.d("Can't create file to take picture!");
                return false;
            }

            if (photo != null) {
                mImageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", photo);
                DebugLog.d("photo url : " + photo.getName());
                DebugLog.d("mimage url : " + mImageUri.getPath());

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                //        intent.putExtra("crop", "true");
                intent.putExtra("outputX", 480);
                intent.putExtra("outputY", 480);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                activity.startActivityForResult(intent, MenuShootImage);

                return true;
            }
        }

        return false;
    }

    private File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir;
        boolean createDirStatus;

        if (CommonUtil.isExternalStorageReadOnly()) {

            DebugLog.d("external storage is read only");
            Crashlytics.log("storage is read only");

            MyApplication.getInstance().toast("Storage is read-only. Make sure it is writeble", Toast.LENGTH_LONG);

            return null;
        } else {

            if (CommonUtil.isExternalStorageAvailable()) {

                DebugLog.d("external storage available");

                tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                tempDir = new File(tempDir, Constants.FOLDER_TOWER_INSPECTION); // create temp folder
                tempDir = new File(tempDir, schedule.id); // create schedule folder

                if (!tempDir.exists()) {
                    createDirStatus = tempDir.mkdir();
                    if (!createDirStatus) {
                        createDirStatus = tempDir.mkdirs();
                        if (!createDirStatus) {
                            DebugLog.e("failed to create dir : " + tempDir.getPath());
                            Crashlytics.log("failed to create dir : " + tempDir.getPath());
                        } else {
                            DebugLog.d("success create dir : " + tempDir.getPath());
                        }
                    }
                }

                DebugLog.d("tempDir path : " + tempDir.getPath());
                DebugLog.d("tempDir absolute path : " + tempDir.getAbsolutePath());
                DebugLog.d("tempDir canonical path : " + tempDir.getCanonicalPath());
                DebugLog.d("tempDir string path : " + tempDir.toString());

                return File.createTempFile(part, ext, tempDir);

            } else {

                Crashlytics.log("storage is not available");
                MyApplication.getInstance().toast("Storage is not available", Toast.LENGTH_LONG);
                return null;
            }

        }

    }

	//called after camera intent finished
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

				if (MyApplication.getInstance().isScheduleNeedCheckIn()) {
					photoLocation = CommonUtil.getPersistentLocation(scheduleId);
					if (photoLocation != null) {
						siteLatitude  = photoLocation.first();
						siteLongitude = photoLocation.second();
					} else {
						Crashlytics.log(Log.ERROR, "photolocation", "Persistent photo location error (null)");
					}
				}

				String[] textMarks = new String[3];
				//String photoDate = photoItem.getPhotoDate();
				String photoDate = DateTools.getCurrentDate();
				String latitude = siteLatitude;
				String longitude = siteLongitude;

				textMarks[0] = "Lat. : "+  latitude + ", Long. : "+ longitude;
				//textMarks[1] = "Accurate up to : "+accuracy+" meters";
				textMarks[1] = "Distance to site : " + MyApplication.getInstance().checkinDataModel.getDistance() + " meters";
				textMarks[2] = "Photo date : "+photoDate;

				File file = ImageUtil.resizeAndSaveImageCheckExifWithMark(this, photo.getName(), schedule.id, textMarks);

				if (null != file) {

					if (CommonUtil.isExternalStorageAvailable()) {
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

					if (!CommonUtil.isCurrentLocationError(latitude, longitude)) {
						photoItem.deletePhoto();
						photoItem.setImage(photo, latitude, longitude, accuracy);

					} else {

						DebugLog.e("location error : " + this.getResources().getString(R.string.sitelocationisnotaccurate));
						MyApplication.getInstance().toast(this.getResources().getString(R.string.sitelocationisnotaccurate), Toast.LENGTH_SHORT);
					}

					return;
				}

				DebugLog.e("file = null. Pengambilan foto gagal. Silahkan ulangi kembali");
				MyApplication.getInstance().toast("Pengambilan foto gagal. Silahkan ulangi kembali", Toast.LENGTH_SHORT);
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
			rowModel = new RowModel();
			rowModel = rowModel.getAllItemsByRowId(workFormGroupId, rowId);
			column = ColumnModel.getAllItemByWorkFormGroupId(workFormGroupId);

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
					form.setWorkFormGroupName(workFormGroupName);
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
			for (RowModel model : rowModel.children) {
				checkHeaderName(model);
				x++;
				publishProgress(x*100/rowModel.children.size());
				finishInflate = false;
				DebugLog.d("-----------------------------------------------");
				DebugLog.d("========================= child row id : "+model.id);
				DebugLog.d("========================= child row ancestry : "+rowModel.ancestry);
				DebugLog.d("-----------------------------------------------");
				form = new ItemFormRenderModel();
				form.setSchedule(schedule);
				form.setColumn(column);
				form.setRowColumnModels(model.row_columns,parentLabel);
				if (form.hasInput){
					indexes.add(indexes.get(indexes.size()-1) + form.getCount());
					String label = form.getLabel();
					while (labels.indexOf(label) != -1){
						label = label+".";
					}
					labels.add(label);
					formModels.add(form);
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

			ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, labels);
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

        if (adapter!=null && !adapter.isEmpty() && !MyApplication.getInstance().isInCheckHasilPm()) {

            DebugLog.d("\n\n ==== ON BACK PRESSED ====");
            DebugLog.d("scheduleId = " + scheduleId);
            DebugLog.d("workFormGroupName = " + workFormGroupName);
            DebugLog.d("is in check hasil pm ? " + MyApplication.getInstance().isInCheckHasilPm());
            DebugLog.d("Jumlah item adapter : " + adapter.getCount());

            String mandatoryLabel = "";
            boolean mandatoryFound = false;

            for (int i = 0; i < adapter.getCount(); i++) {

                ItemFormRenderModel item = adapter.getItem(i);

                DebugLog.d("no. "+ i);
                DebugLog.d("\titem type = " + item.type);
                if (item.workItemModel!=null) {
                    DebugLog.d("\titem label = " + item.workItemModel.label);
                    DebugLog.d("\titem isMandatory = " + item.workItemModel.mandatory);
                    DebugLog.d("\titem isDisabled = " + item.workItemModel.disable);
                } else
                    DebugLog.d("\titem workitemmodel = null");

                if (item.itemValue != null && !TextUtils.isEmpty(item.itemValue.value)) DebugLog.d("\titem value = " +item.itemValue.value);

                if (list.contains(item.type) && item.workItemModel != null) {
                    boolean isMandatory = item.workItemModel.mandatory;
                    if (item.itemValue == null && isMandatory) {
                        mandatoryLabel = item.workItemModel.label;
                        mandatoryFound = true;
						Toast.makeText(activity, mandatoryLabel + " wajib diisi", Toast.LENGTH_SHORT).show();
                        break;
                    } else if (item.itemValue != null) {

                        ItemValueModel filledItem = item.itemValue;

                        if (isMandatory) {

                            if (TextUtils.isEmpty(filledItem.value)) {

                                // no photo file and photo status not equal "NA"
                                if (item.type == ItemFormRenderModel.TYPE_PICTURE_RADIO && !TextUtils.isEmpty(item.itemValue.photoStatus) && !item.itemValue.photoStatus.equalsIgnoreCase(Constants.NA)) {
                                    mandatoryLabel = item.workItemModel.label;
                                    mandatoryFound = true;
                                    MyApplication.getInstance().toast("Photo item" + mandatoryLabel + " harus ada", Toast.LENGTH_SHORT);
                                    break;
                                } else if (item.type == ItemFormRenderModel.TYPE_PICTURE_RADIO && !ItemValueModel.isPictureRadioItemValidated(item.workItemModel, item.itemValue)){
                                    mandatoryLabel = item.workItemModel.label;
                                    mandatoryFound = true;
                                    break;
                                }
                            }
                        } else {

                            if (item.type == ItemFormRenderModel.TYPE_PICTURE_RADIO && !ItemValueModel.isPictureRadioItemValidated(item.workItemModel, item.itemValue)){
                                mandatoryLabel = item.workItemModel.label;
                                mandatoryFound = true;
                                break;
                            }
                        }
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