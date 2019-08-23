package com.sap.inspection.model.form;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.view.ui.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.value.FormValueModel;
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
    public FormValueModel itemValue;
    public ItemFormRenderModel parent;
    public OperatorModel operator;
    public int operatorId;
    public int rowId;
    public int workFormGroupId;
    public String workFormGroupName;
    public String workTypeName;
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

    // SAP only
    private String wargaId;
    private String barangId;

    public void setParent(ItemFormRenderModel parent) {
        this.parent = parent;
    }

    public void setColumn(ArrayList<ColumnModel> column) {
        this.columns = column;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public void setSchedule(ScheduleBaseModel schedule) {
        this.schedule = schedule;
    }

    public void setWorkFormGroupId(int workFormGroupId) {
        this.workFormGroupId = workFormGroupId;
    }

    public void setWorkFormGroupName(String workFormGroupName) {
        this.workFormGroupName = workFormGroupName;
    }

    public void setWorkTypeName(String workTypeName) {
        this.workTypeName = workTypeName;
        DebugLog.d("workTypeName : " + workTypeName);
    }

    public void setWargaid(String wargaId) {
        this.wargaId = wargaId;
        DebugLog.d("wargaid : " + wargaId);
    }

    public void setBarangid(String barangId) {
        this.barangId = barangId;
        DebugLog.d("barangid : " + barangId);
    }

    public void setRowColumnModels(Vector<RowColumnModel> rowColumnModels, String parentLabel) {

        if (schedule.operators == null || schedule.operators.size() == 0) {
            DebugLog.d("operator none");
            MyApplication.getInstance().toast("Tidak ada operator", Toast.LENGTH_LONG);
            return;
        }

        // get column data with position = 1
        DebugLog.d("> Find first item with column position = 1... \n\n");
        firstItem = null;
        int firstColId = -1;
        for (ColumnModel columnModel : columns) {
            if (columnModel.position == 1) {
                firstColId = columnModel.id;
                DebugLog.d("Found first column ! (col id, col name) : (" + columnModel.id + ", " + columnModel.column_name + ")");
                break;
            }
        }

        if (firstColId == -1)
            return;

        DebugLog.d("looping through row_col row items ... ");
        DebugLog.d("|\trow_col_id\t|\trow_id\t|\tcol_id\t|\twork_form_group_id\t|");

        for (int i = 0; i < rowColumnModels.size(); i++) {
            RowColumnModel rowcol = rowColumnModels.get(i);
            if (rowcol.column_id == firstColId) {
                DebugLog.d("|\t" + rowcol.id + "\t\t|\t" + rowcol.row_id + "\t|\t" + rowcol.column_id + "\t\t|\t" + rowcol.work_form_group_id + "\t\t| --> found first item");
                firstItem = rowColumnModels.remove(i);
            } else
                DebugLog.d("|\t" + rowcol.id + "\t\t|\t" + rowcol.row_id + "\t|\t" + rowcol.column_id + "\t\t|\t" + rowcol.work_form_group_id + "\t\t|");
        }

        if (firstItem == null || firstItem.items == null)
            return;

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            int hiddenItemId = checkHiddenHeadItem(firstItem.items);
            if (hiddenItemId == -1) {
                generateFirstCell(parentLabel);
            }
        } else {
            generateFirstCell(parentLabel);
        }

        generateOtherCells(rowColumnModels);
    }

    private void generateFirstCell(String parentLabel) {

        //generate first cell
        DebugLog.d("firstitem.items.size() = " + firstItem.items.size());

        if (firstItem.items.size() != 0) {

            DebugLog.d("> generate first cell (header) ");
            boolean isFirstItemVisible = firstItem.items.get(0).visible;
            this.type = TYPE_HEADER;                                        DebugLog.d("TYPE\t:\t" + this.type);
            this.workItemModel = firstItem.items.get(0);                    DebugLog.d("-WORKFORMITEM id\t:\t" + this.workItemModel.id);
            this.label = workItemModel.label;                               DebugLog.d("-WORKFORMITEM label\t:\t" + this.label);
            this.hasPicture = workItemModel.pictureEndPoint != null;        DebugLog.d("HASPICTURE ?\t" + this.hasPicture);
            if (parentLabel != null)
                workItemModel.label = workItemModel.label + " \n " + parentLabel;
            if (this.workItemModel.field_type.equalsIgnoreCase("label") && !workItemModel.expand) {
                firstItem.items.remove(0);
            }
            ItemFormRenderModel child = new ItemFormRenderModel();
            child.type = TYPE_HEADER_DIVIDER;
            child.parent = this;

            // add first item only if visible is true
            if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && isFirstItemVisible) {
                add(child);
            } else {
                add(child);
            }
        }

        if (!TextUtils.isEmpty(getColumn(firstItem.column_id).column_name)) {
            if (checkAnyInputHead(firstItem.items)) {
                ItemFormRenderModel child = new ItemFormRenderModel();
                child.type = TYPE_COLUMN;
                child.column = getColumn(firstItem.column_id);
                child.parent = this;
                add(child);
            }
        }

        boolean anyInput = checkAnyInput(firstItem.items);
        if (!anyInput) {
            operator = schedule.operators.get(schedule.operator_number);
            generateItemsPerOperator(firstItem, schedule.operators.get(schedule.operator_number).id);
        } else {
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
    }

    private void generateOtherCells(Vector<RowColumnModel> rowColumnModels) {

        //generate other cell
        DebugLog.d("> generate other cell for header " + this.label);
        DebugLog.d("|\trow_col_id\t|\trow_id\t|\tcol_id\t|\twork_form_group_id\t|\titem size\t|");
        for (RowColumnModel rowcol : rowColumnModels) {
            DebugLog.d("|\t" + rowcol.id + "\t\t|\t" + rowcol.row_id + "\t|\t" + rowcol.column_id + "\t\t|\t" + rowcol.work_form_group_id + "\t\t|\t" + rowcol.items.size() + "\t|");
            if (rowcol.items.size() > 0) {
                ItemFormRenderModel child = new ItemFormRenderModel();
                child.type = TYPE_COLUMN;
                child.column = getColumn(rowcol.column_id);
                child.parent = this;
                add(child);
                boolean anyInput = checkAnyInput(rowcol.items);

                if (!anyInput) {
                    DebugLog.d("input type not found");
                    generateItemsPerOperator(rowcol, schedule.operators.get(0).id);
                } else {
                    DebugLog.d("input type found");
                    Vector<OperatorModel> operatorItems = schedule.operators;
                    if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) &&
                            workTypeName.equalsIgnoreCase(MyApplication.getContext().getString(R.string.corrective))) {
                        int inputTypeItemId = getInputTypeItemId(rowcol.items);
                        if (inputTypeItemId != -1) {
                            CorrectiveScheduleResponseModel.CorrectiveItem correctiveItem = CorrectiveScheduleConfig.getCorrectiveItem(Integer.valueOf(schedule.id), workFormGroupId, inputTypeItemId);
                            if (correctiveItem != null) {
                                Vector<Integer> operatorIds = correctiveItem.getOperator();
                                operatorItems = new Vector<>();
                                for (int operatorId : operatorIds) {
                                    OperatorModel operatorItem = OperatorModel.getOperatorById(operatorId);
                                    operatorItems.add(operatorItem);
                                }
                            }
                        }
                    }

                    for (int i = 0; i < operatorItems.size(); i++) {
                        child = new ItemFormRenderModel();
                        child.type = TYPE_OPERATOR;
                        child.operator = operatorItems.get(i);
                        child.parent = this;
                        add(child);
                        generateItemsPerOperator(rowcol, operatorItems.get(i).id);
                        if (operatorItems.size() - 2 >= 0 && i < operatorItems.size() - 1) {
                            child = new ItemFormRenderModel();
                            child.type = TYPE_LINE_DEVIDER;
                            child.parent = this;
                            add(child);
                        }
                    }
                }
            }
        }
    }
    public String getPercent() {
        return percent == 0 ? "" : percent + "%";
    }

    public void addFillableTask() {
        fillableTask++;
    }

    public String getWargaId() {
        return wargaId;
    }

    public String getBarangId() {
        return barangId;
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

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            child.setSchedule(schedule);
            child.setWargaid(getWargaId());
            child.setBarangid(getBarangId());
        }

        children.add(child);
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

    private void generateItemsPerOperator(RowColumnModel rowCol, int operatorId) {
        for (int i = 0; i < rowCol.items.size(); i++) {
            WorkFormItemModel item = rowCol.items.get(i);
            DebugLog.d("(id, label, field type, scope type, operatorId) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ", " + operatorId + ")");
            if (rowCol.items.get(i).field_type == null) {
                if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) && !rowCol.items.get(i).visible)
                    continue;
                else
                    continue;
            }
            generateViewItem(rowCol.row_id, item, operatorId);
        }
    }

    //check if any input type
    private boolean checkAnyInput(Vector<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null && !item.scope_type.equalsIgnoreCase("all")) {
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ") --> found ");
                return true;
            } else
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ")");
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

    private int checkHiddenHeadItem(Vector<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            DebugLog.d("item (id, label, type, isVisible) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.visible + ")");
            if (!item.visible){
                DebugLog.d("item (id, label, type, isVisible) : (" + item.id + ", " + item.label + ", " + item.field_type + "," + item.visible + ") --> found hidden");
                return item.id;
            }
        }
        return -1;
    }

    private int getInputTypeItemId(Vector<WorkFormItemModel> items) {

        int itemId = -1;
        for (WorkFormItemModel item : items) {
            if (!TextUtils.isEmpty(item.field_type) && !item.field_type.equalsIgnoreCase("label")) {
                itemId = item.id;
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ") --> found ");
            } else
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ")");
        }
        return itemId;
    }

    private void generateViewItem(int rowId, WorkFormItemModel workItemModel, int operatorId) {
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

        ItemFormRenderModel child = new ItemFormRenderModel();

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            String wargaId  = getWargaId();
            String barangId = getBarangId();
            child.itemValue = FormValueModel.getItemValue(schedule.id, workItemModel.id, operatorId, wargaId, barangId);
        } else {
            child.itemValue = FormValueModel.getItemValue(schedule.id, workItemModel.id, operatorId);
        }

        child.rowId = rowId;
        child.operatorId = operatorId;
        child.schedule = schedule;
        child.workItemModel = workItemModel;

        if (workItemModel.field_type.equalsIgnoreCase("label") && workItemModel.expand) {
            hasInput = true;
            child.type = TYPE_EXPAND;
            this.addFillableTask();
            child.parent = this;
            add(child);
        } else if (workItemModel.field_type.equalsIgnoreCase("text_field")) {
            Log.d("default_value", "default value : " + child.workItemModel.default_value);
            hasInput = true;
            child.type = TYPE_TEXT_INPUT;
            this.addFillableTask();
            child.parent = this;
            add(child);
        } else if (workItemModel.field_type.equalsIgnoreCase("checkbox")) {
            hasInput = true;
            child.type = TYPE_CHECKBOX;
            this.addFillableTask();
            child.parent = this;
            add(child);
        } else if (workItemModel.field_type.equalsIgnoreCase("radio") || workItemModel.field_type.equalsIgnoreCase("dropdown")) {
            hasInput = true;
            child.type = TYPE_RADIO;
            this.addFillableTask();
            child.parent = this;
            add(child);
        } else if (workItemModel.field_type.equalsIgnoreCase("file")) {
            hasInput = true;
            child.type = TYPE_PICTURE_RADIO;
            this.addFillableTask();
            child.parent = this;
            if (this.children.get(this.children.size() - 1).type == TYPE_OPERATOR)
                child.operator = this.children.remove(this.children.size() - 1).operator;
            add(child);
        }
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
