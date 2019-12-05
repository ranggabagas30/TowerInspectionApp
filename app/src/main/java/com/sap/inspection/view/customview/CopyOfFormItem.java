package com.sap.inspection.view.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sap.inspection.R;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.form.WorkFormColumnModel;
import com.sap.inspection.model.form.ItemUpdateResultViewModel;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;

public class CopyOfFormItem extends RelativeLayout {

	private LinearLayout rootItem;
	//	private ArrayList<RowColumnModel> rowColumnModels;
	//	private ArrayList<WorkFormItemModel> items;
	private ArrayList<WorkFormColumnModel> column;
	private Context context;
	private String label;
	private TextView rowTitle;
	private TextView rowSubColored;
	private TextView rowSubPlain;
	private ScheduleBaseModel schedule;
	private FormTextChange formTextChange;
	private OnCheckedChangeListener checkedChangeListener;
	private OnClickListener photoListener;
	private ItemUpdateResultViewModel itemUpdateModel;
	private boolean hasInput = false;
	
	public String getLabel() {
		return label;
	}

	public void setPhotoListener(OnClickListener photoListener) {
		this.photoListener = photoListener;
	}
	
	public void setFormTextChange(FormTextChange formTextChange) {
		this.formTextChange = formTextChange;
	}

	public void setCheckedChangeListener(
			OnCheckedChangeListener checkedChangeListener) {
		this.checkedChangeListener = checkedChangeListener;
	}

	public ItemUpdateResultViewModel getItemUpdateModel() {
		return itemUpdateModel;
	}

	public void setSchedule(ScheduleBaseModel schedule) {
		this.schedule = schedule;
	}

	public CopyOfFormItem(Context context) {
		super(context);
		init(context);
	}

	public CopyOfFormItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CopyOfFormItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		this.context = context;
		if (itemUpdateModel == null)
			itemUpdateModel = new ItemUpdateResultViewModel();

