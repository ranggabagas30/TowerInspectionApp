package com.sap.inspection.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class MyTextView extends TextView {

	public MyTextView(Context context) {
		super(context);
		init(context);
	}

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	
	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		setBold(context, false);
	}
	
	public void setBold(Context context, boolean bold){
		Typeface type = Typeface.createFromAsset(context.getAssets(),"fonts/helvetica_lt_45_light_0.ttf");
		if (bold)
			setTypeface(type,Typeface.BOLD);
		else
			setTypeface(type);
	}

}
