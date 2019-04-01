package com.sap.inspection.model.config.formimbaspetir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.inspection.model.ConfigModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;

public class FormImbasPetirConfig
{
    private ArrayList<ImbasPetirData> data;

    public ArrayList<ImbasPetirData> getData ()
    {
        return data;
    }

    public void setData (ArrayList<ImbasPetirData> data)
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [data = "+data+"]";
    }


    /**
     *
     * crud operation
     *
     * */

    public static void createImbasPetirConfig() {

        FormImbasPetirConfig formImbasPetirConfig = new FormImbasPetirConfig();
        formImbasPetirConfig.setData(new ArrayList<>()); // set empty config data

        ConfigModel config = new ConfigModel();
        config.configName = ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name();
        config.configData = new Gson().toJson(formImbasPetirConfig);

        ConfigModel.save(config.configName, config.configData);
    }

    public static FormImbasPetirConfig getImbasPetirConfig() {

        DebugLog.d("check imbas petir config");
        DebugLog.d("retrieve config data.... ");

        ConfigModel config = ConfigModel.getConfig( new String[]{DbManager.colConfigName},
                                                    new String[]{ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name()});

        if (config != null) // config not found
            return new Gson().fromJson(config.configData, FormImbasPetirConfig.class);
        else
            return null;
    }

    public static int getWargaIndex(ArrayList<Warga> wargas, String wargaId) {

        int indexFound = -1;
        int size = wargas.size();
        for (int i = 0; i < size; i++) {
            if (wargas.get(i).getWargaid().equalsIgnoreCase(wargaId)) {
                indexFound = i;
                break;
            }
        }
        return indexFound;
    }

    // get list of data warga
    public static ArrayList<Warga> getDataWarga(int dataIndex) {

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            ImbasPetirData data = formImbasPetirConfig.getData().get(dataIndex);

            return data.getWarga();
        }

        return null;
    }


    // insert n empty data warga
    public static void insertDataWarga(int dataIndex, int amountOfWarga) {

       FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

       if (formImbasPetirConfig != null) {

           ArrayList<ImbasPetirData> dataList = formImbasPetirConfig.getData();
           ImbasPetirData data = dataList.get(dataIndex);
           ArrayList<Warga> wargas = data.getWarga();

           int size = wargas.size();

           DebugLog.d("size of data warga : " + size);
           DebugLog.d("add empty data warga ...");
           for (int i = 1; i <= amountOfWarga; i++) {

               int wargake = i + size;
               String wargaId = "new" + String.valueOf(wargake);

               Warga warga = new Warga();
               warga.setWargaid(wargaId); // real id
               warga.setWargake(wargake); // dummy index only
               warga.setBarang(new ArrayList<>());

               wargas.add(warga);

               DebugLog.d("wargake : " + wargake);
               DebugLog.d("wargaId : " + wargaId);
           }

           updateDataWarga(dataIndex, wargas);
       }
    }

    // insert a list of data warga
    public static void updateDataWarga(int dataIndex, ArrayList<Warga> wargas) {

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            ArrayList<ImbasPetirData> dataList = formImbasPetirConfig.getData();

            ImbasPetirData data = dataList.get(dataIndex);
            data.setWarga(wargas);

            dataList.set(dataIndex, data);

            formImbasPetirConfig.setData(dataList);

            String configName = ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name();
            String configData = new Gson().toJson(formImbasPetirConfig);

            ConfigModel.save(configName, configData);
        }
    }

    public static boolean removeDataWarga(String scheduleId, String wargaId) {

        // Todo: remove FormValue with wargaId = {wargaId}, scheduleId = {scheduleId}
        // Todo: remove data warga config by {wargaId} and {scheduleId}
        // Todo: update data warga config and save the config

        int dataIndex = getDataIndex(scheduleId);

        ArrayList<Warga> wargas = getDataWarga(dataIndex);

        if (wargas != null) {

            int indexRemove = getWargaIndex(wargas, wargaId);

            if (indexRemove != -1) {

                // remove FormValue with wargaId = {wargaId}, scheduleId = {scheduleId}
                ItemValueModel.delete(scheduleId, ItemValueModel.UNSPECIFIED, ItemValueModel.UNSPECIFIED, wargaId, null);

                // remove data warga config by {wargaId} and {scheduleId}
                DebugLog.d("remove wargaid : " + wargaId);
                wargas.remove(indexRemove);

                // update data warga config
                updateDataWarga(dataIndex, wargas);

                return true;
            } else {

                DebugLog.d("wargaid not found");
            }

        } else {

            DebugLog.d("list of data warga is empty");
        }

        return false;
    }


    public static int getDataIndex(String scheduleId) {

        int indexFound = -1;

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            int index = 0;
            for (ImbasPetirData data : formImbasPetirConfig.getData()) {

                if (data.getScheduleid().equalsIgnoreCase(scheduleId)) {

                    // data found
                    indexFound = index;
                    break;
                }

                index++;
            }
        }

        return indexFound;
    }

    public static boolean isDataExist(String scheduleId) {

        return getDataIndex(scheduleId) != -1;
    }

    public static void insertNewData(String scheduleId) {

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            ArrayList<ImbasPetirData> dataList = formImbasPetirConfig.getData();

            ImbasPetirData data = new ImbasPetirData();

            data.setScheduleid(scheduleId);
            data.setWarga(new ArrayList<>());

            dataList.add(data);

            formImbasPetirConfig.setData(dataList);

            ConfigModel.save(ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name(), new Gson().toJson(formImbasPetirConfig));

        }
    }
}
