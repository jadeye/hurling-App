/*
 *  ShotsListActivity.java
 *
 *  Written by: Fintan Mahon
 *  Description: Lists shots statistics data from database. 
 *  
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats.db.run;

import fm.matchstats.HelpActivity;
import fm.matchstats.R;
import fm.matchstats.db.ShotContentProvider;

import android.net.Uri;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

public class ShotsListActivity extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shot_list_layout);

		fillData();
		registerForContextMenu(getListView());
	}

	// method to read in panel list from database and list contents on screen
	private void fillData() {
		// retreive shots info from database
		Uri allTitles = ShotContentProvider.CONTENT_URI;
		String[] from = new String[] { ShotContentProvider.TIME,
				ShotContentProvider.TEAM, ShotContentProvider.OUTCOME,
				ShotContentProvider.TYPE, "panel_nickname",
				ShotContentProvider.POSITION };

		// create array to map these fields to
		int[] to = new int[] { R.id.stext1, R.id.stext2, R.id.stext3,
				R.id.stext4, R.id.stext5, R.id.stext6 };

		// load database info from PanelContentProvider into a cursor and use an
		// adapter to display on screen
		Cursor c1 = getContentResolver().query(allTitles, null, null, null,
				null);
		SimpleCursorAdapter reminders = new SimpleCursorAdapter(this,
				R.layout.shot_row_layout, c1, from, to, 0);
		setListAdapter(reminders);
	}

	// set up long press menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.list_menu_longpress, menu);
	}

	@Override
	// deal with selection from long press menu
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete1:
			// Delete a row / player
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			Uri uri = Uri
					.parse(ShotContentProvider.CONTENT_URI + "/" + info.id);
			getContentResolver().delete(uri, null, null);
			Toast.makeText(getApplicationContext(), "Shot Deleted",
					Toast.LENGTH_LONG).show();
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
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
		switch (item.getItemId()) {
		case R.id.helpList:
			Intent ihelp = new Intent(this, HelpActivity.class);
			ihelp.putExtra("HELP_ID", R.string.reviewListHelp);
			startActivity(ihelp);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
