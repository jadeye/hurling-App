/*
 *  CSVExport.java
 *
 *  Written by: Fintan Mahon 12101524
 *  Description: Handles exporting database tables to CSV files
 *  Written on: Jan 2013
 *  
 *  Uses OPENCSV Library for CSV file import/export/parsing from http://opencsv.sourceforge.net/
 *  
 *  
 */
package fm.matchstats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;
import android.database.sqlite.SQLiteDatabase;

public class CSVExport extends AsyncTask<String, Void, Boolean> {
	// private ProgressDialog dialog = new ProgressDialog(getActivity.this);
	private final ProgressDialog dialog;
	private Context context;
	private SQLiteDatabase teamDatabase = null;
	private File file;
	private CSVWriter csvWrite;

	protected CSVExport(Context context) {
		dialog = new ProgressDialog(context);
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		Log.v("in CSVExport", "off we go");
	}

	@Override
	protected Boolean doInBackground(final String... args) {
		// get date to use as timestamp for filename
	//	Date currentDate = new Date(System.currentTimeMillis());
	//	SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy_");
	//	String date = sdf.format(currentDate);

		// get database
		teamDatabase = context.openOrCreateDatabase("team",
				Context.MODE_PRIVATE, null);
		// set up export directory - use match_BU sub directory of normal device
		// storage
		File exportDir = new File(Environment.getExternalStorageDirectory(),
				"match_BU");
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}

		if (args[0].equals("match")) {
			// write data from shots table of database
			file = new File(exportDir,  "shots.csv");
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// Flatten data from database and export
				// raw sql query start off with data from MATCH table
				// left join to get all SHOTS table data for each match
				// another left join to get player name from PANEL table
				Cursor curCSV = teamDatabase
						.rawQuery(
								"SELECT m._id, m.date, m.location, m.homeTeam, "
										+ "m.oppTeam, m.minshalf, m.time1, m.time2, "
										+ "m.owngoals1, m.ownpoints1, m.owngoals2, "
										+ "m.ownpoints2, m.oppgoals1, m.opppoints1, "
										+ "m.oppgoals2, m.opppoints2, "
										+ "s.time, s.team, s.outcome, s.type, "
										+ "p.nickname, "
										+ "p.firstname, p.surname, s.posn "
										+ "FROM match m LEFT JOIN shots s ON s.matchID=m._id "
										+ "LEFT JOIN panel p ON s.playerID=p._id",
								null);
				// cvsWrite from imported library parses data to string array
				csvWrite.writeNext(curCSV.getColumnNames());
				while (curCSV.moveToNext()) {
					String arrStr[] = { curCSV.getString(0),
							curCSV.getString(1), curCSV.getString(2),
							curCSV.getString(3), curCSV.getString(4),
							curCSV.getString(5), curCSV.getString(6),
							curCSV.getString(7), curCSV.getString(8),
							curCSV.getString(9), curCSV.getString(10),
							curCSV.getString(11), curCSV.getString(12),
							curCSV.getString(13), curCSV.getString(14),
							curCSV.getString(15), curCSV.getString(16),
							curCSV.getString(17), curCSV.getString(18),
							curCSV.getString(19), curCSV.getString(20),
							curCSV.getString(21), curCSV.getString(22),
							curCSV.getString(23) };
					csvWrite.writeNext(arrStr);
				}
				csvWrite.close();
				curCSV.close();

			} catch (SQLException sqlEx) {
				Log.e("shots sql", sqlEx.getMessage(), sqlEx);
				return false;
			} catch (IOException e) {
				Log.e("shots io", e.getMessage(), e);
				return false;
			}

			file = new File(exportDir,  "frees.csv");
			// export data from frees table of database
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// Flatten data from database and export
				// raw sql query start off with data from MATCH table
				// left join to get all FREES table data for each match
				// another left join to get player name from PANEL table
				Cursor curCSV = teamDatabase
						.rawQuery(
								"SELECT m._id, m.date, m.location, m.homeTeam, "
										+ "m.oppTeam, m.minshalf, m.time1, m.time2, "
										+ "m.owngoals1, m.ownpoints1, m.owngoals2, "
										+ "m.ownpoints2, m.oppgoals1, m.opppoints1, "
										+ "m.oppgoals2, m.opppoints2, "
										+ "f.time, f.team, f.reason, p.nickname, "
										+ "p.firstname, p.surname, f.posn "
										+ "FROM match m left JOIN frees f ON f.matchID=m._id "
										+ "left join panel p on f.playerID=p._id",
								null);

