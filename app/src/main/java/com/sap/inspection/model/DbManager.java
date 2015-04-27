package com.sap.inspection.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.RowColumnModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.form.WorkFormGroupModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.migrate.BlankPatch;
import com.sap.inspection.model.migrate.DBPatch;
import com.sap.inspection.model.migrate.GeneralDropCreatePatch;
import com.sap.inspection.model.migrate.GeneralPatch5;

public class DbManager extends SQLiteOpenHelper {

	private final String TAG = getClass().getName();

	public static final String dbName = "sap.db";
	static final int schema_version = 5;

	public static final String colCreatedAt = "created_at";
	public static final String colUpdatedAt = "updated_at";
	public static final String colTime = "time_attempt";

	public static final String colID = "id";
	public static final String colName = "name";


	// token table
	public static final String mTokenTable 	= "Tokens";
	public static final String colAccToken 	= "accToken";

	// role table
	public static final String mRoles 		= "Roles";
	public static final String colRoleName 	= "roleName";

	// user table
	public static final String mUsers 		= "Users";
	public static final String colUserName 	= "userName";
	public static final String colPassword 	= "password";
	public static final String colRoleID 	= "roleID";

	// login log table
	public static final String mLoginLogs 		= "LoginLogs";
	public static final String colFileName 		= "fileName";
	public static final String colStatusLogin 	= "statusLogin";

	// site table
	public static final String mSite 			= "Sites";

	// operator table
	public static final String mOperator 		= "Operators";

	// workType table
	public static final String mWorkType 		= "WorkTypes";

	// schedule table
	public static final String mSchedule 		= "Schedules";
	public static final String colUserId 		= "userId";
	public static final String colSiteId 		= "siteId";
	public static final String colSiteName 		= "siteName";
	public static final String colSiteLocation 	= "siteLocation";
	public static final String colOperatorIds 	= "operatorIds";
	public static final String colOperatorName 	= "operatorName";
	public static final String colProjectId 	= "projectId";
	public static final String colProjectName 	= "projectName";
	public static final String colWorkTypeId 	= "workTypeId";
	public static final String colWorkTypeName 	= "workTypeName";
	public static final String colWorkDate 		= "workDate";
	public static final String colDayDate 		= "dayDate";
	public static final String colWorkDateStr 	= "workDateStr";
	public static final String colProgress 		= "progress";
	public static final String colStatus 		= "status";
	public static final String colSumTask 		= "sumTask";
	public static final String colSumDone 		= "sumDone";

	// work form
	public static final String mWorkForm 		= "WorkForms";
	public static final String colNotes 		= "notes";
	public static final String colSumInput 		= "simInput";

	// work form groups
	public static final String mWorkFormGroup 	= "WorkFormGroups";
	public static final String colPosition 		= "position";
	public static final String colWorkFormId 	= "workFormId";
	public static final String colDescription 	= "description";
	public static final String colIsTable 		= "isTable";
	public static final String colAncestry 		= "ancestry";

	// work form items
	public static final String mWorkFormItem 			= "WorkFormItems";
	public static final String colSearchable 			= "searchable";
	public static final String colMandatory 			= "mandatory";
	public static final String colVisible	 			= "visible";
	public static final String colScopeType	 			= "scopeType";
	public static final String colDefaultValue	 		= "defaultValue";
	public static final String colListable 				= "listable";
	public static final String colWorkFormGroupId 		= "workFormGroupId";
	public static final String colFieldType		 		= "fieldType";
	public static final String colLable 				= "lable";
	public static final String colLableKey 				= "lableKey";
	public static final String colWorkFormRowColumnId 	= "workFromRowColumnId";
	public static final String colPicture		 		= "picture";
	//	public static final String colWorkFromTableId 		= "workFromTableId";

	//	work form items
	public static final String mWorkFormOption 			= "WorkFormOptions";
	public static final String colWorkFormItemId 		= "workFormItemId";

	// work form tables
	public static final String mWorkFormTable 		= "workFormTables";

	// work form row
	public static final String mWorkFormRow 		= "workFormRows";
	public static final String colFilledTask 		= "filledTask";
	public static final String colUploadedTask 		= "uploadedTask";

	// work form column
	public static final String mWorkFormColumn 		= "workFormColumns";

	// work form rowcol
	public static final String mWorkFormRowCol 		= "workFormRowNColumns";
	public static final String colRowId 			= "myRowId";
	public static final String colColId 			= "columnId";
	public static final String colParentId 			= "parentId";
	public static final String colLevel 			= "level";

	// Form Value
	public static final String mFormValue 		= "FromValues";
	public static final String colScheduleId 	= "scheduleId";
	public static final String colItemId	 	= "itemId";
	public static final String colIsPhoto 		= "isPhoto";
	public static final String colValue 		= "value";

	// Site table
	//	public static final String mSites 			= "Sites";
	//	public static final String colSiteName 		= "siteName";
	//	public static final String colCode 			= "code";
	//	public static final String colLocationId	= "locationId";
	//	public static final String colLongitude		= "longitude";
	//	public static final String colLatitude		= "latitude";
	//	public static final String colLicense		= "license";
	

