package apt.tutorial;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

class RestaurantSQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="lunchlist.db";
	private static final int SCHEMA_VERSION=1;
	
	public RestaurantSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE restaurants (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, type TEXT, notes TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("LunchList", "Upgrading database, which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS restaurants");
		onCreate(db);
	}
}