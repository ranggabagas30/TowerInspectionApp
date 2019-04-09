package com.sap.inspection.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;

import java.io.File;


public class PhotoItemRadio extends RelativeLayout {
	protected Context context;
	protected View rootView;
	protected View root;
	protected View photoRoot;
    protected View upload;
	protected TextView label;
	//	protected TextView failed;
	protected TextView latitude;
	protected TextView longitude;
	protected TextView accuracy;
	protected TextView photodate;
	protected TextView uploadstatus;
	protected TextView mandatory;
	protected EditText remark;
	protected EditText material_request;
	protected RadioGroup radioGroup;
	protected ImageButtonForList button;
	protected RadioButton ok;
	protected RadioButton nok;
	protected RadioButton na;
	protected View noPicture;
	protected ImageView imageView;
	protected ProgressBar progress;
	protected String scheduleId;
	protected int itemId;
	protected int operatorId;
	protected ItemValueModel value;
	protected String labelText;
	private ItemFormRenderModel itemFormRenderModel;
	private boolean onInit = false;
	private boolean isAudit = false;

	public void setItemFormRenderModel(ItemFormRenderModel itemFormRenderModel) {
		this.itemFormRenderModel = itemFormRenderModel;
	}

	public void setLabel(String label) {
		this.labelText = label;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
		if (value != null)
			value.scheduleId = scheduleId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
		if (value != null)
			value.itemId = itemId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
		if (value != null)
			value.operatorId = operatorId;
	}

	public PhotoItemRadio(Context context) {
		super(context);
		init(context);
	}

	public PhotoItemRadio(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	public PhotoItemRadio(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;

		root = LayoutInflater.from(context).inflate(R.layout.photo_layout, this, true);
		photoRoot = root.findViewById(R.id.photolayout);
		button = (ImageButtonForList) root.findViewById(R.id.button);
		button.setTag(this);
        upload = root.findViewById(R.id.upload);
        upload.setTag(this);
		noPicture =  root.findViewById(R.id.no_picture);
		imageView = (ImageView) root.findViewById(R.id.photo);
		imageView.setTag(this);
		latitude = (TextView) root.findViewById(R.id.latitude);
		longitude = (TextView) root.findViewById(R.id.longitude);
		mandatory = (TextView) root.findViewById(R.id.mandatory);
		mandatory.setTag(this);
		accuracy = (TextView) root.findViewById(R.id.accuracy);
		photodate = (TextView) root.findViewById(R.id.photodate);
		uploadstatus = (TextView) root.findViewById(R.id.uploadstatus);
		label = (TextView) root.findViewById(R.id.label);
		progress = (ProgressBar) root.findViewById(R.id.progress);
		remark = (EditText) root.findViewById(R.id.remark);
		remark.addTextChangedListener(textWatcher);
		radioGroup = (RadioGroup) root.findViewById(R.id.radioGroup);
		ok = (RadioButton) root.findViewById(R.id.radioOK);
		nok = (RadioButton) root.findViewById(R.id.radioNOK);
		na = (RadioButton) root.findViewById(R.id.radioNA);
		radioGroup.setOnCheckedChangeListener(changeListener);
		value = new ItemValueModel();

		toggleEditable();
	}

	public void setButtonClickListener(OnClickListener buttonClickListener){
		button.setOnClickListener(buttonClickListener);
	}

    public void setUploadClickListener(OnClickListener uploadClickListener){
        upload.setOnClickListener(uploadClickListener);
    }

	public void setValue(ItemValueModel value, boolean initValue) {
		if (initValue){
			radioGroup.setOnCheckedChangeListener(null);
			onInit = true;
		}
		setValue(value);
		if (initValue){
			radioGroup.setOnCheckedChangeListener(changeListener);
			onInit = false;
		}
	}

	public void setValue(ItemValueModel value) {
		this.value = value;
		DebugLog.d("value : " + this.value);
		imageView.setImageResource(R.drawable.logo_app);
		if (itemFormRenderModel.operator != null && itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
			label.setText(itemFormRenderModel.operator.name+"\n"+itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", "")
					);
		else if (itemFormRenderModel.workItemModel != null && itemFormRenderModel.workItemModel.label != null)
			label.setText(itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));

		if (value != null){
			//rangga
			if (this.value.remark != null) {
				if (this.value.remark.isEmpty()) {
					remark.setText("");
				} else {
					DebugLog.d("value remark : " + this.value.remark);
					remark.setText(this.value.remark);
				}
			} else {
				remark.setText("");
			}

			// if no picture then show no picture icon
			if (value.value == null)
				noPicture.setVisibility(View.VISIBLE);

			DebugLog.d("value.photoStatus : " + value.photoStatus );
			if (value.photoStatus != null){
				if (value.photoStatus.equalsIgnoreCase("ok")){

					button.setVisibility(View.VISIBLE);
					ok.setChecked(true);
					setPhotoRootVisiblity("OK");

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
						mandatory.setVisibility(View.GONE);
				}
				else if (value.photoStatus.equalsIgnoreCase("nok")){

					button.setVisibility(View.VISIBLE);
					nok.setChecked(true);
					setPhotoRootVisiblity("NOK");

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
						if(remark.getText().toString().equalsIgnoreCase("") || value.remark.equalsIgnoreCase("")){
							mandatory.setVisibility(View.VISIBLE);
						} else {
							mandatory.setVisibility(View.GONE);
						}
					}
				}
				else if (value.photoStatus.equalsIgnoreCase("na")){

					button.setVisibility(View.GONE);
					photoRoot.setVisibility(View.GONE);
					na.setChecked(true);
					value.photoStatus="NA";

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
						mandatory.setVisibility(View.GONE);
				}
			}
			else{
				button.setVisibility(View.VISIBLE);
				photoRoot.setVisibility(View.GONE);
				/*if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
					radioGroup.check(R.id.radioNOK);
					setPhotoRootVisiblity("NOK");
					mandatory.setVisibility(View.VISIBLE);
				}*/
			}
		}else {
			DebugLog.d("value = null, reset");
			reset();
		}
	}

