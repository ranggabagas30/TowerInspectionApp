package com.sap.inspection.rules;

import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.value.ItemValueModel;

public abstract class SavingRule {
	public abstract void save(ItemFormRenderModel itemFormRenderModel, ItemValueModel value);
}
