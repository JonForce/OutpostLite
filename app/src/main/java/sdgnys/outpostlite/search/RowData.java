package sdgnys.outpostlite.search;

/**
 * Created by jforce on 8/10/2017.
 *
 * This is a simple class that represents one search result.
 */
class RowData {
	
	public static final String[] fields =
		{ "SWIS", "PARCEL_ID", "PRINT_KEY", "LOC_ST_NBR", "LOC_ST_NAME", "LOC_MAIL_ST_SUFF", "LOC_MUNI_NAME" };
	
	/** These integers correspond to where in the values array the data is stored.
	 * They must match their position in the fields array. */
	public static final int
		SWIS = 0,
		PARCEL_ID = 1,
		PRINT_KEY = 2,
		LOC_ST_NBR = 3,
		LOC_ST_NAME = 4,
		LOC_MAIL_ST_SUFF = 5,
		LOC_MUNI_NAME = 6;
	
	public final String[] values = new String[fields.length];
}