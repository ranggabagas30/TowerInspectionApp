package com.sap.inspection.manager;

import com.sap.inspection.model.value.FormValueModel;

import java.util.ArrayList;

/**
 * Created by domikado on 12/27/17.
 */

public interface AsyncUploadResponse {
    void itemUploadResponse(FormValueModel formValueModel);
    void itemUploadResponse(ArrayList<FormValueModel> formValueModelArrayList);
}
