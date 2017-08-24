package sdgnys.outpostlite.search;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ParcelDataTable;

/**
 * Created by jforce on 8/10/2017.
 * This class is a tool that will search the SQLite database given a set of search parameters.
 */
public class Search {
	
	private ArrayList<RowData> results;
	
	/** Creating a search will query the database given the specified search terms. The search will
	 * execute immediately on object creation.
	 * @param context The Activity's context.
	 * @param searchTerms The terms to search by.
	 */
	public Search(Context context, RowData searchTerms) {
		this.results = new ArrayList<>();
		
		// Establish a connection to the database.
		Database database = new Database(context);
		// Generate the query string based on the terms we want to search for.
		String query = getQuery(searchTerms);
		
		// Begin the search!
		executeSearch(query, database);
	}
	
	/** @return the results of the search. The search is automatically executed on object creation. */
	public ArrayList<RowData> getResults() {
		return results;
	}
	
	/** Query the database and fill the results array with entries. */
	private void executeSearch(String query, Database database) {
		// Query the database and move to the first result.
		Cursor cursor = database.getReadableDatabase().rawQuery(query, null);
		cursor.moveToFirst();
		
		// While there are still more results to read,
		while (!cursor.isAfterLast()) {
			// Data represents the data for this particular search result.
			RowData data = new RowData();
			// For every column returned by the search,
			for (int column = 0; column < cursor.getColumnCount(); column ++) {
				// Get the column name.
				String columnName = cursor.getColumnName(column);
				// Find a field in data that matches the column name,
				for (int field = 0; field < RowData.fields.length; field ++) {
					if (columnName.equals(RowData.fields[field]))
						// And put the search data into data.
						data.values[field] = cursor.getString(column);
				}
			}
			
			results.add(data);
			cursor.moveToNext();
		}
	}
	
	/** @return a query that will pull all data from the database that matches the search terms. */
	private String getQuery(RowData searchTerms) {
		// Define the query as starting off with the basic shit.
		String query = "SELECT * FROM " + ParcelDataTable.TABLE_NAME;
		// This variable may seem tricky, but I'm about to break it down for you.
		// Basically, we only want to add 1 WHERE clause to the query. So as we're iterating over
		// the fields that need to go in the query, this will keep track if we've already added it or not.
		boolean isFirst = true;
		
		// For every field that could possibly be searched,
		for (int field = 0; field < RowData.fields.length; field ++) {
			// Get the name of the field.
			String fieldName = RowData.fields[field];
			// If they have a search term specified for that field,
			if (searchTerms.values[field] != null && !searchTerms.values[field].equals("")) {
				// We have to add it to the query.
				
				// If this is the first term we're adding to the query,
				if (isFirst) {
					query += " WHERE ";
					isFirst = false;
				} else
					query += " AND ";
				
				// Add the search term to the query. (The end part makes it case insensitive)
				query += fieldName + " = '" + searchTerms.values[field] + "' COLLATE NOCASE";
			}
		}
		
		return query;
	}
}