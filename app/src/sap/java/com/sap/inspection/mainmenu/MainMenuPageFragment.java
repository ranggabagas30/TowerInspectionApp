package com.sap.inspection.mainmenu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.inspection.view.ui.BaseActivity;
import com.sap.inspection.R;
import com.sap.inspection.view.ui.fragments.BaseFragment;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;

public class MainMenuPageFragment extends BaseFragment {
	private final String KEY_TITLES = "KEY_TITLES";
	private final String KEY_ICONS  = "KEY_ICONS";

	private ArrayList<Integer> titles;
	private ArrayList<Integer> icons;
	private int[] ids = {R.id.s1,R.id.s2,R.id.s3,R.id.s4,R.id.s5,R.id.s6,R.id.s7,R.id.s8};
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
		View root = inflater.inflate(R.layout.fragment_mainmenu_page_2, null, false);
		initUserName(root);

		if (savedInstanceState != null) {
			DebugLog.d("Load saved instance state");
			titles = savedInstanceState.getIntegerArrayList(KEY_TITLES);
			icons  = savedInstanceState.getIntegerArrayList(KEY_ICONS);
		}

		DebugLog.d("titles size : " + titles.size());
		for (int i = 0; i < titles.size(); i++) {
			initView(root, ids[i], i);
		}
		return root;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(KEY_TITLES, titles);
		outState.putIntegerArrayList(KEY_ICONS, icons);
	}

	private void initUserName(View root){
		TextView user = root.findViewById(R.id.user);
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
