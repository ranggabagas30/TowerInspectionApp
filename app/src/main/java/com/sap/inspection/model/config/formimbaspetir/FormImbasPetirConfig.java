package com.sap.inspection.model.config.formimbaspetir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.inspection.constant.Constants;
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

    public static String getRegisteredWargaId(String scheduleId, String wargaId) {

        int dataindex = getDataIndex(scheduleId);

        if (dataindex != -1) {

            DebugLog.d( "schedule data index found, get registered warga id");
            ArrayList<Warga> wargas = getDataWarga(dataindex);
            Warga warga = getWarga(wargas, wargaId);

            return warga.getWargaid();
        }
        DebugLog.d("schedule data index not found");
        return null;
    }

    public static Warga getWarga(ArrayList<Warga> wargas, String wargaId) {

        int indexFound = getWargaIndex(wargas, wargaId);

        if (indexFound != - 1) {
            return wargas.get(indexFound);
        }
        return null;
    }

    public static int getWargaIndex(ArrayList<Warga> wargas, String wargaSearchId) {

        int indexFound = -1;
        int size = wargas.size();
        for (int i = 0; i < size; i++) {

            String wargaid = wargas.get(i).getWargaid();
            String wargake = wargas.get(i).getWargake();
            DebugLog.d("wargasearchid : " + wargaSearchId);
            DebugLog.d("(wargaid, wargake) = (" + wargaid + "," + wargake +")");

            if (wargaid.equalsIgnoreCase(wargaSearchId) || wargake.equalsIgnoreCase(wargaSearchId)) {
                indexFound = i;
                break;
            }
        }
        return indexFound;
    }

    public static void updateWarga(String scheduleId, String oldWargaId, String newWargaId) {

        int dataindex = FormImbasPetirConfig.getDataIndex(scheduleId);

        if (dataindex != -1) {

            ArrayList<Warga> wargas = FormImbasPetirConfig.getDataWarga(dataindex);

            if (wargas != null && !wargas.isEmpty()) {

                int wargaindex = getWargaIndex(wargas, oldWargaId);

                Warga warga = wargas.get(wargaindex);
                warga.setWargaid(newWargaId);

                wargas.set(wargaindex, warga);

                FormImbasPetirConfig.updateDataWarga(dataindex, wargas);

                DebugLog.d("update config oldwargaid (" + oldWargaId + ") to (" + newWargaId + ")");
            }
        }
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

            // get imbas petir data config by index
            ImbasPetirData data = dataList.get(dataIndex);

            // retrieve current list of data warga
            ArrayList<Warga> wargas = data.getWarga();
            int size = wargas.size();
            int countaddwarga = data.getCountaddwarga();

            DebugLog.d("count add warga : " + countaddwarga);
            DebugLog.d("size of data warga : " + size);
            DebugLog.d("add " + amountOfWarga + " empty data warga ...");

            // create {amountofdata} empty data warga
            for (int i = 1; i <= amountOfWarga; i++) {

                int wargake = i + countaddwarga;
                String wargaId = "new" + String.valueOf(wargake);

                Warga warga = new Warga();  //\\ init warga
                warga.setWargake(wargaId);    // temporary id
                warga.setWargaid(wargaId);   // real id
                warga.setRegistered(false); // flag not registered yet for added warga data
                warga.setBarang(new ArrayList<>());

                wargas.add(warga);

                DebugLog.d("wargake : " + warga.getWargake());
                DebugLog.d("wargaId : " + warga.getWargaid());
            }
            countaddwarga = countaddwarga + amountOfWarga;

            // update current list of warga
            data.setWarga(wargas);

            // update current count of add data warga
            data.setCountaddwarga(countaddwarga);

            dataList.set(dataIndex, data);

            formImbasPetirConfig.setData(dataList);

            String configName = ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name();
            String configData = new Gson().toJson(formImbasPetirConfig);

            ConfigModel.save(configName, configData);
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
