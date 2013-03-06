/*
 *  MatchRecordFragment.java
 *
 *  Written by: Fintan Mahon 12101524
 *  
 *  Description: GUI to get input and display output for
 *  1. match timer
 *  2. match score
 *  3. match statistics
 *  
 * store data to database tables and pass relevant details into MatchReviewFragment
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import fm.matchstats.R;
import fm.matchstats.db.FreeContentProvider;
import fm.matchstats.db.MatchContentProvider;
import fm.matchstats.db.PanelContentProvider;
import fm.matchstats.db.PuckOutContentProvider;
import fm.matchstats.db.ShotContentProvider;

public class MatchRecordFragment extends Fragment {

	// declare and initialise variables
	public int minsPerHalf = 30;
	private int homeGoals = 0, homePoints = 0, oppGoals = 0, oppPoints = 0;
	private Timer timer;
	private TextView tHalf, tStartTime, tTimeGone, tTimeToGo, tTimeLeft;
	private TextView tHomeTotal, tUpDownDrawText, tHomeDifference, tOppTotal;
	private TextView tShotResult, tShotType, tShotPlayer, tShotPosn;
	private TextView tFreeReason, tFreePlayer, tFreePosn;
	private TextView tPuckOutReason, tPuckOutPlayer, tPuckOutPosn;
	private TextView tOurTeam, tOppTeam;
	private Button bResetTime, bResetScore, bResetStats;
	private Button bStartStop, bDecreaseTime, bIncreaseTime;
	private Button bDecHomeGoals, bHomeGoals, bHomePoints, bDecHomePoints;
	private Button bDecOppGoals, bOppGoals, bOppPoints, bDecOppPoints;
	private Button bShotHome, bShotOpp, bMinsPerHalf;
	private Button bShotHomeCommit, bShotOppCommit;
	private Button bFreeHome, bFreeCommitHome, bFreeCommitOpp, bFreeOpp;
	private Button bPuckOutHome, bPuckOutCommitHome, bPuckOutCommitOpp,
			bPuckOutOpp;
	private Button bUndo;
	private int SHOT_FREE_PUCK_OUT;
	private long matchID = -1; // flag to determine if match ID created in
								// database
	private String[] TEAM = new String[26];// stores output strings for saved
	// preferences

	private Handler h = new Handler();
	private long starttime = 0;
	private Date currentDate;
	private SimpleDateFormat sdf, sdftime;
	private AlertDialog alertshot = null, alertpitch = null;
	private String[] teamLineUp = new String[26];
	private String[] minsList;
	private String[] undoString = new String[6];
	private HashMap<String, Integer> playerIDLookUp = new HashMap<String, Integer>();
	// setup uri to read panel from database
	private Uri allTitles = PanelContentProvider.CONTENT_URI;
	private String[] projection = { PanelContentProvider.PANELID,
			PanelContentProvider.FIRSTNAME, PanelContentProvider.SURNAME,
			PanelContentProvider.NICKNAME };
	private ArrayList<String[]> undoList = new ArrayList<String[]>();
	private long rowId;

	@Override
	// start main method to display screen
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.record_frag_layout, container, false);

		// on very first start up after installation
		// if no player names from setup screen just use a number to indicate
		// position
		if (teamLineUp[1] == null) {
			for (int i = 1; i <= 15; i++) {
				teamLineUp[i] = String.valueOf(i);
			}
		}

		// open sharedpreferences file to read in saved persisted data on
		// startup
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);

		// get the tag name of this Fragment and pass it up to the parent
		// activity MatchApplication so that this Fragment may be accessed
		// by other fragments through using a reference created from tag name
		String myTag = getTag();
		((MatchApplication) getActivity()).setTagFragmentRecord(myTag);

		// set up text buttons edittexts etc.
		tHalf = (TextView) v.findViewById(R.id.which_half);
		tStartTime = (TextView) v.findViewById(R.id.start_time);
		tTimeGone = (TextView) v.findViewById(R.id.time_gone);
		tTimeToGo = (TextView) v.findViewById(R.id.time_to_go);
		tTimeLeft = (TextView) v.findViewById(R.id.time_left);
		tHomeTotal = (TextView) v.findViewById(R.id.home_total);
		tUpDownDrawText = (TextView) v.findViewById(R.id.up_down_draw_text);
		tHomeDifference = (TextView) v.findViewById(R.id.home_difference);
		tOppTotal = (TextView) v.findViewById(R.id.opp_total);
		tShotResult = (TextView) v.findViewById(R.id.textViewShotResult);
		tShotType = (TextView) v.findViewById(R.id.textViewShotType);
		tShotPlayer = (TextView) v.findViewById(R.id.textViewShotPlayer);
		tShotPosn = (TextView) v.findViewById(R.id.textViewShotPosn1);
		tFreeReason = (TextView) v.findViewById(R.id.textViewFreeType);
		tFreePlayer = (TextView) v.findViewById(R.id.textViewFreePlayer);
		tFreePosn = (TextView) v.findViewById(R.id.textViewFreePosn);
		tPuckOutReason = (TextView) v.findViewById(R.id.textViewPuckType);
		tPuckOutPlayer = (TextView) v.findViewById(R.id.textViewPuckPlayer);
		tOurTeam = (TextView) v.findViewById(R.id.ourTeam);
		tOppTeam = (TextView) v.findViewById(R.id.oppTeam);

		bStartStop = (Button) v.findViewById(R.id.start_stop_timer);
		bDecreaseTime = (Button) v.findViewById(R.id.decrease_timer);
		bIncreaseTime = (Button) v.findViewById(R.id.increase_timer);
		bResetTime = (Button) v.findViewById(R.id.reset_timer);

		bDecHomeGoals = (Button) v.findViewById(R.id.dec_home_goals);
		bHomeGoals = (Button) v.findViewById(R.id.home_goals);
		bHomePoints = (Button) v.findViewById(R.id.home_points);
		bDecHomePoints = (Button) v.findViewById(R.id.dec_home_points);
		bDecOppGoals = (Button) v.findViewById(R.id.dec_opp_goals);
		bOppGoals = (Button) v.findViewById(R.id.opp_goals);
		bOppPoints = (Button) v.findViewById(R.id.opp_points);
		bDecOppPoints = (Button) v.findViewById(R.id.dec_opp_points);
		bResetScore = (Button) v.findViewById(R.id.reset_score);
		bResetStats = (Button) v.findViewById(R.id.reset_stats);
		bUndo = (Button) v.findViewById(R.id.buttonUndo);
		// bUndo.setOnClickListener(undoOnClickListener);

		// //////////////////////set Team Names//////////////////////////
		// use persisted data if it exists else use default data
		tOurTeam.setText(sharedPref.getString("OURTEAM", "OWN TEAM"));
		tOppTeam.setText(sharedPref.getString("OPPTEAM", "OPPOSITION"));

		// //////////set up array of names to persist team lineup////////
		// and read in saved lineup if it exists. Default to position number
		// if there are no saved names
		TEAM[0] = "T00";
		for (int i = 1; i <= 25; i++) {
			TEAM[i] = "T" + String.format("%02d", i);
			teamLineUp[i] = sharedPref.getString(TEAM[i], String.valueOf(i));
		}

		// /////////////////////load MATCHID flag//////////////////////////
		// load matchID to use as foreign key in database saves. Defaults
		// to -1 if an actual value does not exist
		matchID = sharedPref.getLong("MATCHID", -1);

		// ///////////////////MINUTES PER HALF SECTION////////////////////////
		bMinsPerHalf = (Button) v.findViewById(R.id.mins_per_half);
		// set mins per half from saved value if it exists, else default to 30
		bMinsPerHalf.setText(String.valueOf(sharedPref
				.getInt("MINSPERHALF", 30)));
		minsPerHalf = sharedPref.getInt("MINSPERHALF", 30);
		// set click listener for mins per half button
		bMinsPerHalf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View w) {
				Button b = (Button) w;
				// read list of allowable times from array in assets and put in
				// adapter to display in alertdialog for selection
				minsList = getResources().getStringArray(R.array.minsPerHalf);
				ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
						getActivity(), R.layout.single_row_layout, minsList);
				new AlertDialog.Builder(getActivity())
						.setTitle("set minutes per half")
						.setAdapter(adapter1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// convert string input to integer
										minsPerHalf = Integer
												.valueOf(minsList[which]);
										// put new value on button
										bMinsPerHalf.setText(minsList[which]);
										dialog.dismiss();
									}
								}).create().show();
			}
		});

		// ///////////////////////////TIMER SETUP///////////////////////////
		// retrieve saved value if its there
		starttime = sharedPref.getLong("STARTTIME", 0);
		String[] str = new String[2];// stores display text for 1st/2nd half
		// set text on screen according to whether in first half or 2nd half
		// and whether timer is running or not
		if ((sharedPref.getString("TIMERBUTTON", "start") == "stop")
				&& (sharedPref.getString("HALFTEXT", "START FIRST HALF") == "IN FIRST HALF")) {
			str = settTimer("start", "START FIRST HALF");
			bStartStop.setText(str[0]);
			tHalf.setText(str[1]);
		} else if ((sharedPref.getString("TIMERBUTTON", "start") == "stop")
				&& (sharedPref.getString("HALFTEXT", "START FIRST HALF") == "IN SECOND HALF")) {
			str = settTimer("start", "START SECOND HALF");
			bStartStop.setText(str[0]);
			tHalf.setText(str[1]);
		} else if ((sharedPref.getString("TIMERBUTTON", "start") == "start")
				&& (sharedPref.getString("HALFTEXT", "START FIRST HALF") == "START SECOND HALF")) {
			str = settTimer("stop", "IN FIRST HALF");
			bStartStop.setText(str[0]);
			tHalf.setText(str[1]);
		}

		// clicklistener for start/stop button toggle
		bStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button b = (Button) v;
				String[] str = new String[2];
				str = settTimer((String) b.getText(), (String) tHalf.getText());
				b.setText(str[0]);
				tHalf.setText(str[1]);
			}
		});

		// clicklistener for increment time button
		// if clicked add a minute to the timer be subtracting a minute from the
		// timer starttime. Update starttime text too
		bIncreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (starttime != 0) {
					starttime = starttime - 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

		// clicklistener for decrement time button
		// if clicked take a minute to the timer be subtracting a minute from
		// the
		// timer starttime. Update starttime text too
		bDecreaseTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((starttime != 0)
						&& (System.currentTimeMillis() - starttime > 30000)) {
					starttime = starttime + 30000;
					currentDate = new Date(starttime);
					sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
					tStartTime.setText("Start Time: " + sdf.format(currentDate));
				}
			}
		});

		// reset timer button click listener
		bResetTime.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (timer != null) {
					timer.cancel();
					timer.purge();
					h.removeCallbacks(run);
				}
				bStartStop.setText("start");
				tHalf.setText("START FIRST HALF");
				tTimeGone.setText("00:00");
				tTimeToGo.setText("00:00");
				tStartTime.setText("Start Time: 00:00");
				starttime = 0;
				v.playSoundEffect(SoundEffectConstants.CLICK);
				return true;
			}
		});

		// /////////////////////////////SCORE///////////////////////////////////////
		// one clickListener handles all input from score buttons
		bDecHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomeGoals.setOnClickListener(scoreAddClickListener);
		bHomePoints.setOnClickListener(scoreAddClickListener);
		bDecHomePoints.setOnClickListener(scoreAddClickListener);
		bDecOppGoals.setOnClickListener(scoreAddClickListener);
		bOppGoals.setOnClickListener(scoreAddClickListener);
		bOppPoints.setOnClickListener(scoreAddClickListener);
		bDecOppPoints.setOnClickListener(scoreAddClickListener);

		// ///////HANDLE SCORES FROM PERSISTED SHARED PREFERENCES////
		homeGoals = sharedPref.getInt("HOMEGOALS", 0);
		homePoints = sharedPref.getInt("HOMEPOINTS", 0);
		oppGoals = sharedPref.getInt("OPPGOALS", 0);
		oppPoints = sharedPref.getInt("OPPPOINTS", 0);

		if (homeGoals + homePoints + oppGoals + oppPoints > 0) {
			bHomeGoals.setText(String.valueOf(homeGoals));
			bHomePoints.setText(String.valueOf(homePoints));
			bOppGoals.setText(String.valueOf(oppGoals));
			bOppPoints.setText(String.valueOf(oppPoints));
			setTotals();
		}

		// reset score button click listener
		// set scores back to 0
		bResetScore.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// reset score in this fragment and also on REVIEW fragment
				bHomeGoals.setText("+");
				homeGoals = homePoints = oppGoals = oppPoints = 0;
				// reset score in REVIEW fragment
				// get reference to REVIEW fragment from parent activity
				// MatchApplication and use reference to execute setHomeGoals
				// method in REVIEW fragment which will reset score there to 0
				((MatchApplication) getActivity()).getFragmentReview()
						.settHomeGoals(0);
				bHomePoints.setText("+");
				((MatchApplication) getActivity()).getFragmentReview()
						.settHomePoints(0);
				bOppGoals.setText("+");
				((MatchApplication) getActivity()).getFragmentReview()
						.settOppGoals(0);
				bOppPoints.setText("+");
				((MatchApplication) getActivity()).getFragmentReview()
						.settOppPoints(0);
				tHomeTotal.setText("(0)");
				tOppTotal.setText("(0)");
				tUpDownDrawText.setText("drawn game. ");
				tHomeDifference.setText(" ");
				v.playSoundEffect(SoundEffectConstants.CLICK);
				return true;
			}
		});

		// stats button click listener just diplays message to longpress
		bResetStats.setOnClickListener(resetClickListener);
		bResetScore.setOnClickListener(resetClickListener);
		bResetTime.setOnClickListener(resetClickListener);

		// reset stats button click listener
		// set all stats back to zero on REVIEW fragment screen
		bResetStats.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// get reference to REVIEW fragment from parent activity
				// MatchApplication and use reference to execute resetStats
				// method in REVIEW fragment which will reset stats there to 0
				((MatchApplication) getActivity()).getFragmentReview()
						.resetStats();
				// clear undo list
				undoList.clear();
				// empty database tabless
				getActivity().getContentResolver().delete(
						Uri.parse(FreeContentProvider.CONTENT_URI + "/"), null,
						null);
				getActivity().getContentResolver().delete(
						Uri.parse(ShotContentProvider.CONTENT_URI + "/"), null,
						null);
				getActivity().getContentResolver().delete(
						Uri.parse(PuckOutContentProvider.CONTENT_URI + "/"),
						null, null);
				v.playSoundEffect(SoundEffectConstants.CLICK);
				Toast.makeText(getActivity(), "Stats Reset", Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});

		// ////////////////////SHOT STATS SETUP///////////////////
		bShotHomeCommit = (Button) v.findViewById(R.id.buttonShotCommitHome);
		bShotOppCommit = (Button) v.findViewById(R.id.buttonShotCommitOpp);
		tShotResult = (TextView) v.findViewById(R.id.textViewShotResult);
		tShotType = (TextView) v.findViewById(R.id.textViewShotType);
		tShotPlayer = (TextView) v.findViewById(R.id.textViewShotPlayer);
		tShotPosn = (TextView) v.findViewById(R.id.textViewShotPosn1);
		bShotHome = (Button) v.findViewById(R.id.buttonShotHome);
		bShotHome.setOnClickListener(statsClickListener);
		bShotOpp = (Button) v.findViewById(R.id.buttonShotOpp);
		bShotOpp.setOnClickListener(statsClickListener);

		// //////////////////////////FREE STATS
		// SETUP////////////////////////////

		tFreeReason = (TextView) v.findViewById(R.id.textViewFreeType);
		tFreePlayer = (TextView) v.findViewById(R.id.textViewFreePlayer);
		tFreePosn = (TextView) v.findViewById(R.id.textViewFreePosn);
		bFreeCommitHome = (Button) v.findViewById(R.id.buttonFreeCommitHome);
		bFreeCommitOpp = (Button) v.findViewById(R.id.buttonFreeCommitOpp);
		bFreeHome = (Button) v.findViewById(R.id.buttonFreeHome);
		bFreeHome.setOnClickListener(statsClickListener);
		bFreeOpp = (Button) v.findViewById(R.id.buttonFreeOpp);
		bFreeOpp.setOnClickListener(statsClickListener);

		// //////////////////////PUCKOUT STATS SETUP////////////////////////////

		tPuckOutReason = (TextView) v.findViewById(R.id.textViewPuckType);
		tPuckOutPlayer = (TextView) v.findViewById(R.id.textViewPuckPlayer);
		tPuckOutPosn = (TextView) v.findViewById(R.id.textViewPuckPosn);
		bPuckOutCommitHome = (Button) v.findViewById(R.id.buttonPuckCommitHome);
		bPuckOutCommitOpp = (Button) v.findViewById(R.id.buttonPuckCommitOpp);
		bPuckOutHome = (Button) v.findViewById(R.id.buttonPuckHome);
		bPuckOutHome.setOnClickListener(statsClickListener);
		bPuckOutOpp = (Button) v.findViewById(R.id.buttonPuckOpp);
		bPuckOutOpp.setOnClickListener(statsClickListener);

		// load panel from database and assign to arraylist ready to be used
		// in stats recording dialogs
		CursorLoader cL = new CursorLoader(getActivity(), allTitles,
				projection, null, null, PanelContentProvider.NICKNAME);
		Cursor c1 = cL.loadInBackground();
		c1.moveToFirst();
		// read player nicknames and players IDs into a hash map so you can
		// get ID associated with player nickname
		playerIDLookUp.clear();
		if (c1.getCount() > 0) {
			do {
				// read player / playerID pairs into hashmap
				playerIDLookUp
						.put(String
								.format("%1$-6s",
										c1.getString(c1
												.getColumnIndexOrThrow(PanelContentProvider.NICKNAME))),
								c1.getInt(c1
										.getColumnIndexOrThrow(PanelContentProvider.PANELID)));
			} while (c1.moveToNext());
		}

		return v;
	}

	// ********************************************************************//
	// ///////////////////////////END OF ONCREATE SECTION //////////////////
	// ********************************************************************//

	// for reset buttons diplay message to long click, won't work with ordinary
	// click
	OnClickListener resetClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get reference to REVIEW fragment from parent activity
			// MatchApplication and use reference to execute resetStats
			// method in REVIEW fragment which will reset stats there to 0
			Toast.makeText(getActivity(), "Long Press to Reset",
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public void onPause() {
		// Save/persist data to be used on reopen
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
				"team_stats_record_data", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt("MINSPERHALF", minsPerHalf);
		editor.putLong("STARTTIME", starttime);
		editor.putInt("HOMEGOALS", homeGoals);
		editor.putInt("HOMEPOINTS", homePoints);
		editor.putInt("OPPGOALS", oppGoals);
		editor.putInt("OPPPOINTS", oppPoints);
		editor.putString("TIMERBUTTON", bStartStop.getText().toString());
		editor.putString("HALFTEXT", tHalf.getText().toString());
		editor.putString("OURTEAM", tOurTeam.getText().toString());
		editor.putString("OPPTEAM", tOppTeam.getText().toString());
		editor.putLong("MATCHID", matchID);
		// save team lineup
		for (int i = 0; i <= 25; i++) {
			editor.putString(TEAM[i], teamLineUp[i]);
		}
		editor.commit();
	}

	// Run Match timer section. Set text strings and timer based on 4
	// possibilities:
	// 1. ready to start first half
	// 2. first half running
	// 3. first half ended ready to start second half
	// 4. second half running
	private String[] settTimer(String bStr, String bHalf) {
		String[] str = new String[2];
		sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
		sdftime = new SimpleDateFormat("HH:mm:ss");

		if (bStr.equals("start") && bHalf.equals("START SECOND HALF")) {
			// 3. first half ended ready to start second half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			tStartTime.setText("Second Half Start Time: "
					+ sdf.format(currentDate));
			timer = new Timer();
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "IN SECOND HALF";
			return str;
		} else if (bStr.equals("stop") && bHalf.equals("IN SECOND HALF")) {
			// 4. second half running
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			h.removeCallbacks(run);
			// when second half stops write mins per half, secon half start time
			// and final score to database
			if (matchID >= 0) {
				ContentValues values = new ContentValues();
				values.put("time2", sdftime.format(currentDate));
				values.put("owngoals2", homeGoals);
				values.put("ownpoints2", homePoints);
				values.put("oppgoals2", oppGoals);
				values.put("opppoints2", oppPoints);
				Uri uri = Uri.parse(MatchContentProvider.CONTENT_URI + "/"
						+ matchID);
				getActivity().getContentResolver().update(uri, values, null,
						null);
			}

			starttime = 0;
			str[0] = "start";
			str[1] = "START FIRST HALF";
			return str;
		} else if (bStr.equals("stop") && bHalf.equals("IN FIRST HALF")) {
			// 2. first half running
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}
			h.removeCallbacks(run);
			// when first half stops write mins per half, start time and score
			// to
			// database
			if ((matchID >= 0) && (currentDate != null)) {
				ContentValues values = new ContentValues();
				values.put("minshalf", minsPerHalf);
				values.put("time1", sdftime.format(currentDate));
				values.put("owngoals1", homeGoals);
				values.put("ownpoints1", homePoints);
				values.put("oppgoals1", oppGoals);
				values.put("opppoints1", oppPoints);
				Uri uri = Uri.parse(MatchContentProvider.CONTENT_URI + "/"
						+ matchID);
				getActivity().getContentResolver().update(uri, values, null,
						null);
			}

			starttime = 0;
			str[0] = "start";
			str[1] = "START SECOND HALF";

			return str;
		} else {
			// 1. ready to start first half
			if (starttime == 0)
				starttime = System.currentTimeMillis();
			currentDate = new Date(starttime);
			sdf = new SimpleDateFormat("HH:mm:ss   dd-MM-yyyy");
			tStartTime.setText("First Half Start Time: "
					+ sdf.format(currentDate));
			timer = new Timer();
			h.postDelayed(run, 0);
			str[0] = "stop";
			str[1] = "IN FIRST HALF";
			return str;
		}
	}

	// *****************************************************************//
	// *****************************************************************//
	// *****************************************************************//
	// clickListener to Deal With SHOTS input when user touches commit button
	OnClickListener shotCommitClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int button = ((Button) v).getId();
			// ca.. method to update shots and scores
			updateShots(button, 1);
			// write shots stats data to database
			// validation - must have saved starting setup first to
			// get matchID foreign key
			if (matchID < 0) {
				Toast.makeText(
						getActivity(),
						"Shot Not Saved to Database\n"
								+ "Save Starting Team on Setup Page First",
						Toast.LENGTH_LONG).show();
			} else {
				ContentValues values = new ContentValues();
				values.put("matchID", matchID);
				Date currentDate = new Date(System.currentTimeMillis());
				sdf = new SimpleDateFormat("HH:mm:ss");
				values.put("time", sdf.format(currentDate));
				// slight difference in handling home team and opposition team
				if (((Button) v).getId() == R.id.buttonShotCommitHome)
					values.put("team", tOurTeam.getText().toString());
				else
					values.put("team", tOppTeam.getText().toString());

				if (((Button) v).getId() == R.id.buttonShotCommitHome) {
					values.put("team", tOurTeam.getText().toString());
					// for home team look up hashMap to get PlayerID as foreign
					// key
					if (playerIDLookUp.get(tShotPlayer.getText().toString()) != null) {
						values.put("playerID", playerIDLookUp.get(tShotPlayer
								.getText().toString()));
					} else {
						// enter blank if name/id not found
						values.put("playerID", "");
					}
				} else {
					values.put("team", tOppTeam.getText().toString());
					// save player position number for opposition team, no name
					int playerNo = 0;
					try {
						playerNo = Integer.parseInt(tShotPlayer.getText()
								.toString());
					} catch (NumberFormatException nfe) {
						Log.v("matchrecord", "player input error #01");
					}

					if (playerNo > 0)
						// write it as negative number to distinguish it from
						// home team playerIDs which are always positive
						values.put("playerID", -Integer.parseInt(tShotPlayer
								.getText().toString()));
					else
						values.put("playerID", "");
				}
				values.put("outcome", tShotResult.getText().toString());
				values.put("type", tShotType.getText().toString());
				values.put("posn", tShotPosn.getText().toString());
				// use ShotContentProvider to ass record to database
				Uri uri = getActivity().getContentResolver().insert(
						ShotContentProvider.CONTENT_URI, values);
				long id = Long.parseLong(uri.getLastPathSegment());
				if (id > 0) {
					rowId = id;
				}
				Toast.makeText(getActivity(), "Shot Saved to database",
						Toast.LENGTH_SHORT).show();
			}
			// store details in undoList
			String[] undoString = { String.valueOf(rowId),
					String.valueOf(button), "shot",
					tShotResult.getText().toString(),
					tShotType.getText().toString(),
					tShotPosn.getText().toString(),
					tShotPlayer.getText().toString() };
			// push onto undoList Stack
			undoList.add(0, undoString);
			// keep stack size at 5 maximum
			if (undoList.size() > 5)
				undoList.remove(5);
			// light up undo button
			bUndo.setTextColor(Color.parseColor("#000000"));
			bUndo.setOnClickListener(undoOnClickListener);

			// reset text and buttons to null and normal colour
			bShotHomeCommit.setTextColor(Color.parseColor("#bbbbbb"));
			bShotOppCommit.setTextColor(Color.parseColor("#bbbbbb"));
			bShotHomeCommit.setOnClickListener(null);
			bShotOppCommit.setOnClickListener(null);
			tShotResult.setText(null);
			tShotType.setText(null);
			tShotPlayer.setText(null);
			tShotPosn.setText(null);
		}

	};

	// //////////////////////////////////////////////////////////////////////
	// method to update score and update shots data in review scrreen
	public void updateShots(int button, int count) {
		switch (button) {
		case R.id.buttonShotCommitHome:
			// for home team commit
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (tShotResult.getText().equals("goal")) {
				// increment goal counter
				if (homeGoals + count >= 0) {
					homeGoals = homeGoals + count;
					bHomeGoals.setText(String.valueOf(homeGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((MatchApplication) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					// change display from + to 0 if first score
					if (bHomePoints.getText().equals("+"))
						bHomePoints.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// update goal counter display in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotGoalsHome(count);
				// increment goal from play counter in review page
				// unless score was from free/45/65/penalty
				if ((!tShotType.getText().equals("free"))
						&& (!tShotType.getText().equals("45/65"))
						&& (!tShotType.getText().equals("penalty"))
						&& (!tShotType.getText().equals("sideline"))) {
					((MatchApplication) getActivity()).getFragmentReview()
							.addtShotGoalsPlayHome(count);
				}
			} else if (tShotResult.getText().equals("point")) {
				// increment points counter
				if (homePoints + count >= 0) {
					homePoints = homePoints + count;
					bHomePoints.setText(String.valueOf(homePoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((MatchApplication) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					// change display from + to 0 if first score
					if (bHomeGoals.getText().equals("+"))
						bHomeGoals.setText("0");
					// remind user score is updated in case they try and do it
					// manually
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// update points counter display in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotPointsHome(count);
				// increment goal from play counter in review page
				// unless score was from free/45/65
				if ((!tShotType.getText().equals("free"))
						&& (!tShotType.getText().equals("45/65"))
						&& (!tShotType.getText().equals("penalty"))
						&& (!tShotType.getText().equals("sideline"))) {
					((MatchApplication) getActivity()).getFragmentReview()
							.addtShotPointsPlayHome(count);
				}
			} else if (tShotResult.getText().equals("wide")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotWidesHome(count);
			} else if (tShotResult.getText().equals("45/65")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShot45sHome(count);
			} else if (tShotResult.getText().equals("saved")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotSavedHome(count);
			} else if (tShotResult.getText().equals("short")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotShortHome(count);
			} else if (tShotResult.getText().equals("off posts")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotPostsHome(count);
			} else
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotHome(count);
			break;
		case R.id.buttonShotCommitOpp:
			// for opposition team
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (tShotResult.getText().equals("goal")) {
				// increment goal counter
				if (oppGoals + count >= 0) {
					oppGoals = oppGoals + count;
					bOppGoals.setText(String.valueOf(oppGoals));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((MatchApplication) getActivity()).getFragmentReview()
							.settOppGoals(oppGoals);
					if (bOppPoints.getText().equals("+"))
						bOppPoints.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// increment goal counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotGoalsOpp(count);
				// increment goal from play counter in review page
				// unless scrore was from free/45/65
				if ((!tShotType.getText().equals("free"))
						&& (!tShotType.getText().equals("45/65"))
						&& (!tShotType.getText().equals("penalty"))
						&& (!tShotType.getText().equals("sideline"))) {
					((MatchApplication) getActivity()).getFragmentReview()
							.addtShotGoalsPlayOpp(count);
				}
			} else if (tShotResult.getText().equals("point")) {
				// increment points counter
				if (oppPoints + count >= 0) {
					oppPoints = oppPoints + count;
					bOppPoints.setText(String.valueOf(oppPoints));
					// update totals
					setTotals();
					// increment score in REVIEW fragment
					((MatchApplication) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					if (bOppGoals.getText().equals("+"))
						bOppGoals.setText("0");
					Toast.makeText(getActivity(), "Score Updated",
							Toast.LENGTH_SHORT).show();
				}
				// increment goal counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotPointsOpp(count);
				// increment goal from play counter in review page
				// unless scrore was from free/45/65
				if ((!tShotType.getText().equals("free"))
						&& (!tShotType.getText().equals("45/65"))
						&& (!tShotType.getText().equals("penalty"))
						&& (!tShotType.getText().equals("sideline"))) {
					((MatchApplication) getActivity()).getFragmentReview()
							.addtShotPointsPlayOpp(count);
				}
			} else if (tShotResult.getText().equals("wide")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotWidesOpp(count);
			} else if (tShotResult.getText().equals("45/65")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShot45sOpp(count);
			} else if (tShotResult.getText().equals("saved")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotSavedOpp(count);
			} else if (tShotResult.getText().equals("short")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotShortOpp(count);
			} else if (tShotResult.getText().equals("off posts")) {
				// increment counter in review page
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotPostsOpp(count);
			} else
				((MatchApplication) getActivity()).getFragmentReview()
						.addtShotOpp(count);

			break;
		}

	}

	// *****************************************************************//
	// *****************************************************************//
	// *****************************************************************//
	// clickListener to Deal With FREES input when user touches commit button
	OnClickListener freeCommitClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int button = ((Button) v).getId();
			// call method to update frees display on review page
			updateFrees(button, 1);
			// write to database
			// validation - must have saved starting setup in setup screen first
			// to
			// get matchID foreign key
			if (matchID < 0) {
				Toast.makeText(
						getActivity(),
						"Free Not Saved to Database\n"
								+ "Save Starting Team on Setup Page First",
						Toast.LENGTH_LONG).show();
			} else {
				ContentValues values = new ContentValues();
				values.put("matchID", matchID);
				Date currentDate = new Date(System.currentTimeMillis());
				sdf = new SimpleDateFormat("HH:mm:ss");
				values.put("time", sdf.format(currentDate));
				if (((Button) v).getId() == R.id.buttonFreeCommitHome)
					values.put("team", tOurTeam.getText().toString());
				else
					values.put("team", tOppTeam.getText().toString());
				values.put("reason", tFreeReason.getText().toString());
				// look up hash Map to get PlayerID as foreign key
				if (playerIDLookUp.get(tFreePlayer.getText().toString()) != null) {
					values.put("playerID", playerIDLookUp.get(tFreePlayer
							.getText().toString()));
				} else {
					values.put("playerID", "");
				}
				values.put("posn", tFreePosn.getText().toString());
				// use free content provider to insert into database table
				Uri uri = getActivity().getContentResolver().insert(
						FreeContentProvider.CONTENT_URI, values);
				long id = Long.parseLong(uri.getLastPathSegment());
				if (id > 0) {
					rowId = id;
				}
				Toast.makeText(getActivity(), "Free Saved to database",
						Toast.LENGTH_SHORT).show();
			}

			// store details in undoList
			String[] undoString = { String.valueOf(rowId),
					String.valueOf(button), "frees",
					tFreeReason.getText().toString(), null,
					tFreePosn.getText().toString(),
					tFreePlayer.getText().toString() };
			// push onto undoList Stack
			undoList.add(0, undoString);
			// keep stack size at 5 maximum
			if (undoList.size() > 5)
				undoList.remove(5);
			bUndo.setTextColor(Color.parseColor("#000000"));
			bUndo.setOnClickListener(undoOnClickListener);

			// reset text and buttons to null and normal colour
			bFreeCommitHome.setTextColor(Color.parseColor("#bbbbbb"));
			bFreeCommitOpp.setTextColor(Color.parseColor("#bbbbbb"));
			bFreeCommitHome.setOnClickListener(null);
			bFreeCommitOpp.setOnClickListener(null);
			tFreeReason.setText(null);
			tFreePlayer.setText(null);
			tFreePosn.setText(null);
		}

	};

	// //////////////////////////////////////////////////////////////////////
	// method to update frees data in review scrreen
	public void updateFrees(int button, int count) {
		switch (button) {

		// for home team
		case R.id.buttonFreeCommitHome:
			// WRITE TO REVIEW PAGE///////////////////////////////////
			((MatchApplication) getActivity()).getFragmentReview()
					.addFreeWonHome(count);
			try {
				// work out which half of field free was in according to
				// position number recorded greater or less than 10
				if (((Integer.valueOf(tFreePosn.getText().toString())) > 0)
						&& ((Integer.valueOf(tFreePosn.getText().toString())) <= 10))
					((MatchApplication) getActivity()).getFragmentReview()
							.addFreeWonHomeOwn(count);

				else if (((Integer.valueOf(tFreePosn.getText().toString())) > 10)
						&& ((Integer.valueOf(tFreePosn.getText().toString())) <= 20))
					((MatchApplication) getActivity()).getFragmentReview()
							.addFreeWonHomeOpp(count);
			} catch (NumberFormatException nfe) {
				Log.v("matchrecord", "pitch position error");
			}

			break;
		case R.id.buttonFreeCommitOpp:
			// for opposition team
			// WRITE TO REVIEW PAGE///////////////////////////////////
			((MatchApplication) getActivity()).getFragmentReview()
					.addFreeWonOpp(count);
			try {
				// work out which half of field free was in according to
				// position number recorded greater or less than 10
				if (((Integer.valueOf(tFreePosn.getText().toString())) > 0)
						&& ((Integer.valueOf(tFreePosn.getText().toString())) <= 10))
					((MatchApplication) getActivity()).getFragmentReview()
							.addFreeWonOppOpp(count);

				else if (((Integer.valueOf(tFreePosn.getText().toString())) > 10)
						&& ((Integer.valueOf(tFreePosn.getText().toString())) <= 20))
					((MatchApplication) getActivity()).getFragmentReview()
							.addFreeWonOppOwn(count);
			} catch (NumberFormatException nfe) {
				Log.v("matchrecord", "pitch position error");
			}
			break;
		}
	}

	// *****************************************************************//
	// *****************************************************************//
	// *****************************************************************//
	// clickListener to Deal With puckouts input when user touches commit button
	OnClickListener puckOutCommitClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int button = ((Button) v).getId();
			// call method to update puckouts display on review page
			updatePuckOuts(button, 1);

			// write to database
			// validation - must have saved starting setup first to
			// get matchID foreign key
			if (matchID < 0) {
				Toast.makeText(
						getActivity(),
						"Puckout Not Saved to Database\n"
								+ "Save Starting Team on Setup Page First",
						Toast.LENGTH_LONG).show();
			} else {
				ContentValues values = new ContentValues();
				values.put("matchID", matchID);
				Date currentDate = new Date(System.currentTimeMillis());
				sdf = new SimpleDateFormat("HH:mm:ss");
				values.put("time", sdf.format(currentDate));
				if (((Button) v).getId() == R.id.buttonPuckCommitHome)
					values.put("team", tOurTeam.getText().toString());
				else
					values.put("team", tOppTeam.getText().toString());
				values.put("outcome", tPuckOutReason.getText().toString());
				// look up hash Map to get PlayerID as foreign key
				if (playerIDLookUp.get(tPuckOutPlayer.getText().toString()) != null) {
					values.put("playerID", playerIDLookUp.get(tPuckOutPlayer
							.getText().toString()));
				} else {
					values.put("playerID", "");
				}
				values.put("posn", tPuckOutPosn.getText().toString());
				// use puckout content provider to insert into database table
				Uri uri = getActivity().getContentResolver().insert(
						PuckOutContentProvider.CONTENT_URI, values);
				long id = Long.parseLong(uri.getLastPathSegment());
				if (id > 0) {
					rowId = id;
				}

				Toast.makeText(getActivity(), "Puckout Saved to database",
						Toast.LENGTH_SHORT).show();
			}

			// store details in undoList
			String[] undoString = { String.valueOf(rowId),
					String.valueOf(button), "puckouts",
					tPuckOutReason.getText().toString(), null,
					tPuckOutPosn.getText().toString(),
					tPuckOutPlayer.getText().toString() };
			// push onto undoList Stack
			undoList.add(0, undoString);
			// keep stack size at 5 maximum
			if (undoList.size() > 5)
				undoList.remove(5);
			bUndo.setTextColor(Color.parseColor("#000000"));
			bUndo.setOnClickListener(undoOnClickListener);

			// reset text and buttons to null and normal colour
			// reset clickListener to null
			bPuckOutCommitHome.setTextColor(Color.parseColor("#bbbbbb"));
			bPuckOutCommitOpp.setTextColor(Color.parseColor("#bbbbbb"));
			bPuckOutCommitHome.setOnClickListener(null);
			bPuckOutCommitOpp.setOnClickListener(null);
			tPuckOutReason.setText(null);
			tPuckOutPlayer.setText(null);
			tPuckOutPosn.setText(null);
		}

	};

	// //////////////////////////////////////////////////////////////////////
	// method to update puckouts data in review scrreen
	public void updatePuckOuts(int button, int count) {

		switch (button) {
		// for home team
		case R.id.buttonPuckCommitHome:
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (tPuckOutReason.getText().equals("won high catch"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonCleanHome(count);
			else if (tPuckOutReason.getText().equals("won clean"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonCleanHome(count);
			else if (tPuckOutReason.getText().equals("won on break")) {
				Log.e("record", "won break " + count);

				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonBreakHome(count);
			} else if (tPuckOutReason.getText().equals("over sideline"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckOtherHome(count);
			else if (tPuckOutReason.getText().equals("lost high catch"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostCleanHome(count);
			else if (tPuckOutReason.getText().equals("lost clean"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostCleanHome(count);
			else if (tPuckOutReason.getText().equals("lost on break"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostBreakHome(count);
			else if (tPuckOutReason.getText().equals("lost on break"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostBreakHome(count);
			else if (tPuckOutReason.getText().equals("other"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckOtherHome(count);
			break;
		case R.id.buttonPuckCommitOpp:
			// WRITE TO REVIEW PAGE///////////////////////////////////
			if (tPuckOutReason.getText().equals("won high catch"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonCleanOpp(count);
			else if (tPuckOutReason.getText().equals("won clean"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonCleanOpp(count);
			else if (tPuckOutReason.getText().equals("won on break"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckWonBreakOpp(count);
			else if (tPuckOutReason.getText().equals("over sideline"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckOtherOpp(count);
			else if (tPuckOutReason.getText().equals("lost high catch"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostCleanOpp(count);
			else if (tPuckOutReason.getText().equals("lost clean"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostCleanOpp(count);
			else if (tPuckOutReason.getText().equals("lost on break"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckLostBreakOpp(count);
			else if (tPuckOutReason.getText().equals("other"))
				((MatchApplication) getActivity()).getFragmentReview()
						.addPuckOtherOpp(count);
			break;
		}

	}

	// **********************************************************************//
	/*-------------------DIALOG FOR STATS INPUT*---------------------------*///
	// handles button clicks for shot / free / puckout ///
	// **********************************************************************//
	OnClickListener statsClickListener = new OnClickListener() {
		String dialogNeutral; // For Shots input
		String dialogTitle; // For Shots input

		@Override
		public void onClick(View w) {
			// clear default values from input textfields
			tPuckOutReason.setText(null);
			tPuckOutPlayer.setText(null);
			tPuckOutPosn.setText(null);
			tFreeReason.setText(null);
			tFreePlayer.setText(null);
			tFreePosn.setText(null);
			tShotResult.setText(null);
			tShotType.setText(null);
			tShotPlayer.setText(null);
			tShotPosn.setText(null);

			// use SHOT_FREE_PUCK_OUT to store which button was pressed
			SHOT_FREE_PUCK_OUT = ((Button) w).getId();

			// throw up stats input screen layout
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View vv = inflater.inflate(R.layout.shot_layout, null);

			// light up the relative COMMIT button and set clickListener ready
			// for when input is finished
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
				bShotHomeCommit.setTextColor(Color.parseColor("#ff0000"));
				bShotHomeCommit.setOnClickListener(shotCommitClickListener);
				dialogNeutral = "Enter Location";
				dialogTitle = "select pitch position";
				break;
			case R.id.buttonFreeHome:
				bFreeCommitHome.setTextColor(Color.parseColor("#ff0000"));
				bFreeCommitHome.setOnClickListener(freeCommitClickListener);
				dialogNeutral = "Select Player";
				dialogTitle = "if applicable, select own player who was fouled";
				vv = inflater.inflate(R.layout.free_layout, null);
				break;
			case R.id.buttonPuckHome:
				bPuckOutCommitHome.setTextColor(Color.parseColor("#ff0000"));
				bPuckOutCommitHome
						.setOnClickListener(puckOutCommitClickListener);
				dialogNeutral = "Select Player";
				dialogTitle = "select own player who won/lost your own puckout";
				vv = inflater.inflate(R.layout.puckout_layout, null);
				break;
			case R.id.buttonShotOpp:
				bShotOppCommit.setTextColor(Color.parseColor("#ff0000"));
				bShotOppCommit.setOnClickListener(shotCommitClickListener);
				dialogNeutral = "Enter Location";
				dialogTitle = "select pitch position";
				break;
			case R.id.buttonFreeOpp:
				bFreeCommitOpp.setTextColor(Color.parseColor("#ff0000"));
				bFreeCommitOpp.setOnClickListener(freeCommitClickListener);
				dialogNeutral = "Select Player";
				dialogTitle = "if applicable, select own player who committed foul";
				vv = inflater.inflate(R.layout.free_layout, null);
				break;
			case R.id.buttonPuckOpp:
				bPuckOutCommitOpp.setTextColor(Color.parseColor("#ff0000"));
				bPuckOutCommitOpp
						.setOnClickListener(puckOutCommitClickListener);
				dialogNeutral = "Select Player";
				dialogTitle = "select own player who won/lost opposition puckout";
				vv = inflater.inflate(R.layout.puckout_layout, null);
				break;
			}

			// set up entry dialog for selecting ptich position
			AlertDialog.Builder builder;
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:

				builder = new AlertDialog.Builder(getActivity())
						.setView(vv)
						// ok button just closes the dialog
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// light up the save button
										dialog.dismiss();
									}
								})

						// set up pitch position sub dialog/layout on the
						// neutral button
						.setNeutralButton(dialogNeutral,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// show Pitch Dialog
										LayoutInflater inflater = getActivity()
												.getLayoutInflater();
										// throw up pitch layout screen
										View vv = inflater.inflate(
												R.layout.pitch_layout, null);
										AlertDialog.Builder builder = new AlertDialog.Builder(
												getActivity()).setTitle(
												dialogTitle).setView(vv);
										// Set up button click listener for each
										// pitch position
										Button[] bb = new Button[21];
										for (int i = 1; i <= 20; i++) {
											bb[i] = (Button) vv
													.findViewById(getResources()
															.getIdentifier(
																	"ButtonPitch"
																			+ String.format(
																					"%02d",
																					i),
																	"id",
																	"fm.matchstats"));
											bb[i].setOnClickListener(getPitchClickListener);
											bb[i].setText(String.valueOf(i));
										}
										MatchRecordFragment.this.alertpitch = builder
												.create();
										MatchRecordFragment.this.alertpitch
												.show();
										dialog.dismiss();
									}
								});
				break;

			default:
				// for frees and puckouts
				builder = new AlertDialog.Builder(getActivity())
						.setView(vv)
						// ok button just closes the dialog
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// light up the save button
										dialog.dismiss();
									}
								})

						// set up pitch position sub dialog/layout on the
						// neutral
						// button
						.setNeutralButton(dialogNeutral,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// show Pitch Dialog
										LayoutInflater inflater = getActivity()
												.getLayoutInflater();
										// throw up pitch layout screen
										View vv = inflater.inflate(
												R.layout.team_layout, null);
										AlertDialog.Builder builder = new AlertDialog.Builder(
												getActivity()).setTitle(
												dialogTitle).setView(vv);
										// Set up button click listener for each
										// pitch position

										Button[] bb = new Button[16];
										for (int i = 1; i <= 15; i++) {
											bb[i] = (Button) vv
													.findViewById(getResources()
															.getIdentifier(
																	"ButtonP"
																			+ String.format(
																					"%02d",
																					i),
																	"id",
																	"fm.matchstats"));
											// For Home team assign player
											// name to team lineup
											// For Opposition just use
											// position numbers

											bb[i].setText(teamLineUp[i]);
											bb[i].setOnClickListener(getPlayerClickListener);
										}

										MatchRecordFragment.this.alertpitch = builder
												.create();
										MatchRecordFragment.this.alertpitch
												.show();
										dialog.dismiss();
									}
								});
				break;
			}

			// Set up the Radio button clickListeners for Shot Outcome / Free
			// Type / Puckout result input
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonPuckHome:
			case R.id.buttonPuckOpp:
				// just 3 choices for puck outs
				RadioButton[] rbrpo = new RadioButton[4];
				for (int i = 0; i < 4; i++) {
					rbrpo[i] = (RadioButton) vv.findViewById(getResources()
							.getIdentifier(
									"radio_shot_r" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbrpo[i].setOnClickListener(getOutcomeClickListener);
				}
				break;
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:
				// 7 choices for shots
				RadioButton[] rbrshot = new RadioButton[7];
				for (int i = 0; i < 7; i++) {
					rbrshot[i] = (RadioButton) vv.findViewById(getResources()
							.getIdentifier(
									"radio_shot_r" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbrshot[i].setOnClickListener(getOutcomeClickListener);
				}
				break;
			default:
				// 6 choices frees
				RadioButton[] rbr = new RadioButton[4];
				for (int i = 0; i < 4; i++) {
					rbr[i] = (RadioButton) vv.findViewById(getResources()
							.getIdentifier(
									"radio_shot_r" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbr[i].setOnClickListener(getOutcomeClickListener);
				}
				break;
			}

			// Set up the Radio button clickListeners for Shot Type / Free
			// Type / Puckout result input
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:
				// 4 options for shot type
				RadioButton[] rbtShot = new RadioButton[6];
				for (int i = 0; i < 6; i++) {
					rbtShot[i] = (RadioButton) vv.findViewById(getResources()
							.getIdentifier(
									"radio_shot_t" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbtShot[i].setOnClickListener(getTypeClickListener);
				}
				break;
			case R.id.buttonFreeHome:
			case R.id.buttonFreeOpp:
				// 5 options for frees
				RadioButton[] rbtFree = new RadioButton[4];
				for (int i = 0; i < 4; i++) {
					rbtFree[i] = (RadioButton) vv.findViewById(getResources()
							.getIdentifier(
									"radio_shot_t" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbtFree[i].setOnClickListener(getTypeClickListener);
				}
				break;
			case R.id.buttonPuckHome:
			case R.id.buttonPuckOpp:
				// 3 options for puckouts
				RadioButton[] rbtPuckOut = new RadioButton[4];
				for (int i = 0; i < 4; i++) {
					rbtPuckOut[i] = (RadioButton) vv
							.findViewById(getResources().getIdentifier(
									"radio_shot_t" + String.format("%02d", i),
									"id", "fm.matchstats"));
					rbtPuckOut[i].setOnClickListener(getTypeClickListener);
				}
				break;
			default:
				break;
			}

			// for shots assign clickListener and names to team layout from
			// teamLineUp
			Button[] bb;
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:
				bb = new Button[16];
				for (int i = 1; i <= 15; i++) {
					bb[i] = (Button) vv.findViewById(getResources()
							.getIdentifier(
									"ButtonP" + String.format("%02d", i), "id",
									"fm.matchstats"));
					// For Home team assign player name to team lineup
					// For Opposition just use position numbers
					if (SHOT_FREE_PUCK_OUT == bShotHome.getId()
							|| SHOT_FREE_PUCK_OUT == bFreeHome.getId()
							|| SHOT_FREE_PUCK_OUT == bFreeOpp.getId()
							|| SHOT_FREE_PUCK_OUT == bPuckOutOpp.getId()
							|| SHOT_FREE_PUCK_OUT == bPuckOutHome.getId())
						bb[i].setText(teamLineUp[i]);
					else
						bb[i].setText(Integer.toString(i));
					bb[i].setOnClickListener(getPlayerClickListener);
				}
				break;
			// for frees and puckouts assign clicklistener to pitch positions
			default:
				bb = new Button[21];
				for (int i = 1; i <= 20; i++) {
					bb[i] = (Button) vv.findViewById(getResources()
							.getIdentifier(
									"ButtonPitch" + String.format("%02d", i),
									"id", "fm.matchstats"));
					bb[i].setOnClickListener(getPitchClickListener);
					bb[i].setText(String.valueOf(i));
				}
				break;
			}

			MatchRecordFragment.this.alertshot = builder.create();
			MatchRecordFragment.this.alertshot.show();
		}

	};

	// Listener to get player name
	OnClickListener getPlayerClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			Button b = (Button) vvv;
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:
				tShotPlayer.setText(b.getText());
				break;
			case R.id.buttonFreeHome:
			case R.id.buttonFreeOpp:
				tFreePlayer.setText(b.getText());
				break;
			case R.id.buttonPuckHome:
			case R.id.buttonPuckOpp:
				tPuckOutPlayer.setText(b.getText());
				break;
			}
			// close off dialog if necessary
			if (MatchRecordFragment.this.alertpitch != null)
				MatchRecordFragment.this.alertpitch.dismiss();
		}
	};

	// Listener to get shot outcome
	OnClickListener getOutcomeClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			if ((SHOT_FREE_PUCK_OUT == R.id.buttonShotHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonShotOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_r00:
					tShotResult.setText("goal");
					break;
				case R.id.radio_shot_r01:
					tShotResult.setText("point");
					break;
				case R.id.radio_shot_r02:
					tShotResult.setText("wide");
					break;
				case R.id.radio_shot_r03:
					tShotResult.setText("45/65");
					break;
				case R.id.radio_shot_r04:
					tShotResult.setText("saved");
					break;
				case R.id.radio_shot_r05:
					tShotResult.setText("short");
					break;
				case R.id.radio_shot_r06:
					tShotResult.setText("off posts");
					break;
				}
			} else if ((SHOT_FREE_PUCK_OUT == R.id.buttonFreeHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonFreeOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_r00:
					tFreeReason.setText("steps");
					break;
				case R.id.radio_shot_r01:
					tFreeReason.setText("chop");
					break;
				case R.id.radio_shot_r02:
					tFreeReason.setText("push/pull/trip");
					break;
				case R.id.radio_shot_r03:
					tFreeReason.setText("contact/strike");
					break;
				}
			} else if ((SHOT_FREE_PUCK_OUT == R.id.buttonPuckHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonPuckOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_r00:
					tPuckOutReason.setText("won high catch");
					break;
				case R.id.radio_shot_r01:
					tPuckOutReason.setText("won clean");
					break;
				case R.id.radio_shot_r02:
					tPuckOutReason.setText("won on break");
					break;
				case R.id.radio_shot_r03:
					tPuckOutReason.setText("over sideline");
					break;
				}
			}
		}
	};

	// listener to get shot type
	OnClickListener getTypeClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {

			if ((SHOT_FREE_PUCK_OUT == R.id.buttonShotHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonShotOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_t00:
					tShotType.setText("air strike");
					break;
				case R.id.radio_shot_t01:
					tShotType.setText("ground strike");
					break;
				case R.id.radio_shot_t02:
					tShotType.setText("free");
					break;
				case R.id.radio_shot_t03:
					tShotType.setText("45/65");
					break;
				case R.id.radio_shot_t04:
					tShotType.setText("sideline");
					break;
				case R.id.radio_shot_t05:
					tShotType.setText("penalty");
					break;
				}
			} else if ((SHOT_FREE_PUCK_OUT == R.id.buttonFreeHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonFreeOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_t00:
					tFreeReason.setText("charging");
					break;
				case R.id.radio_shot_t01:
					tFreeReason.setText("holding");
					break;
				case R.id.radio_shot_t02:
					tFreeReason.setText("throw");
					break;
				case R.id.radio_shot_t03:
					tFreeReason.setText("other");
					break;
				}
			} else if ((SHOT_FREE_PUCK_OUT == R.id.buttonPuckHome)
					|| (SHOT_FREE_PUCK_OUT == R.id.buttonPuckOpp)) {
				switch (vvv.getId()) {
				case R.id.radio_shot_t00:
					tPuckOutReason.setText("lost high catch");
					break;
				case R.id.radio_shot_t01:
					tPuckOutReason.setText("lost clean");
					break;
				case R.id.radio_shot_t02:
					tPuckOutReason.setText("lost on break");
					break;
				case R.id.radio_shot_t03:
					tPuckOutReason.setText("other");
					break;
				}
			}
		}
	};

	// listener to get pitch position
	OnClickListener getPitchClickListener = new OnClickListener() {
		@Override
		public void onClick(View vvv) {
			Button b = (Button) vvv;
			// determine if we're dealing with shot or free or puckout
			switch (SHOT_FREE_PUCK_OUT) {
			case R.id.buttonShotHome:
			case R.id.buttonShotOpp:
				tShotPosn.setText(b.getText());
				break;
			case R.id.buttonFreeHome:
			case R.id.buttonFreeOpp:
				tFreePosn.setText(b.getText());
				break;
			case R.id.buttonPuckHome:
			case R.id.buttonPuckOpp:
				tPuckOutPosn.setText(b.getText());
				break;
			}
			if (MatchRecordFragment.this.alertpitch != null)
				MatchRecordFragment.this.alertpitch.dismiss();
		}
	};

	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//
	// *******************************************************************//

	// ///////////////////SCORE CLICK LISTENER//////////////////////////////////
	OnClickListener scoreAddClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// for each case update score display in this fragment
			// and update score display in REVIEW fragment
			switch (v.getId()) {
			case R.id.home_goals:
				homeGoals++;
				bHomeGoals.setText(String.valueOf(homeGoals));
				((MatchApplication) getActivity()).getFragmentReview()
						.settHomeGoals(homeGoals);
				// if first score change + on buttons to 0
				if (bHomePoints.getText().equals("+"))
					bHomePoints.setText("0");
				break;
			case R.id.home_points:
				homePoints++;
				bHomePoints.setText(String.valueOf(homePoints));
				((MatchApplication) getActivity()).getFragmentReview()
						.settHomePoints(homePoints);
				// if first score change + on buttons to 0
				if (bHomeGoals.getText().equals("+"))
					bHomeGoals.setText("0");
				break;
			case R.id.opp_goals:
				oppGoals++;
				bOppGoals.setText(String.valueOf(oppGoals));
				((MatchApplication) getActivity()).getFragmentReview()
						.settOppGoals(oppGoals);
				// if first score change + on buttons to 0
				if (bOppPoints.getText().equals("+"))
					bOppPoints.setText("0");
				break;
			case R.id.opp_points:
				oppPoints++;
				bOppPoints.setText(String.valueOf(oppPoints));
				((MatchApplication) getActivity()).getFragmentReview()
						.settOppPoints(oppPoints);
				// if first score change + on buttons to 0
				if (bOppGoals.getText().equals("+"))
					bOppGoals.setText("0");
				break;
			case R.id.dec_home_goals:
				if (homeGoals > 0) {
					homeGoals--;
					bHomeGoals.setText(String.valueOf(homeGoals));
					((MatchApplication) getActivity()).getFragmentReview()
							.settHomeGoals(homeGoals);
					break;
				} else
					return;
			case R.id.dec_home_points:
				if (homePoints > 0) {
					homePoints--;
					bHomePoints.setText(String.valueOf(homePoints));
					((MatchApplication) getActivity()).getFragmentReview()
							.settHomePoints(homePoints);
					break;
				} else
					return;
			case R.id.dec_opp_goals:
				if (oppGoals > 0) {
					oppGoals--;
					bOppGoals.setText(String.valueOf(oppGoals));
					((MatchApplication) getActivity()).getFragmentReview()
							.settOppGoals(oppGoals);
					break;
				} else
					return;
			case R.id.dec_opp_points:
				if (oppPoints > 0) {
					oppPoints--;
					bOppPoints.setText(String.valueOf(oppPoints));
					((MatchApplication) getActivity()).getFragmentReview()
							.settOppPoints(oppPoints);
					break;
				} else
					return;
			}
			// update totals values and text
			setTotals();
		}
	};

	// method to calculate total score from goals and points
	// and update if home team is ahead or behind or if game is a draw
	private void setTotals() {
		int homeTotal = (homeGoals * 3) + homePoints;
		tHomeTotal.setText("(" + String.valueOf(homeTotal) + ")");
		int oppTotal = (oppGoals * 3) + oppPoints;
		tOppTotal.setText("(" + String.valueOf(oppTotal) + ")");

		if (homeTotal > oppTotal) {
			tUpDownDrawText.setText("up by: ");
			tHomeDifference.setText("(" + String.valueOf(homeTotal - oppTotal)
					+ ")");
		} else if (homeTotal < oppTotal) {
			tUpDownDrawText.setText("down by: ");
			tHomeDifference.setText("(" + String.valueOf(-homeTotal + oppTotal)
					+ ")");
		} else {
			tUpDownDrawText.setText("drawn game. ");
			tHomeDifference.setText(" ");
		}
	}

	// //////////////////////////TIMER///////////////////////////////
	// set up thread to run match timer
	Runnable run = new Runnable() {
		@Override
		public void run() {
			long millis = System.currentTimeMillis() - starttime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			tTimeGone.setText(String.format("%02d:%02d", minutes, seconds));
			if (minsPerHalf - minutes > 0) {
				tTimeToGo.setText(String.format("%02d:%02d", minsPerHalf - 1
						- minutes, 60 - seconds));
			} else {
				tTimeToGo.setText(String.format("%02d:%02d", minutes
						- minsPerHalf, seconds));
				tTimeLeft.setText("extra time:");
			}
			h.postDelayed(this, 1000);
		}
	};

	// this method is called from the SETUP fragment to update the names of the
	// home and away teams and to receive team line and teams from setup screen
	public void setTeamLineUp(String[] teamLineUp, String homeTeam,
			String oppTeam) {
		this.teamLineUp = teamLineUp;
		if (!homeTeam.equals(""))
			tOurTeam.setText(homeTeam);
		if (!oppTeam.equals(""))
			tOppTeam.setText(oppTeam);
	}

	// this method is called from the SETUP fragment to update provide the
	// matchID
	// value from the database. This is used as a foreign key when saving
	// records
	// to database
	public void setMatchID(long matchID) {
		this.matchID = matchID;
	}

	// Undo last 5 stats entries
	OnClickListener undoOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (undoList.size() > 0) {
				// pop next undo from stack
				String[] undoStr = new String[7];
				undoStr = undoList.remove(0);
				if (undoStr[2].equals("shot")) {
					tShotResult.setText(undoStr[3]);
					tShotType.setText(undoStr[4]);
					tShotPlayer.setText(undoStr[6]);
					tShotPosn.setText(undoStr[5]);
					updateShots(Integer.valueOf(undoStr[1]), -1);
					try {
						Uri uri = Uri.parse(ShotContentProvider.CONTENT_URI
								+ "/" + Long.valueOf(undoStr[0]));
						getActivity().getContentResolver().delete(uri, null,
								null);
					} catch (IllegalArgumentException e) {
						Log.e("shot undo", "- " + e);
					}
					tShotResult.setText(null);
					tShotType.setText(null);
					tShotPlayer.setText(null);
					tShotPosn.setText(null);
				} else if (undoStr[2].equals("frees")) {
					tFreeReason.setText(undoStr[3]);
					tFreePlayer.setText(undoStr[6]);
					tFreePosn.setText(undoStr[5]);
					updateFrees(Integer.valueOf(undoStr[1]), -1);
					try {
						Uri uri = Uri.parse(FreeContentProvider.CONTENT_URI
								+ "/" + Long.valueOf(undoStr[0]));
						getActivity().getContentResolver().delete(uri, null,
								null);
					} catch (IllegalArgumentException e) {
						Log.e("free undo", "- " + e);
					}
					tFreeReason.setText(null);
					tFreePlayer.setText(null);
					tFreePosn.setText(null);
				} else if (undoStr[2].equals("puckouts")) {
					tPuckOutReason.setText(undoStr[3]);
					tPuckOutPlayer.setText(undoStr[6]);
					tPuckOutPosn.setText(undoStr[5]);
					updatePuckOuts(Integer.valueOf(undoStr[1]), -1);
					try {
						Uri uri = Uri.parse(PuckOutContentProvider.CONTENT_URI
								+ "/" + Long.valueOf(undoStr[0]));
						getActivity().getContentResolver().delete(uri, null,
								null);
					} catch (IllegalArgumentException e) {
						Log.e("puckouts undo", "- " + e);
					}
					tPuckOutReason.setText(null);
					tPuckOutPlayer.setText(null);
					tPuckOutPosn.setText(null);
				}
				// if undo stack empty, turn off button
				if (undoList.size() <= 0) {
					bUndo.setTextColor(Color.parseColor("#bbbbbb"));
					bUndo.setOnClickListener(null);
				}
			} else
				Toast.makeText(getActivity(), "Error, nothing to Undo",
						Toast.LENGTH_SHORT).show();
		}
	};

}
