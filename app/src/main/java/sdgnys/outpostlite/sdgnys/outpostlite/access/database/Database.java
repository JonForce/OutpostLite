package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import static sdgnys.outpostlite.Logger.log;

/**
 * Created by jforce on 8/8/2017.
 */
public class Database extends SQLiteOpenHelper {
	
	public static final int VERSION = 4;
	public static final String NAME = "Database.db";
	
	public Database(Context context) {
		super(context, NAME, null, VERSION);
	}
	
	public void printDatabase(String tableName) {
		SQLiteDatabase database = getReadableDatabase();
		
		Cursor cursor = database.rawQuery("select * from " + tableName, null);
		
		cursor.moveToFirst();
		log("Printing out the database...");
		
		// Print the header.
		String header = "";
	    for (String column : cursor.getColumnNames())
	    	header += column + ", ";
		log(header);
		
		// Print all of the contents for every record.
		while (!cursor.isAfterLast()) {
			String line = "";
			for (int i = 0; i < cursor.getColumnCount(); i ++)
				line += cursor.getString(i) + ", ";
			
			log(line);
		    cursor.moveToNext();
	    }
	}
	
	/** Reset the database to its factory state. */
	public void resetDatabase(SQLiteDatabase database) {
		// Delete the tables.
		database.execSQL(SqlQueries.deleteTableQuery(ParcelDataTable.TABLE_NAME));
		database.execSQL(SqlQueries.deleteTableQuery(ImageDataTable.TABLE_NAME));
		
		// Create the database.
		onCreate(database);
	}
	
	/** This method is used for adding a new record to the sqlite database. The length of the
	 * input data must exactly match the number of columns in the table.
	 * @param data The values of the columns to add in the new record.
	 * @return The newly created row ID.
	 */
	public long addRecord(DataTable table, String ... data) {
		if (data.length != table.columns().length)
			throw new RuntimeException(
					"New record data must have the same length as the " +
					"number of columns. Database : " + table.columns().length +
					" Data : " + data.length);
		
		ContentValues values = new ContentValues();
		for (int i = 0; i < table.columns().length; i ++)
			values.put(table.columns()[i], data[i]);
		
		long newRowId = getWritableDatabase().insert(table.name(), null, values);
		
		return newRowId;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createParcelDataTable(db);
		createImageDataTable(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		resetDatabase(db);
	}
	
	private void createImageDataTable(SQLiteDatabase db) {
		String query = SqlQueries.createDatabaseQuery(
				ImageDataTable.TABLE_NAME,
				ImageDataTable._ID,
				ImageDataTable.columns,
				ImageDataTable.columnDataTypes
		);
		db.execSQL(query);
	}
	
	private void createParcelDataTable(SQLiteDatabase db) {
		String query = SqlQueries.createDatabaseQuery(
				ParcelDataTable.TABLE_NAME,
				ParcelDataTable._ID,
				ParcelDataTable.columns,
				ParcelDataTable.columnDataTypes
		);
		db.execSQL(query);
	}
}
