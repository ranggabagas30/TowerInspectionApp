package com.appmogli.widget;

import java.util.LinkedHashMap;
import java.util.Vector;

import com.sap.inspection.model.ScheduleBaseModel;

public class Dataset {

	private LinkedHashMap<String, Vector<ScheduleBaseModel>> sectionItems = new LinkedHashMap<String, Vector<ScheduleBaseModel>>();

	public static final String DATA_COLUMN = "data";

	public static final int TYPE_DATA = 1;

	public static final String ITEM_PREFIX = "data-";

	public static final String[] COLUMNS = new String[] { DATA_COLUMN, "_id" };
	
	private static volatile int INDEX = 1;
	
	private LinkedHashMap<String, Vector<ScheduleBaseModel>> sectionCursors = new LinkedHashMap<String, Vector<ScheduleBaseModel>>();

	public void addSection(String sectionName, Vector<ScheduleBaseModel> vector) {
		sectionItems.put(sectionName, vector);
	}

	public Vector<ScheduleBaseModel> getSectionCursor(String sectionName) {
		Vector<ScheduleBaseModel> cursor = (Vector<ScheduleBaseModel>) sectionCursors.get(sectionName);
		if( cursor == null) {
			cursor = new Vector<ScheduleBaseModel>();
			sectionCursors.put(sectionName, cursor);
		}
		return cursor;
	}
	
	public LinkedHashMap<String, Vector<ScheduleBaseModel>> getSectionCursorMap() {
		if(sectionCursors.isEmpty()) {
			 for(String sectionName : sectionItems.keySet()) {
				 getSectionCursor(sectionName);
			 }
		}
		return sectionCursors;
	}

}
