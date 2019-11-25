package com.sap.inspection.rules.saving;

import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.rules.SavingRule;

public class PreventiveSave extends SavingRule{

	@Override
	public void save(ItemFormRenderModel itemFormRenderModel, FormValueModel value){
		if(!itemFormRenderModel.getWorkItemModel().scope_type.equalsIgnoreCase("operator")){
			for (OperatorModel operatorModel : itemFormRenderModel.getSchedule().operators) {
				value.operatorId = operatorModel.id;
				value.uploadStatus = FormValueModel.UPLOAD_NONE;
				value.save();
			}
		}else{
			value.uploadStatus = FormValueModel.UPLOAD_NONE;
			value.save();
		}
	}
}