	// Performing a database existence check
//	private boolean checkDataBase() {
//	    SQLiteDatabase checkDb = null;
//	    try {
//	        String path = DATABASE_PATH + DATABASE_NAME;
//	        checkDb = SQLiteDatabase.openDatabase(path, null,
//	                SQLiteDatabase.OPEN_READONLY);
//	    } catch (SQLException e) {
//	        Log.e(this.getClass().toString(), context.getString(R.string.str_error_while_checking_db));
//	    }
//
//	    if (checkDb != null) {
//	        checkDb.close();
//	    }
//	    return checkDb != null;
//	}
//
//	// Method for copying the database
//	private void copyDataBase() throws IOException {
//	    //Log.i(this.getClass().toString(), "... in copyDataBase ");
//	    InputStream externalDbStream = context.getAssets().open(DATABASE_NAME);
//
//	    String outFileName = DATABASE_PATH + DATABASE_NAME;
//
//	    OutputStream localDbStream = new FileOutputStream(outFileName);
//
//	    byte[] buffer = new byte[1024];
//	    int bytesRead;
//	    while ((bytesRead = externalDbStream.read(buffer)) > 0) {
//	        localDbStream.write(buffer, 0, bytesRead);
//	    }
//
//	    localDbStream.close();
//	    externalDbStream.close();
//	}
//
//	public void createDataBase() {
//	    //Log.i(this.getClass().toString(), "... in createDataBase ");
//	    boolean dbExist = checkDataBase();
//	    if (!dbExist) {
//	        this.getReadableDatabase();
//	        try {
//	            copyDataBase();
//	        } catch (IOException e) {
//	            Log.e(this.getClass().toString(), context.getString(R.string.str_copying_error));
//	            throw new Error(context.getString(R.string.str_error_copying_database_exclamation));
//	        }
//	    } else {
//	        //Log.i(this.getClass().toString(), "Database already exists");
//	    }
//	}
//
//	public SQLiteDatabase openDataBase() throws SQLException {
//	    String path = DATABASE_PATH + DATABASE_NAME;
//	    // Log.i(this.getClass().toString(), "Starting openDatabase " + path);
//	    if (db == null) {
//	        createDataBase();
//	        db = SQLiteDatabase.openDatabase(path, null,
//	                SQLiteDatabase.OPEN_READWRITE);
//	    }
//
//	    return db;
//	}

	public DbManager(Context context,String userName) {
		super(context, userName+"_"+dbName, null, schema_version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDB(db);
	}
	
	public static void createDB(SQLiteDatabase db){
		//		Token Model
		db.execSQL(TokenModel.createDB());
		//		User Model
		db.execSQL(UserModel.createDB());
		//		Role Model
		db.execSQL(RoleModel.createDB());
		//		Login Log Model
		db.execSQL(LoginLogModel.createDB());
		//		Schedule Model
		db.execSQL(ScheduleBaseModel.createDB());
		//		Site Model
		db.execSQL(SiteModel.createDB());
		//		Operator Model
		db.execSQL(OperatorModel.createDB());
		//		Worktype Model
		db.execSQL(WorkTypeModel.createDB());
		//		Form Model
		db.execSQL(WorkFormModel.createDB());
		//		Form Group Model
		db.execSQL(WorkFormGroupModel.createDB());
		//		Form Row Model
		db.execSQL(RowModel.createDB());
		//		Form Column Model
		db.execSQL(ColumnModel.createDB());
		//		Form Row Column Model
		db.execSQL(RowColumnModel.createDB());
		//		Form Item Model
		db.execSQL(WorkFormItemModel.createDB());
		//		Form Item Option Model
		db.execSQL(WorkFormOptionsModel.createDB());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		for (int i=oldVersion; i<newVersion; i++) {
			PATCHES[i].apply(db);
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (int i=oldVersion; i>newVersion; i++) {
			PATCHES[i-1].revert(db);
		}
	}

	private  final DBPatch[] PATCHES = new DBPatch[] {
			new BlankPatch(),
			new BlankPatch(),
			new BlankPatch(),
			new GeneralDropCreatePatch(),
			new GeneralPatch5()
	};

	public static void dropTable(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS " + mTokenTable);
		//		User Model
		db.execSQL("DROP TABLE IF EXISTS " + mUsers);
		//		Role Model
		db.execSQL("DROP TABLE IF EXISTS " + mRoles);
		//		Login Log Model
		db.execSQL("DROP TABLE IF EXISTS " + mLoginLogs);
		//		Schedule Model
		db.execSQL("DROP TABLE IF EXISTS " + mSchedule);
		//		Site Model
		db.execSQL("DROP TABLE IF EXISTS " + mSite);
		//		Operator Model
		db.execSQL("DROP TABLE IF EXISTS " + mOperator);
		//		Worktype Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkType);

		//		Form Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkForm);

		//		Form Group Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormGroup);

		//		Form Row Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormRow);

		//		Form Column Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormColumn);

		//		Form Row Column Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormRowCol);

		//		Form Item Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormItem);

		//		Form Item Option Model
		db.execSQL("DROP TABLE IF EXISTS " + mWorkFormOption);
	}
}
