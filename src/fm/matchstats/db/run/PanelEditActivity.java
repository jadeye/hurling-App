/*
 *  PanelEditActivity.java
 *
 *  Written by: Fintan Mahon
 *  Description: Creates a new player and adds to database or edits details of an existing player 
 *  and updates the database
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats.db.run;

import fm.matchstats.HelpActivity;
import fm.matchstats.R;
import fm.matchstats.db.PanelContentProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PanelEditActivity extends Activity {

	// set up variables
	private EditText pFirstnameText;
	private EditText pSurnameText;
	private EditText pNicknameText;
	private EditText pPhoneText;
	private EditText pAddressText;
	private Button pConfirmButton;
	private Long pRowId;
	private String pPanelName;
	private TextView tPanelName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.panel_edit_layout);

		// uncomment next line to input dummy panel if needed for testing
		// setPanel();

		// initialise buttons and edittexts
		pFirstnameText = (EditText) findViewById(R.id.firstname);
		pSurnameText = (EditText) findViewById(R.id.surname);
		pNicknameText = (EditText) findViewById(R.id.nickname);
		pPhoneText = (EditText) findViewById(R.id.phone);
		pAddressText = (EditText) findViewById(R.id.address);
		pConfirmButton = (Button) findViewById(R.id.savebutton);
		tPanelName = (TextView) findViewById(R.id.panelNameEdit);

		// get a row id and panelname if restarting the Activity and there is
		// one saved
		pRowId = savedInstanceState != null ? savedInstanceState
				.getLong(PanelContentProvider.PANELID) : 0;
		pPanelName = savedInstanceState != null ? savedInstanceState
				.getString(PanelContentProvider.PANELNAME) : null;
		Log.v("panelName start", "-- " + pPanelName);

		// get panelname from intent
		Bundle extras = getIntent().getExtras();
		pPanelName = extras != null ? extras
				.getString(PanelContentProvider.PANELNAME) : null;
		Log.v("panelName intent", "-- " + pPanelName);

		tPanelName.setText("Panel name: "+pPanelName);

		// Log.v("mrow", "- " + mRowId);
		// fix for savedinstancestate crash/problem. Can't save null with
		// onSaveInstanceState when activity stops so save -1 as equivalent
		// -1 equivalent to null
		// so mRowId can by null if coming from PanelListActivity or -1 if
		// coming from Application restart
		// if ((pRowId != null) && (pRowId < 0))
		// pRowId = null;

		// call method to get screen input and then create or update player
		registerButtonListenersAndSetDefaultText();
	}

	@Override
	// when starting the activity get the row/player id from the calling
	// activity if one is passed in in intent
	protected void onResume() {
		super.onResume();
		// call method to get row id
		setRowIdFromIntent();
		// call method to update screen with player values for editing
		populateFields();
	}

	// check if row id is passed in from calling activity intent
	private void setRowIdFromIntent() {
		Log.v("intent", "- " + pRowId);
		if (pRowId == 0) {
			Bundle extras = getIntent().getExtras();
			pRowId = extras != null ? extras
					.getLong(PanelContentProvider.PANELID) : 0;
			Log.v("intent", "- " + pRowId);
		}
	}

	// for editing an existing player this method retrieves player details from
	// database based on the id and displays them on screen
	private void populateFields() {
		// Only populate the text boxes
		// if the row is not null from the database.
		if (pRowId > 0) {
			Uri uri = Uri
					.parse(PanelContentProvider.CONTENT_URI + "/" + pRowId);
			Cursor c1 = getContentResolver().query(uri, null, null, null,
					PanelContentProvider.NICKNAME);
			c1.moveToFirst();
			pFirstnameText.setText(c1.getString(c1
					.getColumnIndexOrThrow(PanelContentProvider.FIRSTNAME)));
			pSurnameText.setText(c1.getString(c1
					.getColumnIndexOrThrow(PanelContentProvider.SURNAME)));
			pNicknameText.setText(c1.getString(c1
					.getColumnIndexOrThrow(PanelContentProvider.NICKNAME)));
			pPhoneText.setText(c1.getString(c1
					.getColumnIndexOrThrow(PanelContentProvider.PHONE)));
			pAddressText.setText(c1.getString(c1
					.getColumnIndexOrThrow(PanelContentProvider.ADDRESS)));
			c1.close();
		}
	}

	@Override
	// save current row id if its not null and the activity stops
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(PanelContentProvider.PANELNAME, pPanelName);

		// saving null to outState causes crash, use -1 instead
		// Long mRowIdOut;
		// if (pRowId == null)
		// mRowIdOut = (long) -1;
		// else
		// mRowIdOut = pRowId;
		outState.putLong(PanelContentProvider.PANELID, pRowId);
	}

	// Save Button Listener
	private void registerButtonListenersAndSetDefaultText() {
		pConfirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			// validate that nickname is between 3 and 6 characters long
			public void onClick(View view) {
				if ((pNicknameText.getText().toString().length() > 2)
						&& (pNicknameText.getText().toString().length() <= 6)) {
					// if OK call method to save entered details to database
					saveState();
					setResult(RESULT_OK);
					Toast.makeText(PanelEditActivity.this,
							getString(R.string.task_saved_message),
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(
							PanelEditActivity.this,
							"You must enter a Nickname \nbetween 3 and 6 characters long\n\nTry again please",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	// method to save new player or update existing player details in database
	private void saveState() {
		if (pRowId == 0) {
			// insert new player using PanelContentProvider
			ContentValues values = new ContentValues();
			values.put("firstname", pFirstnameText.getText().toString());
			values.put("surname", pSurnameText.getText().toString());
			values.put("nickname", pNicknameText.getText().toString());
			values.put("phone", pPhoneText.getText().toString());
			values.put("address", pAddressText.getText().toString());
			values.put("panelname", pPanelName);
			Uri uri = getContentResolver().insert(
					PanelContentProvider.CONTENT_URI, values);
			long id = Long.parseLong(uri.getLastPathSegment());
			if (id > 0) {
				pRowId = id;
				}
		} else {
			// update existing player using PanelContentProvider
			ContentValues values = new ContentValues();
			values.put("firstname", pFirstnameText.getText().toString());
			values.put("surname", pSurnameText.getText().toString());
			values.put("nickname", pNicknameText.getText().toString());
			values.put("phone", pPhoneText.getText().toString());
			values.put("address", pAddressText.getText().toString());
			values.put("panelname", pPanelName);
			Uri uri = Uri
					.parse(PanelContentProvider.CONTENT_URI + "/" + pRowId);
			getContentResolver().update(uri, values, null, null);
		}
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
		ihelp = new Intent(this, HelpActivity.class);
		ihelp.putExtra("HELP_ID", R.string.panelEditHelp);
		startActivity(ihelp);
		return true;
	}

	// insert panel into database for testing/development
	protected void setPanel() {
		String[][] panelIn = {
				{ "Sean", "Comerford", "SeanL", "086 8599235",
						"87 fortfield road terenure D6W" },
				{ "Eoin", "Dennehy", "Fitzie", "086 1642994",
						"94 Wainsfort Rd, Terenure, dublin 6W" },
				{ "Gavin", "Douglas", "Gav", "086 8390910",
						"14 anne devlin road rathfarnham D14" },
				{ "Daragh", "Elmore", "DaraA", "086 2268492",
						"357a Orwell Park Close, Templeogue, Dublin 6W" },
				{ "Hugh", "Fitzgibbon", "Hugh", "087 2151417",
						"19 Hazelbrook Rd, Terenure, Dublin 6W" },
				{ "Ciaran", "Gillligan", "Brady", "085 1683887",
						"25 Terenure Road West, Dublin 6W" },
				{ "Denis", "Hynfman", "Denis", "086 8559573",
						"6 Grangebrook Park, Whitechurch Road, Dublin 16" },
				{ "Eoin", "Magee", "Prendy", "087 2733618",
						"12 The Court, Cypress Downs, Dublin 6W" },
				{ "Sean", "Mahon", "SeanM", "087 2750141",
						"45 Templeogue Village" },
				{ "Daragh", "MacCormac", "DaraOS", "087 3315979",
						"25a Forest Close, Kingswood, Dublin 6W" },
				{ "Ciaran", "O' Keeffe", "Ciaran", "087 9583723",
						"112 Wainsfort Road, Terenure, Dublin 6W" },
				{ "Chris", "O'Neill", "ChrisB", "086 8726048",
						"4 Sycamore Park, Kingswood, Dublin 24" },
				{ "Thomas", "O'Shea", "Walshy", "086 8369581",
						"53 Greenlea Road, Terenure Dublin 6W" },
				{ "Donal", "Walsh", "Donal", "087 6672949",
						"34 Kilnamanagh Road, Walkinstown, Dublin 12" },
				{ "Niall", "Wrynne", "Niall", "086 1034456",
						"22 Wainsfort Manor Crescent, Terenure, Dublin 6W" },
				{ "Rian", "Finnegan", "Rian", "087 7569773",
						"29 St Annes, Templeogue Dublin6w" },
				{ "Adam", "Finnegan", "Adam", "087 7569773",
						"29 St Annes, Templeogue Dublin6w" },
				{ "Eoin", "Lewis", "Nagle", "087 2520674",
						"56 Rathdown Avenue, Terenure D6w" },
				{ "Eanna", "costelloe", "Eanna", "087 2212249",
						"32 Cypress Park, Templeogue D6w" },
				{ "Luke", "Malin", "Luke", "087 6389185",
						"2 Wainsfort Gardens, Templeogue D6w" },
				{ "Kenneth", "Mullen", "Kenny", "087 8144189",
						"38 Lavarna Grove Terenure Terenure D6w" },
				{ "Seamus", "Lewis", "Seamus", "087 2520674",
						"56 Rathdown Avenue, Terenure D6w" },
				{ "Conor", "redmond", "Seery", "087 2684016",
						"16 Castlefield Court, C’field Manor D16 " },
				{ "Shane", "Reynolds", "Shane", "087 2996390",
						"4 Rosehall, Templeogue D6w" } };

		ContentValues values = new ContentValues();
		for (int i = 0; i < panelIn.length; i++) {
			values.put("firstname", panelIn[i][0]);
			values.put("surname", panelIn[i][1]);
			values.put("nickname", panelIn[i][2]);
			values.put("phone", panelIn[i][3]);
			values.put("address", panelIn[i][4]);
			getContentResolver().insert(PanelContentProvider.CONTENT_URI,
					values);
			// long id = Long.parseLong(uri.getLastPathSegment());

		}

	}
}
