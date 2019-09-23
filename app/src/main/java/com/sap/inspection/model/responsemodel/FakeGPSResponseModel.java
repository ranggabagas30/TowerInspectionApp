package com.sap.inspection.model.responsemodel;

import com.google.gson.annotations.SerializedName;

public class FakeGPSResponseModel extends BaseResponseModel{

	public Data data;
	public String respond_in;

	public class Data {
		@SerializedName("time_detected")
		private String timeDetected;

		@SerializedName("app_version")
		private String appVersion;

		@SerializedName("updated_at")
		private String updatedAt;

		@SerializedName("user_id")
		private int userId;

		@SerializedName("site_id")
		private int siteId;

		@SerializedName("created_at")
		private String createdAt;

		@SerializedName("id")
		private int id;

		@SerializedName("message")
		private String message;

		public void setTimeDetected(String timeDetected){
			this.timeDetected = timeDetected;
		}

		public String getTimeDetected(){
			return timeDetected;
		}

		public void setAppVersion(String appVersion){
			this.appVersion = appVersion;
		}

		public String getAppVersion(){
			return appVersion;
		}

		public void setUpdatedAt(String updatedAt){
			this.updatedAt = updatedAt;
		}

		public String getUpdatedAt(){
			return updatedAt;
		}

		public void setUserId(int userId){
			this.userId = userId;
		}

		public int getUserId(){
			return userId;
		}

		public void setSiteId(int siteId){
			this.siteId = siteId;
		}

		public int getSiteId(){
			return siteId;
		}

		public void setCreatedAt(String createdAt){
			this.createdAt = createdAt;
		}

		public String getCreatedAt(){
			return createdAt;
		}

		public void setId(int id){
			this.id = id;
		}

		public int getId(){
			return id;
		}

		public void setMessage(String message){
			this.message = message;
		}

		public String getMessage(){
			return message;
		}
	}

	@Override
	public String toString(){
		return
				"\nFakeGPSResponseModel -- \n{" + '\n' +
						"id = '" + data.getId() + '\'' + '\n' +
						",user_id = '" + data.getUserId() + '\'' + '\n' +
						",site_id = '" + data.getSiteId() + '\'' + '\n' +
						",time_detected = '" + data.getTimeDetected() + '\'' + '\n' +
						",app_version = '" + data.getAppVersion() + '\'' + '\n' +
						",message = '" + data.getMessage() + '\'' + '\n' +
						",created_at = '" + data.getCreatedAt() + '\'' + '\n' +
						",updated_at = '" + data.getUpdatedAt() + '\'' + '\n' +
						"}";
	}

}