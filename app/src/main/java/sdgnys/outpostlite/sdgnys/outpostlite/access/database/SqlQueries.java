package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

/**
 * Created by jforce on 8/8/2017.
 */
public class SqlQueries {
	
	public static String deleteTableQuery(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
	}
	
	public static String get(String SWIS, String PRINT_KEY, String PARCEL_ID,
	                         String tableName, String columnName) {
		return "SELECT " + columnName + " FROM " + tableName + " " +
				"WHERE SWIS = " + SWIS + " AND " +
				"PRINT_KEY = \"" + PRINT_KEY + "\" AND " +
				"PARCEL_ID = " + PARCEL_ID + ";";
	}
	
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
