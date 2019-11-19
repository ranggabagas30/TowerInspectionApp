package com.sap.inspection.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import com.sap.inspection.R;
import com.sap.inspection.constant.GlobalVar;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.view.dialog.DeleteAllDataDialog;
import com.sap.inspection.view.dialog.DeleteAllSchedulesDialog;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.ArrayList;

public class DialogUtil {

    /** asking GPS permission  **/
    public static void showGPSdialog(Context context) {
        new LovelyStandardDialog(context, R.style.CheckBoxTintTheme)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setButtonsColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle(context.getString(R.string.informationGPS))
                .setMessage("Silahkan aktifkan GPS")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, v -> {
                    Intent gpsOptionsIntent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(gpsOptionsIntent);
                }).show();
    }

    /** enable network **/
    public static void showEnableNetworkDialog(Context context) {
        if (!GlobalVar.getInstance().anyNetwork(context)){
            new LovelyStandardDialog(context, R.style.CheckBoxTintTheme)
                    .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                    .setButtonsColor(ContextCompat.getColor(context, R.color.theme_color))
                    .setIcon(R.drawable.logo_app)
                    .setTitle("Information")
                    .setMessage("No internet connection. Please connect your network.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, v -> {
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
        }
    }

    /** take picture camera app recommendation */
    public static void showTakePictureDialog(Context context, LovelyChoiceDialog.OnItemSelectedListener<? super String> onItemSelectedListener) {
        new LovelyChoiceDialog(context, R.style.CheckBoxTintTheme)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle("Pengambilan foto")
                .setMessage("Aplikasi akan melakukan pengambilan foto. Untuk hasil lebih baik, disarankan untuk memilih " +
                        "\"Camera\" default (bawaan device) jika aplikasi menawarkan metode pengambilan foto")
                .setCancelable(true)
                .setItems(new String[]{
                        context.getString(R.string.positive_understood_dont_show_again),
                        context.getString(R.string.positive_understood),
                        context.getString(R.string.negative_cancel)
                }, onItemSelectedListener)
                .show();
    }

    public static void singleChoiceScheduleRoutingDialog(Context context, LovelyChoiceDialog.OnItemSelectedListener<String> onItemSelectedListener) {
        ArrayList<String> routingSchedules = new ArrayList<>();
        routingSchedules.add(context.getString(R.string.routing_segment));
        routingSchedules.add(context.getString(R.string.handhole));
        routingSchedules.add(context.getString(R.string.hdpe));
        routingSchedules.add(context.getString(R.string.focut));

        new LovelyChoiceDialog(context)
                .setTopColor(ContextCompat.getColor(context, R.color.theme_color))
                .setIcon(R.drawable.logo_app)
                .setTitle("Choose schedule")
                .setMessage("Please select one of routing schedules")
                .setItems(routingSchedules, onItemSelectedListener)
                .show();
    }

    public static DeleteAllDataDialog deleteAllDataDialog(Context context) {
        return new DeleteAllDataDialog(context);
    }

    public static DeleteAllDataDialog deleteAllDataDialog(Context context, ScheduleBaseModel schedule) {
        return new DeleteAllDataDialog(context, schedule);
    }

    public static DeleteAllSchedulesDialog deleteAllSchedulesDialog(Context context) {
        return new DeleteAllSchedulesDialog(context);
    }

    public static void showUploadAllDataDialog(Context context, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.reuploadAllData))
                .setMessage(context.getString(R.string.areyousurereuploaddata))
                .setPositiveButton("Upload", positiveClickListener)
                .setNegativeButton("Batal", negativeClickListener)
                .create().show();
    }

    public static void showRejectionDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setOnCancelListener(DialogInterface::dismiss).create().show();
    }

    public static void showWarningUpdateFormDialog(Context context, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.warning_update_form_title))
                .setMessage(context.getString(R.string.warning_update_form_message))
                .setPositiveButton(android.R.string.ok, positiveClickListener)
                .setNegativeButton(android.R.string.cancel, negativeClickListener)
                .create().show();
    }

    public static void showCreateFoCutScheduleDialog(Context context, LovelyTextInputDialog.OnTextInputConfirmListener onTextInputConfirmListener) {
        new LovelyTextInputDialog(context, R.color.theme_color)
                .setTopColorRes(R.color.item_drill_red)
                .setTopTitle(R.string.app_name)
                .setTitle("Create FO CUT schedule")
                .setMessage("Input TT Number")
                .setConfirmButton(android.R.string.ok, onTextInputConfirmListener)
                .setCancelable(true)
                .show();
    }

    public static void showEditFoCutScheduleDialog(Context context, LovelyTextInputDialog.OnTextInputConfirmListener onTextInputConfirmListener) {
        new LovelyTextInputDialog(context, R.color.theme_color)
                .setTopColorRes(R.color.item_drill_red)
                .setTopTitle(R.string.app_name)
                .setTitle("Edit FO CUT schedule")
                .setMessage("Input new TT Number")
                .setConfirmButton(android.R.string.ok, onTextInputConfirmListener)
                .setCancelable(true)
                .show();
    }
}
