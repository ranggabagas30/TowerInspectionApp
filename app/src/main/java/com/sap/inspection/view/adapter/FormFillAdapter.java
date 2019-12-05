package com.sap.inspection.view.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.aakira.expandablelayout.Utils;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.rules.SavingRule;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.customview.FormInputText;
import com.sap.inspection.view.customview.MyTextView;
import com.sap.inspection.view.customview.PhotoItem;
import com.sap.inspection.view.customview.PhotoItemRadio;

import java.util.ArrayList;
import java.util.List;

public class FormFillAdapter extends MyBaseAdapter {

	private Context context;
	private ArrayList<ItemFormRenderModel> models;
	private ArrayList<ItemFormRenderModel> shown;
	private String scheduleId;
	private String workTypeName;
	private String workFormGroupName;
	private int workFormGroupId;

	// SAP only
	private String wargaId;
	private String barangId;
	private boolean isScheduleApproved;
	private boolean isChecklistOrSiteInformation;

	private OnClickListener photoListener;
	private OnClickListener uploadListener;
	private SparseArray<List<ItemFormRenderModel>> sparseArray = new SparseArray<>();
	private SavingRule savingRule;
	private SparseBooleanArray expandState = new SparseBooleanArray();

	public FormFillAdapter(Context context) {
		this.context = context;
		if (null == models)
			models = new ArrayList<>();
		if (null == shown)
			shown = new ArrayList<>();
	}

	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}

	public void setWorkFormGroupId(int workFormGroupId) {
		this.workFormGroupId = workFormGroupId;
	}

	public void setWorkFormGroupName(String workFormGroupName) {
		this.workFormGroupName = workFormGroupName;
		isChecklistOrSiteInformation = workFormGroupName.equalsIgnoreCase("checklist") ||
									  workFormGroupName.equalsIgnoreCase("site information");
	}

	public void setSavingRule(SavingRule savingRule) {
		this.savingRule = savingRule;
	}

	public void setPhotoListener(OnClickListener photoListener) {
		this.photoListener = photoListener;
	}

    public void setUploadListener(OnClickListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	// SAP only
	public void setWargaId(String wargaId) {
		DebugLog.d("wargaid = " + wargaId);
		this.wargaId = wargaId;
	}

	// SAP only
	public void setBarangId(String barangId) {
		DebugLog.d("barangid = " + barangId);
		this.barangId = barangId;
	}

	public void setItems(ArrayList<ItemFormRenderModel> models){
		this.models = models;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		shown.clear();

		// add all children item models into the list (flattening)
		for (ItemFormRenderModel model : models) {
			shown.addAll(model.getModels());
		}

		// remove corrective item from shown list
		for (int i = 0; i < shown.size(); i++) {
			ItemFormRenderModel model = shown.get(i);
			if (model.getColumn()!=null && !TextUtils.isEmpty(model.getColumn().column_name)
					&& model.getColumn().column_name.equalsIgnoreCase("corrective"))
				shown.remove(i);
		}

		DebugLog.d("item size = " + shown.size());

		// add all items' label into the shown list
		List<String> labels = new ArrayList<>();
		for (int i = 0; i < this.shown.size(); i++) {
			ItemFormRenderModel item = this.shown.get(i);
			if (item.getType()==ItemFormRenderModel.TYPE_EXPAND)
				labels.add(item.getWorkItemModel().label);
		}

		// removing item from shown which has type = TYPE HEADER
		for (int i = 0; i < labels.size(); i++) {
			String s = labels.get(i);
			for (int j = 0; j < shown.size(); j++) {
				if (s.equalsIgnoreCase(shown.get(j).getLabel()) && shown.get(j).getType()==ItemFormRenderModel.TYPE_HEADER) {
					shown.remove(j);
					break;
				}
			}
		}

		expandState.clear();
		for (int i = 0; i < models.size(); i++) {
			expandState.append(i, true);
		}
		super.notifyDataSetChanged();
	}

	private void updateView() {
		DebugLog.d("aaa shown size="+shown.size()+" sparseArray size="+sparseArray.size());
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return shown.size();
	}

	@Override
	public int getViewTypeCount() {
		return ItemFormRenderModel.MAX_TYPE;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).getType();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public ItemFormRenderModel getItem(int position) {
		return shown.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		ItemFormRenderModel currentItem = getItem(position);

        DebugLog.d("position="+position+" type="+getItemViewType(position));
		if (convertView == null) {
			DebugLog.d("convertView == null");
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_CHECKBOX:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_checkbox,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.checkBox = convertView.findViewById(R.id.item_form_check);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				break;
			case ItemFormRenderModel.TYPE_COLUMN:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_column,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_HEADER_DIVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header_devider,null);
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				((MyTextView) convertView.findViewById(R.id.item_form_label)).setBold(context, true);
				holder.upload_status = convertView.findViewById(R.id.item_form_upload_status);
				holder.colored = convertView.findViewById(R.id.item_form_colored);
				holder.plain = convertView.findViewById(R.id.item_form_plain);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_label,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_LINE_DEVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_line_devider,null);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_operator,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo_radio,null);
				holder.photoRadio = convertView.findViewById(R.id.item_form_photo);
				holder.upload = convertView.findViewById(R.id.item_form_upload);
				holder.photoRadio.setAudit(isAudit());
				holder.photoRadio.setButtonTakePictureListener(photoListener);
				holder.upload.setOnClickListener(uploadListener);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo,null);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				holder.photo = convertView.findViewById(R.id.item_form_photo);
				holder.upload = convertView.findViewById(R.id.item_form_upload);
				holder.photo.setAudit(isAudit());
				holder.photo.setButtonTakePictureListener(photoListener);
                holder.upload.setOnClickListener(uploadListener);
				holder.photo.setSavingRule(savingRule);
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_radio,null);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.radio = convertView.findViewById(R.id.item_form_radio);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_text_field,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.description = convertView.findViewById(R.id.item_form_description);
				holder.input = convertView.findViewById(R.id.item_form_input);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_expand,null);
				holder.label = convertView.findViewById(R.id.item_form_expand_title);
				((MyTextView) convertView.findViewById(R.id.item_form_expand_title)).setBold(context, true);
				holder.expandButton = convertView.findViewById(R.id.item_form_expand_button);
				break;
			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				convertView = new View(context);
				break;
			}
			holder.picture = convertView.findViewById(R.id.picture);
			convertView.setTag(holder);
		} else {
			DebugLog.d("convertView != null");
			holder = (ViewHolder) convertView.getTag();
			DebugLog.d("convertView tag : " + holder);
		}


		if (currentItem.getWorkItemModel() != null)
			DebugLog.d( "picture : " + currentItem.getWorkItemModel().pictureEndPoint);

		if (holder.picture != null){
			if (currentItem.getWorkItemModel() != null && TextUtils.isEmpty(currentItem.getWorkItemModel().pictureEndPoint)){
				DebugLog.d( "picture show : " + currentItem.getWorkItemModel().pictureEndPoint);
				holder.picture.setVisibility(View.VISIBLE);
				//ImageLoader.getInstance().displayImage("file://" + currentItem.getWorkItemModel().pictureEndPoint, holder.picture);
			}
			else
				holder.picture.setVisibility(View.GONE);
		}

		switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_COLUMN:
				DebugLog.d("TYPE_COLUMN");
				holder.label.setText(currentItem.getColumn().column_name);
				DebugLog.d("label : " + currentItem.getColumn().column_name);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				DebugLog.d("TYPE_LABEL");
				holder.label.setText(currentItem.getWorkItemModel().label);
				DebugLog.d("label : " + currentItem.getWorkItemModel().label);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				DebugLog.d("TYPE_OPERATOR");
				holder.label.setText(currentItem.getOperator().name);
				DebugLog.d(currentItem.getOperator().name);
				break;
			case ItemFormRenderModel.TYPE_CHECKBOX:
				DebugLog.d("TYPE_CHECKBOX");
				holder.label.setText(currentItem.getWorkItemModel().label);
				DebugLog.d("checkbox itemvalue : "+(currentItem.getItemValue() == null ? currentItem.getItemValue() : currentItem.getItemValue().value));
				reviseCheckBox(holder.checkBox, currentItem, currentItem.getItemValue() == null ? null : currentItem.getItemValue().value.split("[,]"), currentItem.getRowId(), currentItem.getOperatorId());
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				DebugLog.d("TYPE_RADIO");
				DebugLog.d("label : " + currentItem.getWorkItemModel().label);
				DebugLog.d("radio button itemvalue : "+(currentItem.getItemValue() == null ? currentItem.getItemValue() : currentItem.getItemValue().value));
				holder.label.setText(currentItem.getWorkItemModel().label);
				reviseRadio(holder.radio, currentItem, currentItem.getItemValue() == null ? null : currentItem.getItemValue().value.split("[|]"), currentItem.getRowId(), currentItem.getOperatorId());
				setMandatoryVisibility(holder,getItem(position));

				break;
			case ItemFormRenderModel.TYPE_HEADER:
				DebugLog.d("TYPE HEADER");
				DebugLog.d("workFormGroupId = " + workFormGroupId);
                DebugLog.d("workFormGroupName = " + workFormGroupName);
				DebugLog.d("workTypeName = " + workTypeName);
				if (workFormGroupName.equalsIgnoreCase("Photograph") && BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
					DebugLog.d("Parent label : " + currentItem.getLabel());
					holder.upload_status.setVisibility(View.VISIBLE);
					int i = 0;
					for (ItemFormRenderModel child : currentItem.getChildren()) {
						i++;
						DebugLog.d("=child ke-" + i);
						DebugLog.d("=child label : " + child.getLabel());
						DebugLog.d("=child type : " + child.getType());
						if (child.getType() == ItemFormRenderModel.TYPE_PICTURE_RADIO) {

							if (null != child.getItemValue()) {
								DebugLog.d("=child scheduleId : " + child.getItemValue().scheduleId);
								DebugLog.d("=child itemId : " + child.getItemValue().itemId);
								DebugLog.d("=child getOperatorId() : " + child.getItemValue().itemId);
								DebugLog.d("=child uploadStatus : " + child.getItemValue().uploadStatus);
								DebugLog.d("=child photoStatus : " + child.getItemValue().photoStatus);
								DebugLog.d("=child remark : " + child.getItemValue().remark);

								if (child.getItemValue().uploadStatus == FormValueModel.UPLOAD_DONE) {
									holder.upload_status.setText("SUCCESS");
								} else
								if (child.getItemValue().uploadStatus == FormValueModel.UPLOAD_NONE) {
									holder.upload_status.setText("NOT COMPLETE");
									break;
								} else
								if (child.getItemValue().uploadStatus == FormValueModel.UPLOAD_FAIL) {
									holder.upload_status.setText("FAILED");
									break;
								}
							}
							else {
								DebugLog.d("=child.getItemValue() = null");
								holder.upload_status.setText("NOT COMPLETE");
								break;
							}

						}
					}
				}
				holder.label.setText(currentItem.getWorkItemModel().labelHeader);
				holder.colored.setText(currentItem.getPercentage());
				holder.plain.setText(currentItem.getWhenExact());
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				DebugLog.d("TYPE_PICTURE_RADIO");
				holder.photoRadio.setScheduleId(scheduleId);
				holder.photoRadio.setWorkTypeName(workTypeName);
				holder.photoRadio.setItemFormRenderModel(getItem(position));
				holder.photoRadio.setItemValue(currentItem.getItemValue(),true);
				holder.upload.setTag(position);
				setUploadButtonVisibility(holder);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				DebugLog.d("TYPE_PICTURE");
				holder.photo.setItemFormRenderModel(getItem(position));
				holder.photo.setItemValue(currentItem.getItemValue(),true);
				holder.upload.setTag(position);
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				DebugLog.d("TYPE_TEXT_INPUT");
				holder.label.setText(currentItem.getWorkItemModel().label);
				if(currentItem.getWorkItemModel().description == null){
					holder.description.setVisibility(View.GONE);
				} else{
					holder.description.setVisibility(View.VISIBLE);
					holder.description.setText(currentItem.getWorkItemModel().description);
				}
				if (currentItem.getWorkItemModel().default_value != null) {
					holder.input.setInputType(InputType.TYPE_CLASS_NUMBER);
					DebugLog.d("default value not null");
					if (currentItem.getWorkItemModel().default_value.isEmpty()) {
						holder.input.setHint("0");
					} else {
						holder.input.setHint(currentItem.getWorkItemModel().default_value);
					}
				}

				holder.input.setTextChange(null);
				holder.input.setTag(getItem(position));
				holder.input.setText("");
				if (currentItem.getItemValue() != null)
					holder.input.setText(currentItem.getItemValue().value);
				holder.input.setTextChange(textInputListener);
				holder.input.setEnabled(isInputItemEnabled(currentItem.getWorkItemModel()));
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				DebugLog.d("TYPE_EXPAND");
				holder.label.setText(currentItem.getWorkItemModel().label);
				holder.label.setOnClickListener(view -> processExpand(holder,position));
				DebugLog.d("position="+position+" state="+expandState.get(position));
				holder.expandButton.setRotation(expandState.get(position) ? 180f : 0f);
				holder.expandButton.setOnClickListener(view -> processExpand(holder,position));
				break;
			default:
				break;
		}

		return convertView;
	}

	private void setMandatoryVisibility(ViewHolder viewHolder, ItemFormRenderModel itemFormRenderModel) {
		if (itemFormRenderModel.getWorkItemModel().mandatory) {
			viewHolder.mandatory.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mandatory.setVisibility(View.GONE);
		}
	}

	private void setUploadButtonVisibility(ViewHolder holder) {

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			if (StringUtil.isNotNullAndNotEmpty(wargaId) || StringUtil.isNotNullAndNotEmpty(barangId)) {
				holder.upload.setVisibility(View.INVISIBLE);
			} else
				holder.upload.setVisibility(View.VISIBLE);
		}
	}

	private void reviseCheckBox(LinearLayout linear,ItemFormRenderModel item, String[] split, int rowId, int operatorId){
		boolean isHorizontal;
		boolean isEnabled = isInputItemEnabled(item.getWorkItemModel());

		isHorizontal = 3 >= item.getWorkItemModel().options.size();
		DebugLog.d("isHorizontal : " + isHorizontal);
		DebugLog.d("linear child count after addview : " + linear.getChildCount());
		for (int i = 0; i< linear.getChildCount(); i++){
			CheckBox checkBox = (CheckBox) linear.getChildAt(i);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(false);
			checkBox.setEnabled(isEnabled);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		DebugLog.d("linear child count before addview : " + linear.getChildCount());
		for (int i = 0; i< linear.getChildCount(); i++){
			//binding checkbox
			if (i < item.getWorkItemModel().options.size()) {
				CheckBox checkBox = (CheckBox) linear.getChildAt(i);
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setText(item.getWorkItemModel().options.get(i).label);
				isHorizontal = item.getWorkItemModel().options.get(i).label.length() < 4;
				checkBox.setTag(item);
				checkBox.setOnCheckedChangeListener(null);
				if (split != null)
					for(int j = 0; j < split.length; j++) {
						if (item.getWorkItemModel().options.get(i).value.equalsIgnoreCase(split[j]))
							checkBox.setChecked(true);
					}
				else
					checkBox.setChecked(false);
				checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else linear.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = linear.getChildCount(); i < item.getWorkItemModel().options.size(); i++){
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(item.getWorkItemModel().options.get(i).label);
			isHorizontal = item.getWorkItemModel().options.get(i).label.length() < 4;
			checkBox.setTag(item);
			checkBox.setEnabled(isEnabled);
			linear.addView(checkBox);
			checkBox.setOnCheckedChangeListener(null);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.getWorkItemModel().options.get(i).value.equalsIgnoreCase(split[j]))
						checkBox.setChecked(true);
				}
			else
				checkBox.setChecked(false);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		linear.setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
	}

	private void reviseRadio(RadioGroup radioGroup, ItemFormRenderModel item, String[] split, int rowId, int operatorId){

		boolean isHorizontal;
		boolean isEnabled = isInputItemEnabled(item.getWorkItemModel());

		for (int i = 0; i< radioGroup.getChildCount(); i++){
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(null);
			radioButton.setEnabled(isEnabled);
		}
		radioGroup.clearCheck();
		isHorizontal = 3 >= item.getWorkItemModel().options.size();
		DebugLog.d("isHorizontal : " + isHorizontal);
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			//binding checkbox
			if (i < item.getWorkItemModel().options.size()){
				RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
				radioButton.setVisibility(View.VISIBLE);
				radioButton.setText(item.getWorkItemModel().options.get(i).label);
				isHorizontal = item.getWorkItemModel().options.get(i).label.length() < 4;
				radioButton.setTag(item);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.getWorkItemModel().options.get(i).value.equalsIgnoreCase(split[j]))
							radioGroup.check(radioButton.getId());
					}
				DebugLog.d("checkedChangeListener ... ");
				radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else radioGroup.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = radioGroup.getChildCount(); i < item.getWorkItemModel().options.size(); i++){
			RadioButton radioButton = new RadioButton(context);
			radioButton.setText(item.getWorkItemModel().options.get(i).label);
			isHorizontal = item.getWorkItemModel().options.get(i).label.length() < 4;
			radioButton.setTag(item);
			radioButton.setEnabled(isEnabled);
			radioGroup.addView(radioButton);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.getWorkItemModel().options.get(i).value.equalsIgnoreCase(split[j]))
						radioGroup.check(radioButton.getId());
				}
			radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		radioGroup.setOrientation(isHorizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
	}

	private class ViewHolder {
		TextView label;
		TextView upload_status;
		TextView colored;
		TextView plain;
		TextView description;
		PhotoItemRadio photoRadio;
		PhotoItem photo;
		LinearLayout checkBox;
		FormInputText input;
		RadioGroup radio;
		ImageView picture;
		TextView mandatory;
		ImageView upload;
		LinearLayout expandButton;
	}

	OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
		ItemFormRenderModel item = (ItemFormRenderModel) buttonView.getTag();
		String value = null;
		for (WorkFormOptionsModel option : item.getWorkItemModel().options)
			if (option.label.equals(buttonView.getText())){
				value = option.value;
				break;
			}
		DebugLog.d( "-=-=-=- value : " + value);
		saveValue(item, isChecked, true, value);
	};

	FormTextChange textInputListener = (inputTextValue, view) -> {
		if (view.getTag() != null){
			ItemFormRenderModel item = (ItemFormRenderModel) view.getTag();
			saveValueForTextInput(item, inputTextValue);
		}
	};

	private boolean isInputItemEnabled(WorkFormItemModel workFormItem) {

		boolean isEnabled = false;
		if (!workFormItem.disable) {
			if ((TowerApplication.getInstance().IS_CHECKING_HASIL_PM() && isChecklistOrSiteInformation) || !TowerApplication.getInstance().IS_CHECKING_HASIL_PM())
				isEnabled = true;
		}
		DebugLog.d("workItemDisable : " + workFormItem.disable);
		DebugLog.d("is SAP ? : " + BuildConfig.FLAVOR.equalsIgnoreCase("sap"));
		DebugLog.d("is checking hasil pm ? " + TowerApplication.getInstance().IS_CHECKING_HASIL_PM());
		DebugLog.d("is checklist or site information ? " + isChecklistOrSiteInformation);
		DebugLog.d("is enabled ? " + isEnabled);
		return isEnabled;
	}

	private void saveValue(ItemFormRenderModel itemFormRenderModel, boolean isAdding, boolean isCompundButton, String value) {

		DebugLog.d("\n== SAVING VALUE ===");

		if (itemFormRenderModel.getItemValue() == null){
			itemFormRenderModel.setItemValue(new FormValueModel());
			itemFormRenderModel.getItemValue().operatorId = itemFormRenderModel.getOperatorId();
			itemFormRenderModel.getItemValue().itemId = itemFormRenderModel.getWorkItemModel().id;
			itemFormRenderModel.getItemValue().scheduleId = itemFormRenderModel.getSchedule().id;
			itemFormRenderModel.getItemValue().rowId = itemFormRenderModel.getRowId();
			itemFormRenderModel.getItemValue().value = value;
		}

		// SAP only
		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			itemFormRenderModel.getItemValue().wargaId = wargaId;
			itemFormRenderModel.getItemValue().barangId = barangId;
		}

		if (isCompundButton){
			if (isAdding){ //adding value on check box
				DebugLog.d("adding value");
				if (itemFormRenderModel.getItemValue().value == null){
					itemFormRenderModel.getItemValue().value = value;
					itemFormRenderModel.getSchedule().sumTaskDone++;
				} else{
					// any value apply before
					String[] chkBoxValue = itemFormRenderModel.getItemValue().value.split("[,]");
					for(int i = 0; i < chkBoxValue.length; i++){ 
						if (chkBoxValue[i].equalsIgnoreCase(value))
							break;
						if (i == chkBoxValue.length - 1)
							itemFormRenderModel.getItemValue().value += ","+value;
					}
				}

				itemFormRenderModel.getItemValue().uploadStatus = FormValueModel.UPLOAD_NONE;
				saveAfterCheck(itemFormRenderModel);

			} else { // deleting on checkbox
				DebugLog.d("deleting value");
				String[] chkBoxValue = itemFormRenderModel.getItemValue().value.split("[,]");
				itemFormRenderModel.getItemValue().value = "";

				for(int i = 0; i < chkBoxValue.length; i++){ 
					if (!chkBoxValue[i].equalsIgnoreCase(value))
						if (i == chkBoxValue.length - 1 || chkBoxValue[chkBoxValue.length - 1].equalsIgnoreCase(value))
							itemFormRenderModel.getItemValue().value += chkBoxValue[i];
						else
							itemFormRenderModel.getItemValue().value += chkBoxValue[i]+',';
				}

				if (itemFormRenderModel.getItemValue().value.equalsIgnoreCase("")){
					deleteAfterCheck(itemFormRenderModel);
					itemFormRenderModel.getSchedule().sumTaskDone--;
				}
				else{
					itemFormRenderModel.getItemValue().uploadStatus = FormValueModel.UPLOAD_NONE;
					saveAfterCheck(itemFormRenderModel);
				}
			}
		}

		itemFormRenderModel.getSchedule().save();

		if (itemFormRenderModel.getItemValue() != null &&
			itemFormRenderModel.getSchedule() != null) {
			DebugLog.d("=== ITEM UPDATES ===");
			DebugLog.d("isAdding="+isAdding+", isCompundButton="+isCompundButton+", value="+value);
			DebugLog.d("item scheduleid : " + itemFormRenderModel.getItemValue().scheduleId);
			DebugLog.d("item operatorid : " + itemFormRenderModel.getItemValue().operatorId);
			DebugLog.d("item itemid : " + itemFormRenderModel.getItemValue().itemId);
			DebugLog.d("item siteid : " + itemFormRenderModel.getItemValue().siteId);
			DebugLog.d("item gpsaccur : " + itemFormRenderModel.getItemValue().gpsAccuracy);
			DebugLog.d("item rowid : " + itemFormRenderModel.getItemValue().rowId);
			DebugLog.d("item remark : " + itemFormRenderModel.getItemValue().remark);
			DebugLog.d("item photostatus : " + itemFormRenderModel.getItemValue().photoStatus);
			DebugLog.d("item latitude : " + itemFormRenderModel.getItemValue().latitude);
			DebugLog.d("item longitude : " + itemFormRenderModel.getItemValue().longitude);
			DebugLog.d("item value : " + itemFormRenderModel.getItemValue().value);
			DebugLog.d("item uploadstatus : " + itemFormRenderModel.getItemValue().uploadStatus);
			DebugLog.d("item photodate : " + itemFormRenderModel.getItemValue().photoDate);
			DebugLog.d("item value : " + itemFormRenderModel.getItemValue().value);
			DebugLog.d("item wargaid : " + itemFormRenderModel.getItemValue().wargaId);
			DebugLog.d("item barangid : " + itemFormRenderModel.getItemValue().barangId);
			DebugLog.d("schedule task done : "+itemFormRenderModel.getSchedule().sumTaskDone);
		}
	}

	private void saveValueForTextInput(ItemFormRenderModel itemFormRenderModel, String value) {

		DebugLog.d("\n== SAVING VALUE ===");
		boolean isAdding = false;
		if (itemFormRenderModel.getItemValue() == null){
			itemFormRenderModel.setItemValue(new FormValueModel());
			isAdding = true;
		}

		itemFormRenderModel.getItemValue().operatorId = itemFormRenderModel.getOperatorId();
		itemFormRenderModel.getItemValue().itemId = itemFormRenderModel.getWorkItemModel().id;
		itemFormRenderModel.getItemValue().scheduleId = itemFormRenderModel.getSchedule().id;
		itemFormRenderModel.getItemValue().rowId = itemFormRenderModel.getRowId();
		itemFormRenderModel.getItemValue().value = value; // save value changes
		itemFormRenderModel.getItemValue().uploadStatus = FormValueModel.UPLOAD_NONE;
		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			itemFormRenderModel.getItemValue().wargaId = wargaId;
			itemFormRenderModel.getItemValue().barangId = barangId;
		}
		itemFormRenderModel.getItemValue().save();

		if (isAdding){
			DebugLog.d("adding item value to table");
			itemFormRenderModel.getSchedule().sumTaskDone++;
			itemFormRenderModel.getSchedule().save();
		}

		if (TextUtils.isEmpty(value)) { // kalau valuenya = "", maka didelete dari FormValue
			DebugLog.d("deleting item value from table");
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				FormValueModel.delete(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId, itemFormRenderModel.getItemValue().operatorId, itemFormRenderModel.getItemValue().wargaId, itemFormRenderModel.getItemValue().barangId);
			else
				FormValueModel.delete(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId, itemFormRenderModel.getItemValue().operatorId);
			itemFormRenderModel.setItemValue(null);
			itemFormRenderModel.getSchedule().sumTaskDone--;
			itemFormRenderModel.getSchedule().save();
		}

		if (itemFormRenderModel.getItemValue() != null &&
				itemFormRenderModel.getSchedule() != null) {
			DebugLog.d("=== ITEM UPDATES ===");
			DebugLog.d("item scheduleid : " + itemFormRenderModel.getItemValue().scheduleId);
			DebugLog.d("item operatorid : " + itemFormRenderModel.getItemValue().operatorId);
			DebugLog.d("item itemid : " + itemFormRenderModel.getItemValue().itemId);
			DebugLog.d("item siteid : " + itemFormRenderModel.getItemValue().siteId);
			DebugLog.d("item gpsaccur : " + itemFormRenderModel.getItemValue().gpsAccuracy);
			DebugLog.d("item rowid : " + itemFormRenderModel.getItemValue().rowId);
			DebugLog.d("item remark : " + itemFormRenderModel.getItemValue().remark);
			DebugLog.d("item photostatus : " + itemFormRenderModel.getItemValue().photoStatus);
			DebugLog.d("item latitude : " + itemFormRenderModel.getItemValue().latitude);
			DebugLog.d("item longitude : " + itemFormRenderModel.getItemValue().longitude);
			DebugLog.d("item value : " + itemFormRenderModel.getItemValue().value);
			DebugLog.d("item uploadstatus : " + itemFormRenderModel.getItemValue().uploadStatus);
			DebugLog.d("item photodate : " + itemFormRenderModel.getItemValue().photoDate);
			DebugLog.d("item value : " + itemFormRenderModel.getItemValue().value);
			DebugLog.d("item wargaid : " + itemFormRenderModel.getItemValue().wargaId);
			DebugLog.d("item barangid : " + itemFormRenderModel.getItemValue().barangId);
			DebugLog.d("schedule task done : "+itemFormRenderModel.getSchedule().sumTaskDone);
		}
	}
	private void saveAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.getWorkItemModel().scope_type.equalsIgnoreCase("all")){
			for (OperatorModel operator : itemFormRenderModel.getSchedule().operators){
				itemFormRenderModel.getItemValue().operatorId = operator.id;
				itemFormRenderModel.getItemValue().save();
			}
		}else
			itemFormRenderModel.getItemValue().save();
	}
	
	private void deleteAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.getWorkItemModel().scope_type.equalsIgnoreCase("all")) {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				FormValueModel.deleteAllBy(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId, itemFormRenderModel.getItemValue().wargaId, itemFormRenderModel.getItemValue().barangId);
			} else
				FormValueModel.deleteAllBy(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId);

		} else {
			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				FormValueModel.delete(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId, itemFormRenderModel.getItemValue().operatorId, itemFormRenderModel.getItemValue().wargaId, itemFormRenderModel.getItemValue().barangId);
			else
				FormValueModel.delete(itemFormRenderModel.getItemValue().scheduleId, itemFormRenderModel.getItemValue().itemId, itemFormRenderModel.getItemValue().operatorId);
		}
	}

	private boolean isAudit() {
		if (workTypeName == null)
			return false;
		return workTypeName.equalsIgnoreCase("SITE AUDIT");
	}

	public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
		animator.setDuration(200);
		animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
		return animator;
	}

	private void processExpand(ViewHolder holder, int position) {
		if (shown.size()>position+1) {
			DebugLog.d("aaa position=" + position);
			if (expandState.get(position)) {
				createRotateAnimator(holder.expandButton, 180f, 0f).start();
				expandState.put(position, false);
				DebugLog.d("aaa position=" + position + " state=false");
				List<ItemFormRenderModel> models = new ArrayList<>();
				boolean collapse = true;
				while (collapse) {
					ItemFormRenderModel item = shown.get(position + 1);
					if (item.getType() != ItemFormRenderModel.TYPE_EXPAND) {
						models.add(shown.remove(position + 1));
					} else {
						collapse = false;
					}
				}
				sparseArray.put(position,models);
			} else {
				createRotateAnimator(holder.expandButton, 0f, 180f).start();
				expandState.put(position, true);
				DebugLog.d("aaa position=" + position + " state=true");
				List<ItemFormRenderModel> models = sparseArray.get(position);
				if (models!=null && !models.isEmpty()) {
					for (int i = 0; i < models.size(); i++) {
						shown.add(position + i + 1, models.get(i));
					}
				}
			}
			updateView();
		}
	}
}