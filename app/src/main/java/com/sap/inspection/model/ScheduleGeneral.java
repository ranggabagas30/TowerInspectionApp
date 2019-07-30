package com.sap.inspection.model;


import com.sap.inspection.BuildConfig;
import com.sap.inspection.constant.Constants;

public class ScheduleGeneral extends ScheduleBaseModel {

	@Override
	public String getTitle() {
//		return project != null ? project.name : "";
		return site != null ? site.name : "";
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public String getTask() {
		return work_type != null ? work_type.name : "";
	}

	@Override
	public String getPlace() {
		return site != null ? site.name : "";
	}

	@Override
	protected ScheduleBaseModel newObject() {
		return new ScheduleGeneral();
	}

	@Override
	public String getPercentColor() {
		if (statusColor != null)
			return statusColor;
		else if (getStatus().equalsIgnoreCase("new"))
			return "#4cbcf3";
		else if (getStatus().equalsIgnoreCase("on progress"))
			return "#16d7b2";
		else if (getStatus().equalsIgnoreCase("pending"))
			return "#f75b54";
		else if (getStatus().equalsIgnoreCase("completed"))
			return "#4cbcf3";
		else if (getStatus().equalsIgnoreCase("canceled"))
			return "#f75b54";
		else if (getStatus().equalsIgnoreCase("filled"))
			return "#16d7b20";
		else if (getStatus().equalsIgnoreCase("uploaded"))
			return "#4cbcf3";
		return "#d0d0d0";
	}

	@Override
	public String getTaskColor() {

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			return site.color_rtpo;
		} else {
			if (taskColor != null)
				return taskColor;
			else if (getTask().equalsIgnoreCase("preventive"))
				return "#16d7b2";
			else if (getTask().equalsIgnoreCase("corrective"))
				return "#f75b54";
			else if (getTask().equalsIgnoreCase("new location"))
				return "#4cbcf3";
			else if (getTask().equalsIgnoreCase("colocation"))
				return "#f75b54";
			return "#d0d0d0";
		}
	}

}
