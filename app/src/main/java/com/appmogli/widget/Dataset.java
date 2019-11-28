package com.appmogli.widget;

import com.sap.inspection.model.ScheduleBaseModel;

import java.util.LinkedHashMap;
import java.util.ArrayList;

public class Dataset {

	private LinkedHashMap<String, ArrayList<ScheduleBaseModel>> sectionItems = new LinkedHashMap<String, ArrayList<ScheduleBaseModel>>();

	public static final String DATA_COLUMN = "data";

	public static final int TYPE_DATA = 1;

	public static final String ITEM_PREFIX = "data-";

	public static final String[] COLUMNS = new String[] { DATA_COLUMN, "_id" };
	
	private static volatile int INDEX = 1;
	
	private LinkedHashMap<String, ArrayList<ScheduleBaseModel>> sectionCursors = new LinkedHashMap<String, ArrayList<ScheduleBaseModel>>();

	public void addSection(String sectionName, ArrayList<ScheduleBaseModel> vector) {
		sectionItems.put(sectionName, vector);
	}

	public ArrayList<ScheduleBaseModel> getSectionCursor(String sectionName) {
		ArrayList<ScheduleBaseModel> cursor = sectionCursors.get(sectionName);
		if( cursor == null) {
			cursor = new ArrayList<ScheduleBaseModel>();
			sectionCursors.put(sectionName, cursor);
		}
		return cursor;
	}
	
	public LinkedHashMap<String, ArrayList<ScheduleBaseModel>> getSectionCursorMap() {
		if(sectionCursors.isEmpty()) {
			 for(String sectionName : sectionItems.keySet()) {
				 getSectionCursor(sectionName);
			 }
		}
		return sectionCursors;
	}

}
