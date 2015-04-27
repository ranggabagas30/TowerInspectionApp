package com.sap.inspection.connection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.sap.inspection.model.responsemodel.BaseResponseModel;

public class ConnectionHandler extends Handler {
	
	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();
		Gson gson = new Gson();
		if (bundle.getString("json") != null){
			BaseResponseModel userResponseModel = gson.fromJson(bundle.getString("json"), BaseResponseModel.class);
//			if ()
		}else
			return;
	}

}
