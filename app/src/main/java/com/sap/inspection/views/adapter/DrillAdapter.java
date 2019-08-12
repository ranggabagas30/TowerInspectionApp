package com.sap.inspection.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.inspection.R;

public class DrillAdapter extends MyBaseAdapter {

	private Context context;

	public DrillAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return 100;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			view = LayoutInflater.from(context).inflate(R.layout.item_drill,null);
			holder.arrow = (ImageView) view.findViewById(R.id.item_drill_arrow);
			holder.title = (TextView) view.findViewById(R.id.item_drill_title);
			holder.colored = (TextView) view.findViewById(R.id.item_drill_subcolored);
			holder.plain = (TextView) view.findViewById(R.id.item_drill_subplain);
			holder.contentLayout = view.findViewById(R.id.item_drill_contentlayout);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();
		
		if (view != null){
			Animation animation = new ScaleAnimation((float)1.0, (float)1.0 ,(float)0, (float)1.0);
			animation.setDuration(300);
			view.startAnimation(animation);
			animation = null;
		}

		return view; 
	}

	private class ViewHolder {
		public ImageView arrow;
		public TextView title;
		public TextView colored;
		public TextView plain;
		public View contentLayout;
	}


}
