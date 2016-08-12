package com.sap.inspection.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
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
import com.sap.inspection.R;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;

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
	protected EditText remark;
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
		//		photoRoot = root.findViewById(R.id.layout_helper1);
		photoRoot = root.findViewById(R.id.photolayout);
		button = (ImageButtonForList) root.findViewById(R.id.button);
		button.setTag(this);
        upload = root.findViewById(R.id.upload);
        upload.setTag(this);
		noPicture =  root.findViewById(R.id.no_picture);
		imageView = (ImageView) root.findViewById(R.id.photo);
		imageView.setTag(this);
		//		failed = (TextView) root.findViewById(R.id.failed);
		latitude = (TextView) root.findViewById(R.id.latitude);
		longitude = (TextView) root.findViewById(R.id.longitude);
		accuracy = (TextView) root.findViewById(R.id.accuracy);
		label = (TextView) root.findViewById(R.id.label);
		progress = (ProgressBar) root.findViewById(R.id.progress);
		remark = (EditText) root.findViewById(R.id.remark);
		remark.addTextChangedListener(textWatcher);
		/*
		int maxLength = 60;    
		remark.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
		*/
		radioGroup = (RadioGroup) root.findViewById(R.id.radioGroup);
		ok = (RadioButton) root.findViewById(R.id.radioOK);
		nok = (RadioButton) root.findViewById(R.id.radioNOK);
		na = (RadioButton) root.findViewById(R.id.radioNA);
		radioGroup.setOnCheckedChangeListener(changeListener);
		value = new ItemValueModel();
	}

	public void setButtonClickListener(OnClickListener buttonClickListener){
		button.setOnClickListener(buttonClickListener);
		//		imageView.setOnClickListener(buttonClickListener);
	}

    public void setUploadClickListener(OnClickListener uploadClickListener){
        upload.setOnClickListener(uploadClickListener);
        //		imageView.setOnClickListener(buttonClickListener);
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
		imageView.setImageResource(R.drawable.logo_app);
		if (itemFormRenderModel.operator != null && itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("operator"))
			label.setText(itemFormRenderModel.operator.name+"\n"+itemFormRenderModel.itemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", "")
					);
		//		if (itemFormRenderModel.operator != null && itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("all"))
		//			label.setText(itemFormRenderModel.operator.name);
		else if (itemFormRenderModel.itemModel != null && itemFormRenderModel.itemModel.label != null)
			label.setText(itemFormRenderModel.itemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));

		if (value != null){
			// if no picture then show no picture icon
			if (value.value == null)
				noPicture.setVisibility(View.VISIBLE);
			if (value.photoStatus != null){
				if (value.photoStatus.equalsIgnoreCase("ok")){
					button.setVisibility(View.VISIBLE);
//					remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
					ok.setChecked(true);
					setPhotoRootVisiblity("OK");
				}
				else if (value.photoStatus.equalsIgnoreCase("nok")){
					button.setVisibility(View.VISIBLE);
//					remark.setVisibility(View.VISIBLE);
					nok.setChecked(true);
					setPhotoRootVisiblity("NOK");
				}
				else if (value.photoStatus.equalsIgnoreCase("na")){
					button.setVisibility(View.GONE);
					photoRoot.setVisibility(View.GONE);
//					remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
					na.setChecked(true);
					value.photoStatus="NA";
				}
			}
			else{
				button.setVisibility(View.VISIBLE);
				photoRoot.setVisibility(View.GONE);
			}
			if (value.remark != null)
				remark.setText(value.remark);

		}else reset();
	}

	private void setItemFormRenderedValue(){
		if (itemFormRenderModel.itemValue == null){
			if(!itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("operator"))
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
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(context);
		if (!DbRepositoryValue.getInstance().getDB().isOpen())
			DbRepositoryValue.getInstance().open(context);
		setItemFormRenderedValue();
		DebugLog.d( value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+value.value);
		if(!itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("operator")){
			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
				value.operatorId = operatorModel.id;
				value.uploadStatus = ItemValueModel.UPLOAD_NONE;
				value.save();
			}
		}else{
			value.uploadStatus = ItemValueModel.UPLOAD_NONE;
			value.save();
		}
	}

	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (onInit)
				return;
			initValue();
			if (value != null){
				value.remark = s.toString();
				save();
			}
		}
	};

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			initValue();
			switch (checkedId) {
			case R.id.radioOK:
				button.setVisibility(View.VISIBLE);
//				remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
				setPhotoRootVisiblity("OK");
				break;

			case R.id.radioNOK:
				button.setVisibility(View.VISIBLE);
//				remark.setVisibility(View.VISIBLE);
				setPhotoRootVisiblity("NOK");
				break;

			case R.id.radioNA:
				button.setVisibility(View.GONE);
//				remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
				photoRoot.setVisibility(View.GONE);
				if (value!=null)
					value.photoStatus = "NA";
				break;

			default:
				break;
			}
			if (value!=null)
				save();
		}
	};

	private void setPhotoRootVisiblity(String photoStatus){
		if (value!=null){
			value.photoStatus=photoStatus;
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
		}
	}

	public void setPhotoDate() {
		value.createdAt = DateTools.getCurrentDate();
	}
	public void setImage(String uri,String latitude, String longitude,int accuracy){
		if (value != null){
			value.value = uri;
			value.latitude = latitude;
			value.longitude = longitude;
			value.gpsAccuracy = accuracy;
		}
		else{
			reset();
			return;
		}
		save();

//		this.latitude.setText("Lat. : "+ Float.parseFloat(latitude)/1000000);
//		this.longitude.setText("Long. : "+Float.parseFloat(longitude)/1000000);
		this.latitude.setText("Lat. : "+ latitude);
		this.longitude.setText("Long. : "+ longitude);
		this.accuracy.setText("Accurate up to : "+accuracy+" meters");

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
				value.itemId = itemFormRenderModel.itemModel.id;
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
		return itemFormRenderModel.itemModel.id;
	}

}
