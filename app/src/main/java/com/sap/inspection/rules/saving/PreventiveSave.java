package com.sap.inspection.rules.saving;

import android.util.Log;

import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.rules.SavingRule;

public class PreventiveSave extends SavingRule{

	@Override
	public void save(ItemFormRenderModel itemFormRenderModel, ItemValueModel value){
		if(!itemFormRenderModel.itemModel.scope_type.equalsIgnoreCase("operator")){
			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
				value.operatorId = operatorModel.id;
				value.uploadStatus = ItemValueModel.UPLOAD_NONE;
				value.save();
			}
		}else{
			value.uploadStatus = ItemValueModel.UPLOAD_NONE;
			value.save();
		}
	}
}
