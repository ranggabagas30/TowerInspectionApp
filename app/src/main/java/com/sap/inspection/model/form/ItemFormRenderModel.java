package com.sap.inspection.model.form;

import android.os.Debug;
import android.os.Parcel;
import android.util.Log;

import com.google.gson.Gson;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class ItemFormRenderModel extends BaseModel {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_HEADER = 1;
    public static final int TYPE_PICTURE_RADIO = 2;
    public static final int TYPE_OPERATOR = 3;
    public static final int TYPE_CHECKBOX = 4;
    public static final int TYPE_RADIO = 5;
    public static final int TYPE_LINE_DEVIDER = 6;
    public static final int TYPE_TEXT_INPUT = 7;
    public static final int TYPE_COLUMN = 8;
    public static final int TYPE_LABEL = 9;
    public static final int TYPE_HEADER_DIVIDER = 10;
    public static final int TYPE_PICTURE = 11;
    public static final int TYPE_EXPAND = 12;
    public static final int MAX_TYPE = 13;

    public RowColumnModel firstItem;
    public WorkFormItemModel workItemModel;
    public ItemValueModel itemValue;
    public ItemFormRenderModel parent;
    public OperatorModel operator;
    public int operatorId;
    public int rowId;
    public String workFormGroupName;
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
        return percent == 0 ? "" : percent + "%";
    }

    /*public void setPercent() {
        ItemValueModel model = new ItemValueModel();
        filledTask = model.countTaskDone(schedule.id, rowId);
        countPercent();
    }
*/
    public void addFillableTask() {
        fillableTask++;
    }

    public void setSchedule(ScheduleBaseModel schedule) {
        this.schedule = schedule;
    }

    public void setWorkFormGroupName(String workFormGroupName) {
        this.workFormGroupName = workFormGroupName;
    }

    public void addFilled() {
        filledTask++;
        countPercent();
    }

    public void subFilled() {
        filledTask++;
        countPercent();
    }

    private void countPercent() {
        percent = fillableTask == 0 ? 0 : filledTask * 100 / fillableTask;
        DebugLog.d("-=--=-=- percent : " + percent);
        DebugLog.d("-=--=-=- filled task : " + filledTask);
        DebugLog.d("-=--=-=- fillable task : " + fillableTask);
    }

    public String getWhen() {
        if (percent == 0)
            return "no action yet";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(when);
        return DateTools.timeElapse(calendar);
    }

    public void add(ItemFormRenderModel child) {
        if (children == null)
            children = new ArrayList<>();
        child.setParent(this);
        children.add(child);
    }

    public void setParent(ItemFormRenderModel parent) {
        this.parent = parent;
    }

    public int getCount() {
        if (open && children != null)
            return children.size() + 1;
        return 1;
    }

    public ArrayList<ItemFormRenderModel> getModels() {
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

    public void setRowColumnModels(Vector<RowColumnModel> rowColumnModels, String parentLabel) {
        if (schedule.operators == null || schedule.operators.size() == 0) {
            DebugLog.d("operator none");
            return;
        }
//		do {
//			firstItem = rowColumnModels.remove(0);
//		}
//		while (firstItem.items.size() == 0);
        //init the header
        firstItem = null;
        int firstColId = -1;
        for (ColumnModel columnModel : columns) {
            if (columnModel.position == 1) {
                firstColId = columnModel.id;
                DebugLog.d("first column : " + columnModel.column_name + " colid " + columnModel.id);
                break;
            }
        }
        DebugLog.d("first column detected : " + firstColId);
        if (firstColId == -1)
            return;

        for (int i = 0; i < rowColumnModels.size(); i++) {
            DebugLog.d("current row col id : " + rowColumnModels.get(i).id);
            if (rowColumnModels.get(i).column_id == firstColId) {
                firstItem = rowColumnModels.remove(i);
                DebugLog.d("first item id : " + firstItem.id);
            }
        }

        if (firstItem == null)
            return;

        //generate first cell
        if (firstItem.items.size() != 0) {
            this.type = TYPE_HEADER;
            this.workItemModel = firstItem.items.get(0);
            this.label = workItemModel.label;
            this.hasPicture = workItemModel.pictureEndPoint != null;
            DebugLog.d("====================== check if picture is not null : " + workItemModel.pictureEndPoint);
            if (parentLabel != null)
                workItemModel.label = workItemModel.label + " \n " + parentLabel;
            if (this.workItemModel.field_type.equalsIgnoreCase("label") && !workItemModel.expand) {
                firstItem.items.remove(0);
            }
            else if (workItemModel.field_type.equalsIgnoreCase("file")) {
                DebugLog.d("item details : ");
                DebugLog.d("item parent label : " + workItemModel.label);
                DebugLog.d("id : " + workItemModel.id);
                DebugLog.d("field type : file");
                DebugLog.d("scope type : " + workItemModel.scope_type);
                DebugLog.d("isExpand type : " + workItemModel.expand);
            }
            ItemFormRenderModel child = new ItemFormRenderModel();
            child.type = TYPE_HEADER_DIVIDER;
            child.parent = this;
            add(child);
        }


        boolean anyInput = checkAnyInput(firstItem.items);

        if (getColumn(firstItem.column_id).column_name != null && !getColumn(firstItem.column_id).column_name.equalsIgnoreCase("")) {
            if (checkAnyInputHead(firstItem.items)) {
                ItemFormRenderModel child = new ItemFormRenderModel();
                child.type = TYPE_COLUMN;
                child.column = getColumn(firstItem.column_id);
                child.parent = this;
                add(child);
            }
        }

        DebugLog.d("anyInput = " + anyInput);
        DebugLog.d("operators size = " + schedule.operators.size());
        DebugLog.d("operator=" + new Gson().toJson(schedule.operators));
        DebugLog.d("schedule_values=" + new Gson().toJson(schedule.schedule_values));
        DebugLog.d("operator number=" + schedule.operator_number);
        if (!anyInput) {
            operator = schedule.operators.get(schedule.operator_number);
            generateItemsPerOperator(firstItem, schedule.operators.get(schedule.operator_number).id);
            DebugLog.d("operator "+schedule.operator_number);
        } else {
            DebugLog.d("operator loop");
            for (int i = 0; i < schedule.operators.size(); i++) {
                DebugLog.d("operator id " + schedule.operators.get(i).id);
                ItemFormRenderModel child = new ItemFormRenderModel();
                child.type = TYPE_OPERATOR;
                child.operator = schedule.operators.get(i);
                child.parent = this;
                add(child);
                generateItemsPerOperator(firstItem, schedule.operators.get(i).id);
                if (schedule.operators.size() - 2 >= 0 && i < schedule.operators.size() - 1) {
                    child = new ItemFormRenderModel();
                    child.type = TYPE_LINE_DEVIDER;
                    child.parent = this;
                    add(child);
                }
            }
        }

        //generate other cell
        for (RowColumnModel rowCol : rowColumnModels) {
            if (rowCol.items.size() > 0) {
                ItemFormRenderModel child = new ItemFormRenderModel();
                child.type = TYPE_COLUMN;
                child.column = getColumn(rowCol.column_id);
                child.parent = this;
                add(child);
                anyInput = checkAnyInput(rowCol.items);

                if (!anyInput) {
                    generateItemsPerOperator(rowCol, schedule.operators.get(0).id);
                } else
                    for (int i = 0; i < schedule.operators.size(); i++) {
                        child = new ItemFormRenderModel();
                        child.type = TYPE_OPERATOR;
                        child.operator = schedule.operators.get(i);
                        child.parent = this;
                        add(child);
                        generateItemsPerOperator(rowCol, schedule.operators.get(i).id);
                        if (schedule.operators.size() - 2 >= 0 && i < schedule.operators.size() - 1) {
                            child = new ItemFormRenderModel();
                            child.type = TYPE_LINE_DEVIDER;
                            child.parent = this;
                            add(child);
                        }
                    }
            }
        }
    }

    private void generateItemsPerOperator(RowColumnModel rowCol, int operatorId) {
        for (int i = 0; i < rowCol.items.size(); i++) {
            DebugLog.d("item : " + rowCol.items.get(i).label + " id : " + rowCol.items.get(i).id + " operator id : " + operatorId);
            if (rowCol.items.get(i).id == 441)
                DebugLog.d("===================== item : " + rowCol.items.get(i).label + " id : " + rowCol.items.get(i).id + "=================");
            if (rowCol.items.get(i).field_type == null)
                continue;
            generateViewItem(rowCol.row_id, rowCol.items.get(i), operatorId);
        }
    }

    //check if any input type
    private boolean checkAnyInput(Vector<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            DebugLog.d("scope type : " + item.scope_type);
            if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null && !item.scope_type.equalsIgnoreCase("all"))
                return true;
        }
        return false;
    }

    private boolean checkAnyInputHead(Vector<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            DebugLog.d("scope type : " + item.scope_type);
            if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null)
                return true;
        }
        return false;
    }

    private void generateViewItem(int rowId, WorkFormItemModel workItemModel, int operatorId) {
        DebugLog.d("item label : " + workItemModel.label + " id : " + workItemModel.id + " expand="+workItemModel.expand);
        DebugLog.d("item description : " + workItemModel.description + " id : " + workItemModel.id + "");
        if (workItemModel.pictureEndPoint != null)
            hasPicture = true;
        if (workItemModel.field_type.equalsIgnoreCase("label") && !workItemModel.expand) {
            ItemFormRenderModel child = new ItemFormRenderModel();
            child.type = TYPE_LABEL;
            child.workItemModel = workItemModel;
            child.parent = this;
            add(child);
            return;
        }

        DebugLog.d(schedule.id + " | " + workItemModel.id + " | " + operatorId + " | " + rowId);
        ItemValueModel initValue = new ItemValueModel();
        ItemFormRenderModel child = new ItemFormRenderModel();
        child.workItemModel = workItemModel;
        child.itemValue = initValue.getItemValue(schedule.id, workItemModel.id, operatorId);
        child.rowId = rowId;
        child.operatorId = operatorId;
        child.schedule = schedule;
        DebugLog.d("value : " + initValue.value);
        DebugLog.d("uploadstatus : " + initValue.uploadStatus);
        if (workItemModel.field_type.equalsIgnoreCase("label") && workItemModel.expand) {
            hasInput = true;
            child.type = TYPE_EXPAND;
            this.addFillableTask();
            child.parent = this;
            add(child);
            return;
        } else if (workItemModel.field_type.equalsIgnoreCase("text_field")) {
            hasInput = true;
            child.type = TYPE_TEXT_INPUT;
            Log.d("default_value", "default value : " + child.workItemModel.default_value);

            this.addFillableTask();
            child.parent = this;
            add(child);
            return;
        } else if (workItemModel.field_type.equalsIgnoreCase("checkbox")) {
            hasInput = true;
            child.type = TYPE_CHECKBOX;
            this.addFillableTask();
            child.parent = this;
            add(child);
            return;
        } else if (workItemModel.field_type.equalsIgnoreCase("radio") || workItemModel.field_type.equalsIgnoreCase("dropdown")) {
            hasInput = true;
            child.type = TYPE_RADIO;
            this.addFillableTask();
            child.parent = this;
            add(child);
            return;
        } else if (workItemModel.field_type.equalsIgnoreCase("file")) {
            hasInput = true;
            child.type = TYPE_PICTURE_RADIO;
            DebugLog.d("fieldType : file");
            DebugLog.d("workFormGroupName : " + workFormGroupName);
            if ("Photograph".equalsIgnoreCase(workFormGroupName) && BuildConfig.FLAVOR.equalsIgnoreCase("sap")) {
                child.workItemModel.mandatory = true;
                child.workItemModel.save();
            }
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

    private ColumnModel getColumn(int colId) {
        DebugLog.d("-==-=--=" + this.columns.size());
        for (ColumnModel oneColumn : this.columns) {
            if (colId == oneColumn.id)
                return oneColumn;
        }
        return null;
    }

}
