package com.sap.inspection.view.dialog;



import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.inspection.R;
import com.sap.inspection.util.PrefUtil;

public class DeleteAllDataDialog {

	private OnClickListener positive;
	private OnClickListener negative;
	private OnPositiveClickListener onPositiveClickListener;
	private EditText password;
	private AlertDialog dialog;
	private String scheduleId;

	public Context context;

	public DeleteAllDataDialog(Context prompt, String scheduleId) {
		this.context = prompt;
		this.scheduleId = scheduleId;
	}

	public void show(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View v = LayoutInflater.from(context).inflate(R.layout.dialog_send_feedback, null);
		initializeView(v);
		dialog.setView(v);
		dialog.setTitle("Delete All Data");
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
					onPositiveClickListener.onPositiveClick(scheduleId);
				}
			}
			else
				Toast.makeText(context, "Please enter the correct password", Toast.LENGTH_SHORT).show();
		}
	};

	public DeleteAllDataDialog setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {
		this.onPositiveClickListener = onPositiveClickListener;
		return this;
	}

	public interface OnPositiveClickListener {

		void onPositiveClick(String scheduleId);

	}
}
