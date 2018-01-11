package com.sap.inspection.manager;

import com.sap.inspection.model.value.ItemValueModel;

import java.util.ArrayList;

/**
 * Created by domikado on 12/27/17.
 */

public interface AsyncUploadResponse {
    void itemUploadResponse(ItemValueModel itemValueModel);
    void itemUploadResponse(ArrayList<ItemValueModel> itemValueModelArrayList);
}
