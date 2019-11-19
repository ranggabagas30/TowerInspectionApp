package com.sap.inspection.view.dialog;



import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.inspection.R;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.util.PrefUtil;

public class DeleteAllDataDialog {

	private OnClickListener positive;
	private OnClickListener negative;
	private OnPositiveClickListener onPositiveClickListener;
	private EditText password;
	private AlertDialog dialog;
	private String scheduleId;
	private ScheduleBaseModel schedule;

	public Context context;

	public DeleteAllDataDialog(Context context) {
		this(context, null);
	}

	public DeleteAllDataDialog(Context prompt, ScheduleBaseModel schedule) {
		this.context = prompt;
		this.schedule = schedule;
	}

	public void show(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View v = LayoutInflater.from(context).inflate(R.layout.dialog_send_feedback, null);
		initializeView(v);
        dialog.setView(v);
        dialog.setCancelable(true);
        dialog.setTitle("Delete All Data");
        dialog.setMessage("Aplikasi akan menghapus seluruh data isian form, file foto, dan cache. " +
                " Mohon upload data terlebih dahulu jika diperlukan");
		if (schedule != null) {
		    dialog.setTitle("Delete schedule");
		    dialog.setMessage("Aplikasi akan menghapus seluruh data isian form dan file foto untuk schedule site ini. " +
                    " Mohon upload data terlebih dahulu jika diperlukan");

        }
		this.dialog = dialog.show();
	}

	private void initializeView(View v){
		v.findViewById(R.id.negative).setOnClickListener(negativeClickListener);
		v.findViewById(R.id.positive).setOnClickListener(positiveClickListener);
		password = v.findViewById(R.id.comment);
	}

	OnClickListener negativeClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			v.setTag(password.getText().toString());
			dialog.dismiss();
			if (negative != null)
				negative.onClick(v);
		}
	};

	OnClickListener positiveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			v.setTag(password.getText().toString());
			dialog.dismiss();
			boolean passTrue = PrefUtil.getStringPref(R.string.password, null).equals(password.getText().toString());
			if (passTrue) {
				if (onPositiveClickListener != null) {
					onPositiveClickListener.onPositiveClick(schedule);
				}
			}
			else
				Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show();
		}
	};

	public DeleteAllDataDialog setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {
		this.onPositiveClickListener = onPositiveClickListener;
		return this;
	}

	public interface OnPositiveClickListener {
		void onPositiveClick(ScheduleBaseModel schedule);
	}
}
