/*
 *  TrainingContentProvider.java
 *
 *  Reference: BooksProvider class from Beginning Android 4 Application Development by Wei-Meng Lee
 *  http://www.wrox.com/WileyCDA/WroxTitle/Beginning-Android-4-Application-Development.productCd-1118199545,descCd-DOWNLOAD.html
 *  modified to suit
 *  
 *  Description: This class is the Content Provider for the training table in the App database
 *  it facilitates CRUD operations on the database table and notifies ContentResolver whenever
 *  the database is changed
 *  
 *  All ContentProvider classes are based on PanelContentProvider.java.  Check there for best annotation
 *  
 *  Written on: Jan 2013
 *  
 * 
 */
package fm.matchstats.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class TrainingContentProvider extends BaseProvider {
	public static final String PROVIDER_NAME = "fm.matchstats.provider.training";
	public static final String BASE_PATH = "trainingdetails";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/" + BASE_PATH);

	public static final String _ID = "_id";
	private static final int TRAINING = 1;
	private static final int TRAINING_ID = 2;
	private static final int TRAININGREVIEW = 3;

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH, TRAINING);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH + "/#", TRAINING_ID);
		uriMatcher.addURI(PROVIDER_NAME, BASE_PATH + "R", TRAININGREVIEW);
	}

	// ---for database use---
	private SQLiteDatabase matchAppDB;
	public static final String DATABASE_TABLE = "training";
	public static final String TRAININGID = "_id";
	public static final String PANELNAME = "panelname";
	public static final String DATE = "date";
	public static final String LOCATION = "locn";
	public static final String COMMENTS = "comments";

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		matchAppDB = dbHelper.getWritableDatabase();
		return (matchAppDB == null) ? false : true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case TRAINING:
			count = matchAppDB.delete(DATABASE_TABLE, selection, selectionArgs);
			break;
		case TRAINING_ID:
			String id = uri.getPathSegments().get(1);
			count = matchAppDB.delete(DATABASE_TABLE, _ID
					+ " = "
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		// ---get all players---
		case TRAINING:
			return "fm.matchstats.cursor.dir/fm.matchstats.players ";
			// ---get a particular player---
		case TRAINING_ID:
			return "fm.matchstats.cursor.item/fm.matchstats.players ";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// ---Original returns URI - changed to return long
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		long rowID = matchAppDB.insert(DATABASE_TABLE, "", values);

		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		Cursor c;
		sqlBuilder.setTables(DATABASE_TABLE);

		switch (uriMatcher.match(uri)) {
		// ---get all players---
		case TRAINING_ID:
			sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));
			if (sortOrder == null || sortOrder == "")
				sortOrder = TRAININGID;
			c = sqlBuilder.query(matchAppDB, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case TRAINING:
			if (sortOrder == null || sortOrder == "")
				sortOrder = TRAININGID;
			c = sqlBuilder.query(matchAppDB, projection, selection,
					selectionArgs, null, null, sortOrder);
			break;
		case TRAININGREVIEW:
			c = matchAppDB
					.rawQuery(
							"SELECT t._id, t.date, t.locn, t.comments, "
									+ "p.nickname "
									+ "FROM training t LEFT JOIN attendance a ON a.trainingID=t._id "
									+ "left join panel p on a.playerID=p._id "
									+ "where t.panelname='" + selection + "' "
									+ "order by t._id desc, a.playerID", null);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		// ---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case TRAINING:
			count = matchAppDB.update(DATABASE_TABLE, values, selection,
					selectionArgs);
			break;
		case TRAINING_ID:
			count = matchAppDB.update(
					DATABASE_TABLE,
					values,
					_ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
