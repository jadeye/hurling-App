/*
 *  MatchReviewFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to display match score and match statistics data summary. 
 *  Also can start activities to view detailed tables of match statistics
 *  
 * store data to database tables and pass relevant details into MatchRecordReview
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats;

import fm.matchstats.db.run.FreesListActivity;
import fm.matchstats.db.run.PuckOutsListActivity;
import fm.matchstats.db.run.ShotsListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MatchReviewFragment extends Fragment {
	private int homeGoals, homePoints, homeTotal, oppGoals, oppPoints,
			oppTotal;
	private TextView tHomeGoals, tHomePoints, tHomeTotal, tOppGoals,
			tOppPoints;
	private TextView tShotsTotalHome;
	private TextView tShotGoalsHome, tShotGoalsHomePerCent;
	private TextView tShotWidesHome, tShotWidesHomePerCent;
	private TextView tShotPointsHome, tShotPointsHomePerCent;
	private TextView tShot45sHome, tShot45sHomePerCent;
	private TextView tShotSavedHome, tShotSavedHomePerCent;
	private TextView tShotShortHome, tShotShortHomePerCent;
	private TextView tShotPostsHome, tShotPostsHomePerCent;

	private TextView tShotGoalsPlayHome, tShotGoalsPlayHomePerCent;
	private TextView tShotPointsPlayHome, tShotPointsPlayHomePerCent;
	private TextView tShotGoalsPlayOpp, tShotGoalsPlayOppPerCent;
	private TextView tFreeWonHome, tFreeWonHomePerCent;
	private TextView tFreeWonHomeOwn, tFreeWonHomeOwnPerCent;
	private TextView tFreeWonHomeOpp, tFreeWonHomeOppPerCent;
	private TextView tFreeWonOpp, tFreeWonOppPerCent;
	private TextView tFreeWonOppOwn, tFreeWonOppOwnPerCent;
	private TextView tFreeWonOppOpp, tFreeWonOppOppPerCent;
	private TextView tShotPointsPlayOpp, tShotPointsPlayOppPerCent;
	private TextView tPuckWonCleanHome, tPuckWonCleanHomePerCent;
	private TextView tPuckLostCleanHome, tPuckLostCleanHomePerCent;
	private TextView tPuckWonBreakHome, tPuckWonBreakHomePerCent;
	private TextView tPuckLostBreakHome, tPuckLostBreakHomePerCent;
	private TextView tPuckOtherHome, tPuckOtherHomePerCent;
	private TextView tPuckWonCleanOpp, tPuckWonCleanOppPerCent;
	private TextView tPuckLostCleanOpp, tPuckLostCleanOppPerCent;
	private TextView tPuckWonBreakOpp, tPuckWonBreakOppPerCent;
	private TextView tPuckLostBreakOpp, tPuckLostBreakOppPerCent;
	private TextView tPuckOtherOpp, tPuckOtherOppPerCent;
	private TextView tOwnTeam, tOppTeam;
	private String sOwnTeam, sOppTeam;

	private int shotHomeTotal = 0, shotGoalsHome = 0, shotPointsHome = 0;
	private int shotGoalsPlayHome = 0, shotPointsPlayHome = 0;
	private int shotGoalsPlayOpp = 0, shotPointsPlayOpp = 0;
	private int shotWidesHome = 0, shot45sHome = 0, shotSavedHome = 0,
			shotShortHome = 0, shotPostsHome = 0;
	private int shotOpp = 0, shotHome = 0;
	private int freeWonHome = 0, freeWonHomeOwn = 0, freeWonHomeOpp = 0;
	private int freeWonOpp = 0, freeWonOppOwn = 0, freeWonOppOpp = 0;
	private int freeTotal = 0;
	int puckWonCleanHome = 0, puckWonCleanHomePerCent = 0;
	int puckLostCleanHome = 0, puckLostCleanHomePerCent = 0;
	int puckWonBreakHome = 0, puckWonBreakHomePerCent = 0;
	int puckLostBreakHome = 0, puckLostBreakHomePerCent = 0;
	int puckWonCleanOpp = 0, puckWonCleanOppPerCent = 0;
	int puckLostCleanOpp = 0, puckLostCleanOppPerCen = 0;
	int puckWonBreakOpp = 0, puckWonBreakOppPerCent = 0;
	int puckLostBreakOpp = 0, puckLostBreakOppPerCent = 0;
	int puckOutTotalHome = 0, puckOutTotalOpp = 0;
	int puckOtherHome = 0, puckOtherHomePerCent = 0;
	int puckOtherOpp = 00, puckOtherOppPerCent = 0;

	private TextView tOppTotal, tShotsTotalOpp;
	private TextView tShotGoalsOpp, tShotGoalsOppPerCent;
	private TextView tShotWidesOpp, tShotWidesOppPerCent;
	private TextView tShotPointsOpp, tShotPointsOppPerCent;
	private TextView tShot45sOpp, tShot45sOppPerCent;
	private TextView tShotSavedOpp, tShotSavedOppPerCent;
	private TextView tShotShortOpp, tShotShortOppPerCent;
	private TextView tShotPostsOpp, tShotPostsOppPerCent;
	private int shotOppTotal = 0, shotGoalsOpp = 0, shotPointsOpp = 0;
	private int shotWidesOpp = 0, shot45sOpp = 0, shotSavedOpp = 0,
			shotShortOpp = 0, shotPostsOpp = 0;

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.review_frag_layout, container, false);

		// Open up shared preferences file to read in persisted data on startup
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);

		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name
		String myTag = getTag();
		((MatchApplication) getActivity()).setTagFragmentReview(myTag);

		// set up text buttons edittexts etc.
		tOwnTeam = (TextView) v.findViewById(R.id.textViewRevHome);
		tOppTeam = (TextView) v.findViewById(R.id.textViewRevOpp);

		tOwnTeam.setText(sharedPref.getString("OURTEAM", "OWN TEAM"));
		tOppTeam.setText(sharedPref.getString("OPPTEAM", "OPPOSITION"));

		tHomeGoals = (TextView) v.findViewById(R.id.tVHomeGoals);
		tHomePoints = (TextView) v.findViewById(R.id.tVHomePoints);
		tHomeTotal = (TextView) v.findViewById(R.id.tVHomeTotal);
		tOppGoals = (TextView) v.findViewById(R.id.tVOppGoals);
		tOppPoints = (TextView) v.findViewById(R.id.tVOppPoints);
		tOppTotal = (TextView) v.findViewById(R.id.tVOppTotal);

		tShotsTotalHome = (TextView) v.findViewById(R.id.tVwShotsTotalNo);
		tShotGoalsHome = (TextView) v.findViewById(R.id.tVwShotsGoalsNo);
		tShotGoalsHomePerCent = (TextView) v.findViewById(R.id.tVwShotsGoalsP);
		tShotPointsHome = (TextView) v.findViewById(R.id.tVwShotsPointsNo);
		tShotPointsHomePerCent = (TextView) v
				.findViewById(R.id.tVwShotsPointsP);
		tShotWidesHome = (TextView) v.findViewById(R.id.tVwShotsWidesNo);
		tShotWidesHomePerCent = (TextView) v.findViewById(R.id.tVwShotsWidesP);
		tShot45sHome = (TextView) v.findViewById(R.id.tVwShots45sNo);
		tShot45sHomePerCent = (TextView) v.findViewById(R.id.tVwShots45sP);
		tShotSavedHome = (TextView) v.findViewById(R.id.tVwShotsSavedNo);
		tShotSavedHomePerCent = (TextView) v.findViewById(R.id.tVwShotsSavedP);
		tShotShortHome = (TextView) v.findViewById(R.id.tVwShotsShortNo);
		tShotShortHomePerCent = (TextView) v.findViewById(R.id.tVwShotsShortP);
		tShotPostsHome = (TextView) v.findViewById(R.id.tVwShotsPostsNo);
		tShotPostsHomePerCent = (TextView) v.findViewById(R.id.tVwShotsPostsP);

		tShotGoalsPlayHome = (TextView) v.findViewById(R.id.tVGoalsPlay);
		tShotGoalsPlayHomePerCent = (TextView) v
				.findViewById(R.id.tVGoalsPlayP);
		tShotPointsPlayHome = (TextView) v.findViewById(R.id.tVPointsPlay);
		tShotPointsPlayHomePerCent = (TextView) v
				.findViewById(R.id.tVPointsPlayP);
		tShotGoalsPlayOpp = (TextView) v.findViewById(R.id.tVGoalsOppPlay);
		tShotGoalsPlayOppPerCent = (TextView) v
				.findViewById(R.id.tVGoalsOppPlayP);
		tShotPointsPlayOpp = (TextView) v.findViewById(R.id.tVPointsOppPlay);
		tShotPointsPlayOppPerCent = (TextView) v
				.findViewById(R.id.tVPointsOppPlayP);

		tShotsTotalOpp = (TextView) v.findViewById(R.id.tVwShotsTotalOppNo);
		tShotGoalsOpp = (TextView) v.findViewById(R.id.tVwShotsGoalsOppNo);
		tShotGoalsOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsGoalsOppP);
		tShotPointsOpp = (TextView) v.findViewById(R.id.tVwShotsPointsOppNo);
		tShotPointsOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsPointsOppP);
		tShotWidesOpp = (TextView) v.findViewById(R.id.tVwShotsWidesOppNo);
		tShotWidesOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsWidesOppP);
		tShot45sOpp = (TextView) v.findViewById(R.id.tVwShots45sOppNo);
		tShot45sOppPerCent = (TextView) v.findViewById(R.id.tVwShots45sOppP);
		tShotSavedOpp = (TextView) v.findViewById(R.id.tVwShotsSavedOppNo);
		tShotSavedOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsSavedOppP);
		tShotShortOpp = (TextView) v.findViewById(R.id.tVwShotsShortOppNo);
		tShotShortOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsShortOppP);
		tShotPostsOpp = (TextView) v.findViewById(R.id.tVwShotsPostsOppNo);
		tShotPostsOppPerCent = (TextView) v
				.findViewById(R.id.tVwShotsPostsOppP);

		// Set up output for frees
		tFreeWonHome = (TextView) v.findViewById(R.id.tVwFreeWonHome);
		tFreeWonHomePerCent = (TextView) v.findViewById(R.id.tVwFreeWonHomeP);
		tFreeWonHomeOwn = (TextView) v.findViewById(R.id.tVwFreeWonHomeOwn);
		tFreeWonHomeOwnPerCent = (TextView) v
				.findViewById(R.id.tVwFreeWonHomeOwnP);
		tFreeWonHomeOpp = (TextView) v.findViewById(R.id.tVwFreeWonHomeOpp);
		tFreeWonHomeOppPerCent = (TextView) v
				.findViewById(R.id.tVwFreeWonHomeOppP);
		tFreeWonOpp = (TextView) v.findViewById(R.id.tVwFreeWonOpp);
		tFreeWonOppPerCent = (TextView) v.findViewById(R.id.tVwFreeWonOppP);
		tFreeWonOppOwn = (TextView) v.findViewById(R.id.tVwFreeWonOppOwn);
		tFreeWonOppOwnPerCent = (TextView) v
				.findViewById(R.id.tVwFreeWonOppOwnP);
		tFreeWonOppOpp = (TextView) v.findViewById(R.id.tVwFreeWonOppOpp);
		tFreeWonOppOppPerCent = (TextView) v
				.findViewById(R.id.tVwFreeWonOppOppP);

		// Set up output for puckouts
		tPuckWonCleanHome = (TextView) v.findViewById(R.id.tVwPuckWonCleanHome);
		tPuckWonCleanHomePerCent = (TextView) v
				.findViewById(R.id.tVwPuckWonCleanHomeP);
		tPuckLostCleanHome = (TextView) v
				.findViewById(R.id.tVPuckLostCleanHome);
		tPuckLostCleanHomePerCent = (TextView) v
				.findViewById(R.id.tVwPuckLostCleanHomeP);
		tPuckWonBreakHome = (TextView) v.findViewById(R.id.tVPuckWonBreakHome);
		tPuckWonBreakHomePerCent = (TextView) v
				.findViewById(R.id.tVwPuckWonBreakHomeP);
		tPuckLostBreakHome = (TextView) v
				.findViewById(R.id.tVPuckLostBreakHome);
		tPuckLostBreakHomePerCent = (TextView) v
				.findViewById(R.id.tVwPuckLostBreakHomeP);
		tPuckOtherHome = (TextView) v.findViewById(R.id.tVwPuckOtherHome);
		tPuckOtherHomePerCent = (TextView) v
				.findViewById(R.id.tVwPuckOtherHomeP);
		tPuckWonCleanOpp = (TextView) v.findViewById(R.id.tVwPuckWonCleanOpp);
		tPuckWonCleanOppPerCent = (TextView) v
				.findViewById(R.id.tVwPuckWonCleanOppP);
		tPuckLostCleanOpp = (TextView) v.findViewById(R.id.tVPuckLostCleanOpp);
		tPuckLostCleanOppPerCent = (TextView) v
				.findViewById(R.id.tVwPuckLostCleanOppP);
		tPuckWonBreakOpp = (TextView) v.findViewById(R.id.tVPuckWonBreakOpp);
		tPuckWonBreakOppPerCent = (TextView) v
				.findViewById(R.id.tVwPuckWonBreakOppP);
		tPuckLostBreakOpp = (TextView) v.findViewById(R.id.tVPuckLostBreakOpp);
		tPuckLostBreakOppPerCent = (TextView) v
				.findViewById(R.id.tVwPuckLostBreakOppP);
		tPuckOtherOpp = (TextView) v.findViewById(R.id.tVwPuckOtherOpp);
		tPuckOtherOppPerCent = (TextView) v.findViewById(R.id.tVwPuckOtherOppP);

		// Read in score from persisted data
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		// update screen if persisted data exists
		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			settHomeGoals(homeGoals);
			settHomePoints(homePoints);
			settOppGoals(oppGoals);
			settOppPoints(oppPoints);
		}

		// setup shots/frees/puckouts values from persisted data
		shotGoalsHome = sharedPref.getInt("SHOTGOALSHOME", 0);
		shotPointsHome = sharedPref.getInt("SHOTPOINTSHOME", 0);
		shotWidesHome = sharedPref.getInt("SHOTWIDESHOME", 0);
		shot45sHome = sharedPref.getInt("SHOT45SHOME", 0);
		shotSavedHome = sharedPref.getInt("SHOTSAVEDHOME", 0);
		shotShortHome = sharedPref.getInt("SHOTSHORTHOME", 0);
		shotPostsHome = sharedPref.getInt("SHOTPOSTSHOME", 0);
		shotHome = sharedPref.getInt("SHOTHOME", 0);
		shotGoalsPlayHome = sharedPref.getInt("SHOTGOALSPLAYHOME", 0);
		shotPointsPlayHome = sharedPref.getInt("SHOTPOINTSPLAYHOME", 0);
		if (shotGoalsHome + shotPointsHome + shotWidesHome + shot45sHome
				+ shotSavedHome + shotShortHome + shotPostsHome + shotHome > 0) {
			// method to update screen display for home team shots
			updateShotHome();
		}

		shotGoalsOpp = sharedPref.getInt("SHOTGOALSOPP", 0);
		shotPointsOpp = sharedPref.getInt("SHOTPOINTSOPP", 0);
		shotWidesOpp = sharedPref.getInt("SHOTWIDESOPP", 0);
		shot45sOpp = sharedPref.getInt("SHOT45SOPP", 0);
		shotSavedOpp = sharedPref.getInt("SHOTSAVEDOPP", 0);
		shotShortOpp = sharedPref.getInt("SHOTSHORTOPP", 0);
		shotPostsOpp = sharedPref.getInt("SHOTPOSTSOPP", 0);
		shotOpp = sharedPref.getInt("SHOTOPP", 0);
		shotGoalsPlayOpp = sharedPref.getInt("SHOTGOALSPLAYOPP", 0);
		shotPointsPlayOpp = sharedPref.getInt("SHOTPOINTSPLAYOPP", 0);
		if (shotGoalsOpp + shotPointsOpp + shotWidesOpp + shot45sOpp
				+ shotSavedOpp + shotShortOpp + shotPostsOpp + shotOpp > 0) {
			// method to update screen display for opposition shots
			updateShotOpp();
		}

		freeWonHome = sharedPref.getInt("FREEWONHOME", 0);
		freeWonHomeOwn = sharedPref.getInt("FREEWONOWNHOME", 0);
		freeWonHomeOpp = sharedPref.getInt("FREEWONOPPHOME", 0);
		freeWonOpp = sharedPref.getInt("FREEWONOPP", 0);
		freeWonOppOwn = sharedPref.getInt("FREEWONOWNOPP", 0);
		freeWonOppOpp = sharedPref.getInt("FREEWONOPPOPP", 0);
		if (freeWonHome + freeWonHomeOwn + freeWonHomeOpp + freeWonOpp
				+ freeWonOppOwn + freeWonOppOpp > 0) {
			// method to update screen display for frees
			updateFree();
		}

		puckWonCleanHome = sharedPref.getInt("PUCKWONCLEANHOME", 0);
		puckLostCleanHome = sharedPref.getInt("PUCKLOSTCLEANHOME", 0);
		puckWonBreakHome = sharedPref.getInt("PUCKWONBREAKHOME", 0);
		puckLostBreakHome = sharedPref.getInt("PUCKLOSTBREAKHOME", 0);
		puckOtherHome = sharedPref.getInt("PUCKOTHERHOME", 0);
		if (puckWonCleanHome + puckLostCleanHome + puckWonBreakHome
				+ puckLostBreakHome + puckOtherHome > 0) {
			// method to update screen display for home puckouts
			updatePuckOutHome();
		}
		puckWonCleanOpp = sharedPref.getInt("PUCKWONCLEANOPP", 0);
		puckLostCleanOpp = sharedPref.getInt("PUCKLOSTCLEANOPP", 0);
		puckWonBreakOpp = sharedPref.getInt("PUCKWONBREAKOPP", 0);
		puckLostBreakOpp = sharedPref.getInt("PUCKLOSTBREAKOPP", 0);
		puckOtherOpp = sharedPref.getInt("PUCKOTHEROPP", 0);
		if (puckWonCleanOpp + puckLostCleanOpp + puckWonBreakOpp
				+ puckLostBreakOpp + puckOtherOpp > 0) {
			// method to update screen display for opposition puckouts
			updatePuckOutOpp();
		}

		// launch activity to look at shots table in database
		Button bShots = (Button) v.findViewById(R.id.revShots);
		bShots.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),
						ShotsListActivity.class);
				v.getContext().startActivity(intent);
			}
		});  

		// launch activity to look at frees table in database
		Button bFrees = (Button) v.findViewById(R.id.revFrees);
		bFrees.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),
						FreesListActivity.class);
				v.getContext().startActivity(intent);
			}
		});

		// launch activity to look at puckouts table in database
		Button bPuckOuts = (Button) v.findViewById(R.id.revPuckouts);
		bPuckOuts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),
						PuckOutsListActivity.class);
				v.getContext().startActivity(intent);
			}
		});

		return v;

	}

	// *******************Home Shots********************///
	// increment counters for home team shots
	public void addtShotGoalsHome(int i) {
		shotGoalsHome = shotGoalsHome + i;
		updateShotHome();
	}

	public void addtShotGoalsPlayHome(int i) {
		shotGoalsPlayHome = shotGoalsPlayHome + i;
		updateShotHome();
	}

	public void addtShotPointsHome(int i) {
		shotPointsHome = shotPointsHome + i;
		updateShotHome();
	}

	public void addtShotPointsPlayHome(int i) {
		shotPointsPlayHome = shotPointsPlayHome + i;
		updateShotHome();
	}

	public void addtShotWidesHome(int i) {
		shotWidesHome = shotWidesHome + i;
		updateShotHome();
	}

	public void addtShot45sHome(int i) {
		shot45sHome = shot45sHome + i;
		updateShotHome();
	}

	public void addtShotSavedHome(int i) {
		shotSavedHome = shotSavedHome + i;
		updateShotHome();
	}

	public void addtShotShortHome(int i) {
		shotShortHome = shotShortHome + i;
		updateShotHome();
	}

	public void addtShotPostsHome(int i) {
		shotPostsHome = shotPostsHome + i;
		updateShotHome();
	}

	public void addtShotHome(int i) {
		shotHome = shotHome + i;
		updateShotHome();
	}

	// method to update percentages and display home shots parameters
	public void updateShotHome() {
		shotHomeTotal = shotGoalsHome + shotPointsHome + shotWidesHome
				+ shot45sHome + shotSavedHome + shotShortHome + shotPostsHome
				+ shotHome;
		if (shotHomeTotal > 0) {
			tShotGoalsHomePerCent.setText(String.valueOf(shotGoalsHome * 100
					/ shotHomeTotal));
			tShotPointsHomePerCent.setText(String.valueOf(shotPointsHome * 100
					/ shotHomeTotal));
			tShotWidesHomePerCent.setText(String.valueOf(shotWidesHome * 100
					/ shotHomeTotal));
			tShot45sHomePerCent.setText(String.valueOf(shot45sHome * 100
					/ shotHomeTotal));
			tShotSavedHomePerCent.setText(String.valueOf(shotSavedHome * 100
					/ shotHomeTotal));
			tShotShortHomePerCent.setText(String.valueOf(shotShortHome * 100
					/ shotHomeTotal));
			tShotPostsHomePerCent.setText(String.valueOf(shotPostsHome * 100
					/ shotHomeTotal));
			tShotGoalsPlayHomePerCent.setText(String.valueOf(shotGoalsPlayHome
					* 100 / shotHomeTotal));
			tShotPointsPlayHomePerCent.setText(String
					.valueOf(shotPointsPlayHome * 100 / shotHomeTotal));
		} else {
			tShotGoalsHomePerCent.setText("0");
			tShotPointsHomePerCent.setText("0");
			tShotWidesHomePerCent.setText("0");
			tShot45sHomePerCent.setText("0");
			tShotSavedHomePerCent.setText("0");
			tShotShortHomePerCent.setText("0");
			tShotPostsHomePerCent.setText("0");
			tShotGoalsPlayHomePerCent.setText("0");
			tShotPointsPlayHomePerCent.setText("0");
		}

		tShotsTotalHome.setText(String.valueOf(shotHomeTotal));
		tShotGoalsHome.setText(String.valueOf(shotGoalsHome));
		tShotPointsHome.setText(String.valueOf(shotPointsHome));
		tShotWidesHome.setText(String.valueOf(shotWidesHome));
		tShot45sHome.setText(String.valueOf(shot45sHome));
		tShotSavedHome.setText(String.valueOf(shotSavedHome));
		tShotShortHome.setText(String.valueOf(shotShortHome));
		tShotPostsHome.setText(String.valueOf(shotPostsHome));
		tShotGoalsPlayHome.setText(String.valueOf(shotGoalsPlayHome));
		tShotPointsPlayHome.setText(String.valueOf(shotPointsPlayHome));
	}

	// *******************Opp Shots********************///
	// increment counters for opposition team shots
	public void addtShotGoalsOpp(int i) {
		shotGoalsOpp = shotGoalsOpp + i;
		updateShotOpp();
	}

	public void addtShotGoalsPlayOpp(int i) {
		shotGoalsPlayOpp = shotGoalsPlayOpp + i;
		updateShotOpp();
	}

	public void addtShotPointsOpp(int i) {
		shotPointsOpp = shotPointsOpp + i;
		updateShotOpp();
	}

	public void addtShotPointsPlayOpp(int i) {
		shotPointsPlayOpp = shotPointsPlayOpp + i;
		updateShotOpp();
	}

	public void addtShotWidesOpp(int i) {
		shotWidesOpp = shotWidesOpp + i;
		updateShotOpp();
	}

	public void addtShot45sOpp(int i) {
		shot45sOpp = shot45sOpp + i;
		updateShotOpp();
	}

	public void addtShotSavedOpp(int i) {
		shotSavedOpp = shotSavedOpp + i;
		updateShotOpp();
	}

	public void addtShotShortOpp(int i) {
		shotShortOpp = shotShortOpp + i;
		updateShotOpp();
	}

	public void addtShotPostsOpp(int i) {
		shotPostsOpp = shotPostsOpp + i;
		updateShotOpp();
	}

	public void addtShotOpp(int i) {
		shotOpp = shotOpp + i;
		updateShotOpp();
	}

	// method to update percentages and display opposition shots parameters
	public void updateShotOpp() {
		shotOppTotal = shotGoalsOpp + shotPointsOpp + shotWidesOpp + shot45sOpp
				+ shotSavedOpp + shotShortOpp + shotPostsOpp + shotOpp;
		if (shotOppTotal > 0) {
			tShotGoalsOppPerCent.setText(String.valueOf(shotGoalsOpp * 100
					/ shotOppTotal));
			tShotPointsOppPerCent.setText(String.valueOf(shotPointsOpp * 100
					/ shotOppTotal));
			tShotWidesOppPerCent.setText(String.valueOf(shotWidesOpp * 100
					/ shotOppTotal));
			tShot45sOppPerCent.setText(String.valueOf(shot45sOpp * 100
					/ shotOppTotal));
			tShotSavedOppPerCent.setText(String.valueOf(shotSavedOpp * 100
					/ shotOppTotal));
			tShotShortOppPerCent.setText(String.valueOf(shotShortOpp * 100
					/ shotOppTotal));
			tShotPostsOppPerCent.setText(String.valueOf(shotPostsOpp * 100
					/ shotOppTotal));
			tShotGoalsPlayOppPerCent.setText(String.valueOf(shotGoalsPlayOpp
					* 100 / shotOppTotal));
			tShotPointsPlayOppPerCent.setText(String.valueOf(shotPointsPlayOpp
					* 100 / shotOppTotal));
		} else {
			tShotGoalsOppPerCent.setText("0");
			tShotPointsOppPerCent.setText("0");
			tShotWidesOppPerCent.setText("0");
			tShot45sOppPerCent.setText("0");
			tShotSavedOppPerCent.setText("0");
			tShotShortOppPerCent.setText("0");
			tShotPostsOppPerCent.setText("0");
			tShotGoalsPlayOppPerCent.setText("0");
			tShotPointsPlayOppPerCent.setText("0");
		}

		tShotsTotalOpp.setText(String.valueOf(shotOppTotal));
		tShotGoalsOpp.setText(String.valueOf(shotGoalsOpp));
		tShotPointsOpp.setText(String.valueOf(shotPointsOpp));
		tShotWidesOpp.setText(String.valueOf(shotWidesOpp));
		tShot45sOpp.setText(String.valueOf(shot45sOpp));
		tShotSavedOpp.setText(String.valueOf(shotSavedOpp));
		tShotShortOpp.setText(String.valueOf(shotShortOpp));
		tShotPostsOpp.setText(String.valueOf(shotPostsOpp));
		tShotGoalsPlayOpp.setText(String.valueOf(shotGoalsPlayOpp));
		tShotPointsPlayOpp.setText(String.valueOf(shotPointsPlayOpp));
	}

	// ////////////////Update Free Section////////////////////////////
	// increment counters for frees
	public void addFreeWonHome(int i) {
		freeWonHome = freeWonHome + i;
		updateFree();
	}

	public void addFreeWonHomeOwn(int i) {
		freeWonHomeOwn = freeWonHomeOwn + i;
		updateFree();
	}

	public void addFreeWonHomeOpp(int i) {
		freeWonHomeOpp = freeWonHomeOpp + i;
		updateFree();
	}

	public void addFreeWonOpp(int i) {
		freeWonOpp = freeWonOpp + i;
		updateFree();
	}

	public void addFreeWonOppOwn(int i) {
		freeWonOppOwn = freeWonOppOwn + i;
		updateFree();
	}

	public void addFreeWonOppOpp(int i) {
		freeWonOppOpp = freeWonOppOpp + i;
		updateFree();
	}

	// method to update percentages and display frees parameters
	public void updateFree() {
		freeTotal = freeWonHome + freeWonOpp;
		tFreeWonHome.setText(String.valueOf(freeWonHome));
		tFreeWonHomeOwn.setText(String.valueOf(freeWonHomeOwn));
		tFreeWonHomeOpp.setText(String.valueOf(freeWonHomeOpp));
		tFreeWonOpp.setText(String.valueOf(freeWonOpp));
		tFreeWonOppOwn.setText(String.valueOf(freeWonOppOwn));
		tFreeWonOppOpp.setText(String.valueOf(freeWonOppOpp));
		if (freeTotal > 0) {
			tFreeWonHomePerCent.setText(String.valueOf(freeWonHome * 100
					/ freeTotal));
			tFreeWonHomeOwnPerCent.setText(String.valueOf(freeWonHomeOwn * 100
					/ freeTotal));
			tFreeWonHomeOppPerCent.setText(String.valueOf(freeWonHomeOpp * 100
					/ freeTotal));
			tFreeWonOppPerCent.setText(String.valueOf(freeWonOpp * 100
					/ freeTotal));
			tFreeWonOppOwnPerCent.setText(String.valueOf(freeWonOppOwn * 100
					/ freeTotal));
			tFreeWonOppOppPerCent.setText(String.valueOf(freeWonOppOpp * 100
					/ freeTotal));
		} else {
			tFreeWonHomePerCent.setText("0");
			tFreeWonHomeOwnPerCent.setText("0");
			tFreeWonHomeOppPerCent.setText("0");
			tFreeWonOppPerCent.setText("0");
			tFreeWonOppOwnPerCent.setText("0");
			tFreeWonOppOppPerCent.setText("0");
		}

	}

	// ////////////////Update PuckOuts Section////////////////////////////
	// increment counters for puck outs
	public void addPuckWonCleanHome(int i) {
		puckWonCleanHome = puckWonCleanHome + i;
		updatePuckOutHome();
	}

	public void addPuckLostCleanHome(int i) {
		puckLostCleanHome = puckLostCleanHome + i;
		updatePuckOutHome();
	}

	public void addPuckWonBreakHome(int i) {
		Log.e("review", "won break "+i);

		puckWonBreakHome = puckWonBreakHome + i;
		updatePuckOutHome();
	}

	public void addPuckLostBreakHome(int i) {
		puckLostBreakHome = puckLostBreakHome + i;
		updatePuckOutHome();
	}

	public void addPuckOtherHome(int i) {
		puckOtherHome = puckOtherHome + i;
		updatePuckOutHome();
	}

	public void addPuckWonCleanOpp(int i) {
		puckWonCleanOpp = puckWonCleanOpp + i;
		updatePuckOutOpp();
	}

	public void addPuckLostCleanOpp(int i) {
		puckLostCleanOpp = puckLostCleanOpp + i;
		updatePuckOutOpp();
	}

	public void addPuckWonBreakOpp(int i) {
		puckWonBreakOpp = puckWonBreakOpp + i;
		updatePuckOutOpp();
	}

	public void addPuckLostBreakOpp(int i) {
		puckLostBreakOpp = puckLostBreakOpp + i;
		updatePuckOutOpp();
	}

	public void addPuckOtherOpp(int i) {
		puckOtherOpp = puckOtherOpp + i;
		updatePuckOutOpp();
	}

	// method to update percentages and display puckout parameters
	public void updatePuckOutHome() {
		puckOutTotalHome = puckWonCleanHome + puckLostCleanHome
				+ puckWonBreakHome + puckLostBreakHome + puckOtherHome;
		tPuckWonCleanHome.setText(String.valueOf(puckWonCleanHome));
		tPuckLostCleanHome.setText(String.valueOf(puckLostCleanHome));
		tPuckWonBreakHome.setText(String.valueOf(puckWonBreakHome));
		tPuckLostBreakHome.setText(String.valueOf(puckLostBreakHome));
		tPuckOtherHome.setText(String.valueOf(puckOtherHome));
		if (puckOutTotalHome > 0) {
			tPuckWonCleanHomePerCent.setText(String.valueOf(puckWonCleanHome
					* 100 / puckOutTotalHome));
			tPuckLostCleanHomePerCent.setText(String.valueOf(puckLostCleanHome
					* 100 / puckOutTotalHome));
			tPuckWonBreakHomePerCent.setText(String.valueOf(puckWonBreakHome
					* 100 / puckOutTotalHome));
			tPuckLostBreakHomePerCent.setText(String.valueOf(puckLostBreakHome
					* 100 / puckOutTotalHome));
			tPuckOtherHomePerCent.setText(String.valueOf(puckOtherHome * 100
					/ puckOutTotalHome));
		} else {
			tPuckWonCleanHomePerCent.setText("0");
			tPuckLostCleanHomePerCent.setText("0");
			tPuckWonBreakHomePerCent.setText("0");
			tPuckLostBreakHomePerCent.setText("0");
			tPuckOtherHomePerCent.setText("0");
		}
	}

	// method to update percentages and display puckout parameters
	public void updatePuckOutOpp() {
		puckOutTotalOpp = puckWonCleanOpp + puckLostCleanOpp + puckWonBreakOpp
				+ puckLostBreakOpp + puckOtherOpp;
		tPuckWonCleanOpp.setText(String.valueOf(puckWonCleanOpp));
		tPuckLostCleanOpp.setText(String.valueOf(puckLostCleanOpp));
		tPuckWonBreakOpp.setText(String.valueOf(puckWonBreakOpp));
		tPuckLostBreakOpp.setText(String.valueOf(puckLostBreakOpp));
		tPuckOtherOpp.setText(String.valueOf(puckOtherOpp));
		if (puckOutTotalOpp > 0) {
			tPuckWonCleanOppPerCent.setText(String.valueOf(puckWonCleanOpp
					* 100 / puckOutTotalOpp));
			tPuckLostCleanOppPerCent.setText(String.valueOf(puckLostCleanOpp
					* 100 / puckOutTotalOpp));
			tPuckWonBreakOppPerCent.setText(String.valueOf(puckWonBreakOpp
					* 100 / puckOutTotalOpp));
			tPuckLostBreakOppPerCent.setText(String.valueOf(puckLostBreakOpp
					* 100 / puckOutTotalOpp));
			tPuckOtherOppPerCent.setText(String.valueOf(puckOtherOpp * 100
					/ puckOutTotalOpp));
		} else {
			tPuckWonCleanOppPerCent.setText("0");
			tPuckLostCleanOppPerCent.setText("0");
			tPuckWonBreakOppPerCent.setText("0");
			tPuckLostBreakOppPerCent.setText("0");
			tPuckOtherOppPerCent.setText("0");
		}
	}

	// ///////////UPDATE SCORES////////////////////////////
	// methods called from RECORD fragment to update score
	// and totals
	public void settHomeGoals(int i) {
		homeGoals = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomeGoals.setText(String.valueOf(homeGoals));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settHomePoints(int i) {
		homePoints = i;
		homeTotal = homeGoals * 3 + homePoints;
		tHomePoints.setText(String.valueOf(homePoints));
		tHomeTotal.setText(String.valueOf(homeTotal));
	}

	public void settOppGoals(int i) {
		oppGoals = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppGoals.setText(String.valueOf(oppGoals));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	public void settOppPoints(int i) {
		oppPoints = i;
		oppTotal = oppGoals * 3 + oppPoints;
		tOppPoints.setText(String.valueOf(oppPoints));
		tOppTotal.setText(String.valueOf(oppTotal));
	}

	// method to reset all stats values to zero
	public void resetStats() {
		shotGoalsHome = 0;
		shotPointsHome = 0;
		shotWidesHome = 0;
		shot45sHome = 0;
		shotSavedHome = 0;
		shotShortHome = 0;
		shotPostsHome = 0;
		shotHome = 0;
		shotGoalsPlayHome = 0;
		shotPointsPlayHome = 0;

		shotGoalsOpp = 0;
		shotPointsOpp = 0;
		shotWidesOpp = 0;
		shot45sOpp = 0;
		shotSavedOpp = 0;
		shotShortOpp = 0;
		shotPostsOpp = 0;
		shotOpp = 0;
		shotGoalsPlayOpp = 0;
		shotPointsPlayOpp = 0;

		freeWonHome = 0;
		freeWonHomeOwn = 0;
		freeWonHomeOpp = 0;
		freeWonOpp = 0;
		freeWonOppOwn = 0;
		freeWonOppOpp = 0;

		puckWonCleanHome = 0;
		puckLostCleanHome = 0;
		puckWonBreakHome = 0;
		puckLostBreakHome = 0;
		puckOtherHome = 0;
		puckWonCleanOpp = 0;
		puckLostCleanOpp = 0;
		puckWonBreakOpp = 0;
		puckLostBreakOpp = 0;
		puckOtherOpp = 0;

		if (shotHomeTotal > 0)
			updateShotHome();
		if (shotOppTotal > 0)
			updateShotOpp();
		if (freeTotal > 0)
			updateFree();
		if (puckOutTotalHome > 0)
			updatePuckOutHome();
		if (puckOutTotalOpp > 0)
			updatePuckOutOpp();

	}

	// ///////////////////////////END OF ONCREATE///////////////////////////
	@Override
	public void onPause() {
		// persist data out to shared preferences file to be available for start
		// up
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_review_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		
		editor.putString("OURTEAM", tOwnTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());

		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);

		editor.putInt("SHOTGOALSHOME", shotGoalsHome);
		editor.putInt("SHOTPOINTSHOME", shotPointsHome);
		editor.putInt("SHOTWIDESHOME", shotWidesHome);
		editor.putInt("SHOT45SHOME", shot45sHome);
		editor.putInt("SHOTSAVEDHOME", shotSavedHome);
		editor.putInt("SHOTSHORTHOME", shotShortHome);
		editor.putInt("SHOTPOSTSHOME", shotPostsHome);
		editor.putInt("SHOTHOME", shotHome);
		editor.putInt("SHOTGOALSPLAYHOME", shotGoalsPlayHome);
		editor.putInt("SHOTPOINTSPLAYHOME", shotPointsPlayHome);
		editor.putInt("SHOTGOALSOPP", shotGoalsOpp);
		editor.putInt("SHOTPOINTSOPP", shotPointsOpp);
		editor.putInt("SHOTWIDESOPP", shotWidesOpp);
		editor.putInt("SHOT45SOPP", shot45sOpp);
		editor.putInt("SHOTSAVEDOPP", shotSavedOpp);
		editor.putInt("SHOTSHORTOPP", shotShortOpp);
		editor.putInt("SHOTPOSTSOPP", shotPostsOpp);
		editor.putInt("SHOTOPP", shotOpp);
		editor.putInt("SHOTGOALSPLAYOPP", shotGoalsPlayOpp);
		editor.putInt("SHOTPOINTSPLAYOPP", shotPointsPlayOpp);

		editor.putInt("FREEWONHOME", freeWonHome);
		editor.putInt("FREEWONOWNHOME", freeWonHomeOwn);
		editor.putInt("FREEWONOPPHOME", freeWonHomeOpp);
		editor.putInt("FREEWONOPP", freeWonOpp);
		editor.putInt("FREEWONOWNOPP", freeWonOppOwn);
		editor.putInt("FREEWONOPPOPP", freeWonOppOpp);

		editor.putInt("PUCKWONCLEANHOME", puckWonCleanHome);
		editor.putInt("PUCKLOSTCLEANHOME", puckLostCleanHome);
		editor.putInt("PUCKWONBREAKHOME", puckWonBreakHome);
		editor.putInt("PUCKLOSTBREAKHOME", puckLostBreakHome);
		editor.putInt("PUCKOTHERHOME", puckOtherHome);
		editor.putInt("PUCKWONCLEANOPP", puckWonCleanOpp);
		editor.putInt("PUCKLOSTCLEANOPP", puckLostCleanOpp);
		editor.putInt("PUCKWONBREAKOPP", puckWonBreakOpp);
		editor.putInt("PUCKLOSTBREAKOPP", puckLostBreakOpp);
		editor.putInt("PUCKOTHEROPP", puckOtherOpp);
		
		

		editor.commit();
	}

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamNames(String homeTeam, String oppTeam) {
		if (!homeTeam.equals(""))
			tOwnTeam.setText(homeTeam);
		if (!oppTeam.equals(""))
			tOppTeam.setText(oppTeam);
	}
}
