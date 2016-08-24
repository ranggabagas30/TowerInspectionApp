package com.sap.inspection.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.sap.inspection.R;

public class DbRepository {
	private static DbRepository mInstance = null;
	private SQLiteOpenHelper _databaseHelper;
//	private final Context _context;
	protected SQLiteDatabase _database;
	
	public static DbRepository getInstance() {
        if (mInstance == null) {
//            mInstance = new Repository(ctx.getApplicationContext());
        	mInstance = new DbRepository();
        }
        return mInstance;
    }
	
	private DbRepository() {
	}

	public void open(Context context) {
		if (_database!=null && _database.isOpen()) return;

		SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(_databaseHelper == null || !mPref.getString(context.getString(R.string.user_id), null).equalsIgnoreCase(mPref.getString(context.getString(R.string.latest_user_db), null))) {
			_databaseHelper = null;
			System.gc();
			_databaseHelper = new DbManager(context.getApplicationContext(),mPref.getString(context.getString(R.string.user_id), null));
			mPref.edit().putString(context.getString(R.string.latest_user_db), mPref.getString(context.getString(R.string.user_id), null));
		}
		_database = _databaseHelper.getWritableDatabase();
	}
	
	public void close() {
		if (_database!=null && _database.isOpen())
			_database.close();
	}
	
	public SQLiteDatabase getDB(){
		return _database;
	}
	
	public void clearData(String table){
		 getDB().delete(table, null, null);
	}

	public void clearData(Context context,String table){
		open(context);
		getDB().delete(table, null, null);
		close();
	}
}
