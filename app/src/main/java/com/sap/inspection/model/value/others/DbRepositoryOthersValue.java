package com.sap.inspection.model.value.others;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sap.inspection.R;

public class DbRepositoryOthersValue {
	private static DbRepositoryOthersValue mInstance = null;
	private SQLiteOpenHelper _databaseHelper;
//	private final Context _context;
	protected SQLiteDatabase _database;
	
	public static DbRepositoryOthersValue getInstance() {
        if (mInstance == null) {
//            mInstance = new Repository(ctx.getApplicationContext());
        	mInstance = new DbRepositoryOthersValue();
        }
        return mInstance;
    }

	private DbRepositoryOthersValue() {
//		_context = context;
	}

	public void open(Context context) {
		Log.d(getClass().getName(), "DB Value is open----");
		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(_databaseHelper == null || !mPref.getString(context.getString(R.string.user_id), null).equalsIgnoreCase(mPref.getString(context.getString(R.string.latest_user_db_value), null))) {
			_databaseHelper = null;
			System.gc();
			_databaseHelper = new DbManagerOthersValue(context.getApplicationContext(),mPref.getString(context.getString(R.string.user_id), null));
			mPref.edit().putString(context.getString(R.string.latest_user_db_value), mPref.getString(context.getString(R.string.user_id), null));
		}
		_database = _databaseHelper.getWritableDatabase();
	}

	public void close() {
		Log.d(getClass().getName(), "DB Value is closed-----");
		_database.close();
	}
	
	public SQLiteDatabase getDB(){
		return _database;
	}
}
