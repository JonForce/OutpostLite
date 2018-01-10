package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

/**
 * Created by jforce on 8/8/2017.
 */
public class SqlQueries {
	
	/** This method creates a query that will delete the table with the specified name */
	public static String deleteTableQuery(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	/** This method creates a query that will query for a record and grab a specific column value. */
	public static String get(String SWIS, String PRINT_KEY, String PARCEL_ID,
	                         String tableName, String columnName) {
		return "SELECT " + columnName + " FROM " + tableName + " " +
				"WHERE SWIS = " + SWIS + " AND " +
				"SBL = \"" + PRINT_KEY + "\" AND " +
				"PARCEL_ID = " + PARCEL_ID + ";";
	}
	
	/** This method creates a query that will create a database with the specified parameters.
	 * @param name The name of the new database.
	 * @param columnNames The names of all the columns.
	 * @param columnTypes The data types for the columns.
	 * @return the query that will create the database.
	 */
	public static String createDatabaseQuery(
			String name, String ID, String[] columnNames, String[] columnTypes) {
		String total =
				"CREATE TABLE if not exists " + name + " (" +
				ID + " INTEGER PRIMARY KEY";
		for (int i = 0; i < columnNames.length; i ++) {
			total += "," + columnNames[i] + " " + columnTypes[i];
		}
		total += ")";
		return total;
	}
	
}
