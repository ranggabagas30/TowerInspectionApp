package com.sap.inspection.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.sap.inspection.listener.FormTextChange;

public class FormInputText extends EditText{

	private FormTextChange textChange;

	public void setTextChange(FormTextChange textChange) {
		this.textChange = textChange;
	}

	public FormInputText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FormInputText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public FormInputText(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		addTextChangedListener(textWatcher);
		/*
		int maxLength = 60;    
		setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
		*/
	}

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			if (textChange != null)
				textChange.onTextChange(s.toString(), FormInputText.this);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	};

	@Override
	public void addTextChangedListener(TextWatcher watcher) {
		super.addTextChangedListener(watcher);
	}

}
