/*
 *  MatchApplication.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Parent Activity for the three fragment screens
 *  SETUP, RECORD and REVIEW 
 *  This class sets up a view pager and menu tabs and loads up the
 *  three fragments so that they are swipeable
 *  
 *  Written on: Jan 2013
 *  
 *  references code from Android Reference
 *	http://developer.android.com/reference/android/support/v4/view/ViewPager.html
 *
 *  
 *  
 */
package fm.matchstats;

import fm.matchstats.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MatchApplication extends FragmentActivity {

	private TabsAdapterfm mTabsAdapter;
	private ViewPager mViewPager;
	private String tabFragmentRecord, tabFragmentReview;
	private MatchRecordFragment fragmentRecord;
	private MatchReviewFragment fragmentReview;
	private Bundle bundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set up 3 fragment screens
		// initialise view and view pager
		setContentView(R.layout.tabs_layout);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);

		// set up tabs display in action bar
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		// start up the 3 fragments
		// uses code from Android Reference
		// http://developer.android.com/reference/android/support/v4/view/ViewPager.html
		mTabsAdapter = new TabsAdapterfm(super.getSupportFragmentManager(),
				this, mViewPager);
		mTabsAdapter.addTab(actionBar.newTab().setText("setup"),
				MatchSetupFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("record"),
				MatchRecordFragment.class, null);
		mTabsAdapter.addTab(actionBar.newTab().setText("review"),
				MatchReviewFragment.class, null);

		// if restarting program, return to the last active tab/fragment
		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt(
					"tab", 0));
		}
	}

	@Override
	// save which fragment is active as you close so that you can return
	// to it when restarting
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	// this method is called by the RECORD fragment (MatchRecordFragment) which
	// passes its tag name into here once it starts up. This parent Activity 
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentRecord(String t) {
		tabFragmentRecord = t;
		fragmentRecord = (MatchRecordFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentRecord);
	}

	// method returns the tag name of the active RECORD fragment
	public String getTagFragmentRecord() {
		return tabFragmentRecord;
	}

	// method called by SETUP and REVIEW fragments to get a reference to the RECORD
	// fragment so that they can make method calls/access variables
	public MatchRecordFragment getFragmentRecord() {
		if (fragmentRecord == null)
			fragmentRecord = (MatchRecordFragment) this
					.getSupportFragmentManager().findFragmentByTag(
							tabFragmentRecord);
		return fragmentRecord;
	}


	// this method is called by the REVIEW fragment (MatchReviewFragment) which
	// passes its tag name into here once it starts up. This parent Activity 
	// can use that tag name to create a reference to the fragment
	public void setTagFragmentReview(String t) {
		tabFragmentReview = t;
		fragmentReview = (MatchReviewFragment) this.getSupportFragmentManager()
				.findFragmentByTag(tabFragmentReview);
	}

	// method returns the tag name of the active REVIEW fragment
	public String getTagFragmentReview() {
		return tabFragmentReview;
	}

	// method called by SETUP and RECORD fragments to get a reference to the REVIEW
	// fragment so that they can make method calls/access variables
	public MatchReviewFragment getFragmentReview() {
		if (fragmentReview == null)
			fragmentReview = (MatchReviewFragment) this
					.getSupportFragmentManager().findFragmentByTag(
							tabFragmentReview);
		return fragmentReview;
	}

	// set up help menu in action bar
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuAction = getMenuInflater();
		menuAction.inflate(R.menu.help_menu, menu);
		return true;
	}

	// show help based on which screen is active
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent ihelp;
		switch (getActionBar().getSelectedNavigationIndex()) {
		case 0:
			ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.setupHelp);
			startActivity(ihelp);
			return true;
		case 1:
			ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.recordHelp);
			startActivity(ihelp);
			return true;
		case 2:
			ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.reviewHelp);
			startActivity(ihelp);
			return true;
		default:
			ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.startHelp);
			startActivity(ihelp);
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
