package com.sap.inspection.mainmenu;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.sap.inspection.R;
import com.sap.inspection.mainmenu.fragmentadapter.MainMenuFragmentAdapter;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.ui.fragments.BaseTitleFragment;

import java.util.ArrayList;

/**
 * STP MainMenuFragment
 * */
public class MainMenuFragment extends BaseTitleFragment {
	private ArrayList<Integer> titles;
	private ArrayList<Integer> icons;

	private OnClickListener mainMenuClickListener;
	private MainMenuFragmentAdapter adapter;
	private ViewPager viewPager;

	public static MainMenuFragment newInstance() {
		return new MainMenuFragment();
	}

	@Override
	public View onGetLayout(LayoutInflater inflater) {
		DebugLog.d("onGetLayout");
		View root = inflater.inflate(R.layout.fragment_mainmenu, null, false);
		viewPager = root.findViewById(R.id.viewPager);
		adapter = new MainMenuFragmentAdapter(getChildFragmentManager(), getIcons(), getTitles(), mainMenuClickListener);
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

			int[] icons = { // 6 icons
					R.drawable.ic_schedule,
					R.drawable.ic_preventive,
					R.drawable.ic_corrective,
					R.drawable.ic_settings,
					R.drawable.fofo,
					R.drawable.ic_hasilpm
			};
			this.icons = getArrayList(icons);
		}
		return this.icons;
	}

	private ArrayList<Integer> getTitles(){
		if (this.titles == null){
			int[] titles = { // 6 titles
					R.string.schedule,
					R.string.preventive,
					R.string.corrective,
					R.string.settings,
					R.string.fiber_optic,
					R.string.hasil_PM
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
