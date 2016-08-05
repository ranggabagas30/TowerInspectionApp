package com.sap.inspection.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.sap.inspection.tools.MD5;

public class UserModel extends BaseModel {

	/**
	 * 
	 */
	
//	id : 2
//	username : "user1"
//	email : "user_1@example.com"
//	full_name : "Rickie Eichmann"
//	authentication_token : "k3UXkb51y1FG2NGA9xRW"
//	last_sign_in_ip : "192.168.120.115"
//	last_sign_in_at : "2013-07-24T09:35:15Z"
//	sign_in_count : 2
//	failed_attempts : 0

	public String id;
	public String username;
	public String password;
	public String email;
	public String persistence_token;
	public String full_name;
	public String last_sign_in_ip;
	public String last_sign_in_at;
	public int sign_in_count;
	public int failed_attempts;
	public RoleModel role;

	/**
	 * Standard basic constructor for non-parcel
	 * object creation
	 */
	public UserModel() { ; };

	/**
	 *
	 * Constructor to use when re-constructing object
	 * from a parcel
	 *
	 * @param in a parcel from which to read this object
	 */
	public UserModel(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		// We just need to write each field into the
		// parcel. When we read from parcel, they
		// will come back in the same order
		dest.writeString(id);
		dest.writeString(username);
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
		username = in.readString();
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
		public UserModel createFromParcel(Parcel in) {
			return new UserModel(in);
		}

		public UserModel[] newArray(int size) {
			return new UserModel[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	public void save(Context context) {
		String md5 = MD5.md5(password);
		if (md5 == null){
			Toast.makeText(context, "Error on encripting password", Toast.LENGTH_SHORT).show();
			return;
		}
		
		DbRepository.getInstance().open(context);
		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s) VALUES(?,?,?,?)",
						DbManager.mUsers , DbManager.colID,
						DbManager.colUserName,DbManager.colPassword, 
						DbManager.colRoleID);
		SQLiteStatement stmt = DbRepository.getInstance().getDB()
				.compileStatement(sql);

		bindAndCheckNullString(stmt, 1, id);
		bindAndCheckNullString(stmt, 2, username);
		bindAndCheckNullString(stmt, 3, md5);
		bindAndCheckNullString(stmt, 4, role.id);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(ctx);
		String sql = "DELETE FROM " + DbManager.mUsers;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}


	public UserModel getUserModel(Context context, String userName, String password) {
		String md5 = MD5.md5(password);
		if (md5 == null){
			Toast.makeText(context, "Error on encripting password", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		DbRepository.getInstance().open(context);
		UserModel result = null;

		String table = DbManager.mUsers;
		String[] columns = null;
		String where = DbManager.colUserName + "=? AND "+ DbManager.colPassword + "=? ";
		String[] args = {userName , md5};
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}

		result = getUserFromCursor(cursor,context);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}
	
	public UserModel getFirstUser(Context context) {
		
		DbRepository.getInstance().open(context);
		UserModel result = null;

		String table = DbManager.mUsers;
		String[] columns = null;
		String where =null;
		String[] args = null;
		Cursor cursor;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return result;
		}

		result = getUserFromCursor(cursor,context);
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}
	
	
	
	public int countUser(Context context) {
		DbRepository.getInstance().open(context);

		String table = DbManager.mUsers;
		String[] columns = null;
		String where = null;
		String[] args = null;
		Cursor cursor;
		
		int result;

		cursor = DbRepository.getInstance().getDB().query(true, table, columns, where, args, null, null, null, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			return 0;
		}

		result = cursor.getCount();
		
		cursor.close();
		DbRepository.getInstance().close();
		
		return result;
	}

	private UserModel getUserFromCursor(Cursor c, Context context) {
		UserModel user = new UserModel();

		if (null == c)
			return user;

		user.id = (c.getString(c.getColumnIndex(DbManager.colID)));
		user.username = (c.getString(c.getColumnIndex(DbManager.colUserName)));
//		user.password = (c.getString(c.getColumnIndex(DbManager.colPassword)));

		//get role
		user.role = new RoleModel();
		user.role.id = (c.getString(c.getColumnIndex(DbManager.colRoleID)));
		user.role = user.role.getRoleModel(context, user.role.id);
		return user;
	}

	public static String createDB(){
		return "create table if not exists " + DbManager.mUsers
				+ " (" + DbManager.colID + " varchar, "
				+ DbManager.colUserName + " varchar, "
				+ DbManager.colPassword + " varchar, "
				+ DbManager.colRoleID + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
}