	private void setItemFormRenderedValue(){
		if (itemFormRenderModel.itemValue == null){
			if(!itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
				itemFormRenderModel.schedule.sumTaskDone+=itemFormRenderModel.schedule.operators.size();
			else
				itemFormRenderModel.schedule.sumTaskDone++;
			itemFormRenderModel.schedule.save();
		}
		DebugLog.d("task done : "+itemFormRenderModel.schedule.sumTaskDone);
		if (itemFormRenderModel != null){
			itemFormRenderModel.itemValue = value;
		}
	}

	public void save(Context context){
		DebugLog.d( value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+value.value);
		value.save(context);
	}

	public void save(){
		/*if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(context);
		if (!DbRepositoryValue.getInstance().getDB().isOpen())
			DbRepositoryValue.getInstance().open(context);*/
		setItemFormRenderedValue();
		DebugLog.d( value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+value.value+ " | " +value.createdAt);
		if(!itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator")){
			DebugLog.d("scopeType : operator");
			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
				value.operatorId = operatorModel.id;
				/*if (itemFormRenderModel.workItemModel.work_form_group_id == 3 && itemFormRenderModel.workItemModel.field_type.equalsIgnoreCase("file")) {
					itemFormRenderModel.workItemModel.mandatory = true;
					itemFormRenderModel.workItemModel.save();
				}*/
				value.save();
				DebugLog.d("== operator id : " + value.operatorId);
			}
		}else{
			DebugLog.d("scopeType : All");
			value.save();
		}
	}

	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			DebugLog.d("remark photo radio: " + s.toString());
			if (onInit)
				return;
			initValue();
			if (value != null){
				value.remark = s.toString();
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

					if (s.toString().equalsIgnoreCase("")) {
						if ((nok.isChecked() || value.photoStatus.equalsIgnoreCase("nok")) && !TextUtils.isEmpty(value.photoStatus))
							mandatory.setVisibility(View.VISIBLE);
					} else {
						mandatory.setVisibility(View.GONE);
					}
				}
				DebugLog.d("remark value : " + s.toString());
				save();
			}
		}
	};

	//penambahan override method irwan
