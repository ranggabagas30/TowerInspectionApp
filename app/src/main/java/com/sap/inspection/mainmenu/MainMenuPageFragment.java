package com.sap.inspection.mainmenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arifariyan.baseassets.fragment.BaseFragment;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.R;

import java.util.ArrayList;

public class MainMenuPageFragment extends BaseFragment {
	private ArrayList<Integer> titles;
	private ArrayList<Integer> icons;
	private int[] ids = {R.id.s1,R.id.s2,R.id.s3,R.id.s4,R.id.s5,R.id.s6};
	private OnClickListener mainMenuClickListener;
	
	public static MainMenuPageFragment newInstance(ArrayList<Integer> titles, ArrayList<Integer> icons, OnClickListener mainMenuClickListener) {
		MainMenuPageFragment fragment = new MainMenuPageFragment();
		fragment.titles = titles;
		fragment.icons = icons;
		fragment.mainMenuClickListener = mainMenuClickListener;
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_mainmenu_page, null, false);
		initUserName(root);
		for (int i = 0; i < titles.size(); i++) {
			initView(root, ids[i], i);
		}
		return root;
	}

	private void initUserName(View root){
		TextView user = (TextView) root.findViewById(R.id.user);
		String userName = ((BaseActivity)getActivity()).getPreference(R.string.user_fullname, "");
		user.setText("Hi "+userName);
	}
	
	private void initView(View viewParent, int id, int position){
		View view = viewParent.findViewById(id);
		view.setOnClickListener(mainMenuClickListener);
		view.setTag(titles.get(position));
		ImageView imageView = (ImageView) view.findViewById(R.id.item_mainmenu_icon);
		imageView.setImageResource(icons.get(position));
		TextView textView = (TextView) view.findViewById(R.id.item_mainmenu_title);
		textView.setText(titles.get(position));
	}
	
}
