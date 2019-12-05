package com.sap.inspection.model.value;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;

import java.util.concurrent.atomic.AtomicInteger;

public class DbRepositoryValue {
	private AtomicInteger mOpenCounter = new AtomicInteger();
	private static DbRepositoryValue mInstance = null;
	private static SQLiteOpenHelper _databaseHelper;
	private SQLiteDatabase _database;

	public static synchronized void initializedInstance() {
		if (mInstance == null) {

			DebugLog.d("initialized db repository value instance");
			mInstance = new DbRepositoryValue();

		}
	}
	public static DbRepositoryValue getInstance() {

		if (mInstance == null) {
			throw new IllegalStateException(DbRepositoryValue.class.getSimpleName() +
					" is not initialized, call initialize(..) method first.");
		}
        return mInstance;
    }

	private DbRepositoryValue() {

	}

	public void open(Context context) {
		//DebugLog.d("db repository value opening counter : " + mOpenCounter);
		if (_database!=null && _database.isOpen()) return;

		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(_databaseHelper == null || !mPref.getString(context.getString(R.string.user_id), null).equalsIgnoreCase(mPref.getString(context.getString(R.string.latest_user_db_value), null))) {
			_databaseHelper = null;
			System.gc();
			_databaseHelper = new DbManagerValue(context.getApplicationContext(),mPref.getString(context.getString(R.string.user_id), null));
			mPref.edit().putString(context.getString(R.string.latest_user_db_value), mPref.getString(context.getString(R.string.user_id), null));
		}

		//DebugLog.d("opening new database");
		if (mOpenCounter.incrementAndGet()== 1)
		_database = _databaseHelper.getWritableDatabase();
	}

	public void close() {
		//DebugLog.d("db repository value opening counter : " + mOpenCounter);
		if (_database!=null && _database.isOpen() && mOpenCounter.decrementAndGet() == 0) {

			//DebugLog.d("closing database");
			_database.close();
		}
	}
	
	public SQLiteDatabase getDB(){
		return _database;
	}

	public void clearData(String table){
		try {
			getDB().delete(table, null, null);
		} catch (RuntimeException re) {
			DebugLog.e(re.getMessage(), re);
		}
	}
}
