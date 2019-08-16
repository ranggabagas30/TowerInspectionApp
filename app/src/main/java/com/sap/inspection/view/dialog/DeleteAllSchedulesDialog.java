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

public class DeleteAllSchedulesDialog {


	private OnClickListener positive;
	private OnClickListener negative;
	private EditText password;
	private AlertDialog dialog;

	public Context context;

	public DeleteAllSchedulesDialog(Context prompt)
	{ 
		this.context = prompt;
	}

	public void show(){
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		View v = LayoutInflater.from(context).inflate(R.layout.dialog_send_feedback, null);
		initializeView(v);
		dialog.setView(v);
		dialog.setTitle("Delete All Schedules");
		this.dialog = dialog.show();
	}

	private void initializeView(View v){
		v.findViewById(R.id.negative).setOnClickListener(negativeClickListener);
		v.findViewById(R.id.positive).setOnClickListener(positiveClickListener);
		password = (EditText) v.findViewById(R.id.comment);
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
			if (passTrue){
				if (positive != null)
					positive.onClick(v);
			}
			else
				Toast.makeText(context, "Please enter the correct password", Toast.LENGTH_SHORT).show();
		}
	};

	public DeleteAllSchedulesDialog setOnPositiveClickListener(OnClickListener positive) {
		this.positive = positive;
		return this;
	}

	public void setNegative(OnClickListener negative) {
		this.negative = negative;
	}

	public String getComment(){
		return password.getText().toString();
	}

}
