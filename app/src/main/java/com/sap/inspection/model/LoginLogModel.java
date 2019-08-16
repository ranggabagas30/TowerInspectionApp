package com.sap.inspection.model;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

import com.sap.inspection.view.ui.MyApplication;

public class LoginLogModel extends BaseModel {

	/**
	 * 
	 */

	public String id;
	public String userName;
	public String time;
	public String statusLogin;
	public String fileName;

	/**
	 * Standard basic constructor for non-parcel
	 * object creation
	 */
	public LoginLogModel() { ; };

	/**
	 *
	 * Constructor to use when re-constructing object
	 * from a parcel
	 *
	 * @param in a parcel from which to read this object
	 */
	public LoginLogModel(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeString(id);
		dest.writeString(userName);
		//		dest.writeString(fullname);
		//		dest.writeParcelable(avatar, flags);
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

		id = in.readString();
		userName = in.readString();
		//		fullname = in.readString();
		//		avatar = in.readParcelable(PhotoModel.class.getClassLoader());
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
		public LoginLogModel createFromParcel(Parcel in) {
			return new LoginLogModel(in);
		}

		public LoginLogModel[] newArray(int size) {
			return new LoginLogModel[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public void save(Context context) {

		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s) VALUES(?,?,?,?,?)",
						DbManager.mLoginLogs , DbManager.colID,
						DbManager.colUserName, DbManager.colTime, 
						DbManager.colFileName, DbManager.colStatusLogin);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, userName);
		bindAndCheckNullString(stmt, 3, time);
		bindAndCheckNullString(stmt, 4, fileName);
		bindAndCheckNullString(stmt, 5, statusLogin);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mLoginLogs;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mUsers
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colUserName + " varchar, "
				+ DbManager.colTime + " varchar, "
				+ DbManager.colFileName + " varchar, "
				+ DbManager.colStatusLogin + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}