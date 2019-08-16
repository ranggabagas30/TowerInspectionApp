package com.sap.inspection.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Vector;

public class FragmentsAdapter extends FragmentPagerAdapter {

	protected Vector<Fragment> fragments;
	
	public FragmentsAdapter(FragmentManager fm) {
		super(fm);
		fragments = new Vector<Fragment>();
	}

	@Override
	public Fragment getItem(int index) {
		return fragments.get(index);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	public void addItem(Fragment fragment){
		fragments.add(fragment);
	}

}
