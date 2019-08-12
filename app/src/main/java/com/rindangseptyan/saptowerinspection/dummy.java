package com.rindangseptyan.saptowerinspection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;


public class dummy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sap.inspection.R.layout.activity_dummy);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.sap.inspection.R.menu.menu_dummy, menu);
        return true;
    }

}
