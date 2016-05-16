package com.sap.inspection.model.value.others;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sap.inspection.model.value.CorrectiveValueModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.model.value.RowValueModel;
import com.sap.inspection.tools.DebugLog;

public class DbManagerOthersValue extends SQLiteOpenHelper {

	public static final String dbName = "others_value.db";
	static final int schema_version = 5;

	public static final String colCreatedAt = "created_at";
	public static final String colUpdatedAt = "updated_at";
	public static final String colTime = "time_attempt";

	public static final String colID = "id";
	public static final String colName = "name";
	
	// Corrective Value
	public static final String mCorrectiveValue = "CorrectiveValues";

	// Form Value
	public static final String mFormValue 		= "FormValues";
	public static final String colScheduleId 	= "scheduleId";
	public static final String colItemId	 	= "itemId";
	public static final String colGPSAccuracy	= "GPSAccuracy";
	public static final String colOperatorId	= "operatorId";
	public static final String colIsPhoto 		= "isPhoto";
	public static final String colValue 		= "value";
	public static final String colRemark 		= "remark";
	public static final String colLatitude 		= "latitude";
	public static final String colLongitude 	= "longitude";
	public static final String colPhotoStatus 	= "photoStatus";
	public static final String colUploadStatus 	= "uploadStatus";

	// Row Value
	public static final String mRowValue 		= "RowValues";
	public static final String colSiteId 		= "siteId";
	public static final String colWorkTypeId 	= "workTypeId";
	public static final String colDayDate	 	= "day_date";
	public static final String colRowId	 		= "rowId";
	public static final String colprogress	 	= "progress";
	public static final String colSumTask	 	= "sumTask";
	public static final String colSumFilled	 	= "sumFilled";
	public static final String colUploaded	 	= "sumUploaded";

	public DbManagerOthersValue(Context context,String userName) {
		super(context, userName+"_"+dbName, null, schema_version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//		Form Model
		db.execSQL(ItemValueModel.createDB());
		db.execSQL(CorrectiveValueModel.createDB());
		db.execSQL(RowValueModel.createDB());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		DebugLog.d("========================================");
		DebugLog.d("----------------------------------------");
		DebugLog.d("========================================");

		DebugLog.d("old : "+oldVersion+" new : "+newVersion);
		for (int i=oldVersion; i<newVersion; i++) {
			DebugLog.d("upgrade index : "+i);
			PATCHES[i].apply(db);
		}
		DebugLog.d("========================================");
		DebugLog.d("----------------------------------------");
		DebugLog.d("========================================");
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int i=oldVersion; i>newVersion; i++) {
			PATCHES[i-1].revert(db);
		}
	}

	private static class Patch {
		public void apply(SQLiteDatabase db) {}
		public void revert(SQLiteDatabase db) {}
	}

	private final Patch[] PATCHES = new Patch[] {
			new Patch() {
				public void apply(SQLiteDatabase db) {
//					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) {
//					DebugLog.d("upgrade : second ");
//					db.execSQL("DROP TABLE IF EXISTS " + mFormValue);
//					db.execSQL("DROP TABLE IF EXISTS " + mRowValue);
//					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) {
//					DebugLog.d("upgrade : third ");
//					try {
//						db.execSQL("ALTER TABLE "+mFormValue+" ADD COLUMN "+colUploadStatus+" integer DEFAULT 0");
//					}catch(Exception e){
//						
//					}
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) {
//					DebugLog.d("upgrade : forth ");
//					try {
//						db.execSQL("ALTER TABLE "+mFormValue+" ADD COLUMN "+colSiteId+" integer DEFAULT 0");
//					}catch(Exception e){
//					}
//					try {
//						db.execSQL(CorrectiveValueModel.createDB());
//					}catch(Exception e){
//					}
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) {
					db.execSQL("DROP TABLE IF EXISTS " + mFormValue);
					db.execSQL("DROP TABLE IF EXISTS " + mRowValue);
					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			}
	};
	
}
