package com.sap.inspection.model.form;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sap.inspection.BuildConfig;
import com.sap.inspection.R;
import com.sap.inspection.TowerApplication;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.config.formimbaspetir.CorrectiveScheduleConfig;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.ArrayList;

@org.parceler.Parcel(org.parceler.Parcel.Serialization.BEAN)
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

    private RowColumnModel firstItem;
    private WorkFormItemModel workItemModel;
    private FormValueModel itemValue;
    private ItemFormRenderModel parent;
    private OperatorModel operator;
    private int operatorId;
    private int rowId;
    private int workFormGroupId;
    private String workFormGroupName;
    private String workTypeName;
    private ArrayList<ItemFormRenderModel> children;
    private int type = TYPE_NONE;
    private boolean open = true;
    private boolean hasInput = false;
    private boolean hasPicture = false;
    private ColumnModel column;
    private String label = null;
    private ScheduleGeneral schedule;
    private ArrayList<ColumnModel> columns;
    private boolean isHeader = false;
    private long when = 0;
    private int percent = 10;
    private int fillableTask = 0;
    private int filledTask = 0;
    private String wargaId; // SAP
    private String barangId; // SAP

    public ItemFormRenderModel() {}

    public void setParent(ItemFormRenderModel parent) {
        this.parent = parent;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public void setSchedule(ScheduleGeneral schedule) {
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

    public RowColumnModel getFirstItem() {
        return firstItem;
    }

    public void setFirstItem(RowColumnModel firstItem) {
        this.firstItem = firstItem;
    }

    public WorkFormItemModel getWorkItemModel() {
        return workItemModel;
    }

    public void setWorkItemModel(WorkFormItemModel workItemModel) {
        this.workItemModel = workItemModel;
    }

    public FormValueModel getItemValue() {
        return itemValue;
    }

    public void setItemValue(FormValueModel itemValue) {
        this.itemValue = itemValue;
    }

    public ItemFormRenderModel getParent() {
        return parent;
    }

    public OperatorModel getOperator() {
        return operator;
    }

    public void setOperator(OperatorModel operator) {
        this.operator = operator;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public int getRowId() {
        return rowId;
    }

    public int getWorkFormGroupId() {
        return workFormGroupId;
    }

    public String getWorkFormGroupName() {
        return workFormGroupName;
    }

    public String getWorkTypeName() {
        return workTypeName;
    }

    public ArrayList<ItemFormRenderModel> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<ItemFormRenderModel> children) {
        this.children = children;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isHasInput() {
        return hasInput;
    }

    public void setHasInput(boolean hasInput) {
        this.hasInput = hasInput;
    }

    public boolean isHasPicture() {
        return hasPicture;
    }

    public void setHasPicture(boolean hasPicture) {
        this.hasPicture = hasPicture;
    }

    public ColumnModel getColumn() {
        return column;
    }

    public void setColumn(ColumnModel column) {
        this.column = column;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public ScheduleGeneral getSchedule() {
        return schedule;
    }

    public ArrayList<ColumnModel> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<ColumnModel> columns) {
        this.columns = columns;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    public long getWhen() {
        return when;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getPercent() {
        return percent;
    }

    public int getFillableTask() {
        return fillableTask;
    }

    public void setFillableTask(int fillableTask) {
        this.fillableTask = fillableTask;
    }

    public int getFilledTask() {
        return filledTask;
    }

    public void setFilledTask(int filledTask) {
        this.filledTask = filledTask;
    }

    public void setWargaId(String wargaId) {
        this.wargaId = wargaId;
    }

    public void setBarangId(String barangId) {
        this.barangId = barangId;
    }

    public void setRowColumnModels(ArrayList<RowColumnModel> rowColumnModels, String parentLabel) {
        if (schedule.operators == null || schedule.operators.size() == 0) {
            DebugLog.d("operator none");
            TowerApplication.getInstance().toast("Tidak ada operator", Toast.LENGTH_LONG);
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
            this.type= TYPE_HEADER;                                        DebugLog.d("TYPE\t:\t" + this.getType());
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

    private void generateOtherCells(ArrayList<RowColumnModel> rowColumnModels) {

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
                    ArrayList<OperatorModel> operatorItems = schedule.operators;
                    if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) &&
                            workTypeName.equalsIgnoreCase(TowerApplication.getContext().getString(R.string.corrective))) {
                        int inputTypeItemId = getInputTypeItemId(rowcol.items);
                        if (inputTypeItemId != -1) {
                            CorrectiveScheduleResponseModel.CorrectiveItem correctiveItem = CorrectiveScheduleConfig.getCorrectiveItem(Integer.valueOf(schedule.id), workFormGroupId, inputTypeItemId);
                            if (correctiveItem != null) {
                                ArrayList<Integer> operatorIds = correctiveItem.getOperator();
                                operatorItems = new ArrayList<>();
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

    public String getPercentage() {
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

    public String getWhenExact() {
        if (percent == 0)
            return "no action yet";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(when);
        return DateUtil.timeElapse(calendar);
    }

    public void add(ItemFormRenderModel child) {
        if (children == null)
            children = new ArrayList<>();
        child.setParent(this);

        if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
            child.setSchedule(schedule);
            child.setWargaId(getWargaId());
            child.setBarangId(getBarangId());
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
    private boolean checkAnyInput(ArrayList<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null && !item.scope_type.equalsIgnoreCase("all")) {
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ") --> found ");
                return true;
            } else
                DebugLog.d("(id, label, field type, scope type) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.scope_type + ")");
        }
        return false;
    }

    private boolean checkAnyInputHead(ArrayList<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            DebugLog.d("scope type : " + item.scope_type);
            if (item.field_type != null && !item.field_type.equalsIgnoreCase("label") && item.scope_type != null)
                return true;
        }
        return false;
    }

    private int checkHiddenHeadItem(ArrayList<WorkFormItemModel> items) {
        for (WorkFormItemModel item : items) {
            DebugLog.d("item (id, label, type, isVisible) : (" + item.id + ", " + item.label + ", " + item.field_type + ", " + item.visible + ")");
            if (!item.visible){
                DebugLog.d("item (id, label, type, isVisible) : (" + item.id + ", " + item.label + ", " + item.field_type + "," + item.visible + ") --> found hidden");
                return item.id;
            }
        }
        return -1;
    }

    private int getInputTypeItemId(ArrayList<WorkFormItemModel> items) {

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
