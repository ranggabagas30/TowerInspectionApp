package com.sap.inspection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.rules.saving.PreventiveSave;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.view.FormItem;
import com.sap.inspection.view.PhotoItem;
import com.sap.inspection.views.adapter.FormFillAdapter;

import java.util.ArrayList;

//import com.google.android.maps.GeoPoint;

public class FormCorrectiveActivity extends BaseActivity {


	public static final int REQUEST_CODE = 100;
	private ScheduleBaseModel schedule;
	private Uri mImageUri;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private PhotoItem photoItem;
	private AutoCompleteTextView search;
	private ListView list;
	private ArrayList<CorrectiveValueModel> correctiveValueModels;
	private ArrayList<ItemFormRenderModel> formModels;
	
	FormFillAdapter adapter;

//	private LocationManager locationManager;
//	private LocationListener locationListener;
	private LatLng currentlocation;
	private int accuracy;
	private TextView title;

	private ProgressDialog progressDialog;

	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;

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
		/*
		locationListener = new LocationListener() {

			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub

			}

			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			public void onLocationChanged(Location location) {
				currentlocation = new LatLng(location.getLatitude(), location.getLongitude());
				// TODO Update the Latitude and Longitude of the location
				accuracy = initiateLocation();
			}
		};

		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		// initiate the location using GPS
		setCurrentLocation(new LatLng(0, 0));
		accuracy = initiateLocation();*/

		setContentView(R.layout.activity_form_fill);

		list = (ListView) findViewById(R.id.list);
//		list.setOnItemSelectedListener(itemSelected);
		adapter = new FormFillAdapter(this);
		adapter.setSavingRule(new PreventiveSave());
		adapter.setPhotoListener(photoClickListener);
		list.setAdapter(adapter);
		progressDialog = new ProgressDialog(activity);
		Bundle bundle = getIntent().getExtras();
//		rowId = bundle.getInt("rowId");
//		workFormGroupId = bundle.getInt("workFormGroupId");

		DbRepository.getInstance().open(activity);
		DbRepositoryValue.getInstance().open(activity);
		schedule = new ScheduleGeneral();
		schedule = schedule.getScheduleById(bundle.getString("scheduleId"));

		search = (AutoCompleteTextView) findViewById(R.id.search);
		search.setOnItemClickListener(searchClickListener);
		title = (TextView) findViewById(R.id.header_title);

		progressDialog.setMessage("Generating form...");
		progressDialog.show();
		progressDialog.setCancelable(false);
		
