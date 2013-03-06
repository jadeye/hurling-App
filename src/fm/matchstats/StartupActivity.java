/*
 *  StartupActivity.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Start up screen Activity
 *  Written on: Jan 2013
 *  
 *  Uses modified code from
 *  http://stackoverflow.com/questions/11043175/trying-to-copy-sqlite-db-from-data-to-sd-card
 */

package fm.matchstats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fm.matchstats.R;
import fm.matchstats.db.AttendanceContentProvider;
import fm.matchstats.db.FreeContentProvider;
import fm.matchstats.db.MatchContentProvider;
import fm.matchstats.db.PositionContentProvider;
import fm.matchstats.db.PuckOutContentProvider;
import fm.matchstats.db.ShotContentProvider;
import fm.matchstats.db.TrainingContentProvider;
import fm.matchstats.db.run.PanelListActivity;
import fm.matchstats.db.run.TrainingListActivity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartupActivity extends FragmentActivity {
	private static final String DATABASE_NAME = "team";
	private Context context;
	private String date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Hurling Manager");
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup_layout);
		context = this;

		// set up Buttons
		// One Listener deals with all input
		Button bManagePanel = (Button) findViewById(R.id.managepanel);
		bManagePanel.setOnClickListener(setupClickListener);

		Button bTraining = (Button) findViewById(R.id.training);
		bTraining.setOnClickListener(setupClickListener);

		Button bReset = (Button) findViewById(R.id.resetdata);
		bReset.setOnClickListener(setupClickListener);

		Button bMatch = (Button) findViewById(R.id.match);
		bMatch.setOnClickListener(setupClickListener);

		Button bEmail = (Button) findViewById(R.id.email);
		bEmail.setOnClickListener(setupClickListener);

		Button bQuit = (Button) findViewById(R.id.quit);
		bQuit.setOnClickListener(setupClickListener);
	}

	// Set up Help in ActionBar Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuAction = getMenuInflater();
		menuAction.inflate(R.menu.help_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// create intent with name of help screen to be loaded
		// R.string.startHelp is in the assets
		Intent ihelp = new Intent(this, HelpActivity.class);
		ihelp.putExtra("HELP_ID", R.string.startHelp);
		startActivity(ihelp);
		return super.onMenuItemSelected(featureId, item);
	}

	// Listener do deal with Button Clicks
	OnClickListener setupClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent;
			switch (v.getId()) {
			case R.id.managepanel:
				// start up activity to manage Panel of players
				intent = new Intent(v.getContext(), PanelListActivity.class);
				v.getContext().startActivity(intent);
				break;
			case R.id.training:
				// start up activity to record training attendance
				intent = new Intent(v.getContext(), TrainingListActivity.class);
				v.getContext().startActivity(intent);
				break;
			case R.id.match:
				// start up activity to continue an existing match
				intent = new Intent(v.getContext(), MatchApplication.class);
				v.getContext().startActivity(intent);
				break;
			case R.id.email:
				// call CVSExport class to save database tables to CSV files
				try {
					new CSVExport(context).execute("match");
				} catch (Exception ex) {
					Log.e("Error in StartupActivity", ex.toString());
				}

				// save and email data
				try {
					// get device storage directory
					File sd = Environment.getExternalStorageDirectory();
					// File data = Environment.getDataDirectory();
					// Log.v("sd", "- " + sd);
					// Log.v("data", "- " + data);

					// copy database file from App assets to external storage
					// from where it can be accessed or emailed.
					// If statement to check if its possible to write to storage
					if (sd.canWrite()) {
						// get current database
						File currentDB = new File("/data/data/"
								+ getPackageName() + "/databases/",
								DATABASE_NAME);
						// create new sub directory to store app data
						File dir = new File(sd, "match_BU");
						if (!dir.exists()) {
							dir.mkdirs();
						}
						// get todays date to use as a timestamp when saving
						// files
	//					Date currentDate = new Date(System.currentTimeMillis());
	//					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_");
	//					date = sdf.format(currentDate);
						// add date timestamp to output database file name
						File backupDB = new File(dir, DATABASE_NAME
								+ ".db");

						// copy database to storage
						// N.B. this code based on
						// http://stackoverflow.com/questions/11043175/trying-to-copy-sqlite-db-from-data-to-sd-card
						FileInputStream in = new FileInputStream(currentDB);
						FileOutputStream out = new FileOutputStream(backupDB);
						FileChannel src = in.getChannel();
						FileChannel dst = out.getChannel();
						dst.transferFrom(src, 0, src.size());
						in.close();
						out.close();
					}
				} catch (IOException e) {
					Log.v("error in save DB", "- " + e.toString());
				}
				// set up for emailing database and CSV files from storage
				Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "match database");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "match data attched");
				emailIntent.setType("text/plain");
				String[] emailAttachments = new String[] {
		//				Environment.getExternalStorageDirectory()
		//						+ "/match_BU/" + date + DATABASE_NAME + ".db",
						Environment.getExternalStorageDirectory()
								+ "/match_BU/"  + "shots.csv",
						Environment.getExternalStorageDirectory()
								+ "/match_BU/"  + "frees.csv",
						Environment.getExternalStorageDirectory()
								+ "/match_BU/"  + "puckouts.csv",
						Environment.getExternalStorageDirectory()
								+ "/match_BU/"  + "positions.csv" };
				// put email attachments into an ArrayList
				ArrayList<Uri> uris = new ArrayList<Uri>();
				for (String file : emailAttachments) {
					File uriFiles = new File(file);
					Uri u = Uri.fromFile(uriFiles);
					uris.add(u);
				}
				emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						uris);
				startActivity(Intent.createChooser(emailIntent, "Email:"));
				break;
			case R.id.resetdata:
				// start up new match for recording
				// reset persisted data in SharedPreferences files for each of
				// the three Activities: Setup, Record, Review
				SharedPreferences sharedPrefRecord = getSharedPreferences(
						"team_stats_record_data", Context.MODE_PRIVATE);
				SharedPreferences.Editor editorRecord = sharedPrefRecord.edit();
				editorRecord.putLong("STARTTIME", 0);
				editorRecord.putString("TIMERBUTTON", "start");
				editorRecord.putString("HALFTEXT", "START FIRST TIME");
				editorRecord.putInt("HOMEGOALS", 0);
				editorRecord.putInt("HOMEPOINTS", 0);
				editorRecord.putInt("OPPGOALS", 0);
				editorRecord.putInt("OPPPOINTS", 0);
				editorRecord.putLong("MATCHID", -1);
				editorRecord.putString("OURTEAM", "OWN TEAM");
				editorRecord.putString("OPPTEAM", "OPPOSITION TEAM");
				editorRecord.putInt("MINSPERHALF", 30);
				editorRecord.commit();

				SharedPreferences sharedPrefReview = getSharedPreferences(
						"team_stats_review_data", Context.MODE_PRIVATE);
				SharedPreferences.Editor editorReview = sharedPrefReview.edit();
				editorReview.putString("OURTEAM", "OWN TEAM");
				editorReview.putString("OPPTEAM", "OPPOSITION TEAM");

				editorReview.putInt("HOMEGOALS", 0);
				editorReview.putInt("HOMEPOINTS", 0);
				editorReview.putInt("OPPGOALS", 0);
				editorReview.putInt("OPPPOINTS", 0);
				editorReview.commit();

				editorReview.putInt("SHOTGOALSHOME", 0);
				editorReview.putInt("SHOTPOINTSHOME", 0);
				editorReview.putInt("SHOTWIDESHOME", 0);
				editorReview.putInt("SHOT45SHOME", 0);
				editorReview.putInt("SHOTSAVEDHOME", 0);
				editorReview.putInt("SHOTSHORTHOME", 0);
				editorReview.putInt("SHOTPOSTSHOME", 0);
				editorReview.putInt("SHOTHOME", 0);
				editorReview.putInt("SHOTGOALSPLAYHOME", 0);
				editorReview.putInt("SHOTPOINTSPLAYHOME", 0);
				editorReview.putInt("SHOTGOALSOPP", 0);
				editorReview.putInt("SHOTPOINTSOPP", 0);
				editorReview.putInt("SHOTWIDESOPP", 0);
				editorReview.putInt("SHOT45SOPP", 0);
				editorReview.putInt("SHOTSAVEDOPP", 0);
				editorReview.putInt("SHOTSHORTOPP", 0);
				editorReview.putInt("SHOTPOSTSOPP", 0);
				editorReview.putInt("SHOTOPP", 0);
				editorReview.putInt("SHOTGOALSPLAYOPP", 0);
				editorReview.putInt("SHOTPOINTSPLAYOPP", 0);

				editorReview.putInt("FREEWONHOME", 0);
				editorReview.putInt("FREEWONOWNHOME", 0);
				editorReview.putInt("FREEWONOPPHOME", 0);
				editorReview.putInt("FREEWONOPP", 0);
				editorReview.putInt("FREEWONOWNOPP", 0);
				editorReview.putInt("FREEWONOPPOPP", 0);

				editorReview.putInt("PUCKWONCLEANHOME", 0);
				editorReview.putInt("PUCKLOSTCLEANHOME", 0);
				editorReview.putInt("PUCKWONBREAKHOME", 0);
				editorReview.putInt("PUCKLOSTBREAKHOME", 0);
				editorReview.putInt("PUCKOTHERHOME", 0);
				editorReview.putInt("PUCKWONCLEANOPP", 0);
				editorReview.putInt("PUCKLOSTCLEANOPP", 0);
				editorReview.putInt("PUCKWONBREAKOPP", 0);
				editorReview.putInt("PUCKLOSTBREAKOPP", 0);
				editorReview.putInt("PUCKOTHEROPP", 0);
				editorReview.commit();

				SharedPreferences sharedPrefSetup = getSharedPreferences(
						"team_stats_setup_data", Context.MODE_PRIVATE);
				SharedPreferences.Editor editorSetup = sharedPrefSetup.edit();
				editorSetup.putString("MATCHDATE", "");
				editorSetup.putString("HOMETEAM", "");
				editorSetup.putString("OPPTEAM", "");
				editorSetup.putString("LOCATION", "");
				editorSetup.putInt("MATCHSAVED", 0);
				editorSetup.commit();
				// empty databases
				getContentResolver().delete(
						Uri.parse(FreeContentProvider.CONTENT_URI + "/"), null,
						null);
				getContentResolver().delete(
						Uri.parse(ShotContentProvider.CONTENT_URI + "/"), null,
						null);
				getContentResolver().delete(
						Uri.parse(PuckOutContentProvider.CONTENT_URI + "/"),
						null, null);
				getContentResolver().delete(
						Uri.parse(MatchContentProvider.CONTENT_URI + "/"),
						null, null);
				// getContentResolver().delete(
				// Uri.parse(TrainingContentProvider.CONTENT_URI + "/"),
				// null, null);
				// getContentResolver().delete(
				// Uri.parse(AttendanceContentProvider.CONTENT_URI + "/"),
				// null, null);
				getContentResolver().delete(
						Uri.parse(PositionContentProvider.CONTENT_URI + "/"),
						null, null);

				Intent intent6 = new Intent(v.getContext(),
						MatchApplication.class);
				v.getContext().startActivity(intent6);
				break;
			case R.id.quit:
				// close activity and quit
				finish();
				break;
			}

		}
	};
}
