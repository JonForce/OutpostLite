package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

/**
 * Created by jforce on 8/8/2017.
 */
public class SqlQueries {
	
	public static String deleteTableQuery(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
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
