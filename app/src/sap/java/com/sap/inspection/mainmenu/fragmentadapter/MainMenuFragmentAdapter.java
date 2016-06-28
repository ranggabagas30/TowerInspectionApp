package com.sap.inspection.mainmenu.fragmentadapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View.OnClickListener;

import com.sap.inspection.BaseFragmentAdapter;
import com.sap.inspection.mainmenu.MainMenuPageFragment;
import com.sap.inspection.tools.DebugLog;

import java.util.ArrayList;
import java.util.List;

public class MainMenuFragmentAdapter extends BaseFragmentAdapter{
	private ArrayList<Integer> iconRes;
	private ArrayList<Integer> titleRes;
	private final int SUM_ITEM_PER_PAGE = 6;

	public MainMenuFragmentAdapter(FragmentManager fm, ArrayList<Integer> iconRes, ArrayList<Integer> titleRes, OnClickListener mainMenuClickListener) {
		super(fm);
		this.iconRes = iconRes;
		this.titleRes = titleRes;
		fragmentList = new ArrayList<Fragment>();
		for (int i = 0; i < getSumPage(); i++) {
			fragmentList.add(MainMenuPageFragment.newInstance(getTitleRes(i), getIconsRes(i), mainMenuClickListener));
		}
	}

	private int getSumPage(){
		int sum = titleRes.size() / SUM_ITEM_PER_PAGE;
		if (titleRes.size() % SUM_ITEM_PER_PAGE != 0)
			sum++;
		return sum;
	}
	
	private ArrayList<Integer> getIconsRes(int pagePosition){
		return getRes(iconRes, pagePosition);
	}
	
	private ArrayList<Integer> getTitleRes(int pagePosition){
		return getRes(titleRes, pagePosition);
	}
	
	private ArrayList<Integer> getRes(ArrayList<Integer> resAll, int pagePosition){
		int endIndex = getEndIndex(pagePosition) < resAll.size() ? getEndIndex(pagePosition) : resAll.size();
		DebugLog.d("start : "+getStartIndex(pagePosition)+" end : "+endIndex);
		List<Integer> list = resAll.subList(getStartIndex(pagePosition), endIndex);
		return new ArrayList<Integer>(list);
	}
	
	private int getStartIndex(int pagePosition){
		return pagePosition * SUM_ITEM_PER_PAGE;
	}
	
	private int getEndIndex(int pagePosition){
		return (pagePosition + 1) * SUM_ITEM_PER_PAGE;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		StringBuilder builder = new StringBuilder();
		builder.append(position);
		builder.append("/");
		builder.append(iconRes.size() / SUM_ITEM_PER_PAGE + 1);
		return builder.toString();
	}
	
	
}