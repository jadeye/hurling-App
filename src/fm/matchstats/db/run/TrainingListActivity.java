/*
 *  TrainingListActivity.java
 *
 *  Written by: Fintan Mahon
 *  Description: Records attendance at training
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats.db.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import fm.matchstats.HelpActivity;
import fm.matchstats.R;
import fm.matchstats.db.AttendanceContentProvider;
import fm.matchstats.db.PanelContentProvider;
import fm.matchstats.db.TrainingContentProvider;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;

public class TrainingListActivity extends Activity {

	private ArrayList panelList = new ArrayList(); // ArrayList
	private Cursor c1;
	private String[] players, playersID;
	private boolean[] playersCheck;
	private TextView listPlayers, tLoc, tDate, tComments;
	private StringBuilder str = new StringBuilder("");
	private String panelName;
	private TextView tPanelName;
	private ListView trainList, tListView;
	private ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.train_layout);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Training");
		// get panel name from storage too
		SharedPreferences sharedPrefPanel = this.getSharedPreferences(
				"panellist", Context.MODE_PRIVATE);
		panelName = sharedPrefPanel.getString("PANELNAME", null);
		tPanelName = (TextView) findViewById(R.id.tvTraining);
		tPanelName.setText("Training Attendance for: " + panelName);

/*		// read in panel from database into cursor
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		String[] projection = { PanelContentProvider.PANELID,
				PanelContentProvider.FIRSTNAME, PanelContentProvider.SURNAME,
				PanelContentProvider.NICKNAME };

		CursorLoader cL;
		if (panelName == null)
			cL = new CursorLoader(this, allTitles, projection,
					PanelContentProvider.PANELNAME + " is null ", null,
					PanelContentProvider.NICKNAME);
		else
			cL = new CursorLoader(this, allTitles, projection,
					PanelContentProvider.PANELNAME + " = '" + panelName + "'",
					null, PanelContentProvider.NICKNAME);
		Cursor c1 = cL.loadInBackground();
		// make sure cursor is not empty to avoid crash
		// assign player
		if (c1.getCount() > 0)
			c1.moveToFirst();
		if (c1.getCount() > 0) {
			players = new String[c1.getCount()];
			playersID = new String[c1.getCount()];
			playersCheck = new boolean[c1.getCount()];
			for (int k = 0; k < c1.getCount(); k++) {
				players[k] = String.format("%1$-6s", c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.NICKNAME)));
				playersID[k] = c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.PANELID));
				playersCheck[k] = false;
				c1.moveToNext();
			}
		}
*/
		// set up text view and buttons
		tDate = (EditText) findViewById(R.id.trainDate);
		tComments = (EditText) findViewById(R.id.trainComment);
		tLoc = (EditText) findViewById(R.id.trainLoc);
		//select players present button
		Button bT = (Button) findViewById(R.id.bTraining);
		bT.setOnClickListener(playerSelectClickListener);
		//save players button
		Button bButtonSave = (Button) findViewById(R.id.trainSave);
		listPlayers = (TextView) findViewById(R.id.trainList);
		// review attendance by player
		Button bReview = (Button) findViewById(R.id.bTrainReview);
		// email csv file
		Button bSave = (Button) findViewById(R.id.bTrainEmail);
		bSave.setOnClickListener(emailClickListener);

		// insert date
		Date currentDate = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		tDate.setText(sdf.format(currentDate));

		// read in player list
		fillData();

		// Listener for Save Button
		bButtonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// save data to database
				// validate not saved already and no null values
				if ((tDate.getText().toString().equals(""))
						|| (tDate.getText().toString().equals(" *"))
						|| (panelList.size() < 1)) {

					Toast.makeText(
							TrainingListActivity.this,
							"not saved, please enter date \n"
									+ "and/or select some players",
							Toast.LENGTH_LONG).show();
				} else {
					// save match details to db
					ContentValues values = new ContentValues();
					values.put("date", tDate.getText().toString());
					values.put("locn", tLoc.getText().toString());
					values.put("panelname", panelName);
					values.put("comments", tComments.getText().toString());
					Uri uri = getContentResolver().insert(
							TrainingContentProvider.CONTENT_URI, values);
					long trainingID = Long.parseLong(uri.getLastPathSegment());
					// pass match ID to Record Fragment to act as foreign
					// key in database
					Toast.makeText(TrainingListActivity.this,
							"Training Attendance Saved", Toast.LENGTH_LONG)
							.show();

					ContentValues playersDB = new ContentValues();
					for (int m = 0; m < panelList.size(); m++) {
						playersDB.put("trainingID", trainingID);
						playersDB.put("playerID",
								playersID[(Integer) panelList.get(m)]);

						uri = getContentResolver().insert(
								AttendanceContentProvider.CONTENT_URI,
								playersDB);
					}
				}
				// reset list to null
				panelList.clear();
				listPlayers.setText(null);
				fillData();
				fillList();
			}
		});

		// review attendance by player
		bReview.setOnClickListener(writeSummary);
		trainList = (ListView) findViewById(R.id.trainListView);
		fillList();

	}

	// ////////////////////////////////////////////////////////////////////////////////

	//read in players into string arrays
	private void fillData() {

		// read in panel from database into cursor
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		String[] projection = { PanelContentProvider.PANELID,
				PanelContentProvider.FIRSTNAME, PanelContentProvider.SURNAME,
				PanelContentProvider.NICKNAME };

		CursorLoader cL;
		if (panelName == null)
			cL = new CursorLoader(this, allTitles, projection,
					PanelContentProvider.PANELNAME + " is null ", null,
					PanelContentProvider.NICKNAME);
		else
			cL = new CursorLoader(this, allTitles, projection,
					PanelContentProvider.PANELNAME + " = '" + panelName + "'",
					null, PanelContentProvider.NICKNAME);
		Cursor c1 = cL.loadInBackground();
		// make sure cursor is not empty to avoid crash
		// assign player
		if (c1.getCount() > 0)
			c1.moveToFirst();
		if (c1.getCount() > 0) {
			players = new String[c1.getCount()];
			playersID = new String[c1.getCount()];
			playersCheck = new boolean[c1.getCount()];
			for (int k = 0; k < c1.getCount(); k++) {
				players[k] = String.format("%1$-6s", c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.NICKNAME)));
				playersID[k] = c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.PANELID));
				playersCheck[k] = false;
				c1.moveToNext();
			}
		}
	}

	private void fillList() {
		// create uri to get panel data from content provider
		Uri allTitles = TrainingContentProvider.CONTENT_URI;
		// specify which columns to retrieve from database
		String[] projection = { TrainingContentProvider._ID,
				TrainingContentProvider.DATE, TrainingContentProvider.LOCATION };

		// Create array to specify fields to display in the list
		String[] from = new String[] { TrainingContentProvider.DATE,
				TrainingContentProvider.LOCATION };

		String selection;
		if (panelName != null) {
			selection = TrainingContentProvider.PANELNAME + " = '" + panelName
					+ "'";
		} else {
			selection = TrainingContentProvider.PANELNAME + " is null";
		}

		// create array to map these fields to
		int[] to = new int[] { R.id.text1, R.id.text2 };

		// load database info from PanelContentProvider into a cursor and use an
		// adapter to display on screen
		CursorLoader cL = new CursorLoader(this, allTitles, projection,
				selection, null, TrainingContentProvider._ID + " desc");
		c1 = cL.loadInBackground();
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(this,
				R.layout.train_row_layout, c1, from, to, 0);
		trainList.setAdapter(reminders);
		registerForContextMenu(trainList);
		trainList.setOnItemClickListener(rowListener);
	}

	private OnClickListener writeSummary = new OnClickListener() {
		@Override
		public void onClick(View w) {
			if (reviewAttendance())
				openCSV();
		}
	};

	private OnClickListener emailClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			if (reviewAttendance())
				emailFile();
		}
	};

	private void emailFile() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_SUBJECT, "Training attendance summary");
		intent.putExtra(Intent.EXTRA_TEXT, "Training attendance attached");
		File file = new File(Environment.getExternalStorageDirectory(),
				"match_BU");
		file = new File(file, "training_summary.csv");

		if (!file.exists() || !file.canRead()) {
			Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		Uri uri = Uri.parse("file://" + file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intent, "Send email..."));
	}

	// display list of selected players in
	private boolean reviewAttendance() {
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<String> locations = new ArrayList<String>();
		ArrayList<String> comments = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		int[][] outputInt;// aray to output 1 or 0 for attendance
		TreeMap<String, Integer> namesTree = new TreeMap<String, Integer>();
		HashMap<String, Integer> eventsHash = new HashMap<String, Integer>();
		CSVWriter csvWrite;
		// get SQL query back from TrainingContentProvided
		Uri allTitles = Uri.parse(TrainingContentProvider.CONTENT_URI + "R");
		// results of query into cursor
		// results come back sorted by training event and then by nickname
		Cursor c1 = getContentResolver().query(allTitles, null, panelName,
				null, null);
		// Add unique names into hashmap and arraylist

		// Add unique training events into hashmap and arraylist
		if (c1.getCount() > 0) {
			int iN = 0, iE = 0;
			c1.moveToFirst();
			do {
				if (!namesTree.containsKey(c1.getString(4))) {
					namesTree.put(c1.getString(4), iN);
					iN++;
				}
				if (!eventsHash.containsKey(c1.getString(0))) {
					dates.add(iE, c1.getString(1));
					locations.add(iE, c1.getString(2));
					comments.add(iE, c1.getString(3));
					eventsHash.put(c1.getString(0), iE);
					iE++;
				}
			} while (c1.moveToNext());
			for (Map.Entry<String, Integer> entry : namesTree.entrySet()) {
				names.add(entry.getKey());
			}
			/*
			 * for (iN = 0; iN < namesTree.size(); iN++) { names.add(iN,
			 * c1.getString(4));
			 * 
			 * iN++; }
			 */
			// initialise array to 0
			outputInt = new int[eventsHash.size()][namesTree.size()];
			for (int i = 0; i < eventsHash.size(); i++) {
				for (int j = 0; j < namesTree.size(); j++) {
					outputInt[i][j] = 0;
				}
			}

			// write attendance for each player into array as 1
			c1.moveToFirst();
			do {
				outputInt[eventsHash.get(c1.getString(0))][namesTree.get(c1
						.getString(4))] = 1;
				} while (c1.moveToNext());

			// sum up total attendance for each player
			int[] total = new int[namesTree.size()];
			for (int i = 0; i < namesTree.size(); i++) {
				total[i] = 0;
				for (int j = 0; j < eventsHash.size(); j++) {
					total[i] = total[i] + outputInt[j][i];
				}
			}

			// ///////////////////////////////////////////////////////////////////

			File exportDir = new File(
					Environment.getExternalStorageDirectory(), "match_BU");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			// write summary data from training
			// put date first
			File file = new File(exportDir, "training_summary.csv");
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// 3 rows of headers: date, location, comments
				String[] write = new String[eventsHash.size() + 2];
				write[0] = "";
				write[1] = "";
				// write header dates, decrement so as to
				for (int i = 0; i < eventsHash.size(); i++) {
					write[i + 2] = dates.get(i);
				}
				// write header location
				csvWrite.writeNext(write);
				for (int i = 0; i < eventsHash.size(); i++) {
					write[i + 2] = locations.get(i);
				}
				// write header commnets
				csvWrite.writeNext(write);
				for (int i = 0; i < eventsHash.size(); i++) {
					write[i + 2] = comments.get(i);
				}
				// 2nd column is for totals
				write[1] = "Total";
				csvWrite.writeNext(write);

				// write total and attendance for each player
				// sort firsti
				int i = 0;
				for (Map.Entry<String, Integer> entry : namesTree.entrySet()) {
					names.add(entry.getKey());

					write[0] = names.get(i);
					write[1] = String.valueOf(total[entry.getValue()]);
					/*
					 * for (int j = eventsHash.size()-1; j >= 0; j--) {
					 * write[eventsHash.size()-j + 1] =
					 * String.valueOf(outputInt[j][entry.getValue()]); }
					 */
					for (int j = 0; j < eventsHash.size(); j++) {
						write[j + 2] = String.valueOf(outputInt[j][entry
								.getValue()]);
					}
					csvWrite.writeNext(write);
					i++;
				}

				csvWrite.close();
				c1.close();
			} catch (IOException e) {
				Log.e("training summary", e.getMessage(), e);
			}
			return true;
		} else
			Toast.makeText(TrainingListActivity.this,
					"error, no data to review", Toast.LENGTH_LONG).show();
		return false;
	}

	private void openCSV() {
		File file = new File(Environment.getExternalStorageDirectory(),
				"match_BU");
		file = new File(file, "training_summary.csv");
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(file), "text/csv");
		startActivity(i);
	}

	// display list of selected players in
	private void updateTrainingList() {
		// set string to blank first
		str.replace(0, str.length(), "");
		// add in each player from selected list
		for (int k = 0; k < panelList.size(); k++) {
			str.append((k + 1) + ". " + players[(Integer) panelList.get(k)]
					+ "\n");
		}
		listPlayers.setText(str);
	}

	OnClickListener playerSelectClickListener = new OnClickListener() {
		@Override
		public void onClick(View w) {
			new AlertDialog.Builder(TrainingListActivity.this)
					.setTitle("training attendees")
					.setMultiChoiceItems(players, playersCheck,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									if (isChecked) {
										// If the user checked the item, add it
										// to the selected items
										panelList.add(which);
										playersCheck[which] = true;
									} else if (panelList.contains(which)) {
										// Else, if the item is already in the
										// array, remove it
										panelList.remove(Integer.valueOf(which));
										playersCheck[which] = false;
									}
								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// call method to display list of players
									updateTrainingList();
								}
							}).create().show();
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// AdapterView.AdapterContextMenuInfo info =
		// (AdapterView.AdapterContextMenuInfo) menuInfo;
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.list_menu_longpress, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem itemC) {

		switch (itemC.getItemId()) {
		case R.id.menu_delete1:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) itemC
					.getMenuInfo();
			Uri uri = Uri.parse(TrainingContentProvider.CONTENT_URI + "/"
					+ info.id);
			getContentResolver().delete(uri, null, null);
			Toast.makeText(getApplicationContext(), "Training Session Deleted",
					Toast.LENGTH_LONG).show();
			fillList();
			return true;
		}
		return super.onContextItemSelected(itemC);
	}

	// implement help menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuAction = getMenuInflater();
		menuAction.inflate(R.menu.help_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.helpList:

			Intent ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.trainingHelp);
			startActivity(ihelp);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	OnItemClickListener rowListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int rowID,
				long trainID) {
				String[] valuesPlayers;
			tListView = new ListView(getApplicationContext());

			Uri uri1 = Uri.parse(TrainingContentProvider.CONTENT_URI + "/"
					+ trainID);
			Cursor c1 = getContentResolver().query(uri1, null, null, null,
					TrainingContentProvider._ID);
			Uri uri2 = Uri.parse(AttendanceContentProvider.CONTENT_URI + "_T");
			Cursor c2 = getContentResolver().query(uri2, null,
					String.valueOf(trainID), null,
					AttendanceContentProvider._ID);
			c2.moveToFirst();
			c1.moveToFirst();

			valuesPlayers = new String[c2.getCount() + 3];

			if (c1.getCount() > 0) {
				valuesPlayers[0] = "Date: "
						+ c1.getString(c1
								.getColumnIndexOrThrow(TrainingContentProvider.DATE));
				valuesPlayers[1] = "Location: "
						+ c1.getString(c1
								.getColumnIndexOrThrow(TrainingContentProvider.LOCATION));
				valuesPlayers[2] = "Comments:"
						+ c1.getString(c1
								.getColumnIndexOrThrow(TrainingContentProvider.COMMENTS));
				int i = 3;
				do {
					valuesPlayers[i] = c2
							.getString(c2
									.getColumnIndexOrThrow(PanelContentProvider.NICKNAME))
							+ " - "
							+ c2.getString(c2
									.getColumnIndexOrThrow(PanelContentProvider.FIRSTNAME))
							+ " "
							+ c2.getString(c2
									.getColumnIndexOrThrow(PanelContentProvider.SURNAME));

						i++;
				} while (c2.moveToNext());
				c1.close();
				c2.close();
			}

			adapter = new ArrayAdapter<String>(TrainingListActivity.this,
					R.layout.single_row_layout, valuesPlayers);
			tListView.setAdapter(adapter);
			new AlertDialog.Builder(TrainingListActivity.this)
					.setTitle("training attendees")
					.setView(tListView)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									// call method to display list of players
									updateTrainingList();
								}
							}).create().show();
		}
		
		
	};

}
