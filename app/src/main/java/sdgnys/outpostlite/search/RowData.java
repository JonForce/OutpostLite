package sdgnys.outpostlite.search;

/**
 * Created by jforce on 8/10/2017.
 *
 * This is a simple class that represents one search result.
 */
class RowData {
	
	public static final String[] fields =
		{ "SWIS", "PARCEL_ID", "SBL", "Loc_St_Nbr", "Street", "Loc_Muni_Name", "XML_LOCATION" };
	
	/** These integers correspond to where in the values array the data is stored.
	 * They must match their rowNumber in the fields array. */
	public static final int
		SWIS = 0,
		PARCEL_ID = 1,
		SBL = 2,
			Loc_St_Nbr = 3,
			Street = 4,
			Loc_Muni_Name = 5,
			XML_LOCATION = 6;
	
	public final String[] values = new String[fields.length];
}