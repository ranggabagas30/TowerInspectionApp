package com.sap.inspection.model.value;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sap.inspection.tools.DebugLog;

public class DbManagerValue extends SQLiteOpenHelper {

	public static final String dbName = "value.db";
	static final int schema_version = 8;

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
	public static final String colDisable		= "disable";
	public static final String colPhotoDate		= "photoDate";

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

	public DbManagerValue(Context context,String userName) {
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

		DebugLog.d("old : "+oldVersion+" new : "+newVersion);
		for (int i=oldVersion; i<newVersion; i++) {
			DebugLog.d("upgrade index : "+i);
			PATCHES[i].apply(db);
		}
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

	/** version = i, PATCHES[i-1] **/
	private final Patch[] PATCHES = new Patch[] {
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 1, PATCHES[0] **/
//					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 2, PATCHES[1] **/
//					DebugLog.d("upgrade : second ");
//					db.execSQL("DROP TABLE IF EXISTS " + mFormValue);
//					db.execSQL("DROP TABLE IF EXISTS " + mRowValue);
//					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 3, PATCHES[2] **/
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
				public void apply(SQLiteDatabase db) { /** version 4, PATCHES[3] **/
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
				public void apply(SQLiteDatabase db) { /** version 5, PATCHES[4] **/
					DebugLog.d("general patch 5");
					db.execSQL("DROP TABLE IF EXISTS " + mFormValue);
					db.execSQL("DROP TABLE IF EXISTS " + mRowValue);
					onCreate(db);
				}
				public void revert(SQLiteDatabase db) {
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 6, PATCHES[5] **/
					DebugLog.d("general patch 6");
					db.execSQL("ALTER TABLE " + mFormValue + " ADD COLUMN " + colCreatedAt + " TEXT");
				}
				public void revert(SQLiteDatabase db) {
					db.execSQL("ALTER TABLE "+mFormValue+" DROP COLUMN "+colCreatedAt+" TEXT");
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 7, PATCHES[6] **/
					DebugLog.d("general patch 7");
					db.execSQL("ALTER TABLE " + mFormValue + " ADD COLUMN " + colDisable + " INTEGER DEFAULT 0");
				}
				public void revert(SQLiteDatabase db) {
					db.execSQL("ALTER TABLE "+mFormValue+" DROP COLUMN "+colDisable+" INTEGER DEFAULT 0");
				}
			},
			new Patch() {
				public void apply(SQLiteDatabase db) { /** version 8, PATCHES[7] **/
					DebugLog.d("general patch 8");
					db.execSQL("ALTER TABLE " + mFormValue + " ADD COLUMN " + colPhotoDate + " VARCHAR");
				}
				public void revert(SQLiteDatabase db) {
					db.execSQL("ALTER TABLE " + mFormValue + " RENAME TO " + mFormValue +"temp");
					onCreate(db);
					db.execSQL("INSERT INTO " + mFormValue + " SELECT "
								+ DbManagerValue.colScheduleId + ","
								+ DbManagerValue.colOperatorId + ","
								+ DbManagerValue.colItemId + ","
								+ DbManagerValue.colSiteId + ","
								+ DbManagerValue.colGPSAccuracy + ","
								+ DbManagerValue.colRowId + ","
								+ DbManagerValue.colRemark + ","
								+ DbManagerValue.colPhotoStatus + ","
								+ DbManagerValue.colLatitude + ","
								+ DbManagerValue.colLongitude + ","
								+ DbManagerValue.colValue + ","
								+ DbManagerValue.colUploadStatus + ","
								+ DbManagerValue.colIsPhoto + ","
								+ DbManagerValue.colCreatedAt
								+ " FROM " + mFormValue +"temp");
					db.execSQL("DROP TABLE " + mFormValue+"temp");
				}
			}

	};
	
}
