package com.sap.inspection.rules.saving;

import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.rules.SavingRule;

public class CorrectiveSave extends SavingRule{

	@Override
	public void save(ItemFormRenderModel itemFormRenderModel, FormValueModel value){
		value.uploadStatus = FormValueModel.UPLOAD_NONE;
		value.save();
	}
}