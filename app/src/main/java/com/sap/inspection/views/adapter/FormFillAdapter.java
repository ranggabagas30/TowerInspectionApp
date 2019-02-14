package com.sap.inspection.views.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Debug;
import android.text.InputType;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.aakira.expandablelayout.Utils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.value.DbManagerValue;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.rules.SavingRule;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.Utility;
import com.sap.inspection.view.FormInputText;
import com.sap.inspection.view.MyTextView;
import com.sap.inspection.view.PhotoItem;
import com.sap.inspection.view.PhotoItemRadio;
import com.slidinglayer.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class FormFillAdapter extends MyBaseAdapter {

	private Context context;
	private ItemFormRenderModel workItemModel;
	private ArrayList<ItemFormRenderModel> models;
	private ArrayList<ItemFormRenderModel> shown;
	private String scheduleId;
	private String workType;
	private String workFormGroupName;
	private int workFormGroupId;
	private OnClickListener photoListener;
    private OnClickListener uploadListener;
    private boolean isChecklistOrSiteInformation;
	//	private List<ItemFormRenderModel> shownX = new ArrayList<>();
	private SparseArray<List<ItemFormRenderModel>> sparseArray = new SparseArray<>();

	//	private OnCheckedChangeListener onCheckedChangeListener;
	private SavingRule savingRule;

	private SparseBooleanArray expandState = new SparseBooleanArray();

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public void setWorkFormGroupId(int workFormGroupId) {
		this.workFormGroupId = workFormGroupId;
	}

	public void setWorkFormGroupName(String workFormGroupName) {
		this.workFormGroupName = workFormGroupName;
		isChecklistOrSiteInformation = workFormGroupName.toUpperCase().matches(Constants.regexChecklist) ||
									  workFormGroupName.toUpperCase().matches(Constants.regexSiteInformation);
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

	public FormFillAdapter(Context context) {
		this.context = context;
		if (null == models)
			models = new ArrayList<ItemFormRenderModel>();
		if (null == shown)
			shown = new ArrayList<ItemFormRenderModel>();
		if (null == workItemModel)
			workItemModel = new ItemFormRenderModel();
	}

	public void setItems(ArrayList<ItemFormRenderModel> models){
		this.models = models;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {

		shown.clear();
		for (ItemFormRenderModel model : models) {
			shown.addAll(model.getModels());
		}
		for (int i = 0; i < shown.size(); i++) {
			ItemFormRenderModel model = shown.get(i);
			if (model.column!=null && model.column.column_name!=null
					&& model.column.column_name.equalsIgnoreCase("corrective"))
				shown.remove(i);
		}

		DebugLog.d("models size = "+shown.size());
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < this.shown.size(); i++) {
			ItemFormRenderModel item = this.shown.get(i);
			DebugLog.d("i="+i+" "+item.getLabel()+
					" type="+item.type);
			if (item.type==ItemFormRenderModel.TYPE_EXPAND)
				strings.add(item.workItemModel.label);
		}

		for (int i = 0; i < strings.size(); i++) {
			String s = strings.get(i);
			for (int j = 0; j < shown.size(); j++) {
				if (s.equalsIgnoreCase(shown.get(j).getLabel()) && shown.get(j).type==ItemFormRenderModel.TYPE_HEADER) {
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

	public void updateView() {
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
		return getItem(position).type;
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
		if (convertView == null) {
			DebugLog.d("convertView == null");
			holder = new ViewHolder();
			DebugLog.d("position="+position+" type="+getItemViewType(position));
			switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_CHECKBOX:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_checkbox,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				holder.checkBox = (LinearLayout) convertView.findViewById(R.id.item_form_check);
				holder.mandatory = (TextView) convertView.findViewById(R.id.item_form_mandatory);
				break;
			case ItemFormRenderModel.TYPE_COLUMN:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_column,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_HEADER_DIVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header_devider,null);
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				((MyTextView) convertView.findViewById(R.id.item_form_label)).setBold(context, true);
				holder.upload_status = (TextView) convertView.findViewById(R.id.item_form_upload_status);
				holder.colored = (TextView) convertView.findViewById(R.id.item_form_colored);
				holder.plain = (TextView) convertView.findViewById(R.id.item_form_plain);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_label,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_LINE_DEVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_line_devider,null);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_operator,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo_radio,null);
				holder.photoRadio = (PhotoItemRadio) convertView.findViewById(R.id.item_form_photo);
				holder.upload = (ImageView) convertView.findViewById(R.id.item_form_upload);
				holder.photoRadio.setAudit(isAudit());
				holder.photoRadio.setButtonClickListener(photoListener);
				holder.upload.setOnClickListener(uploadListener);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo,null);
				holder.mandatory = (TextView) convertView.findViewById(R.id.item_form_mandatory);
				holder.photo = (PhotoItem) convertView.findViewById(R.id.item_form_photo);
				holder.upload = (ImageView) convertView.findViewById(R.id.item_form_upload);
				holder.photo.setAudit(isAudit());
				holder.photo.setButtonClickListener(photoListener);
                holder.upload.setOnClickListener(uploadListener);
				holder.photo.setSavingRule(savingRule);
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_radio,null);
				holder.mandatory = (TextView) convertView.findViewById(R.id.item_form_mandatory);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				holder.radio = (RadioGroup) convertView.findViewById(R.id.item_form_radio);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_text_field,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_label);
				holder.description = (TextView) convertView.findViewById(R.id.item_form_description);
				holder.input = (FormInputText) convertView.findViewById(R.id.item_form_input);
				holder.mandatory = (TextView) convertView.findViewById(R.id.item_form_mandatory);

				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_expand,null);
				holder.label = (TextView) convertView.findViewById(R.id.item_form_expand_title);
				((MyTextView) convertView.findViewById(R.id.item_form_expand_title)).setBold(context, true);
				holder.expandButton = (LinearLayout) convertView.findViewById(R.id.item_form_expand_button);
				break;
			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				convertView = new View(context);
				break;
			}
			holder.picture = (ImageView) convertView.findViewById(R.id.picture);
			convertView.setTag(holder);
		} else {
			DebugLog.d("convertView != null");
			holder = (ViewHolder) convertView.getTag();
			DebugLog.d("convertView tag : " + holder);
		}
		
		if (getItem(position).workItemModel != null)
			DebugLog.d( "picture : "+getItem(position).workItemModel.pictureEndPoint);
		if (holder.picture != null){
			if (getItem(position).workItemModel != null && getItem(position).workItemModel.pictureEndPoint != null){
				DebugLog.d( "picture show : "+getItem(position).workItemModel.pictureEndPoint);
				holder.picture.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage("file://"+getItem(position).workItemModel.pictureEndPoint, holder.picture);
			}
			else
				holder.picture.setVisibility(View.GONE);
		}

		switch (getItemViewType(position)) {

			case ItemFormRenderModel.TYPE_COLUMN:
				DebugLog.d("TYPE_COLUMN");
				holder.label.setText(getItem(position).column.column_name);
				DebugLog.d("label : " + getItem(position).column.column_name);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				DebugLog.d("TYPE_LABEL");
				holder.label.setText(getItem(position).workItemModel.label);
				DebugLog.d("label : " + getItem(position).workItemModel.label);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				DebugLog.d("TYPE_OPERATOR");
				holder.label.setText(getItem(position).operator.name);
				DebugLog.d(getItem(position).operator.name);
				break;
			case ItemFormRenderModel.TYPE_CHECKBOX:
				DebugLog.d("TYPE_CHECKBOX");
				check(position);
				holder.label.setText(getItem(position).workItemModel.label);
				DebugLog.d("checkbox itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
				reviseCheckBox(holder.checkBox, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[,]"), getItem(position).rowId, getItem(position).operatorId);
				setMandatory(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				DebugLog.d("TYPE_RADIO");
				DebugLog.d("label : " + getItem(position).workItemModel.label);
				DebugLog.d("radio button itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
				check(position);
				holder.label.setText(getItem(position).workItemModel.label);
				reviseRadio(holder.radio, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[|]"), getItem(position).rowId, getItem(position).operatorId);
				setMandatory(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				DebugLog.d("TYPE HEADER");
				DebugLog.d("workFormGroupId = " + workFormGroupId);
                DebugLog.d("workFormGroupName = " + workFormGroupName);
				DebugLog.d("workType = " + workType);
				if (workFormGroupName.equalsIgnoreCase("Photograph") && BuildConfig.FLAVOR.equalsIgnoreCase("sap")) {
					DebugLog.d("Parent label : " + getItem(position).label);
					holder.upload_status.setVisibility(View.VISIBLE);
					int i = 0;
					for (ItemFormRenderModel child : getItem(position).children) {
						i++;
						DebugLog.d("=child ke-" + i);
						DebugLog.d("=child label : " + child.label);
						DebugLog.d("=child type : " + child.type);
						if (child.type == ItemFormRenderModel.TYPE_PICTURE_RADIO) {

							if (null != child.itemValue)
							{
								DebugLog.d("=child scheduleId : " + child.itemValue.scheduleId);
								DebugLog.d("=child itemId : " + child.itemValue.itemId);
								DebugLog.d("=child operatorId : " + child.itemValue.itemId);
								DebugLog.d("=child uploadStatus : " + child.itemValue.uploadStatus);
								DebugLog.d("=child photoStatus : " + child.itemValue.photoStatus);
								DebugLog.d("=child remark : " + child.itemValue.remark);

								if (child.itemValue.uploadStatus == ItemValueModel.UPLOAD_DONE) {
									holder.upload_status.setText("SUCCESS");
								} else
								if (child.itemValue.uploadStatus == ItemValueModel.UPLOAD_NONE) {
									holder.upload_status.setText("NOT COMPLETE");
									break;
								} else
								if (child.itemValue.uploadStatus == ItemValueModel.UPLOAD_FAIL) {
									holder.upload_status.setText("FAILED");
									break;
								}
							}
							else {
								DebugLog.d("=child.itemValue = null");
								holder.upload_status.setText("NOT COMPLETE");
								break;
							}

						}
					}


				}
				holder.label.setText(getItem(position).workItemModel.labelHeader);
				holder.colored.setText(getItem(position).getPercent());
				holder.plain.setText(getItem(position).getWhen());
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				DebugLog.d("TYPE_PICTURE_RADIO");
				holder.photoRadio.setItemFormRenderModel(getItem(position));
				holder.photoRadio.setValue(getItem(position).itemValue,true);
				holder.upload.setTag(position);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				DebugLog.d("TYPE_PICTURE");
				holder.photo.setItemFormRenderModel(getItem(position));
				holder.photo.setValue(getItem(position).itemValue,true);
				setMandatory(holder,getItem(position));
				holder.upload.setTag(position);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				DebugLog.d("TYPE_TEXT_INPUT");
				holder.label.setText(getItem(position).workItemModel.label);
				if(getItem(position).workItemModel.description == null){
					holder.description.setVisibility(View.GONE);
				}
				else{
					holder.description.setVisibility(View.VISIBLE);
					holder.description.setText(getItem(position).workItemModel.description);
				}
				if (getItem(position).workItemModel.default_value != null) {

					holder.input.setInputType(InputType.TYPE_CLASS_NUMBER);

					DebugLog.d("default value not null");
					if (getItem(position).workItemModel.default_value.isEmpty()) {

					   /* if (CommonUtils.isNumeric(getItem(position).workItemModel.default_value)) {

							holder.input.setHint("0");

                        } else {

							holder.input.setHint("kosong");

                        }*/

						holder.input.setHint("0");

					} else {
						holder.input.setHint(getItem(position).workItemModel.default_value);
					}

				}

                /*if (CommonUtils.isNumeric(getItem(position).workItemModel.default_value)) {

					holder.input.setInputType(InputType.TYPE_CLASS_NUMBER);

                } else {

					holder.input.setInputType(InputType.TYPE_CLASS_TEXT);
                }*/

				holder.input.setTextChange(null);
				holder.input.setTag(getItem(position));
				if (getItem(position).itemValue != null)
					holder.input.setText(getItem(position).itemValue.value);
				else
					holder.input.setText("");
				holder.input.setTextChange(formTextChange);
				holder.input.setEnabled(!getItem(position).workItemModel.disable && !MyApplication.getInstance().isInCheckHasilPm());
				check(position);
				setMandatory(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				DebugLog.d("TYPE_EXPAND");
				holder.label.setText(getItem(position).workItemModel.label);
				holder.label.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						processExpand(holder,position);
					}
				});
				DebugLog.d("position="+position+" state="+expandState.get(position));
				holder.expandButton.setRotation(expandState.get(position) ? 180f : 0f);
				holder.expandButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						processExpand(holder,position);
					}
				});
				break;
			default:
				break;
		}

		//toggleEditable(holder);
		return convertView;
	}

	private void setMandatory(ViewHolder viewHolder, ItemFormRenderModel itemFormRenderModel) {
		if (itemFormRenderModel.workItemModel.mandatory) {
			viewHolder.mandatory.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mandatory.setVisibility(View.GONE);
		}
	}
	private void check(int position){
//		log("============== aaaaaa ============================");
//		log("============== aaaaaa ============================");
//		log("============== aaaaaa ============================");
//		log("row id : "+ getItem(position).rowId);
//		log("schedule Id : "+ getItem(position).schedule.id);
//		log("operator id : "+ getItem(position).operatorId);
//		log("item id : "+ getItem(position).workItemModel.id);
	}

	private void reviseCheckBox(LinearLayout linear,ItemFormRenderModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		boolean isEnabled = !item.workItemModel.disable && !MyApplication.getInstance().isInCheckHasilPm();
		//boolean isEnabled = !item.workItemModel.disable && (!MyApplication.getInstance().isInCheckHasilPm() || isChecklistOrSiteInformation);

		isHorizontal = 3 >= item.workItemModel.options.size();
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
			if (i < item.workItemModel.options.size()){
				CheckBox checkBox = (CheckBox) linear.getChildAt(i);
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setText(item.workItemModel.options.get(i).label);
				isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
				//				checkBox.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
				checkBox.setTag(item);
				checkBox.setOnCheckedChangeListener(null);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
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
		for(int i = linear.getChildCount(); i < item.workItemModel.options.size(); i++){
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(item.workItemModel.options.get(i).label);
			isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
			//			checkBox.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
			checkBox.setTag(item);
			linear.addView(checkBox);
			checkBox.setOnCheckedChangeListener(null);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
						checkBox.setChecked(true);
				}
			else
				checkBox.setChecked(false);
			checkBox.setEnabled(isEnabled);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		linear.setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
	}

	private void reviseRadio(RadioGroup radioGroup,ItemFormRenderModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		boolean isEnabled = !item.workItemModel.disable && !MyApplication.getInstance().isInCheckHasilPm();
		//boolean isEnabled = !item.workItemModel.disable && (!MyApplication.getInstance().isInCheckHasilPm() || isChecklistOrSiteInformation);

		radioGroup.setOrientation(isHorizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
		DebugLog.d("radioGroup child count after addview : " + radioGroup.getChildCount());
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(null);
			radioButton.setEnabled(isEnabled);
			DebugLog.d("radioButton enabled ? " + isEnabled);
		}

		radioGroup.clearCheck();
		isHorizontal = 3 >= item.workItemModel.options.size();
		DebugLog.d("isHorizontal : " + isHorizontal);
		DebugLog.d("radioGroup child count before addview : " + radioGroup.getChildCount());

		for (int i = 0; i< radioGroup.getChildCount(); i++){
			//binding checkbox
			if (i < item.workItemModel.options.size()){
				RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
				radioButton.setVisibility(View.VISIBLE);
				radioButton.setText(item.workItemModel.options.get(i).label);
				isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
				//				radioButton.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
				radioButton.setTag(item);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j])) {
							radioGroup.check(radioButton.getId());
							DebugLog.d("split[" + j + "] = " + split[j]);
						}
					}
                DebugLog.d("checkedChangeListener ... ");
				radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else radioGroup.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = radioGroup.getChildCount(); i < item.workItemModel.options.size(); i++){
			RadioButton radioButton = new RadioButton(context);
			radioButton.setText(item.workItemModel.options.get(i).label);
			isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
			//			radioButton.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
			radioButton.setTag(item);
			radioGroup.addView(radioButton);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j])) {
						radioGroup.check(radioButton.getId());

					}
				}
			radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			radioButton.setEnabled(isEnabled);
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

	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ItemFormRenderModel item = (ItemFormRenderModel) buttonView.getTag();
			String value = null;
			for (WorkFormOptionsModel option : item.workItemModel.options)
				if (option.label.equals(buttonView.getText())){
					value = option.value;
					break;
				}
			DebugLog.d( "-=-=-=- value : "+value);
			saveValue(item, isChecked, true, value);
		}
	};

	FormTextChange formTextChange = new FormTextChange() {

		@Override
		public void onTextChange(String string, View view) {
			if (view.getTag() != null){
				ItemFormRenderModel item = (ItemFormRenderModel) view.getTag();
				saveValue(item, !string.equalsIgnoreCase(""),false,string);
			}
		}
	};

	private void saveValue(ItemFormRenderModel itemFormRenderModel, boolean isAdding, boolean isCompundButton,String value){

		DebugLog.d("=================================================================");
		if (itemFormRenderModel.itemValue == null){
			itemFormRenderModel.itemValue = new ItemValueModel();
			itemFormRenderModel.itemValue.operatorId = itemFormRenderModel.operatorId;
			itemFormRenderModel.itemValue.itemId = itemFormRenderModel.workItemModel.id;
			itemFormRenderModel.itemValue.scheduleId = itemFormRenderModel.schedule.id;
			itemFormRenderModel.itemValue.rowId = itemFormRenderModel.rowId;
		}
		DebugLog.d("isAdding="+isAdding+" isCompundButton="+isCompundButton+" value="+value);
		DebugLog.d("===== value : "+itemFormRenderModel.itemValue.value);
		if (isCompundButton){
			if (isAdding){ //adding value on check box
				DebugLog.d("goto adding");
				// value still null or blank
				if (itemFormRenderModel.itemValue.value == null  || itemFormRenderModel.itemValue.value.equalsIgnoreCase("")){
					itemFormRenderModel.itemValue.value = value;
					itemFormRenderModel.schedule.sumTaskDone ++;
				}
				// any value apply before
				else{
					String[] chkBoxValue = itemFormRenderModel.itemValue.value.split("[,]");
					for(int i = 0; i < chkBoxValue.length; i++){ 
						if (chkBoxValue[i].equalsIgnoreCase(value))
							break;
						if (i == chkBoxValue.length - 1)
							itemFormRenderModel.itemValue.value += ","+value;
					}
				}
				itemFormRenderModel.itemValue.uploadStatus = ItemValueModel.UPLOAD_NONE;
				saveAfterCheck(itemFormRenderModel);
			}else{ // deleting on checkbox
				DebugLog.d("goto deleting");
				String[] chkBoxValue = itemFormRenderModel.itemValue.value.split("[,]");
				itemFormRenderModel.itemValue.value = "";
				//removing unchecked checkbox value
				for(int i = 0; i < chkBoxValue.length; i++){ 
					if (!chkBoxValue[i].equalsIgnoreCase(value))
						if (i == chkBoxValue.length - 1 || chkBoxValue[chkBoxValue.length - 1].equalsIgnoreCase(value))
							itemFormRenderModel.itemValue.value += chkBoxValue[i];
						else
							itemFormRenderModel.itemValue.value += chkBoxValue[i]+','; 
				}
				chkBoxValue = null;
				if (itemFormRenderModel.itemValue.value.equalsIgnoreCase("")){
					deleteAfterCheck(itemFormRenderModel);
					itemFormRenderModel.schedule.sumTaskDone--;
				}
				else{
					itemFormRenderModel.itemValue.uploadStatus = ItemValueModel.UPLOAD_NONE;
					saveAfterCheck(itemFormRenderModel);
				}
			}
		}
		else{
			if (!isAdding){
				if (itemFormRenderModel.itemValue.value != null)
					itemFormRenderModel.itemValue.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId);
				itemFormRenderModel.itemValue = null;
				itemFormRenderModel.schedule.sumTaskDone--;
			}
			else{
				if (itemFormRenderModel.itemValue.value == null)
					itemFormRenderModel.schedule.sumTaskDone++;
				itemFormRenderModel.itemValue.value = value;
				itemFormRenderModel.itemValue.uploadStatus = ItemValueModel.UPLOAD_NONE;
				itemFormRenderModel.itemValue.save();
			}
		}
		itemFormRenderModel.schedule.save();
