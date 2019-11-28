package com.sap.inspection.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class BaseFragmentAdapter extends FragmentPagerAdapter{

    protected ArrayList<Fragment> fragmentList;
    protected FragmentManager fragManager;
	public BaseFragmentAdapter(FragmentManager fm) {
		super(fm);
		this.fragManager = fm;
	}

	@Override
	public Fragment getItem(int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}
	
	public void rebuildItem(int position){
		fragManager.beginTransaction()
			.detach(fragmentList.get(position))
			.attach(fragmentList.get(position))
			.commitAllowingStateLoss();
	}
	
	public void removeAllItems(){
		for(Fragment x : fragmentList){
			try {
				fragManager.beginTransaction().remove(x).commitAllowingStateLoss();
			} catch (Exception e) {
			}
		}
	}
	
	public int getCurrentSelected(){
		int selection = 0;
		for(Fragment d : fragmentList){
			if(d.isVisible() && d.isResumed()){
				break;
			}
			selection++;
		}
		return selection;
	}

}
