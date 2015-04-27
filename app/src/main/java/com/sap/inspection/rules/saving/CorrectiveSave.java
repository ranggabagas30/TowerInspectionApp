package com.sap.inspection.rules.saving;

import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.rules.SavingRule;

public class CorrectiveSave extends SavingRule{

	@Override
	public void save(ItemFormRenderModel itemFormRenderModel, ItemValueModel value){
		value.uploadStatus = ItemValueModel.UPLOAD_NONE;
		value.save();
	}
}