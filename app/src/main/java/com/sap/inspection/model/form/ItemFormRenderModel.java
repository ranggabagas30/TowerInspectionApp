package com.sap.inspection.model.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import android.os.Parcel;
import android.util.Log;

import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DateTools;

public class ItemFormRenderModel extends BaseModel {

	public static final int TYPE_NONE = 0;
	public static final  int TYPE_HEADER = 1;
	public static final  int TYPE_PICTURE_RADIO = 2;
	public static final  int TYPE_OPERATOR = 3;
	public static final  int TYPE_CHECKBOX = 4;
	public static final  int TYPE_RADIO = 5;
	public static final  int TYPE_LINE_DEVIDER = 6;
	public static final  int TYPE_TEXT_INPUT = 7;
	public static final  int TYPE_COLUMN = 8;
	public static final  int TYPE_LABEL = 9;
	public static final  int TYPE_HEADER_DEVIDER = 10;
	public static final  int TYPE_PICTURE = 11;
	public static final  int MAX_TYPE = 12;

	public WorkFormItemModel itemModel;
	public ItemValueModel itemValue;
	public ItemFormRenderModel parent;
	public OperatorModel operator;
	public int operatorId;
	public int rowId;
	public ArrayList<ItemFormRenderModel> children;
	public int type = TYPE_NONE;
	public boolean open = true;
	public boolean hasInput = false;
	public boolean hasPicture = false;
	public ColumnModel column;
	public String label = null;
	public ScheduleBaseModel schedule;

	private ArrayList<ColumnModel> columns;
	private boolean isHeader = false;
	private long when = 0;
	private int percent = 10;
	private int fillableTask = 0;
	private int filledTask = 0;

	public void setColumn(ArrayList<ColumnModel> column) {
		this.columns = column;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public String getPercent() {
		return percent == 0 ? "" : percent+"%";
	}

	public void setPercent() {
		ItemValueModel model = new ItemValueModel();
		filledTask = model.countTaskDone(schedule.id, rowId);
		countPercent();
	}

	public void addFillableTask(){
		fillableTask++;
	}

	public void setSchedule(ScheduleBaseModel schedule) {
		this.schedule = schedule;
	}

	public void addFilled(){
		filledTask++;
		countPercent();
	}

	public void subFilled(){
		filledTask++;
		countPercent();
	}

	private void countPercent(){
		percent = fillableTask == 0? 0 : filledTask * 100 / fillableTask;
		log("-=--=-=- percent : "+percent);
		log("-=--=-=- filled task : "+filledTask);
		log("-=--=-=- fillable task : "+fillableTask);
	}

	public String getWhen(){
		if (percent == 0)
			return "no action yet"; 
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(when);
		return DateTools.timeElapse(calendar);
	}

	public void add(ItemFormRenderModel child){
		if(children == null)
			children = new ArrayList<ItemFormRenderModel>();
		child.setParent(this);
		children.add(child);
	}

	public void setParent(ItemFormRenderModel parent) {
		this.parent = parent;
	}

	public int getCount(){
		if (open && children != null)
			return children.size() + 1;
		return 1;
	}

	public ArrayList<ItemFormRenderModel> getModels(){
		ArrayList<ItemFormRenderModel> models = new ArrayList<ItemFormRenderModel>();
		models.add(this);
		models.addAll(children);
		return models; 
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
	}

	public void setRowColumnModels(Vector<RowColumnModel> rowColumnModels,String parentLabel) {
		if (schedule.operators == null || schedule.operators.size() == 0){
			log("operator none");
			return;
		}
//		do {
//			firstItem = rowColumnModels.remove(0);
//		}
//		while (firstItem.items.size() == 0);
		//init the header
		RowColumnModel firstItem = null;
		int firstColId = -1;
		for(ColumnModel columnModel : columns){
			if (columnModel.position == 1){
				firstColId = columnModel.id;
				log("first column : "+columnModel.column_name+" colid "+columnModel.id);
				break;
			}
		}
		log("first column detected : "+firstColId);
		if (firstColId == -1)
			return;
		
		for(int i = 0 ; i < rowColumnModels.size(); i++){
			log("current row col id : "+rowColumnModels.get(i).id);
			if (rowColumnModels.get(i).column_id == firstColId){
				firstItem = rowColumnModels.remove(i);
				log("first item id : "+firstItem.id);
			}
		}
		if (firstItem == null)
			return;
		//generate first cell
		if (firstItem.items.size() != 0){
			this.type = TYPE_HEADER;
			this.itemModel = firstItem.items.get(0); 
			this.label = itemModel.label;
			this.hasPicture = itemModel.pictureEndPoint != null;
			Log.d(getClass().getName(), "====================== check if picture is not null : "+itemModel.pictureEndPoint);
			if (parentLabel != null)
				itemModel.label = itemModel.label+" \n "+parentLabel;
			if (firstItem.items.get(0).field_type.equalsIgnoreCase("label"))
				firstItem.items.remove(0);

			ItemFormRenderModel child = new ItemFormRenderModel();
			child.type = TYPE_HEADER_DEVIDER;
			child.parent = this;
			add(child);
		}

		
		
		boolean anyInput = checkAnyInput(firstItem.items);
		
		if (getColumn(firstItem.column_id).column_name != null && !getColumn(firstItem.column_id).column_name.equalsIgnoreCase("")){
			if (checkAnyInputHead(firstItem.items)){
				ItemFormRenderModel child = new ItemFormRenderModel();
				child.type = TYPE_COLUMN;
				child.column = getColumn(firstItem.column_id);
				child.parent = this;
				add(child);
			}
		}

		if (!anyInput){
			operator = schedule.operators.get(0);
			generateItemsPerOperator(firstItem,schedule.operators.get(0).id);
		}
		else{
			for (int i = 0; i < schedule.operators.size(); i++) {
				ItemFormRenderModel child = new ItemFormRenderModel();
				child.type = TYPE_OPERATOR;
				child.operator = schedule.operators.get(i);
				child.parent = this;
				add(child);
				generateItemsPerOperator(firstItem,schedule.operators.get(i).id);
				if (schedule.operators.size() - 2 >= 0 && i < schedule.operators.size() - 1){
					child = new ItemFormRenderModel();
					child.type = TYPE_LINE_DEVIDER;
					child.parent = this;
					add(child);
				}
			}
		}

		//generate other cell
		for (RowColumnModel rowCol : rowColumnModels) {
			if (rowCol.items.size() > 0){
				ItemFormRenderModel child = new ItemFormRenderModel();
				child.type = TYPE_COLUMN;
				child.column = getColumn(rowCol.column_id);
				child.parent = this;
				add(child);
				anyInput = checkAnyInput(rowCol.items);

				if (!anyInput){
					generateItemsPerOperator(rowCol,schedule.operators.get(0).id);
				}
				else
					for (int i = 0; i < schedule.operators.size(); i++) {
						child = new ItemFormRenderModel();
						child.type = TYPE_OPERATOR;
						child.operator = schedule.operators.get(i);
						child.parent = this;
						add(child);
						generateItemsPerOperator(rowCol,schedule.operators.get(i).id);
						if (schedule.operators.size() - 2 >= 0 && i < schedule.operators.size() - 1){
							child = new ItemFormRenderModel();
							child.type = TYPE_LINE_DEVIDER;
							child.parent = this;
							add(child);
						}
					}
			}
		}
	}

	private void generateItemsPerOperator(RowColumnModel rowCol,int operatorId){
		for (int i = 0 ; i< rowCol.items.size(); i ++) {
			log("item : "+rowCol.items.get(i).label+" id : "+rowCol.items.get(i).id);
			if (rowCol.items.get(i).id == 441)
				log("===================== item : "+rowCol.items.get(i).label+" id : "+rowCol.items.get(i).id+"=================");
			if (rowCol.items.get(i).field_type == null)
				continue;
			generateViewItem(rowCol.row_id,rowCol.items.get(i),operatorId);
		}
	}

	//check if any input type
	private boolean checkAnyInput(Vector<WorkFormItemModel> items){
		for (WorkFormItemModel item : items) {
			log("scope type : "+item.scope_type);
			if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null && !item.scope_type.equalsIgnoreCase("all"))
				return true;
		}
		return false;
	}
	
