package com.sap.inspection.model.config.formimbaspetir;

import com.google.gson.Gson;
import com.sap.inspection.model.ConfigModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.responsemodel.CorrectiveScheduleResponseModel;

import java.util.ArrayList;

public class CorrectiveScheduleConfig {

    public static void setCorrectiveScheduleConfig(CorrectiveScheduleResponseModel correctiveData) {

        String configData = new Gson().toJson(correctiveData, CorrectiveScheduleResponseModel.class);
        ConfigModel.save(ConfigModel.CONFIG_ENUM.CORRECTIVE_SCHEDULES_AND_ITEMS_CONFIG.name(), configData);
    }

    public static CorrectiveScheduleResponseModel getCorrectiveScheduleConfig() {

        ConfigModel config = ConfigModel.getConfig(new String[]{DbManager.colConfigName}, new String[]{ConfigModel.CONFIG_ENUM.CORRECTIVE_SCHEDULES_AND_ITEMS_CONFIG.name()});
        if (config != null)
            return new Gson().fromJson(config.configData, CorrectiveScheduleResponseModel.class);
        return null;
    }

    public static ArrayList<CorrectiveScheduleResponseModel.CorrectiveSchedule> getListCorrectiveSchedule() {

        CorrectiveScheduleResponseModel correctiveData = getCorrectiveScheduleConfig();
        if (correctiveData != null) {
            return correctiveData.getData();
        }
        return null;
    }

    public static CorrectiveScheduleResponseModel.CorrectiveSchedule getCorrectiveSchedule(int scheduleId) {

        ArrayList<CorrectiveScheduleResponseModel.CorrectiveSchedule> correctiveSchedules = getListCorrectiveSchedule();
        if (correctiveSchedules != null) {
            for (CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule : correctiveSchedules) {
                if (correctiveSchedule.getId().equals(scheduleId)){
                    return correctiveSchedule;
                }
            }
        }
        return null;
    }

    public static ArrayList<CorrectiveScheduleResponseModel.CorrectiveGroup> getListCorrectiveGroup(int scheduleId) {

        CorrectiveScheduleResponseModel.CorrectiveSchedule correctiveSchedule = getCorrectiveSchedule(scheduleId);
        if (correctiveSchedule != null) {
            return correctiveSchedule.getGroup();
        }
        return null;
    }

    public static CorrectiveScheduleResponseModel.CorrectiveGroup getCorrectiveGroup(int scheduleId, int groupId) {

        ArrayList<CorrectiveScheduleResponseModel.CorrectiveGroup> correctiveGroups = getListCorrectiveGroup(scheduleId);
        if (correctiveGroups != null) {
            for (CorrectiveScheduleResponseModel.CorrectiveGroup correctiveGroup : correctiveGroups) {
                if (correctiveGroup.getId().equals(groupId)) {
                    return correctiveGroup;
                }
            }
        }
        return null;
    }

    public static ArrayList<CorrectiveScheduleResponseModel.CorrectiveItem> getListCorrectiveItem(int scheduleId, int groupId) {

        CorrectiveScheduleResponseModel.CorrectiveGroup correctiveGroup = getCorrectiveGroup(scheduleId, groupId);
        if (correctiveGroup != null) {
            return correctiveGroup.getItems();
        }
        return null;
    }

    public static CorrectiveScheduleResponseModel.CorrectiveItem getCorrectiveItem(int scheduleId, int groupId, int itemId) {

        ArrayList<CorrectiveScheduleResponseModel.CorrectiveItem> correctiveItems = getListCorrectiveItem(scheduleId, groupId);
        if (correctiveItems != null) {
            for (CorrectiveScheduleResponseModel.CorrectiveItem correctiveItem : correctiveItems) {
                if (correctiveItem.getId().equals(itemId)) {
                    return correctiveItem;
                }
            }
        }
        return null;
    }
}
