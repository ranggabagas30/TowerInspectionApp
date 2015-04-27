package com.sap.inspection.views.adapter;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.inspection.R;

public class CopyOfSearchAdapter extends MyBaseAdapter {

	private Context context;
	private ArrayList<NameValuePair> models;

	public CopyOfSearchAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return models.size();
	}
	
	public void setModels(ArrayList<NameValuePair> models) {
		this.models = models;
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public NameValuePair getItem(int position) {
		return models.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(android.R.layout.select_dialog_item,null);
			holder.title = (TextView) view.findViewById(android.R.id.text1);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		
		holder.title.setText(getItem(position).getName());
		return view; 
	}

	private class ViewHolder {
		public TextView title;
	}


}