		View root = LayoutInflater.from(context).inflate(R.layout.item_form, null);
		rootItem = root.findViewById(R.id.rootItem);
		rowTitle = root.findViewById(R.id.item_drill_title);
		rowSubColored = root.findViewById(R.id.item_drill_subcolored);
		itemUpdateModel.colored = rowSubColored;
		rowSubColored.setText("asdasdas");
		rowSubPlain = root.findViewById(R.id.item_drill_subplain);
		rowSubPlain.setText("asdasdas");
		itemUpdateModel.plain = rowSubPlain;
		this.addView(root);
	}

	public String getColumnName(int colId){
		for (WorkFormColumnModel oneColumn : this.column) {
			if (colId == oneColumn.id)
				return oneColumn.column_name;
		}
		return "";
	}

	public void setColumns(ArrayList<WorkFormColumnModel> column) {
		this.column = column;
	}

	public void setRowColumnModels(ArrayList<RowColumnModel> rowColumnModels) {
		//		this.rowColumnModels = rowColumnModels;
		RowColumnModel firstItem;
		do {
			firstItem = rowColumnModels.remove(0);
		}
		while (firstItem.items.size() == 0);
		//generate first cell
		if (firstItem.items.size() != 0){
			rowTitle.setText(firstItem.items.get(0).label);
			label = firstItem.items.get(0).label;
		}
//		generateItemsforTitle(firstItem,"operator id");
		
		boolean anyInput = checkAnyInput(firstItem.items);

		if (!anyInput){
			generateItemsforTitle(firstItem,schedule.operators.get(0).id);
		}
		else
			for (int i = 0; i < schedule.operators.size(); i++) {
				TextView textView = new TextView(context);
				textView.setText(schedule.operators.get(i).name);
				textView.setPadding(15, 15, 15, 0);
				rootItem.addView(textView);
				generateItemsforTitle(firstItem,schedule.operators.get(i).id);
			}
		rowSubColored.setText("");
		rowSubPlain.setText("no action yet");

		//generate other cell
		for (RowColumnModel rowCol : rowColumnModels) {
			if (rowCol.items.size() > 0){
				View view = new View(context);
				view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
				view.setBackgroundColor(Color.parseColor("#e0e0e0"));
				rootItem.addView(view);

				TextView textView = new TextView(context);
				textView.setPadding(15, 15, 15, 0);
				textView.setText(getColumnName(rowCol.column_id));
				textView.setTextSize(20);
				textView.setTypeface(null, Typeface.BOLD);
				rootItem.addView(textView);
				anyInput = checkAnyInput(rowCol.items);

				if (!anyInput)
					generateItemsPerOperator(rowCol,schedule.operators.get(0).id);
				else
					for (int i = 0; i < schedule.operators.size(); i++) {
						textView = new TextView(context);
						textView.setText(schedule.operators.get(i).name);
						textView.setPadding(15, 15, 15, 0);
						rootItem.addView(textView);
						generateItemsPerOperator(rowCol,schedule.operators.get(i).id);
					}
			}
		}
		if (!hasInput)
			this.setVisibility(View.GONE);
	}

	private void generateItemsforTitle(RowColumnModel rowCol,int operatorId){
		for (int i = 0 ; i< rowCol.items.size(); i ++) {
			log("item : "+rowCol.items.get(i).label+" id : "+rowCol.items.get(i).id);
			View view2 = null;
			if (i == 0)
				view2 = generateViewItemForTitle(rowCol.row_id,rowCol.items.get(i),operatorId);
			else
				view2 = generateViewItem(rowCol.row_id,rowCol.items.get(i),operatorId);
			if (view2 != null){
				if (i < rowCol.items.size() - 1) //not last item
					view2.setPadding(15, 15, 15, 0);
				else
					view2.setPadding(15, 15, 15, 15); //last item
				rootItem.addView(view2);
			}
		}
	}

	private void generateItemsPerOperator(RowColumnModel rowCol,int operatorId){
		for (int i = 0 ; i< rowCol.items.size(); i ++) {
			log("item : "+rowCol.items.get(i).label+" id : "+rowCol.items.get(i).id);
			if (rowCol.items.get(i).field_type == null)
				continue;
			View view2 = generateViewItem(rowCol.row_id,rowCol.items.get(i),operatorId);
			if (i < rowCol.items.size() - 1) //not last item
				view2.setPadding(15, 15, 15, 0);
			else
				view2.setPadding(15, 15, 15, 15); //last item
			rootItem.addView(view2);
		}
	}

	//check if any input type
	private boolean checkAnyInput(ArrayList<WorkFormItemModel> items){
		for (WorkFormItemModel item : items) {
			log("scope type : "+item.scope_type);
			if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null && !item.scope_type.equalsIgnoreCase("all"))
				return true;
		}
		return false;
	}

	public View generateViewItem(int rowId, WorkFormItemModel item,int operatorId){
		if (item.field_type.equalsIgnoreCase("label")){

			TextView textView = new TextView(context);
			textView.setText(item.label);
			return textView;
		}

		log(schedule.id+" | "+item.id+" | "+operatorId+" | "+rowId);
		FormValueModel initValue = new FormValueModel();
		initValue = FormValueModel.getItemValue(schedule.id,item.id,operatorId);
		log("================================================");
		log("================================================");
		log("================================================");
		if (initValue != null)
			log("value : "+initValue.value);
		else 
			log("initvalue is null");

		if (item.field_type.equalsIgnoreCase("text_field")){
			hasInput = true;
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			TextView textView = new TextView(context);
			textView.setText(item.label);
			linearLayout.addView(textView);

			FormInputText editText = new FormInputText(context);
			editText.setMinimumWidth(100);
			editText.setMinWidth(100);
			editText.setTextChange(formTextChange);
			if (initValue != null)
				editText.setText(initValue.value);
			editText.setTag(rowId+"|"+item.id+"|"+operatorId+"|not|0");
			linearLayout.addView(editText);

			if (item.description != null){
				textView = new TextView(context);
				textView.setText(item.description);
				linearLayout.addView(textView);
			}

			return linearLayout;
		}else if (item.field_type.equalsIgnoreCase("checkbox")){
			hasInput = true;
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			if (item.options.size() > 4)
				linearLayout.setOrientation(LinearLayout.VERTICAL);

            String[] split = null;
			if (initValue != null)
				split = initValue.value.split("[,]");

			for (WorkFormOptionsModel option : item.options) {
				if (option.label.length() > 4)
					linearLayout.setOrientation(LinearLayout.VERTICAL);
				CheckBox checkBox = new CheckBox(context);
				checkBox.setText(option.label);
				setPadding(0, 0, 15, 0);
				checkBox.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+option.value+"|0");
				checkBox.setOnCheckedChangeListener(checkedChangeListener);
				if (initValue != null)
					for (int i = 0; i < split.length; i++)
						if (option.value.equalsIgnoreCase(split[i]))
							checkBox.setChecked(true);
				linearLayout.addView(checkBox);
			}

			if (item.label != null && !item.label.equalsIgnoreCase("")){
				LinearLayout lineLayout = new LinearLayout(context);
				lineLayout.setOrientation(LinearLayout.VERTICAL);
				TextView textView = new TextView(context);
				textView.setText(item.label);
				lineLayout.addView(textView);
				lineLayout.addView(linearLayout);
				return lineLayout;
			}
			return linearLayout;
		}else if (item.field_type.equalsIgnoreCase("radio")){
			hasInput = true;
			RadioGroup radioGroup = new RadioGroup(context);
			radioGroup.setOrientation(RadioGroup.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			if (item.options.size() > 4)
				radioGroup.setOrientation(RadioGroup.VERTICAL);

            String[] split = null;
			if (initValue != null)
				split = initValue.value.split("[,]");
			for (WorkFormOptionsModel option : item.options) {
				if (option.label.length() > 4)
					radioGroup.setOrientation(RadioGroup.VERTICAL);

				RadioButton radioButton = new RadioButton(context);
				radioButton.setText(option.label);

				setPadding(0, 0, 15, 0);
				radioButton.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+option.value+"|0");
				radioButton.setOnCheckedChangeListener(checkedChangeListener);
				radioGroup.addView(radioButton);
				if (initValue != null)
					for (int i = 0; i < split.length; i++){
						log(split[i]+" ||| "+option.value);
						if (option.value.equalsIgnoreCase(split[i]))
							radioGroup.check(radioButton.getId());
					}
			}

			if (item.label != null && !item.label.equalsIgnoreCase("")){
				LinearLayout lineLayout = new LinearLayout(context);
				lineLayout.setOrientation(LinearLayout.VERTICAL);
				TextView textView = new TextView(context);
				textView.setText(item.label);
				lineLayout.addView(textView);
				lineLayout.addView(radioGroup);
				return lineLayout;
			}
			return radioGroup;
		}else if (item.field_type.equalsIgnoreCase("file")){
			hasInput = true;
			PhotoItemRadio photo = new PhotoItemRadio(context);
			photo.setButtonTakePictureListener(photoListener);
			if (initValue == null){
				initValue = new FormValueModel();
				initValue.itemId = item.id;
				initValue.scheduleId = schedule.id;
				initValue.rowId = rowId;
				initValue.typePhoto = true;
				initValue.operatorId = operatorId;
			}
			//photo.notifyDataChanged(initValue);
			return photo;
			//		}else if (item.field_type.equalsIgnoreCase("checkbox")){
			//			LinearLayout linearLayout = new LinearLayout(context);
			//			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			//
			//			for (WorkFormOptionsModel option : item.options) {
			//				CheckBox checkBox = new CheckBox(context);
			//				checkBox.setText(option.label);
			//				setPadding(0, 0, 15, 0);
			//				linearLayout.addView(checkBox);
			//			}
			//			return linearLayout;
		}

		return new View(context);
	}

	public View generateViewItemForTitle(int rowId, WorkFormItemModel item,int operatorId){
		if (item.field_type.equalsIgnoreCase("label")){
			return null;
		}
		
		log(schedule.id+" | "+item.id+" | "+operatorId);
		FormValueModel initValue = new FormValueModel();
		initValue = FormValueModel.getItemValue(schedule.id,item.id,operatorId);
		log("================================================");
		log("================================================");
		log("================================================");
		if (initValue != null)
			log("value : "+initValue.value);
		else 
			log("initvalue is null");

		if (item.field_type.equalsIgnoreCase("text_field")){
			hasInput = true;
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			FormInputText editText = new FormInputText(context);
			editText.setMinimumWidth(100);
			editText.setMinWidth(100);
			editText.setTextChange(formTextChange);
			if (initValue != null)
				editText.setText(initValue.value);
			editText.setTag(rowId+"|"+item.id+"|"+operatorId+"|not|0");
			linearLayout.addView(editText);

			if (item.description != null){
				TextView textView = new TextView(context);
				textView.setText(item.description);
				linearLayout.addView(textView);
			}

			return linearLayout;
		}else if (item.field_type.equalsIgnoreCase("checkbox")){
			hasInput = true;
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			if (item.options.size() > 4)
				linearLayout.setOrientation(LinearLayout.VERTICAL);

            String[] split = null;
			if (initValue != null)
				split = initValue.value.split("[,]");

			for (WorkFormOptionsModel option : item.options) {
				if (option.label.length() > 4)
					linearLayout.setOrientation(LinearLayout.VERTICAL);
				CheckBox checkBox = new CheckBox(context);
				checkBox.setText(option.label);
				setPadding(0, 0, 15, 0);
				checkBox.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+option.value+"|0");
				checkBox.setOnCheckedChangeListener(checkedChangeListener);
				if (initValue != null)
					for (int i = 0; i < split.length; i++)
						if (option.value.equalsIgnoreCase(split[i]))
							checkBox.setChecked(true);
				linearLayout.addView(checkBox);
			}

			return linearLayout;
		}else if (item.field_type.equalsIgnoreCase("radio")){
			hasInput = true;
			RadioGroup radioGroup = new RadioGroup(context);
			radioGroup.setOrientation(RadioGroup.HORIZONTAL);
			itemUpdateModel.sumTask ++;

			if (item.options.size() > 4)
				radioGroup.setOrientation(RadioGroup.VERTICAL);

            String[] split = null;
			if (initValue != null)
				split = initValue.value.split("[,]");
			for (WorkFormOptionsModel option : item.options) {
				if (option.label.length() > 4)
					radioGroup.setOrientation(RadioGroup.VERTICAL);

				RadioButton radioButton = new RadioButton(context);
				radioButton.setText(option.label);

				setPadding(0, 0, 15, 0);
				radioButton.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+option.value+"|0");
				radioButton.setOnCheckedChangeListener(checkedChangeListener);
				radioGroup.addView(radioButton);
				if (initValue != null)
					for (int i = 0; i < split.length; i++){
						log(split[i]+" ||| "+option.value);
						if (option.value.equalsIgnoreCase(split[i]))
							radioGroup.check(radioButton.getId());
					}
			}

			return radioGroup;
		}else if (item.field_type.equalsIgnoreCase("file")){
			hasInput = true;
			PhotoItemRadio photo = new PhotoItemRadio(context);
			photo.setButtonTakePictureListener(photoListener);
			if (initValue == null){
				initValue = new FormValueModel();
				initValue.itemId = item.id;
				initValue.scheduleId = schedule.id;
				initValue.rowId = rowId;
				initValue.typePhoto = true;
				initValue.operatorId = operatorId;
			}
				//photo.notifyDataChanged(initValue);
			return photo;
			//		}else if (item.field_type.equalsIgnoreCase("checkbox")){
			//			LinearLayout linearLayout = new LinearLayout(context);
			//			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			//
			//			for (WorkFormOptionsModel option : item.options) {
			//				CheckBox checkBox = new CheckBox(context);
			//				checkBox.setText(option.label);
			//				setPadding(0, 0, 15, 0);
			//				linearLayout.addView(checkBox);
			//			}
			//			return linearLayout;
		}

		return new View(context);
	}

	private void log(String msg){
		DebugLog.d(msg);
	}
}
