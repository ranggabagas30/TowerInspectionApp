//package com.sap.inspection.task;
//
//import android.os.AsyncTask;
//
//import com.sap.inspection.MainActivity;
//import com.sap.inspection.model.form.ColumnModel;
//import com.sap.inspection.model.form.RowModel;
//import com.sap.inspection.model.form.WorkFormGroupModel;
//import com.sap.inspection.model.form.WorkFormModel;
//
//public class FormSaver extends AsyncTask<Object,Integer,Void> {
//
//	private MainActivity mainActivity;
//	public void setMainActivity(MainActivity mainActivity) {
//		this.mainActivity = mainActivity;
//	}
//
//	@Override
//	protected Void doInBackground(Object... params) {
//		int sum = 0;
//		for (int i = 0; i < params.length; i++) {
//			if (((WorkFormModel)params[i]).groups != null)
//				for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
//					sum += group.table.headers.size();
//					sum += group.table.rows.size();
//				}
//		}
//		int curr = 0;
//		for (int i = 0; i < params.length; i++) {
//			((WorkFormModel)params[i]).save();
//			if (((WorkFormModel)params[i]).groups != null)
//				for (WorkFormGroupModel group : ((WorkFormModel)params[i]).groups) {
//					for (ColumnModel columnModel : group.table.headers) {
//						curr ++;
//						publishProgress(curr*100/sum);
//						columnModel.save();
//					}
//
//					for (RowModel rowModel : group.table.rows) {
//						curr ++;
//						publishProgress(curr*100/sum);
//						rowModel.save();
//					}
//				}
//		}
//		return null;
//	}
//
//	@Override
//	protected void onProgressUpdate(Integer... values) {
//		super.onProgressUpdate(values);
//		mainActivity.setProgressDialogMessage("form","saving forms "+values[0]+" %...");
//	}
//
//	@Override
//	protected void onPostExecute(Void result) {
//		super.onPostExecute(result);
////		mainActivity.setFlagFormSaved(true);
//	}
//}
