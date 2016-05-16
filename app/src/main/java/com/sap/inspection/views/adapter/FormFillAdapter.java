package com.sap.inspection.views.adapter;

import android.content.Context;
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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.R;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.rules.SavingRule;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.FormInputText;
import com.sap.inspection.view.MyTextView;
import com.sap.inspection.view.PhotoItem;
import com.sap.inspection.view.PhotoItemRadio;

import java.util.ArrayList;

public class FormFillAdapter extends MyBaseAdapter {

	private Context context;
	private ArrayList<ItemFormRenderModel> models;
	private ArrayList<ItemFormRenderModel> shown;
	private String scheduleId;
	private String workType;
	private OnClickListener photoListener;
    private OnClickListener uploadListener;
	//	private OnCheckedChangeListener onCheckedChangeListener;
	private SavingRule savingRule;
	
	public void setWorkType(String workType) {
		this.workType = workType;
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
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_CHECKBOX:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_checkbox,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				holder.checkBox = (LinearLayout) view.findViewById(R.id.item_form_check);
				break;
			case ItemFormRenderModel.TYPE_COLUMN:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_column,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_HEADER_DEVIDER:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_header_devider,null);
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_header,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				((MyTextView) view.findViewById(R.id.item_form_label)).setBold(context, true);
				holder.colored = (TextView) view.findViewById(R.id.item_form_colored);
				holder.plain = (TextView) view.findViewById(R.id.item_form_plain);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_label,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_LINE_DEVIDER:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_line_devider,null);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_operator,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_photo_radio,null);
				holder.photoRadio = (PhotoItemRadio) view.findViewById(R.id.item_form_photo);
				holder.photoRadio.setAudit(isAudit());
				holder.photoRadio.setButtonClickListener(photoListener);
                holder.photoRadio.setUploadClickListener(uploadListener);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_photo,null);
				holder.photo = (PhotoItem) view.findViewById(R.id.item_form_photo);
				holder.photo.setAudit(isAudit());
				holder.photo.setButtonClickListener(photoListener);
                holder.photoRadio.setUploadClickListener(uploadListener);
				holder.photo.setSavingRule(savingRule);
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_radio,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				holder.radio = (RadioGroup) view.findViewById(R.id.item_form_radio);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_text_field,null);
				holder.label = (TextView) view.findViewById(R.id.item_form_label);
				holder.description = (TextView) view.findViewById(R.id.item_form_description);
				holder.input = (FormInputText) view.findViewById(R.id.item_form_input);
				break;
			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				view = new View(context);
				break;
			}
			holder.picture = (ImageView) view.findViewById(R.id.picture);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		
		if (getItem(position).itemModel != null)
			DebugLog.d( "picture : "+getItem(position).itemModel.pictureEndPoint);
		if (holder.picture != null){
			if (getItem(position).itemModel != null && getItem(position).itemModel.pictureEndPoint != null){
				DebugLog.d( "picture show : "+getItem(position).itemModel.pictureEndPoint);
				holder.picture.setVisibility(view.VISIBLE);
				ImageLoader.getInstance().displayImage("file://"+getItem(position).itemModel.pictureEndPoint, holder.picture);
			}
			else
				holder.picture.setVisibility(view.GONE);
		}

		switch (getItemViewType(position)) {
		case ItemFormRenderModel.TYPE_COLUMN:
			holder.label.setText(getItem(position).column.column_name);
			break;
		case ItemFormRenderModel.TYPE_LABEL:
			holder.label.setText(getItem(position).itemModel.label);
			break;
		case ItemFormRenderModel.TYPE_OPERATOR:
			holder.label.setText(getItem(position).operator.name);
			break;
		case ItemFormRenderModel.TYPE_CHECKBOX:
			check(position);
			holder.label.setText(getItem(position).itemModel.label);
			DebugLog.d("checkbox itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
			reviseCheckBox(holder.checkBox, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[,]"), getItem(position).rowId, getItem(position).operatorId);
			break;
		case ItemFormRenderModel.TYPE_RADIO:
			check(position);
			holder.label.setText(getItem(position).itemModel.label);
			DebugLog.d("radio button itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
			reviseRadio(holder.radio, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[|]"), getItem(position).rowId, getItem(position).operatorId);
			break;
		case ItemFormRenderModel.TYPE_HEADER:
			holder.label.setText(getItem(position).itemModel.labelHeader);
			holder.colored.setText(getItem(position).getPercent());
			holder.plain.setText(getItem(position).getWhen());
			break;
		case ItemFormRenderModel.TYPE_PICTURE_RADIO:
			holder.photoRadio.setItemFormRenderModel(getItem(position));
			holder.photoRadio.setValue(getItem(position).itemValue,true);
			break;
		case ItemFormRenderModel.TYPE_PICTURE:
			holder.photo.setItemFormRenderModel(getItem(position));
			holder.photo.setValue(getItem(position).itemValue,true);
			break;
		case ItemFormRenderModel.TYPE_TEXT_INPUT:
			holder.label.setText(getItem(position).itemModel.label);
			if(getItem(position).itemModel.description == null){
				holder.description.setVisibility(view.GONE);
			}
			else{
				holder.description.setVisibility(view.VISIBLE);
				holder.description.setText(getItem(position).itemModel.description);
			}
			holder.input.setTextChange(null);
			holder.input.setTag(getItem(position));
			if (getItem(position).itemValue != null)
				holder.input.setText(getItem(position).itemValue.value);
			else
				holder.input.setText("");
			holder.input.setTextChange(formTextChange);
			check(position);
			break;
		default:
			break;
		}
		return view; 
	}

	private void check(int position){
//		log("============== aaaaaa ============================");
//		log("============== aaaaaa ============================");
//		log("============== aaaaaa ============================");
//		log("row id : "+ getItem(position).rowId);
//		log("schedule Id : "+ getItem(position).schedule.id);
//		log("operator id : "+ getItem(position).operatorId);
//		log("item id : "+ getItem(position).itemModel.id);
	}

	private void reviseCheckBox(LinearLayout linear,ItemFormRenderModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		isHorizontal = 3 >= item.itemModel.options.size();
		for (int i = 0; i< linear.getChildCount(); i++){
			CheckBox checkBox = (CheckBox) linear.getChildAt(i);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(false);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		for (int i = 0; i< linear.getChildCount(); i++){
			//binding checkbox
			if (i < item.itemModel.options.size()){
				CheckBox checkBox = (CheckBox) linear.getChildAt(i);
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setText(item.itemModel.options.get(i).label);
				isHorizontal = item.itemModel.options.get(i).label.length() < 4;
				//				checkBox.setTag(rowId+"|"+item.itemModel.id+"|"+operatorId+"|"+item.itemModel.options.get(i).value+"|0");
				checkBox.setTag(item);
				checkBox.setOnCheckedChangeListener(null);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.itemModel.options.get(i).value.equalsIgnoreCase(split[j]))
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
		for(int i = linear.getChildCount(); i < item.itemModel.options.size(); i++){
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(item.itemModel.options.get(i).label);
			isHorizontal = item.itemModel.options.get(i).label.length() < 4;
			//			checkBox.setTag(rowId+"|"+item.itemModel.id+"|"+operatorId+"|"+item.itemModel.options.get(i).value+"|0");
			checkBox.setTag(item);
			linear.addView(checkBox);
			checkBox.setOnCheckedChangeListener(null);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.itemModel.options.get(i).value.equalsIgnoreCase(split[j]))
						checkBox.setChecked(true);
				}
			else
				checkBox.setChecked(false);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		linear.setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
	}

	private void reviseRadio(RadioGroup radioGroup,ItemFormRenderModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(null);
		}
		radioGroup.clearCheck();
		isHorizontal = 3 >= item.itemModel.options.size();
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			//binding checkbox
			if (i < item.itemModel.options.size()){
				RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
				radioButton.setVisibility(View.VISIBLE);
				radioButton.setText(item.itemModel.options.get(i).label);
				isHorizontal = item.itemModel.options.get(i).label.length() < 4;
				//				radioButton.setTag(rowId+"|"+item.itemModel.id+"|"+operatorId+"|"+item.itemModel.options.get(i).value+"|0");
				radioButton.setTag(item);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.itemModel.options.get(i).value.equalsIgnoreCase(split[j]))
							radioGroup.check(radioButton.getId());
					}
				radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else radioGroup.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = radioGroup.getChildCount(); i < item.itemModel.options.size(); i++){
			RadioButton radioButton = new RadioButton(context);
			radioButton.setText(item.itemModel.options.get(i).label);
			isHorizontal = item.itemModel.options.get(i).label.length() < 4;
			//			radioButton.setTag(rowId+"|"+item.itemModel.id+"|"+operatorId+"|"+item.itemModel.options.get(i).value+"|0");
			radioButton.setTag(item);
			radioGroup.addView(radioButton);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.itemModel.options.get(i).value.equalsIgnoreCase(split[j]))
						radioGroup.check(radioButton.getId());
				}
			radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		radioGroup.setOrientation(isHorizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
	}

	private class ViewHolder {
		TextView label;
		TextView colored;
		TextView plain;
		TextView description;
		PhotoItemRadio photoRadio;
		PhotoItem photo;
		LinearLayout checkBox;
		FormInputText input;
		RadioGroup radio;
		ImageView picture;
	}

	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ItemFormRenderModel item = (ItemFormRenderModel) buttonView.getTag();
			String value = null;
			for (WorkFormOptionsModel option : item.itemModel.options)
				if (option.label.equals(buttonView.getText())){
					value = option.value;
					break;
				}
			DebugLog.d( "-=-=-=- value : "+value);
			saveValue(item, isChecked, true, value);
		}
	};

	FormTextChange formTextChange = new  FormTextChange() {

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
			itemFormRenderModel.itemValue.itemId = itemFormRenderModel.itemModel.id;
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
		if (itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("all")){
			for (OperatorModel operator : itemFormRenderModel.schedule.operators){
				itemFormRenderModel.itemValue.operatorId = operator.id;
				itemFormRenderModel.itemValue.save();
			}
		}else
			itemFormRenderModel.itemValue.save();
	}
	
	private void deleteAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("all")){
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
}