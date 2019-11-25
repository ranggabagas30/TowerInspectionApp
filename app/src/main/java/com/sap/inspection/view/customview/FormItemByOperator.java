//package com.sap.inspection.view;
//
//import java.util.Vector;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.sap.inspection.R;
//import com.sap.inspection.model.form.ColumnModel;
//import com.sap.inspection.model.form.RowColumnModel;
//import com.sap.inspection.model.form.WorkFormItemModel;
//import com.sap.inspection.model.form.WorkFormOptionsModel;
//
//public class FormItemByOperator extends RelativeLayout {
//
//	private LinearLayout rootItem;
//	private Vector<RowColumnModel> rowColumnModels;
//	//	private Vector<WorkFormItemModel> items;
//	private Vector<ColumnModel> column;
//	private Context context;
//	private TextView rowTitle;
//	private TextView rowSubColored;
//	private TextView rowSubPlain;
//	private String[] scheduleIds;
//	private String[] operators;
//	
//	public void setScheduleIds(String[] scheduleIds) {
//		this.scheduleIds = scheduleIds;
//	}
//	
//	public void setOperators(String[] operators) {
//		this.operators = operators;
//	}
//
//	public FormItemByOperator(Context context) {
//		super(context);
//		init(context);
//	}
//
//	public FormItemByOperator(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init(context);
//	}
//
//	public FormItemByOperator(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init(context);
//	}
//
//	private void init(Context context){
//		this.context = context;
//		View root = LayoutInflater.from(context).inflate(R.layout.item_form, this, true);
//		rootItem = (LinearLayout) root.findViewById(R.id.rootItem);
//		rowTitle = (TextView) root.findViewById(R.id.item_drill_title);
//		rowSubColored = (TextView) root.findViewById(R.id.item_drill_subcolored);
//		rowSubPlain = (TextView) root.findViewById(R.id.item_drill_subplain);
//	}
//
//	public String getColumnName(String colId){
//		for (ColumnModel oneColumn : this.column) {
//			if (colId.equalsIgnoreCase(oneColumn.id))
//				return oneColumn.column_name;
//		}
//		return "";
//	}
//
//	public void setColumns(Vector<ColumnModel> column) {
//		this.column = column;
//	}
//
//	public void setRowColumnModels(Vector<RowColumnModel> rowColumnModels) {
//		this.rowColumnModels = rowColumnModels;
//		RowColumnModel firstItem = rowColumnModels.remove(0);
//		rowTitle.setText(firstItem.items.get(0).label);
//		rowSubColored.setText("");
//		rowSubPlain.setText("no action yet");
//
//		for (RowColumnModel rowCol : rowColumnModels) {
//			if (rowCol.items.size() > 0){
//				View view = new View(context);
//				view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
//				view.setBackgroundColor(Color.parseColor("#e0e0e0"));
//				rootItem.addView(view);
//
//				TextView textView = new TextView(context);
//				textView.setPadding(15, 15, 15, 0);
//				textView.setText(getColumnName(rowCol.column_id));
//				textView.setTextSize(20);
//				textView.setTypeface(null, Typeface.BOLD);
//				rootItem.addView(textView);
//				log("================== row col : "+rowCol.column_id);
//				for (int i = 0 ; i< rowCol.items.size(); i ++) {
//					log("item : "+rowCol.items.get(i).label);
//					View view2 = generateViewItem(rowCol.items.get(i));
//					if (i < rowCol.items.size() - 1)
//						view2.setPadding(15, 15, 15, 0);
//					else
//						view2.setPadding(15, 15, 15, 15);
//					rootItem.addView(view2);
//				}
//			}
//		}
//	}
//
//	public View generateViewItem(WorkFormItemModel item){
//		if (item.field_type.equalsIgnoreCase("label")){
//
//			TextView textView = new TextView(context);
//			textView.setText(item.label);
//			return textView;
//		}else if (item.field_type.equalsIgnoreCase("text_field")){
//			LinearLayout linearLayout = new LinearLayout(context);
//			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//
//			TextView textView = new TextView(context);
//			textView.setText(item.label);
//			linearLayout.addView(textView);
//
//			EditText editText = new EditText(context);
//			editText.setWidth(100);
//			linearLayout.addView(editText);
//
//			if (item.description != null){
//				textView = new TextView(context);
//				textView.setText(item.description);
//				linearLayout.addView(textView);
//			}
//
//			return linearLayout;
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
//		}
//		return new View(context);
//	}
//
//	private void log(String msg){
//		Log.d(getClass().getName(), msg);
//	}
//}