		FormLoader loader = new FormLoader();
		loader.execute();
	}

	/*
	public int initiateLocation(){
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
//			setCurrentLocation(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.GPS_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.GPS_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy();
		}
		else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
//			setCurrentLocation(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.NETWORK_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.NETWORK_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getAccuracy();
		}
		setCurrentLocation(new LatLng(0,0));
		return 0;

	}*/

	public void setCurrentLocation(LatLng currentGeoPoint) {
		this.currentlocation = currentGeoPoint;
	}
	
	public LatLng getCurrentLocation() {
		return currentlocation;
	}

	OnItemClickListener searchClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			list.setSelection(indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
			DebugLog.d("===== selected : "+parent.getItemAtPosition(position)+" | "+indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
		}
	};

	private class FormLoader extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			CorrectiveValueModel lastModel = new CorrectiveValueModel();
			correctiveValueModels = lastModel.getCorrectiveValue(schedule.id);
			lastModel = null;
			
			int x = 0;
			for (CorrectiveValueModel correctiveValueModel : correctiveValueModels) {
				x++;
				WorkFormItemModel item = new WorkFormItemModel();
				item = item.getItemById(correctiveValueModel.itemId);
				DebugLog.d( "-------------------- ");
				if (!ruleAddItem(correctiveValueModel, lastModel, item)){
					DebugLog.d( "not permited to add item");
					publishProgress(x*100/correctiveValueModels.size());
					continue;
				}
				DebugLog.d( "permited to add item");
				OperatorModel operatorModel = new OperatorModel();
				operatorModel = operatorModel.getOperatorById(correctiveValueModel.operatorId);
				DebugLog.d("Corrective : "+item.label);
				ItemFormRenderModel header = new ItemFormRenderModel();
				if (lastModel == null || lastModel.itemId != correctiveValueModel.itemId || lastModel.operatorId != correctiveValueModel.operatorId){
					header.type = ItemFormRenderModel.TYPE_HEADER;
					header.workItemModel = item;
					header.itemValue = correctiveValueModel;
					header.setSchedule(schedule);
					item.labelHeader = item.label+"\n"+operatorNameForHeader(item, operatorModel);
					DebugLog.d("after changed : "+item.label);
					header.label = item.label;
					formModels.add(header);
					
					ItemFormRenderModel child = new ItemFormRenderModel();
					child.setSchedule(schedule);
					child.type = ItemFormRenderModel.TYPE_HEADER_DIVIDER;
					formModels.get(formModels.size() - 1).add(child);
				}

				lastModel = correctiveValueModel;
				ItemFormRenderModel form = new ItemFormRenderModel();
				form.type = ItemFormRenderModel.TYPE_PICTURE;
				form.workItemModel = item;
				form.itemValue = correctiveValueModel;
				form.label = correctiveValueModel.photoStatus;
				form.setSchedule(schedule);
				formModels.get(formModels.size() - 1).add(form);
				publishProgress(x*100/correctiveValueModels.size());
				
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setMessage("Generating form "+values[0]+" % complete");
		}

		@Override
		protected void onPostExecute(Void result) {
//			if (rowModel.row_columns.size() > 0 && rowModel.row_columns.get(0).items.size() > 0)
				title.setText("Corrective Maintenance");
			progressDialog.dismiss();
			adapter.setItems(formModels);
//			SearchAdapter searchAdapter = new SearchAdapter(activity, android.R.layout.select_dialog_item, android.R.id.text1, indexes);
			ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, labels);
			search.setAdapter(searchAdapter);
			super.onPostExecute(result);
		}
	}
	
	private String operatorNameForHeader(WorkFormItemModel workFormItemModel, OperatorModel operatorModel){
		if (workFormItemModel.scope_type.equalsIgnoreCase("operator"))
			return operatorModel.name;
		else
			return "All Operator";
	}
	
	OnClickListener photoClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			photoItem = (PhotoItem) v.getTag();
			mImageUri = ImageUtil.takePicture(activity);
		}
	};
	
	private boolean ruleAddItem(CorrectiveValueModel curentModel, CorrectiveValueModel lastModel, WorkFormItemModel workItemModel){
		DebugLog.d( "================================================================ : " + workItemModel.scope_type);
		DebugLog.d( "current item id : "+curentModel.itemId+" current operator : "+curentModel.operatorId+" status : "+curentModel.photoStatus);
		if (lastModel != null){
			DebugLog.d( "last item id : "+lastModel.itemId+" last operator : "+lastModel.operatorId+" status : "+lastModel.photoStatus);
			DebugLog.d( "different item id : "+isDifferentItemId(curentModel, lastModel));
			DebugLog.d( "same id with same operator : "+isSameOperatorAndItemId(curentModel, lastModel));
		}
		return lastModel == null || //if first model
				workItemModel.scope_type.equalsIgnoreCase("operator") || // if the scope is operator
				(!workItemModel.scope_type.equalsIgnoreCase("operator") && //if the scope for all operator
				(isDifferentItemId(curentModel, lastModel) ||
				isSameOperatorAndItemId(curentModel, lastModel)));
	}
	
	private boolean isDifferentItemId(CorrectiveValueModel curentModel, CorrectiveValueModel lastModel){
		return lastModel.itemId != curentModel.itemId;
	}
	
	private boolean isSameOperatorAndItemId(CorrectiveValueModel curentModel, CorrectiveValueModel lastModel){
		return !isDifferentItemId(curentModel, lastModel) &&
				lastModel.operatorId == curentModel.operatorId;
	}

	//called after camera intent finished
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == ImageUtil.MenuShootImage && resultCode==RESULT_OK)
		{
			if (photoItem != null && mImageUri != null){
				photoItem.initValue();
				photoItem.deletePhoto();
				DebugLog.d( String.valueOf(currentlocation.latitude)+" || "+String.valueOf(currentlocation.longitude));
				photoItem.setImage(mImageUri.toString(),String.valueOf(currentlocation.latitude),String.valueOf(currentlocation.longitude),accuracy);
			}
		}
	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		googleApiClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		googleApiClient.connect();
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
			currentlocation = new LatLng(location.getLatitude(), location.getLongitude());
			DebugLog.d(String.valueOf(currentlocation.latitude)+" || "+String.valueOf(currentlocation.longitude));
		}
	};

}