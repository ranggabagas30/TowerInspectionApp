package com.sap.inspection.fragments.dialogfragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class TextInputDialog extends DialogFragment {

    private String mTitle;
    private int mTitleTextColor;

    private String mMesssage;
    private int mMessageTextColor;


    public TextInputDialog() {
        // default empty constructor
        // note : do not set any input parameter to this constructor
    }

    public static TextInputDialog newInstance(String title, String message) {
        TextInputDialog frag = new TextInputDialog();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
