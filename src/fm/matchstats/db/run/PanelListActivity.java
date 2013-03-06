/*
 *  PanelListActivity.java
 *
 *  Written by: Fintan Mahon
 *  Description: Lists panel of players from database with options to delete
 *  phone or text players
 *  
 *  Written on: Jan 2013
 *  
 *  Uses OPENCSV Library for CSV file import/export/parsing from http://opencsv.sourceforge.net/
 * 
 */
package fm.matchstats.db.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fm.matchstats.HelpActivity;
import fm.matchstats.R;
import fm.matchstats.db.PanelContentProvider;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.CSVReader;

public class PanelListActivity extends ListActivity {

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 0;
	private static final int SAVE_FILE = 1;
	private static final int LOAD_FILE = 2;
	private static final int NEW_PANEL = 3;
	private static final int NEW_NAME = 4;
	private static final int RENAME_PANEL = 5;
	private static final String HELP_ID = null;
	private Cursor c1;
	private String panelName, panelNameOld;
	private TextView tPanelName;
	private String panel[];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.panel_list_layout);
		// turn off display title in Action bar to allow
		// room for other menu items.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		// set up panelname display
		tPanelName = (TextView) findViewById(R.id.panelNameList);

		SharedPreferences sharedPref = getSharedPreferences("panellist",
				Context.MODE_PRIVATE);
		panelName = sharedPref.getString("PANELNAME", null);

		if (panelName != null)
			tPanelName.setText("Panel name: " + panelName);
		// call method to list players
		fillData();
		// set up long click method

		registerForContextMenu(getListView());
	}

	@Override
	public void onPause() {
		// Save/persist data to be used on reopen
		super.onPause(); // Always call the superclass method first
		SharedPreferences sharedPref = getSharedPreferences("panellist",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("PANELNAME", panelName);
		editor.commit();
	}

	// method to read in panel list from database and list contents on screen
	private void fillData() {
		// create uri to get panel data from content provider
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		// specify which columns to retrieve from database
		String[] projection = { PanelContentProvider.PANELID,
				PanelContentProvider.FIRSTNAME, PanelContentProvider.SURNAME,
				PanelContentProvider.NICKNAME, PanelContentProvider.PHONE,
				PanelContentProvider.ADDRESS, PanelContentProvider.PANELNAME };

		// Create array to specify fields to display in the list
		String[] from = new String[] { PanelContentProvider.NICKNAME,
				PanelContentProvider.FIRSTNAME, PanelContentProvider.SURNAME,
				PanelContentProvider.PHONE };

		String selection;
		if (panelName != null) {
			selection = PanelContentProvider.PANELNAME + " = '" + panelName
					+ "'";
		} else {
			selection = PanelContentProvider.PANELNAME + " is null";
		}

		// create array to map these fields to
		int[] to = new int[] { R.id.text4, R.id.text1, R.id.text2, R.id.text3 };

		// load database info from PanelContentProvider into a cursor and use an
		// adapter to display on screen
		CursorLoader cL = new CursorLoader(this, allTitles, projection,
				selection, null, PanelContentProvider.NICKNAME);
		c1 = cL.loadInBackground();
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(this,
				R.layout.panel_row_layout, c1, from, to, 0);
		setListAdapter(reminders);
	}

	@Override
	// set up long press menu
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.menu_longpress_3, menu);
	}

	@Override
	// deal with selection from long press menu
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			// Delete a row / player
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			Uri uri = Uri.parse(PanelContentProvider.CONTENT_URI + "/"
					+ info.id);
			getContentResolver().delete(uri, null, null);
			Toast.makeText(getApplicationContext(), "Player Deleted",
					Toast.LENGTH_LONG).show();
			// refresh list on screen
			fillData();
			return true;
		case R.id.dialer:
			// Get phone number for the player and make phone call
			// check device has a phone, could be a tablet
			if (getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_TELEPHONY)) {
				AdapterContextMenuInfo infoText2 = (AdapterContextMenuInfo) item
						.getMenuInfo();
				Uri uriText2 = Uri.parse(PanelContentProvider.CONTENT_URI + "/"
						+ infoText2.id);
				Cursor c2 = getContentResolver().query(uriText2, null, null,
						null, PanelContentProvider.NICKNAME);
				c2.moveToFirst();
				String phoneNo = c2.getString(c2
						.getColumnIndexOrThrow(PanelContentProvider.PHONE));
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + phoneNo.trim()));
				startActivity(intent);
			} else
				Toast.makeText(getBaseContext(),
						"Error, can't make phone calls on this device",
						Toast.LENGTH_LONG).show();
			// refresh list on screen
			fillData();
			return true;
		case R.id.sendText:
			// Get phone number for the player and send text
			// check device has a phone, could be a tablet
			if (getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_TELEPHONY)) {
				AdapterContextMenuInfo infoText3 = (AdapterContextMenuInfo) item
						.getMenuInfo();
				Uri uriText3 = Uri.parse(PanelContentProvider.CONTENT_URI + "/"
						+ infoText3.id);
				Cursor c3 = getContentResolver().query(uriText3, null, null,
						null, PanelContentProvider.NICKNAME);
				c3.moveToFirst();
				String textNo = c3.getString(c3
						.getColumnIndexOrThrow(PanelContentProvider.PHONE));
				Intent intentText = new Intent(Intent.ACTION_VIEW);
				intentText.setData(Uri.parse("sms:" + textNo.trim()));
				startActivity(intentText);
			} else
				Toast.makeText(getBaseContext(),
						"Error, can't send sms texts on this device",
						Toast.LENGTH_LONG).show();
			// refresh list on screen
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	// set up action bar menu
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuAction = getMenuInflater();
		menuAction.inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// deal with selection from action bar menu
		switch (item.getItemId()) {
		case R.id.menu_insert:
			// call method to create player
			createPlayer();
			return true;
		case R.id.helpPanelList:
			// call method to display help screen
			displayHelp();
			return true;
		case R.id.newPanel:
			// call method to display help screen
			inputPanelName(NEW_PANEL);
			return true;
		case R.id.changePanel:
			// call method to change form one panel to another
			changePanel();
			return true;
		case R.id.renamePanel:
			// call method to rename a panel name and assign all the players to
			// new panel name
			renamePanel();
			return true;
		case R.id.deletePanel:
			// call method to delete a panel name and its players
			deletePanel();
			return true;

		case R.id.savePanel: // call method to save current panel to thedevice
								// storage
			// don't try and save if there is no data
			if (c1.getCount() > 0)
				savePanel();
			else
				Toast.makeText(getBaseContext(),
						"error: panel is empty\n", Toast.LENGTH_SHORT)
						.show();

			return true;

		case R.id.loadPanel:
			// call method to load a saved panel from the device storage
			inputPanelName(LOAD_FILE);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	// method to get filename for save/load from user
	private void inputPanelName(final int action) {
		// set up dialog to get filename use edittext in an alertdialog to
		// Prompt for filename
		AlertDialog.Builder alert = new AlertDialog.Builder(
				PanelListActivity.this);
		final EditText input = new EditText(PanelListActivity.this);
		input.setId(999);
		switch (action) {
		case SAVE_FILE:
			alert.setTitle("Save Panel");
			break;
		case LOAD_FILE:
			alert.setTitle("Load a Saved Panel");
			break;
		case NEW_NAME:
		case NEW_PANEL:
			alert.setTitle("Enter a Panel Name");
			break;
		case RENAME_PANEL:
			alert.setTitle("Enter new Panel Name");
			break;

		}

		alert.setMessage("Enter Name:");
		alert.setView(input);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface indialog, int which) {
				String inName = input.getText().toString();

				// call method depending on if its save or load
				if (!inName.equals("")) {
					panelName = inName;
					switch (action) {
					case SAVE_FILE:
						savePanel();
						break;
					case LOAD_FILE:
						loadPanel();
						break;
					case NEW_NAME:
						tPanelName.setText(panelName);
						Intent i = new Intent(getApplicationContext(),
								PanelEditActivity.class);
						i.putExtra(PanelContentProvider.PANELNAME, panelName);
						startActivityForResult(i, ACTIVITY_CREATE);
						break;
					case NEW_PANEL:
						tPanelName.setText("Panel name: " + panelName);
						fillData();
						break;
					case RENAME_PANEL:
						tPanelName.setText("Panel name: " + panelName);
						ContentValues values = new ContentValues();
						int count;
						values.put("panelname", panelName);
						// add to panel database
						if (panelNameOld == null)
							count = getContentResolver()
									.update(PanelContentProvider.CONTENT_URI,
											values,
											PanelContentProvider.PANELNAME
													+ " is null", null);
						else
							count = getContentResolver().update(
									PanelContentProvider.CONTENT_URI,
									values,
									PanelContentProvider.PANELNAME + " = '"
											+ panelNameOld + "'", null);

						Toast.makeText(
								getApplicationContext(),
								"panel renamed and " + count
										+ " players upated", Toast.LENGTH_LONG)
								.show();

						tPanelName.setText("Panel name: " + panelName);
						fillData();

						break;
					}
				} else {
					Toast.makeText(getBaseContext(), "Invalid Name, Try Again",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		alert.create();
		alert.show();
	}

	// method to save panel to csv file
	private void savePanel() {
		// create file in the devices storage directory
		if (panelName != null) {

			String fname = Environment.getExternalStorageDirectory()
					+ File.separator + "match_BU" + File.separator + panelName
					+ ".csv";

			File f = new File(fname);
			try {
				f.createNewFile();
				// use CVSWriter object to parse data into CSV file format
				CSVWriter csvWrite = new CSVWriter(new FileWriter(f));
				csvWrite.writeNext(c1.getColumnNames());
				// write contents of cursor to file
				c1.moveToFirst();
				do {
					String arrStr[] = { c1.getString(0), c1.getString(1),
							c1.getString(2), c1.getString(3), c1.getString(4),
							c1.getString(5), c1.getString(6), };
					csvWrite.writeNext(arrStr);
				} while (c1.moveToNext());
				csvWrite.close();
				Toast.makeText(getBaseContext(), "Panel Saved at " + fname,
						Toast.LENGTH_LONG).show();
				// refresh list on screen
				fillData();
			} catch (IOException e) {
				Log.e("save panel failed", e.getMessage(), e);
			}
		} else
			Toast.makeText(
					getBaseContext(),
					"error: panel name is empty\n"
							+ "use rename panel to enter a name",
					Toast.LENGTH_SHORT).show();

	}

	// method to load panel from existing csv file
	private void loadPanel() {
		ContentValues values = new ContentValues();
		String[] inputCSV;
		int readOK = 0;
		// get storage directory
		String fname = Environment.getExternalStorageDirectory()
				+ File.separator + "match_BU" + File.separator + panelName
				+ ".csv";
		if (new File(fname).exists()) {
			try {
				FileInputStream fileStream = new FileInputStream(fname);
				InputStreamReader inStreamReader = new InputStreamReader(
						fileStream);
				// use CVSReader object to read and parse data from CSV file
				CSVReader csvRead = new CSVReader(inStreamReader);
				// throw away the header
				csvRead.readNext();
				// read in row at a time and write to database using
				// PanelContentProvider
				while ((inputCSV = csvRead.readNext()) != null) {
					values.put("firstname", inputCSV[1]);
					values.put("surname", inputCSV[2]);
					values.put("nickname", inputCSV[3]);
					values.put("phone", inputCSV[4]);
					values.put("address", inputCSV[5]);
					// old versions of file don't have panelname so use try
					try {
						values.put("panelname", inputCSV[6]);
						panelName = inputCSV[6];
						readOK = 1;
					} catch (ArrayIndexOutOfBoundsException e) {
						Log.e("old file", e.toString());
					}

					// add to panel database
					getContentResolver().insert(
							PanelContentProvider.CONTENT_URI, values);
				}
				csvRead.close();
				Toast.makeText(getBaseContext(),
						"Panel read successfully from\n" + fname,
						Toast.LENGTH_LONG).show();
				if (readOK == 0)
					panelName = null;
				tPanelName.setText("Panel name: " + panelName);

				fillData();
			} catch (IOException e) {
				Log.e("load panel failed", e.getMessage(), e);
			}

		} else
			Toast.makeText(getBaseContext(),
					"Can't find file, try again.\n" + fname, Toast.LENGTH_LONG)
					.show();
	}

	// method to change panel
	private void changePanel() {
		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		// get list of panel names
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		String[] projection = { PanelContentProvider.PANELNAME };
		CursorLoader cL = new CursorLoader(this, allTitles, projection, null,
				null, PanelContentProvider.PANELNAME);
		c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.PANELNAME));
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
			// put names in string array
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				if (panelList.get(i) == null)
					panel[i] = "null";
				else
					panel[i] = (String) panelList.get(i);

			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					PanelListActivity.this);
			builder.setTitle("select panel to change to");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (panel[which].equals("null"))
								panelName = null;
							else
								panelName = panel[which];
							dialog.dismiss();
							tPanelName.setText("Panel name: " + panelName);
							fillData();

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else
			Toast.makeText(getApplicationContext(), "no panels entered yet",
					Toast.LENGTH_LONG).show();

	}

	// method to rename a panel and players on it
	private void renamePanel() {

		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		// get list of panel names
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		String[] projection = { PanelContentProvider.PANELNAME };
		CursorLoader cL = new CursorLoader(this, allTitles, projection, null,
				null, PanelContentProvider.PANELNAME);
		c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.PANELNAME));
				// create arraylist of panelnames
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
			// put names in string array
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				if (panelList.get(i) == null)
					panel[i] = "null";
				else
					panel[i] = (String) panelList.get(i);

			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					PanelListActivity.this);
			builder.setTitle("select panel to rename");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (panel[which].equals("null"))
								panelNameOld = null;
							else
								panelNameOld = panel[which];

							inputPanelName(RENAME_PANEL);
							dialog.dismiss();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else
			Toast.makeText(getApplicationContext(), "no panels entered yet",
					Toast.LENGTH_LONG).show();

	}

	// method to delete a panel and players on it
	private void deletePanel() {
		ArrayList<String> panelList = new ArrayList<String>();
		String str;
		// get list of panel names
		Uri allTitles = PanelContentProvider.CONTENT_URI;
		String[] projection = { PanelContentProvider.PANELNAME };
		CursorLoader cL = new CursorLoader(this, allTitles, projection, null,
				null, PanelContentProvider.PANELNAME);
		c1 = cL.loadInBackground();
		if (c1.getCount() > 0) {
			c1.moveToFirst();
			do {
				str = c1.getString(c1
						.getColumnIndexOrThrow(PanelContentProvider.PANELNAME));
				if (!panelList.contains(str))
					panelList.add(str);
			} while (c1.moveToNext());
			// put names in string array
			panel = new String[panelList.size()];
			for (int i = 0; i < panelList.size(); i++) {
				if (panelList.get(i) == null)
					panel[i] = "null";
				else
					panel[i] = (String) panelList.get(i);

			}
			c1.close();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					PanelListActivity.this);
			builder.setTitle("select panel to delete");
			builder.setSingleChoiceItems(panel, 0,
					new DialogInterface.OnClickListener() {
						String panelNameDel;

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (panel[which].equals("null"))
								panelNameDel = null;
							else
								panelNameDel = panel[which];
							Uri uri = PanelContentProvider.CONTENT_URI;
							int count;
							if (panelNameDel == null)
								count = getContentResolver().delete(
										uri,
										PanelContentProvider.PANELNAME
												+ " is null", null);
							else
								count = getContentResolver().delete(
										uri,
										PanelContentProvider.PANELNAME + " = '"
												+ panelNameDel + "'", null);
							Toast.makeText(getApplicationContext(),
									"Panel and " + count + " Players Deleted",
									Toast.LENGTH_LONG).show();

							dialog.dismiss();
							if (panelNameDel != null) {
								if (panelNameDel.toString().equals(
										panelNameDel.toString())) {
									panelName = null;
									tPanelName.setText("Panel name: "
											+ panelName);
								}
							}

							fillData();

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		} else
			Toast.makeText(getApplicationContext(), "no panels entered yet",
					Toast.LENGTH_LONG).show();

	}

	// method to create player by calling PanelEditActivity class to get and
	// save player details
	private void createPlayer() {
		// get panename if it doesn't exist
		if (panelName == null) {
			inputPanelName(NEW_NAME);
		} else {
			Intent i = new Intent(this, PanelEditActivity.class);
			i.putExtra(PanelContentProvider.PANELNAME, panelName);
			startActivityForResult(i, ACTIVITY_CREATE);
		}
	}

	// method to display the relevant help screen by calling HelpActivity class.
	// HELP_ID intent parameter is used to specify which help screen to load
	private void displayHelp() {
		Intent ihelp = new Intent(this, HelpActivity.class);
		ihelp.putExtra("HELP_ID", R.string.panelListHelp);
		startActivity(ihelp);
	}

	@Override
	// method to deal with user touching a row/player on the list
	// launch PanelEditActivity with an intent and passing in the the row/player
	// id do that the player details can be edited
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, PanelEditActivity.class);
		i.putExtra(PanelContentProvider.PANELID, id);
		i.putExtra(PanelContentProvider.PANELNAME, panelName);
		// use startActivityForResult so that you can run method
		// onActivityResult on return
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	// method run when PanelContentProvider returns to refresh screen with
	// updated list of players
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}
}
