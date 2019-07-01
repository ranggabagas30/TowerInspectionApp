package com.sap.inspection.rules;

import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.FormValueModel;

public abstract class SavingRule {
	public abstract void save(ItemFormRenderModel itemFormRenderModel, FormValueModel value);
}
