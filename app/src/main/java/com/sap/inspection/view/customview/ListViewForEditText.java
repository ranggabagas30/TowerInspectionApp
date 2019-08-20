//package com.sap.inspection.view;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ListView;
//
//public class ListViewForEditText extends ListView {
//
//	public ListViewForEditText(Context context) {
//		super(context);
//	}
//	
//	public void onItemSelected(AdapterView<?> listView, View view, int position, long id)
//	{
//	    if (position == 1)
//	    {
//	        // listView.setItemsCanFocus(true);
//
//	        // Use afterDescendants, because I don't want the ListView to steal focus
//	        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
//	        myEditText.requestFocus();
//	    }
//	    else
//	    {
//	        if (!listView.isFocused())
//	        {
//	            // listView.setItemsCanFocus(false);
//
//	            // Use beforeDescendants so that the EditText doesn't re-take focus
//	            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//	            listView.requestFocus();
//	        }
//	    }
//	}
//
//	public void onNothingSelected(AdapterView<?> listView)
//	{
//	    // This happens when you start scrolling, so we need to prevent it from staying
//	    // in the afterDescendants mode if the EditText was focused 
//	    listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
//	}
//
//}
