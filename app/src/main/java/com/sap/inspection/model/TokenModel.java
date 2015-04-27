package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

public class TokenModel extends BaseModel {

	/**
	 * 
	 */

	public String accToken;
	
	/**
	 * Standard basic constructor for non-parcel
	 * object creation
	 */
	public TokenModel() { ; };

	/**
	 *
	 * Constructor to use when re-constructing object
	 * from a parcel
	 *
	 * @param in a parcel from which to read this object
	 */
	public TokenModel(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {

		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeString(accToken);
	}

	/**
	 *
	 * Called from the constructor to create this
	 * object from a parcel.
	 *
	 * @param in parcel from which to re-create object
	 */
	private void readFromParcel(Parcel in) {

		// We just need to read back each
		// field in the order that it was
		// written to the parcel

		accToken = in.readString();
	}

	/**
	 *
	 * This field is needed for Android to be able to
	 * create new objects, individually or as arrays.
	 *
	 * This also means that you can use use the default
	 * constructor to create the object and use another
	 * method to hyrdate it as necessary.
	 *
	 * I just find it easier to use the constructor.
	 * It makes sense for the way my brain thinks ;-)
	 *
	 */
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public TokenModel createFromParcel(Parcel in) {
			return new TokenModel(in);
		}

		public TokenModel[] newArray(int size) {
			return new TokenModel[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public void save(Context ctx) {
		log(getClass().getName(), accToken);
		accToken = accToken.replace("access_token", "oauth_token");
		log(getClass().getName(), accToken);
		DbRepository.getInstance().open(ctx);
		String sql = String.format("INSERT OR REPLACE INTO %s(%s) VALUES(?)",
						DbManager.mTokenTable, DbManager.colAccToken);
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);

		bindAndCheckNullString(stmt, 1, accToken);
		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mTokenTable;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}
	
	public static String getAccTokenFromDB(Context context) {
		DbRepository.getInstance().open(context);
		try {

			Cursor c = DbRepository
					.getInstance()
					.getDB()
					.query(DbManager.mTokenTable, null, null, null, null,
							null, null, null);

			if (!c.moveToFirst()) {
				DbRepository.getInstance().close();
				return null;
			}

			String accToken = getFromCursor(c);
			c.close();
			DbRepository.getInstance().close();
			return accToken;
		} catch (Exception e) {
			DbRepository.getInstance().close();
			return null;
		}
	}

	private static String getFromCursor(Cursor c) {
		if (null == c)
			return null;
		
		return (c.getString(c.getColumnIndex(DbManager.colAccToken)));
	}
	
	public static String createDB(){
		return "create table if not exists " + DbManager.mTokenTable
				+ " (" + DbManager.colAccToken + " varchar )";
				
	}
}
