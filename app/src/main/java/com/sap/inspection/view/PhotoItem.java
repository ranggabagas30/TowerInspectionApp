package com.sap.inspection.view;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.R;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.rules.SavingRule;
import com.sap.inspection.rules.saving.PreventiveSave;


public class PhotoItem extends RelativeLayout {
	protected Context context;
	protected View rootView;
	protected View root;
	protected View photoRoot;
	protected TextView label;
	//	protected TextView failed;
	protected TextView latitude;
	protected TextView longitude;
	protected TextView accuracy;
	protected EditText remark;
	protected ImageButtonForList button;
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
	private boolean onTaskDone = true;
	private SavingRule savingRule;
	private boolean isAudit = false;

	public void setItemFormRenderModel(ItemFormRenderModel itemFormRenderModel) {
		this.itemFormRenderModel = itemFormRenderModel;
		Log.d(getClass().getName(), "value : "+itemFormRenderModel.itemValue.value);
		if (itemFormRenderModel.itemValue.value == null || (value !=null && value.value == null))
			onTaskDone = false;
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

	public PhotoItem(Context context) {
		super(context);
		init(context);
	}

	public PhotoItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	public PhotoItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setSavingRule(SavingRule savingRule){
		this.savingRule = savingRule;
	}
	
	private void init(Context context) {
		this.context = context;

		root = LayoutInflater.from(context).inflate(R.layout.photo_layout, this, true);
		//		photoRoot = root.findViewById(R.id.layout_helper1);
		photoRoot = root.findViewById(R.id.photolayout);
		button = (ImageButtonForList) root.findViewById(R.id.button);
		button.setTag(this);
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
		remark.setVisibility(View.VISIBLE);
		root.findViewById(R.id.radioGroup).setVisibility(View.GONE);
		value = new ItemValueModel();
	}

	public void setButtonClickListener(OnClickListener buttonClickListener){
		button.setOnClickListener(buttonClickListener);
		//		imageView.setOnClickListener(buttonClickListener);
	}

	public void setValue(ItemValueModel value, boolean initValue) {
		if (initValue){
			onInit = true;
		}
		setValue(value);
		setPhotoRootVisiblity(value.photoStatus);
		if (initValue){
			onInit = false;
		}
	}

	public void setValue(ItemValueModel value) {
		this.value = value;
		imageView.setImageResource(R.drawable.logo_sap);
		if (itemFormRenderModel.label != null)
			label.setText(itemFormRenderModel.label);
		else if (itemFormRenderModel.operator != null)
			label.setText(itemFormRenderModel.operator.name);

		if (value != null){
			// if no picture then show no picture icon
			if (value.value == null)
				noPicture.setVisibility(View.VISIBLE);
			if (value.remark != null)
				remark.setText(value.remark);
			else
				remark.setText("");

		}else reset();
	}

	private void setItemFormRenderedValue(){
		Log.d(getClass().getName(),"itemFormRenderModel.itemValue.value : "+itemFormRenderModel.itemValue.value);
		//		CorrectiveValueModel temp = new CorrectiveValueModel();
		//		try{
		//			temp = temp.getItemValue(itemFormRenderModel.schedule.id, itemFormRenderModel.itemModel.id, itemFormRenderModel.operatorId);
		//		}catch(Exception e){}
		if (!onTaskDone){
			itemFormRenderModel.schedule.sumTaskDone++;
			itemFormRenderModel.schedule.save();
			Log.d(getClass().getName(),"-=-=-=-=-");
		}
		onTaskDone = true;
		Log.d(getClass().getName(),"task done : "+itemFormRenderModel.schedule.sumTaskDone);
		if (itemFormRenderModel != null)
			itemFormRenderModel.itemValue = value;
	}

	public void save(Context context){
		String tag = getClass().getName();
		Log.d(tag, value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+value.value);
		value.save(context);
	}

	public void save(){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(context);
		if (!DbRepositoryValue.getInstance().getDB().isOpen())
			DbRepositoryValue.getInstance().open(context);
		String tag = getClass().getName();
		Log.d(tag, value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+value.value);
		Log.d(tag, "scope type : "+itemFormRenderModel.itemModel.scope_type);
		setItemFormRenderedValue();
		if (savingRule == null)
			savingRule = new PreventiveSave();
		savingRule.save(itemFormRenderModel, value);
		//		if(!itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("operator")){
		//			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
		//				value.operatorId = operatorModel.id;
		//				value.uploadStatus = ItemValueModel.UPLOAD_NONE;
		//				value.save();
		//			}
		//		}else{
		//			value.uploadStatus = ItemValueModel.UPLOAD_NONE;
		//			value.save();
		//		}
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
		Log.d(getClass().getName(), "into deleted");
		if (value != null && value.value != null){
			Log.d(getClass().getName(), "deleted value not null");
			File fileTemp=new File(value.value.replaceFirst("^file\\:\\/\\/", ""));
			if (fileTemp.exists())
				try{
					fileTemp.delete();
					Log.d(getClass().getName(), "file deleted : "+value.value);
				}catch(Exception e){
					Log.d(getClass().getName(), "file not deleted");
					e.printStackTrace();
				}
		}
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
		remark.setText("");
		remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
		noPicture.setVisibility(View.VISIBLE);
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
		remark.setVisibility(isAudit ? View.VISIBLE : View.GONE);
	}
	
	public void setAudit(boolean isAudit) {
		this.isAudit = isAudit;
	}
}
