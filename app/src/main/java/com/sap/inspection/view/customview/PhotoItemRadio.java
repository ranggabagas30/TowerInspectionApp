package com.sap.inspection.view.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.responsemodel.CheckApprovalResponseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.util.DateUtil;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.FileUtil;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.view.ui.BaseActivity;

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
	protected ImageButtonForList btnTakePicture;
	protected RadioButton ok;
	protected RadioButton nok;
	protected RadioButton na;
	protected View noPicture;
	protected ImageView imageView;
	protected ProgressBar progress;
	protected String scheduleId;
	protected String workTypeName;
	protected int itemId;
	protected int operatorId;
	protected FormValueModel value;
	protected String labelText;
	private ItemFormRenderModel itemFormRenderModel;
	private boolean onInit = false;
	private boolean isAudit;
	private boolean isRoutingSchedule = false;

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
		btnTakePicture = root.findViewById(R.id.btnTakePicture);
		btnTakePicture.setTag(this);
        upload = root.findViewById(R.id.upload);
        upload.setTag(this);
		noPicture =  root.findViewById(R.id.no_picture);
		imageView = root.findViewById(R.id.photo);
		imageView.setTag(this);
		latitude = root.findViewById(R.id.latitude);
		longitude = root.findViewById(R.id.longitude);
		mandatory = root.findViewById(R.id.mandatory);
		mandatory.setTag(this);

		accuracy = root.findViewById(R.id.accuracy);
		photodate = root.findViewById(R.id.photodate);
		uploadstatus = root.findViewById(R.id.uploadstatus);
		label = root.findViewById(R.id.label);
		progress = root.findViewById(R.id.progress);
		remark = root.findViewById(R.id.remark);
		remark.addTextChangedListener(textWatcher);
		radioGroup = root.findViewById(R.id.radioGroup);
		ok = root.findViewById(R.id.radioOK);
		nok = root.findViewById(R.id.radioNOK);
		na = root.findViewById(R.id.radioNA);
		radioGroup.setOnCheckedChangeListener(changeListener);

		toggleEditable();
	}

	public void setButtonTakePictureListener(OnClickListener buttonClickListener){
		btnTakePicture.setOnClickListener(buttonClickListener);
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
		if (value != null)
			value.scheduleId = scheduleId;
	}

	public void setWorkTypeName(String workTypeName) {
	    this.workTypeName = workTypeName;
	    isRoutingSchedule = workTypeName.equalsIgnoreCase(context.getString(R.string.routing_segment)) ||
                            workTypeName.equalsIgnoreCase(context.getString(R.string.handhole)) ||
                            workTypeName.equalsIgnoreCase(context.getString(R.string.hdpe));
    }

	public void setItemFormRenderModel(ItemFormRenderModel itemFormRenderModel) {
		this.itemFormRenderModel = itemFormRenderModel;

		if (itemFormRenderModel.operator != null && itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
			label.setText(itemFormRenderModel.operator.name+"\n"+itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));
		else if (itemFormRenderModel.workItemModel != null && itemFormRenderModel.workItemModel.label != null)
			label.setText(itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));

		if (itemFormRenderModel.workItemModel != null && itemFormRenderModel.workItemModel.mandatory)
			mandatory.setVisibility(VISIBLE);

		if (!TowerApplication.getInstance().IS_CHECKING_HASIL_PM())
			enable();

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) &&
				(itemFormRenderModel.workItemModel.label.equalsIgnoreCase("Photo Penghancuran 1") ||
						itemFormRenderModel.workItemModel.label.equalsIgnoreCase("Photo Penghancuran 2"))) {

			// ignore mandatory when did not get any approval yet
			itemFormRenderModel.workItemModel.disable = true;
			itemFormRenderModel.workItemModel.save();

			disable();

			DebugLog.d("proceed approval checking ... ");
			CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(scheduleId);
			APIHelper.getCheckApproval(context, checkApprovalHandler, scheduleId);
			DebugLog.d("itemFormRenderModel.wargaId = " + itemFormRenderModel.getWargaId());
			DebugLog.d("itemFormRenderModel.barangId = " + itemFormRenderModel.getBarangId());
		}
	}

	public void setItemValue(FormValueModel value, boolean initValue) {

		DebugLog.d("itemid : " + itemFormRenderModel.workItemModel.id);
		DebugLog.d("label : " + itemFormRenderModel.workItemModel.label);
		DebugLog.d("value : " + (itemFormRenderModel.itemValue == null ? "null" : itemFormRenderModel.itemValue.remark));
		DebugLog.d("photo status : " + (itemFormRenderModel.itemValue == null ? "null" : itemFormRenderModel.itemValue.photoStatus));
		DebugLog.d("radio check : " + (radioGroup.getCheckedRadioButtonId() == -1 ? "no check" : radioGroup.getCheckedRadioButtonId()));

		imageView.setImageResource(R.drawable.logo_app);

		if (initValue){
			radioGroup.setOnCheckedChangeListener(null);
			onInit = true;
		}

		setValue(value);

		if (value != null) {

			if (!TextUtils.isEmpty(value.remark)) {
				remark.setText(value.remark);
			} else {
				remark.setText("");
			}

			if (value.photoStatus == null)
				radioGroup.clearCheck();
		}

		if (initValue){
			radioGroup.setOnCheckedChangeListener(changeListener);
			onInit = false;
		}
	}

	public void setValue(FormValueModel value) {
		this.value = value;
		notifyDataChanged();

	}

	public FormValueModel getValue() {
		return value;
	}

	public void initValue(){
		if (value == null){
			value = new FormValueModel();
			if (itemFormRenderModel != null){
				value.itemId = itemFormRenderModel.workItemModel.id;
				value.scheduleId = itemFormRenderModel.schedule.id;
				value.operatorId = itemFormRenderModel.operatorId;

				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
					value.wargaId  = itemFormRenderModel.getWargaId();
					value.barangId = itemFormRenderModel.getBarangId();
				}
			}
		}
	}

	public void notifyDataChanged() {

		if (value != null) {

			photoRoot.setVisibility(GONE);
			noPicture.setVisibility(VISIBLE);
			if (!TextUtils.isEmpty(value.value)) {
				photoRoot.setVisibility(VISIBLE);
				noPicture.setVisibility(INVISIBLE);

				DebugLog.d("value : " + value.value);
				value.value = value.value.replaceFirst("/file:", "");
				value.value = value.value.replaceFirst("file://", "");

				File photoFile = new File(value.value);
				if (photoFile.exists()) {

					Bitmap photoBmp = null;

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
						photoBmp = ImageUtil.loadDecryptedImage(value.value);
					} else {
						try {
							Uri photoUri = FileUtil.getUriFromFile(context, new File(value.value.replaceFirst("/file:", "")));
							photoBmp = BaseActivity.imageLoader.loadImageSync(photoUri.toString());
						} catch (IllegalArgumentException e) {
							DebugLog.e("load image: " + e.getMessage());
						}
					}

					if (photoBmp != null) {
						DebugLog.d("load image");
						imageView.setImageBitmap(photoBmp);
						progress.setVisibility(View.GONE);
						photoRoot.setVisibility(View.VISIBLE);
						if (TextUtils.isEmpty(value.photoStatus)){
							radioGroup.check(R.id.radioOK);
						}

					} else {
						// on loading failed
						progress.setVisibility(View.GONE);
						photoRoot.setVisibility(View.GONE);
						noPicture.setVisibility(View.VISIBLE);
						DebugLog.e(context.getString(R.string.error_load_image));
					}
				} else {
					Toast.makeText(context, "File photo " + value.value + " tidak ditemukan di gallery", Toast.LENGTH_LONG).show();
					// on loading failed
					progress.setVisibility(View.GONE);
					photoRoot.setVisibility(View.GONE);
					noPicture.setVisibility(View.VISIBLE);
					DebugLog.e(context.getString(R.string.error_load_image));
				}
			}

			//notify radiobuttons
			imageView.setVisibility(VISIBLE);
			btnTakePicture.setVisibility(VISIBLE);
            if (!TextUtils.isEmpty(value.photoStatus)) {
                switch (value.photoStatus) {
                    case Constants.OK :
                    	DebugLog.d("check OK");
                        ok.setChecked(true);
                        break;
                    case Constants.NOK :
                    	DebugLog.d("check NOK");
                        nok.setChecked(true);
                        break;
                    case Constants.NA :
                    	DebugLog.d("check NA");
                        na.setChecked(true);
                        imageView.setVisibility(GONE);
                        photoRoot.setVisibility(GONE);
                        noPicture.setVisibility(INVISIBLE);
                        btnTakePicture.setVisibility(GONE);
                        break;
                }
            } else radioGroup.clearCheck();

            // notify button take picture and textview mandatory
			mandatory.setVisibility(VISIBLE);
            if (!itemFormRenderModel.workItemModel.mandatory) {

                // init mandatory label visibility to GONE while it's not a mandatory item
            	mandatory.setVisibility(GONE);

                if (!TextUtils.isEmpty(value.photoStatus)) {

                    // for all common case, if not ok is checked, then shows mandatory label
                    // if remark is empty
                    if (nok.isChecked() || value.photoStatus.equalsIgnoreCase(Constants.NOK)) {

                        if (TextUtils.isEmpty(value.remark))
                            mandatory.setVisibility(VISIBLE);

                    }

                    if (BuildConfig.FLAVOR.equalsIgnoreCase("sap")) {
                        if (isRoutingSchedule) {
                            // for routing schedule, if ok or not ok is checked, then shows mandatory label
                            // if remark is empty
                            if ((nok.isChecked() || value.photoStatus.equalsIgnoreCase(Constants.NOK)) ||
                                    (ok.isChecked() || value.photoStatus.equalsIgnoreCase(Constants.OK))) {

                                if (TextUtils.isEmpty(value.remark))
                                    mandatory.setVisibility(VISIBLE);

                            }
                        }
                    }

                } else {

                    // notice user to fill photo status if remark is not empty
                    if (!TextUtils.isEmpty(value.remark)) {
                        mandatory.setVisibility(VISIBLE);
                    }
                }
            }

        } else {
			reset();
		}
	}

	private void setItemFormRenderedValue(){

		if (itemFormRenderModel.itemValue == null){
			DebugLog.d("itemFormRenderModel.itemValue == null");
			if(!itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
				itemFormRenderModel.schedule.sumTaskDone+=itemFormRenderModel.schedule.operators.size();
			else
				itemFormRenderModel.schedule.sumTaskDone++;
			itemFormRenderModel.schedule.save();
		}
		DebugLog.d("task done : " + itemFormRenderModel.schedule.sumTaskDone);
		if (itemFormRenderModel != null){
			DebugLog.d("itemFormRenderModel != null");
			itemFormRenderModel.itemValue = value;
		}
	}

	public void save(){
		setItemFormRenderedValue();
		DebugLog.d( value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+ value.value+ " | " + value.wargaId + " | " + value.barangId + " | " + value.createdAt );
		if(itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")){
			DebugLog.d("scopeType : All");
			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
				value.operatorId = operatorModel.id;
				value.save();
			}
		} else {
			DebugLog.d("scopeType : operator");
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

            DebugLog.d("text change listening...");

			if (onInit)
				return;

            initValue();
            if (value != null) {
                DebugLog.d("remark photo : " + s.toString());
                value.remark = s.toString();
                save();
                notifyDataChanged();
            }
		}
	};

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			if (onInit)
				return;

			initValue();
		    if (value != null) {

		    	DebugLog.d("value not null, set check");
                switch (checkedId) {
                    case R.id.radioOK:

                    	DebugLog.d("check OK");
                        value.photoStatus = Constants.OK;
                        notifyDataChanged();
                        break;

                    case R.id.radioNOK:

                    	DebugLog.d("check NOK");
                        value.photoStatus = Constants.NOK;
                        notifyDataChanged();
                        break;

                    case R.id.radioNA:

                    	DebugLog.d("check NA");
                        value.photoStatus = Constants.NA;
                        notifyDataChanged();
                        break;
                }

                DebugLog.d("save");
                save();

                return;
            }

            DebugLog.d("no check any radio");
		}
	};

	public void deletePhoto(){
		if (value != null && !TextUtils.isEmpty(value.value)) {
			DebugLog.d("file to be deleted : " + value.value);
			File fileTemp = new File(value.value.replaceFirst("^file\\:\\/\\/", ""));
			if (fileTemp.exists()) {
				try{
					fileTemp.delete();
					DebugLog.d(context.getString(R.string.success_delete_file));
				}catch(Exception e){
					e.printStackTrace();
					DebugLog.e(context.getString(R.string.error_delete_file));
				}
				value.value = "";
				value.uploadStatus = FormValueModel.UPLOAD_NONE;
				save();
				notifyDataChanged();
			}
		}
	}

	public void setUploadstatus(String uploadstatus) {
		this.uploadstatus.setText(uploadstatus);
	}

	public void setImage(File photoPath, String latitude, String longitude,int accuracy){

		DebugLog.d("set image from " + photoPath.toString());

		initValue();

		if (value == null) {
			reset();
			return;
		}

		// updating item value
		value.value = photoPath.getPath();
		value.latitude = latitude;
		value.longitude = longitude;
		value.gpsAccuracy = accuracy;
		value.photoStatus = Constants.OK;
		value.photoDate = DateUtil.getCurrentDate();

		this.latitude.setText("Lat. : "+ latitude);
		this.longitude.setText("Long. : "+ longitude);
		this.accuracy.setText("Accurate up to : "+accuracy+" meters");
		this.photodate.setText("photo date : " + value.photoDate + "");

		save();
		notifyDataChanged();
	}

	private void reset(){
		DebugLog.d("value = null, reset ! ");
		progress.setVisibility(View.GONE);
		photoRoot.setVisibility(View.GONE);
		remark.setText("");
		radioGroup.clearCheck();
	}

	public void setAudit(boolean isAudit) {
		this.isAudit = isAudit;
	}

	public int getItemId() {
		return itemFormRenderModel.workItemModel.id;
	}

	private void toggleEditable() {
		if (TowerApplication.getInstance().IS_CHECKING_HASIL_PM()) {
			DebugLog.d("input is disabled");
			disable();
		} else {
			enable();
		}
	}

	private void enable() {
        if (remark!=null) remark.setEnabled(true);
        if (material_request!=null) material_request.setEnabled(true);
        if (radioGroup!=null) radioGroup.setEnabled(true);
        if (ok!=null) ok.setEnabled(true);
        if (nok!=null) nok.setEnabled(true);
        if (na!=null) na.setEnabled(true);
        if (btnTakePicture!=null) btnTakePicture.setEnabled(true);
    }

    private void disable() {
        if (remark!=null) remark.setEnabled(false);
        if (material_request!=null) material_request.setEnabled(false);
        if (radioGroup!=null) radioGroup.setEnabled(false);
        if (ok!=null) ok.setEnabled(false);
        if (nok!=null) nok.setEnabled(false);
        if (na!=null) na.setEnabled(false);
        if (btnTakePicture!=null) btnTakePicture.setEnabled(false);
    }

    private class CheckApprovalHandler extends Handler {

        private String scheduleId;

        public CheckApprovalHandler(String scheduleId) {
            this.scheduleId = scheduleId;
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");
            Gson gson = new Gson();

            if (isResponseOK) {

                if (bundle.getString("json") != null){

                    CheckApprovalResponseModel checkApprovalResponseModel = gson.fromJson(bundle.getString("json"), CheckApprovalResponseModel.class);
                    checkApprovalResponseModel.toString();

                    if (!checkApprovalResponseModel.messages.equalsIgnoreCase("failed")) {

                        DebugLog.d("check approval success");

						itemFormRenderModel.workItemModel.disable = false;
						itemFormRenderModel.workItemModel.save();

                        FormImbasPetirConfig.setScheduleApproval(scheduleId, true);

                        enable();
                        return;
                    }

                    DebugLog.d("belum ada approval dari STP");
                    TowerApplication.getInstance().toast("Photo penghancuran menunggu approval dari STP", Toast.LENGTH_LONG);

                } else {

                    TowerApplication.getInstance().toast("Gagal mengecek approval. Response json = null", Toast.LENGTH_LONG);

                }
            } else {

                TowerApplication.getInstance().toast("Gagal mengecek approval. Response not OK dari server", Toast.LENGTH_LONG);
                DebugLog.d("response not ok");
            }
        }
    }
}