				csvWrite.writeNext(curCSV.getColumnNames());
				while (curCSV.moveToNext()) {
					String arrStr[] = { curCSV.getString(0),
							curCSV.getString(1), curCSV.getString(2),
							curCSV.getString(3), curCSV.getString(4),
							curCSV.getString(5), curCSV.getString(6),
							curCSV.getString(7), curCSV.getString(8),
							curCSV.getString(9), curCSV.getString(10),
							curCSV.getString(11), curCSV.getString(12),
							curCSV.getString(13), curCSV.getString(14),
							curCSV.getString(15), curCSV.getString(16),
							curCSV.getString(17), curCSV.getString(18),
							curCSV.getString(19), curCSV.getString(20),
							curCSV.getString(21), curCSV.getString(22) };

					csvWrite.writeNext(arrStr);
				}
				csvWrite.close();
				curCSV.close();

			} catch (SQLException sqlEx) {
				Log.e("frees sql", sqlEx.getMessage(), sqlEx);
				return false;
			} catch (IOException e) {
				Log.e("frees io", e.getMessage(), e);
				return false;
			}

			// write data from puckouts table of database
			file = new File(exportDir,  "puckouts.csv");
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// Flatten data from database and export
				// raw sql query start off with data from MATCH table
				// left join to get all PUCKOUTS table data for each match
				// another left join to get player name from PANEL table
				Cursor curCSV = teamDatabase
						.rawQuery(
								"SELECT m._id, m.date, m.location, m.homeTeam, "
										+ "m.oppTeam, m.minshalf, m.time1, m.time2, "
										+ "m.owngoals1, m.ownpoints1, m.owngoals2, "
										+ "m.ownpoints2, m.oppgoals1, m.opppoints1, "
										+ "m.oppgoals2, m.opppoints2, "
										+ "o.time, o.team, o.outcome, p.nickname, "
										+ "p.firstname, p.surname, o.posn "
										+ "FROM match m LEFT JOIN pucks o ON o.matchID=m._id "
										+ "left join panel p on o.playerID=p._id",
								null);

				csvWrite.writeNext(curCSV.getColumnNames());
				while (curCSV.moveToNext()) {
					String arrStr[] = { curCSV.getString(0),
							curCSV.getString(1), curCSV.getString(2),
							curCSV.getString(3), curCSV.getString(4),
							curCSV.getString(5), curCSV.getString(6),
							curCSV.getString(7), curCSV.getString(8),
							curCSV.getString(9), curCSV.getString(10),
							curCSV.getString(11), curCSV.getString(12),
							curCSV.getString(13), curCSV.getString(14),
							curCSV.getString(15), curCSV.getString(16),
							curCSV.getString(17), curCSV.getString(18),
							curCSV.getString(19), curCSV.getString(20),
							curCSV.getString(21), curCSV.getString(22) };
					csvWrite.writeNext(arrStr);
				}
				csvWrite.close();
				curCSV.close();

			} catch (SQLException sqlEx) {
				Log.e("puckouts sql", sqlEx.getMessage(), sqlEx);
				return false;
			} catch (IOException e) {
				Log.e("puckouts io", e.getMessage(), e);
				return false;
			}

			// write data from positions table of database
			file = new File(exportDir,  "positions.csv");
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// Flatten data from database and export
				// raw sql query start off with data from MATCH table
				// left join to get all POSITIONS table data for each match
				// another left join to get player name from PANEL table
				Cursor curCSV = teamDatabase
						.rawQuery(
								"SELECT m._id, m.date, m.location, m.homeTeam, "
										+ "m.oppTeam, ps.time, ps.code, ps.posn, p.nickname, "
										+ "p.firstname, p.surname "
										+ "FROM match m LEFT JOIN positions ps ON ps.matchID=m._id "
										+ "left join panel p on ps.playerID=p._id",
								null);

				csvWrite.writeNext(curCSV.getColumnNames());
				while (curCSV.moveToNext()) {
					String arrStr[] = { curCSV.getString(0),
							curCSV.getString(1), curCSV.getString(2),
							curCSV.getString(3), curCSV.getString(4),
							curCSV.getString(5), curCSV.getString(6),
							curCSV.getString(7), curCSV.getString(8),
							curCSV.getString(9), curCSV.getString(10) };
					csvWrite.writeNext(arrStr);
				}
				csvWrite.close();
				curCSV.close();

			} catch (SQLException sqlEx) {
				Log.e("puckouts sql", sqlEx.getMessage(), sqlEx);
				return false;
			} catch (IOException e) {
				Log.e("puckouts io", e.getMessage(), e);
				return false;
			}

		}

		if (args[0].equals("training")) {

			// write data from training table of database
			file = new File(exportDir,  "training.csv");
			try {
				file.createNewFile();
				// cvsWrite object facilitates parsing data to csv file
				csvWrite = new CSVWriter(new FileWriter(file));
				// Flatten data from database and export
				// raw sql query start off with data from MATCH table
				// left join to get all TRAINING table data for each match
				// another left join to get player name from PANEL table
				Cursor curCSV = teamDatabase
						.rawQuery(
								"SELECT t._id, t.date, t.locn, t.comments, "
										+ "t.panelname, p.nickname, "
										+ "p.firstname, p.surname "
										+ "FROM training t LEFT JOIN attendance a ON a.trainingID=t._id "
										+ "left join panel p on a.playerID=p._id",
								null);

				csvWrite.writeNext(curCSV.getColumnNames());
				while (curCSV.moveToNext()) {
					String arrStr[] = { curCSV.getString(0),
							curCSV.getString(1), curCSV.getString(2),
							curCSV.getString(3), curCSV.getString(4),
							curCSV.getString(5), curCSV.getString(6),
							curCSV.getString(7) };
					csvWrite.writeNext(arrStr);
				}
				csvWrite.close();
				curCSV.close();

			} catch (SQLException sqlEx) {
				Log.e("puckouts sql", sqlEx.getMessage(), sqlEx);
				return false;
			} catch (IOException e) {
				Log.e("puckouts io", e.getMessage(), e);
				return false;
			}

		}

		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		// display success of fail message
		if (success) {
			Toast.makeText(context, "File Export Successful",
					Toast.LENGTH_SHORT).show();
			Log.v("Export successful!", "yay");
		} else {
			Log.v("Export failed", "feck");
			Toast.makeText(context, "File Export Failed", Toast.LENGTH_SHORT)
					.show();
		}
	}
}