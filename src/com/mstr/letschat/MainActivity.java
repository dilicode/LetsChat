package com.mstr.letschat;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.mstr.letschat.fragments.ContactListFragment;

public class MainActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab()
				.setText(R.string.tab_letschat)
				.setTabListener(new TabListener<ContactListFragment>(this, getResources().getString(R.string.tab_letschat), ContactListFragment.class)));
	}
	
	private static class TabListener<T extends Fragment> implements ActionBar.TabListener {
		private Activity activity;
		private String tag;
		private Class<T> clazz;
		private Fragment fragment;
		
		public TabListener(Activity activity, String tag, Class<T> clazz) {
			this.activity = activity;
			this.tag = tag;
			this.clazz = clazz;
			
			fragment = activity.getFragmentManager().findFragmentByTag(tag);
			if (fragment != null && !fragment.isDetached()) {
				activity.getFragmentManager().beginTransaction().detach(fragment).commit();
			}
		}
		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if (fragment == null) {
				fragment = Fragment.instantiate(activity, clazz.getName());
				ft.add(android.R.id.content, fragment, tag);
			} else {
				ft.attach(fragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (fragment != null) {
				ft.detach(fragment);
			}
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {}
	}
}