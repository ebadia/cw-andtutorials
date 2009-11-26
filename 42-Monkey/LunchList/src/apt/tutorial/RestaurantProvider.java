package apt.tutorial;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import java.util.HashMap;

public class RestaurantProvider extends ContentProvider {
	public static HashMap<String, String> DEFAULT_PROJECTION;
	private static final int RESTAURANTS=1;
	private static final int RESTAURANT_ID=2;
	private static final UriMatcher MATCHER;

	static {
		MATCHER=new UriMatcher(UriMatcher.NO_MATCH);
		MATCHER.addURI("apt.tutorial.RestaurantProvider",
									 "restaurants", RESTAURANTS);
		MATCHER.addURI("apt.tutorial.RestaurantProvider",
									 "restaurants/#", RESTAURANT_ID);
	}
	
	public static final class Columns implements BaseColumns {
		public static final Uri CONTENT_URI
				 =Uri.parse("content://apt.tutorial.RestaurantProvider/restaurants");
		public static final String NAME="name";
		public static final String NOTES="notes";
		public static final String ADDRESS="address";
		public static final String TYPE="type";
		public static final String PHONENUMBER="phoneNumber";
		public static final String DEFAULT_SORT_ORDER=NAME;
	}
	
	static {
		DEFAULT_PROJECTION=new HashMap<String, String>();
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns._ID,
																			RestaurantProvider.Columns._ID);
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns.NAME,
																			RestaurantProvider.Columns.NAME);
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns.NOTES,
																			RestaurantProvider.Columns.NOTES);
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns.ADDRESS,
																			RestaurantProvider.Columns.ADDRESS);
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns.TYPE,
																			RestaurantProvider.Columns.TYPE);
		DEFAULT_PROJECTION.put(RestaurantProvider.Columns.PHONENUMBER,
																			RestaurantProvider.Columns.PHONENUMBER);
	}
	
	private SQLiteDatabase db;

	@Override
	public boolean onCreate() {
		db=(new RestaurantSQLiteHelper(getContext())).getWritableDatabase();
		
		return((db == null) ? false : true);
	}
	
	@Override
	public Cursor query(Uri url, String[] projection, String selection,
												String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
		String orderBy;

		qb.setTables(getTableName());
		
		if (isCollectionUri(url)) {
			qb.setProjectionMap(getDefaultProjection());
		}
		else {
			qb.appendWhere(getIdColumnName()+"="+url.getPathSegments().get(1));
		}
		
		if (TextUtils.isEmpty(sort)) {
			orderBy=getDefaultSortOrder();
		}
		else if (sort.startsWith("ORDER BY")) {
			orderBy=sort.substring(9);
		}
		else {
			orderBy=sort;
		}

		Cursor c=qb.query(db, projection, selection, selectionArgs,
											null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		
		return(c);
	}

	@Override
	public String getType(Uri url) {
		if (isCollectionUri(url)) {
			return(getCollectionType());
		}
		
		return(getSingleType());
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		long rowID;
		ContentValues values;
		
		if (initialValues!=null) {
			values=new ContentValues(initialValues);
		}
		else {
			values=new ContentValues();
		}

		if (!isCollectionUri(url)) {
			throw new IllegalArgumentException("Unknown URL " + url);
		}
		
		for (String colName : getRequiredColumns()) {
			if (values.containsKey(colName)==false) {
				throw new IllegalArgumentException("Missing column: "+colName);
			}
		}

		rowID=db.insert(getTableName(), getNullColumnHack(), values);
		
		if (rowID>0) {
			Uri uri=ContentUris.withAppendedId(getContentUri(), rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			
			return(uri);
		}

		throw new SQLException("Failed to insert row into " + url);
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		int count;
		long rowId=0;
		
		if (isCollectionUri(url)) {
			count=db.delete(getTableName(), where, whereArgs);
		}
		else {
			String segment=url.getPathSegments().get(1);
			rowId=Long.parseLong(segment);
			count=db
					.delete(getTableName(), getIdColumnName()+"="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
		}

		getContext().getContentResolver().notifyChange(url, null);
		
		return(count);
	}

	@Override
	public int update(Uri url, ContentValues values,
										String where, String[] whereArgs) {
		int count;
		
		if (isCollectionUri(url)) {
			count=db.update(getTableName(), values, where, whereArgs);
		}
		else {
			String segment=url.getPathSegments().get(1);
			count=db
					.update(getTableName(), values, getIdColumnName()+"="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
		}
	
		getContext().getContentResolver().notifyChange(url, null);
		
		return(count);
	}
	
	private boolean isCollectionUri(Uri url) {
		return(MATCHER.match(url)==RESTAURANTS);
	}
	
	private HashMap<String, String> getDefaultProjection() {
		return(DEFAULT_PROJECTION);
	}
	
	private String getTableName() {
		return("restaurants");
	}
	
	private String getIdColumnName() {
		return("_id");
	}
	
	private String getDefaultSortOrder() {
		return(RestaurantProvider.Columns.DEFAULT_SORT_ORDER);
	}
	
	private String getCollectionType() {
		return("vnd.android.cursor.dir/vnd.apt.tutorial.restaurant");
	}
	
	private String getSingleType() {
		return("vnd.android.cursor.item/vnd.apt.tutorial.restaurant");
	}
	
	private String[] getRequiredColumns() {
		return(new String[] {RestaurantProvider.Columns.NAME});
	}
	
	private String getNullColumnHack() {
		return(RestaurantProvider.Columns.NAME);
	}
	
	private Uri getContentUri() {
		return(RestaurantProvider.Columns.CONTENT_URI);
	}
}