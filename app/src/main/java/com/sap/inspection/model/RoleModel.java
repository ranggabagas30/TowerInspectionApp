package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;

import com.sap.inspection.MyApplication;
import com.sap.inspection.model.value.DbRepositoryValue;

public class RoleModel extends BaseModel {

	/**
	 * 
	 */

	public String id;
	public String roleName;
	
	/**
	 * Standard basic constructor for non-parcel
	 * object creation
	 */
	public RoleModel() { ; };

	/**
	 *
	 * Constructor to use when re-constructing object
	 * from a parcel
	 *
	 * @param in a parcel from which to read this object
	 */
	public RoleModel(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {

		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeString(id);
		dest.writeString(roleName);
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
		roleName = in.readString();
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
		public RoleModel createFromParcel(Parcel in) {
			return new RoleModel(in);
		}

		public RoleModel[] newArray(int size) {
			return new RoleModel[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
	
	public void save(Context ctx) {
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s) VALUES(?,?)",
						DbManager.mRoles , DbManager.colID,
						DbManager.colRoleName);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, roleName);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mRoles;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public RoleModel getRoleModel(Context context, String roleID) {

		RoleModel result = null;

		String table = DbManager.mRoles;
		String[] columns = null;
		String where = DbManager.colID + "=?";
		String[] args = {roleID};
		Cursor cursor;

		if (!DbRepository.getInstance().getDB().isOpen())
			DbRepository.getInstance().open(MyApplication.getInstance());
		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}

		result = getUserFromCursor(cursor);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}

	private RoleModel getUserFromCursor(Cursor c) {
		RoleModel user = new RoleModel();

		if (null == c)
			return user;

		user.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		user.roleName = (c.getString(c.getColumnIndex(DbManager.colRoleName)));

		return user;
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mRoles
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colRoleName + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