//		itemFormRenderModel.parent.setPercent();
//		log("===== value : "+itemFormRenderModel.itemValue.value);
//		log("row id : "+ itemFormRenderModel.itemValue.rowId);
//		log("schedule Id : "+ itemFormRenderModel.itemValue.scheduleId);
//		log("operator id : "+ itemFormRenderModel.itemValue.operatorId);
//		log("item id : "+ itemFormRenderModel.itemValue.itemId);
		DebugLog.d("task done : "+itemFormRenderModel.schedule.sumTaskDone);
		//		setPercentage(itemFormRenderModel.itemValue.rowId);
	}
	
	private void saveAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")){
			for (OperatorModel operator : itemFormRenderModel.schedule.operators){
				itemFormRenderModel.itemValue.operatorId = operator.id;
				itemFormRenderModel.itemValue.save();
			}
		}else
			itemFormRenderModel.itemValue.save();
	}
	
	private void deleteAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")){
			for (OperatorModel operator : itemFormRenderModel.schedule.operators){
				itemFormRenderModel.itemValue.operatorId = operator.id;
				itemFormRenderModel.itemValue.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId);
			}
		}else
			itemFormRenderModel.itemValue.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId);
	}

	private boolean isAudit() {
		if (workType == null)
			return false;
		return workType.equalsIgnoreCase("SITE AUDIT");
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
//				shownX.clear();
				List<ItemFormRenderModel> models = new ArrayList<>();
				boolean collapse = true;
				while (collapse) {
					ItemFormRenderModel item = shown.get(position + 1);
					if (item.type != ItemFormRenderModel.TYPE_EXPAND) {
						models.add(shown.remove(position + 1));
//						shownX.add(shown.remove(position + 1));
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

	private void toggleEditable(ViewHolder holder) {
		if (MyApplication.getInstance().isInCheckHasilPm()) {
			DebugLog.d("input is disabled");
			if (holder.radio != null) holder.radio.setEnabled(false);
			if (holder.input != null) holder.input.setEnabled(false);
			if (holder.checkBox != null) holder.checkBox.setEnabled(false);
			if (holder.photoRadio!= null) holder.photoRadio.setEnabled(false);
		} else {
			DebugLog.d("input is enabled");
			if (holder.radio != null) holder.radio.setEnabled(true);
			if (holder.input != null) holder.input.setEnabled(true);
			if (holder.checkBox != null) holder.checkBox.setEnabled(true);
			if (holder.photoRadio!= null) holder.photoRadio.setEnabled(true);
		}
	}
}