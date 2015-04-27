package com.sap.inspection.views.adapter;

import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchAdapter extends ArrayAdapter<NameValuePair> {

	private Context context;
	private ArrayList<NameValuePair> models;
	
	public SearchAdapter(Context context, int resource, int textViewResourceId,ArrayList<NameValuePair> objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.models = objects;
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
