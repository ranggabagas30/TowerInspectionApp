package com.sap.inspection.views.adapter;

import java.util.Vector;

import android.R.integer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.inspection.R;

public class MainMenuAdapter extends MyBaseAdapter {
	
	private int[] icons;
	private int[] titles;
	private Context context;
	private OnClickListener settingClickListener;
	
	public MainMenuAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return icons == null?0:icons.length;
	}
	
	public void setModels(int[] modelsIcon, int[] modelsString){
		icons = modelsIcon;
		titles = modelsString;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		return null;
	}
	
//	@Override
//	public int getViewTypeCount() {
//		return 2;
//	}
//	
//	@Override
//	public int getItemViewType(int position) {
//		return position == 0 ? 0 : 1;
//	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
				view = LayoutInflater.from(context).inflate(R.layout.item_mainmenu_square,null);
				holder.icon = (ImageView) view.findViewById(R.id.item_mainmenu_icon);
				holder.title = (TextView) view.findViewById(R.id.item_mainmenu_title);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		
			holder.title.setText(titles[position]);
			holder.icon.setImageResource(icons[position]);
		return view; 
	}
	
	public void setSettingClickListener(OnClickListener settingClickListener) {
		this.settingClickListener = settingClickListener;
	}

	private class ViewHolder {
		public ImageView icon;
		public TextView title;
	}


}
