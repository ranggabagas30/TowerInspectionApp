package com.sap.inspection.model.config.formimbaspetir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.inspection.model.ConfigModel;
import com.sap.inspection.model.DbManager;
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

    // get list of data warga
    public static ArrayList<Warga> getDataWarga(int indexOfData) {

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            ImbasPetirData data = formImbasPetirConfig.getData().get(indexOfData);

            return data.getWarga();
        }

        return null;
    }

    // insert n empty data warga
    public static void updateDataWarga(int dataIndex, int amountOfWarga) {

        ArrayList<Warga> wargas = new ArrayList<>();

        for (int i = 1; i <= amountOfWarga; i++) {

            // initialize data warga
            Warga warga = new Warga();
            warga.setWargaid(String.valueOf(i));
            warga.setBarang(new ArrayList<>());

            // add data warga to the list of warga
            wargas.add(warga);
        }

        updateDataWarga(dataIndex, wargas);
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

    public static int indexOfData(String scheduleId) {

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

        return indexOfData(scheduleId) != -1;
    }

    public static void insertNewData(String scheduleId) {

        FormImbasPetirConfig formImbasPetirConfig = getImbasPetirConfig();

        if (formImbasPetirConfig != null) {

            ArrayList<ImbasPetirData> dataList = formImbasPetirConfig.getData();

            ImbasPetirData data = new ImbasPetirData();

            data.setScheduleid(scheduleId);
            data.setWarga(new ArrayList<>());

            DebugLog.d("");
            dataList.add(data);

            formImbasPetirConfig.setData(dataList);

            ConfigModel.save(ConfigModel.CONFIG_ENUM.IMBAS_PETIR_CONFIG.name(), new Gson().toJson(formImbasPetirConfig));

        }
    }
}