	private boolean checkAnyInputHead(Vector<WorkFormItemModel> items){
		for (WorkFormItemModel item : items) {
			log("scope type : "+item.scope_type);
			if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null)
				return true;
		}
		return false;
	}

	private void generateViewItem(int rowId, WorkFormItemModel item,int operatorId){
		if (item.pictureEndPoint != null)
			hasPicture = true;
		if (item.field_type.equalsIgnoreCase("label")){
			ItemFormRenderModel child = new ItemFormRenderModel();
			child.type = TYPE_LABEL;
			child.itemModel = item;
			child.parent = this;
			add(child);
			return;
		}
		log("===================== item label : "+item.label+" id : "+item.id+"=================");
		log("===================== item description : "+item.description+" id : "+item.id+"=================");

		log(schedule.id+" | "+item.id+" | "+operatorId+" | "+rowId);
		ItemValueModel initValue = new ItemValueModel();
		ItemFormRenderModel child = new ItemFormRenderModel();
		child.itemModel = item;
		child.itemValue = initValue.getItemValue(schedule.id,item.id,operatorId); 
		child.rowId = rowId;
		child.operatorId = operatorId;
		child.schedule = schedule;
		log("================================================");
		log("================================================");
		log("================================================");
		log("value : "+initValue.value);

		if (item.field_type.equalsIgnoreCase("text_field")){
			hasInput = true;
			child.type = TYPE_TEXT_INPUT;
			this.addFillableTask();
			child.parent = this;
			add(child);
			return;
		}else if (item.field_type.equalsIgnoreCase("checkbox")){
			hasInput = true;
			child.type = TYPE_CHECKBOX;
			this.addFillableTask();
			child.parent = this;
			add(child);
			return;
		}else if (item.field_type.equalsIgnoreCase("radio") || item.field_type.equalsIgnoreCase("dropdown")){
			hasInput = true;
			child.type = TYPE_RADIO;
			this.addFillableTask();
			child.parent = this;
			add(child);
			return;
		}else if (item.field_type.equalsIgnoreCase("file")){
			hasInput = true;
			child.type = TYPE_PICTURE_RADIO;
			this.addFillableTask();
			child.parent = this;
			if (this.children.get(this.children.size() - 1).type == TYPE_OPERATOR)
				child.operator = this.children.remove(this.children.size() - 1).operator;
			add(child);
			return;
		}
		return;
	}

	public String getLabel() {
		return label;
	}
	
	private ColumnModel getColumn(int colId){
		log("-==-=--="+this.columns.size());
		for (ColumnModel oneColumn : this.columns) {
			if (colId == oneColumn.id)
				return oneColumn;
		}
		return null;
	}

}
