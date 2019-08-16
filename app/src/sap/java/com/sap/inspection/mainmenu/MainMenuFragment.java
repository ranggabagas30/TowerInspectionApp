package com.sap.inspection.mainmenu;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.sap.inspection.R;
import com.sap.inspection.view.ui.fragments.BaseTitleFragment;
import com.sap.inspection.mainmenu.fragmentadapter.MainMenuFragmentAdapter;

import java.util.ArrayList;

public class MainMenuFragment extends BaseTitleFragment {

	private ArrayList<Integer> titles;
	private ArrayList<Integer> icons;

	private OnClickListener mainMenuClickListener;
	private MainMenuFragmentAdapter adapter;
	private ViewPager viewPager;

	public static MainMenuFragment newInstance() {
		MainMenuFragment fragment = new MainMenuFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new MainMenuFragmentAdapter(getActivity().getSupportFragmentManager(), getIcons(), getTitles(), mainMenuClickListener);
	}

	@Override
	public View onGetLayout(LayoutInflater inflater) {
		View root = inflater.inflate(R.layout.fragment_mainmenu, null, false);
		viewPager = (ViewPager) root.findViewById(R.id.viewPager);
		viewPager.setAdapter(adapter);
		return root;
	}

	@Override
	public String getTitle() {
		return "Main Menu";
	}

	public void setMainMenuClickListener(OnClickListener mainMenuClickListener) {
		this.mainMenuClickListener = mainMenuClickListener;
	}

	private ArrayList<Integer> getIcons(){
		if (this.icons == null){
			int[] icons = {
					R.drawable.ic_schedule,
					R.drawable.ic_preventive,
					R.drawable.ic_corrective,
					R.drawable.ic_siteaudit,
					R.drawable.ic_imbas_petir,
					R.drawable.ic_settings,
					R.drawable.ic_hasilpm,
					R.drawable.foo// temporary icon for routing
			};
			this.icons = getArrayList(icons);
		}
		return this.icons;
	}

	private ArrayList<Integer> getTitles(){
		if (this.titles == null){
			int[] titles = {R.string.schedule,
					R.string.preventive,
					R.string.corrective,
					R.string.site_audit,
					R.string.foto_imbas_petir,
					R.string.settings,
					R.string.hasil_PM,
					R.string.routing
			};
			this.titles = getArrayList(titles);
		}
		return this.titles;
	}
	
	private ArrayList<Integer> getArrayList(int[] array){
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for (Integer integer : array) {
			arrayList.add(integer);
		}
		return arrayList;
	}

}