//	TextWatcher textWatcher2 = new TextWatcher() {
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//		}
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s) {
//			DebugLog.d("material photo radio: " + s.toString());
//			if (onInit)
//				return;
//			initValue();
//			if (value != null){
//				value.material_request = s.toString();
//				save();
//			}
//		}
//	};

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			initValue();
			switch (checkedId) {
			case R.id.radioOK:
				button.setVisibility(View.VISIBLE);
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
					mandatory.setVisibility(View.GONE);
				//remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
				setPhotoRootVisiblity("OK");
				break;

			case R.id.radioNOK:
				button.setVisibility(View.VISIBLE);
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

					if(remark.getText().toString().equalsIgnoreCase("") || value.remark.equalsIgnoreCase("")){
						mandatory.setVisibility(View.VISIBLE);
					} else {
						mandatory.setVisibility(View.GONE);
					}
				}
				//remark.setVisibility(View.VISIBLE);
				setPhotoRootVisiblity("NOK");
				break;

			case R.id.radioNA:
				button.setVisibility(View.GONE);
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
					mandatory.setVisibility(View.GONE);
				//remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
				photoRoot.setVisibility(View.GONE);
				if (value!=null)
					value.photoStatus = "NA";
				break;

			default:
				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
					mandatory.setVisibility(View.GONE);
				break;
			}
			if (value!=null)
				save();
		}
	};

	private void setPhotoRootVisiblity(String photoStatus){
		if (value!=null){
			value.photoStatus=photoStatus;
			DebugLog.d("value.photoStatus now : " + photoStatus);
			if (value.value != null){
				photoRoot.setVisibility(View.VISIBLE);
				setImage(value.value, value.latitude, value.longitude, value.gpsAccuracy);
			}
			else
				photoRoot.setVisibility(View.GONE);
		}else
			photoRoot.setVisibility(View.GONE);

	}

	public ItemValueModel getValue() {
		return value;
	}

	//	public void showPhotoAtribute(){
	//		photoRoot.setVisibility(View.VISIBLE);
	//		noPicture.setVisibility(View.GONE);
	//	}

	public void deletePhoto(){
		DebugLog.d( "into deleted");
		if (value != null && value.value != null){
			DebugLog.d( "deleted value not null");
			File fileTemp=new File(value.value.replaceFirst("^file\\:\\/\\/", ""));
			if (fileTemp.exists())
				try{
					fileTemp.delete();
					DebugLog.d( "file deleted : "+value.value);
				}catch(Exception e){
					DebugLog.d( "file not deleted");
					e.printStackTrace();
				}
			value.uploadStatus = ItemValueModel.UPLOAD_NONE;
			DebugLog.d("==> delete photo, reset uploadStatus to " + value.uploadStatus);
			save();
		}
	}

	public String setPhotoDate() {
		value.photoDate = value.createdAt = DateTools.getCurrentDate();
		return value.photoDate;
	}

	public void setUploadstatus(String uploadstatus) {
		this.uploadstatus.setText(uploadstatus);
	}
	public void setImage(String uri,String latitude, String longitude,int accuracy){
		if (value != null){

			value.value = uri;
			value.latitude = latitude;
			value.longitude = longitude;
			value.gpsAccuracy = accuracy;
			/*value.uploadStatus = ItemValueModel.UPLOAD_NONE;
			DebugLog.d("save => uploadStatus : " + value.uploadStatus);*/
		}
		else{
			DebugLog.d("value = null, reset ! ");
			reset();
			return;
		}
		save();

//		this.latitude.setText("Lat. : "+ Float.parseFloat(latitude)/1000000);
//		this.longitude.setText("Long. : "+Float.parseFloat(longitude)/1000000);
		this.latitude.setText("Lat. : "+ latitude);
		this.longitude.setText("Long. : "+ longitude);
		this.accuracy.setText("Accurate up to : "+accuracy+" meters");
        this.photodate.setText("photo date : " + value.photoDate + "");
		//		ImageSize i = ImageSizeUtils.defineTargetSizeForView(imageView, 480, 360);
		//		imageView.setMaxWidth(i.getWidth());
		//		imageView.setMaxHeight(i.getHeight());

		BaseActivity.imageLoader.displayImage(uri,imageView,new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				progress.setVisibility(View.VISIBLE);
				photoRoot.setVisibility(View.VISIBLE);
				noPicture.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.GONE);
				noPicture.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.VISIBLE);
				if (value != null){
					if (value.value == null)
						noPicture.setVisibility(View.VISIBLE);
					if (value.photoStatus == null){
						radioGroup.check(R.id.radioOK);
					}
				}
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.VISIBLE);
				noPicture.setVisibility(View.GONE);
			}

		});
	}

	private void reset(){
		progress.setVisibility(View.GONE);
		photoRoot.setVisibility(View.GONE);
		//		failed.setVisibility(View.GONE);
		remark.setText("");
//		remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
		noPicture.setVisibility(View.VISIBLE);
		radioGroup.clearCheck();
	}

	public void initValue(){
		if (value == null){
			value = new ItemValueModel();
			if (itemFormRenderModel != null){
				value.itemId = itemFormRenderModel.workItemModel.id;
				value.scheduleId = itemFormRenderModel.schedule.id;
				value.operatorId = itemFormRenderModel.operatorId;
			}
		}
	}

	private void resetVisibility(){
		progress.setVisibility(View.GONE);
		photoRoot.setVisibility(View.GONE);
		//		failed.setVisibility(View.GONE);
//		remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
		radioGroup.clearCheck();
	}

	public void setAudit(boolean isAudit) {
		this.isAudit = isAudit;
	}

	public int getItemId() {
		return itemFormRenderModel.workItemModel.id;
	}

	private File createTemporaryFile(String part, String ext) throws Exception
	{
		File tempDir;
		ContextWrapper contextWrapper = new ContextWrapper(context);
		if (CommonUtil.isExternalStorageAvailable()) {
			DebugLog.d("external storage available");
			tempDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
		} else {
			DebugLog.d("external storage not available");
			tempDir = new File(contextWrapper.getFilesDir()+"/Camera/");
		}
		tempDir = new File(tempDir.getAbsolutePath() + "/TowerInspection"); // create temp folder
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		tempDir = new File(tempDir.getAbsolutePath() + "/" + value.scheduleId+ "/"); // create schedule folder
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}

	private void toggleEditable() {
		if (MyApplication.getInstance().isInCheckHasilPm()) {
			DebugLog.d("input is disabled");
			if (remark!=null) remark.setEnabled(false);
			if (material_request!=null) material_request.setEnabled(false);
			if (radioGroup!=null) radioGroup.setEnabled(false);
			if (ok!=null) ok.setEnabled(false);
			if (nok!=null) nok.setEnabled(false);
			if (na!=null) na.setEnabled(false);
			if (button!=null) button.setEnabled(false);
		} else {
			if (remark!=null) remark.setEnabled(true);
			if (material_request!=null) material_request.setEnabled(true);
			if (radioGroup!=null) radioGroup.setEnabled(true);
			if (ok!=null) ok.setEnabled(true);
			if (nok!=null) nok.setEnabled(true);
			if (na!=null) na.setEnabled(true);
			if (button!=null) button.setEnabled(true);
		}
	}
}
