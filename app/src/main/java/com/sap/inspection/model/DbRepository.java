package com.sap.inspection.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.sap.inspection.R;
import com.sap.inspection.tools.DebugLog;

import java.util.concurrent.atomic.AtomicInteger;

public class DbRepository {

	private AtomicInteger mOpenCounter = new AtomicInteger();
	private static DbRepository mInstance = null;
	private static SQLiteOpenHelper _databaseHelper;
	private SQLiteDatabase _database;
//	private final Context _context;

	public static synchronized void initializedInstance() {
		if (mInstance == null) {
			DebugLog.d("initialized db repository instance");
			mInstance = new DbRepository();

		}
	}

	public static synchronized DbRepository getInstance() {
		if (mInstance == null) {
			throw new IllegalStateException(DbRepository.class.getSimpleName() +
					" is not initialized, call initialize(..) method first.");
		}
        return mInstance;
    }
	
	private DbRepository() {

	}

	public void open(Context context) {
		//DebugLog.d("db repository opening counter : " + mOpenCounter);
		if (_database!=null && _database.isOpen()) return;
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(_databaseHelper == null || !mPref.getString(context.getString(R.string.user_id), null).equalsIgnoreCase(mPref.getString(context.getString(R.string.latest_user_db), null))) {
            System.gc();
            //DebugLog.d("user_id : " + mPref.getString(context.getString(R.string.user_id), null));
            _databaseHelper = new DbManager(context.getApplicationContext(),mPref.getString(context.getString(R.string.user_id), null));
            mPref.edit().putString(context.getString(R.string.latest_user_db), mPref.getString(context.getString(R.string.user_id), null));
        }

		//DebugLog.d("opening new database");
		if (mOpenCounter.incrementAndGet() == 1)
		_database = _databaseHelper.getWritableDatabase();
	}
	
	public void close() {
		//DebugLog.d("db repository opening counter : " + mOpenCounter);
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
