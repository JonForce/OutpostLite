package sdgnys.outpostlite.search;

/**
 * Created by jforce on 1/22/2018.
 */
public class SearchResult {
	public static final String[] fields =
			{ "SWIS", "PARCEL_ID", "PRINT_KEY", "SBL", "Loc_St_Nbr", "Street",
					"Loc_Muni_Name", "OwnerNames", "XML_LOCATION" };
	
	/** These integers correspond to where in the values array the data is stored.
	 * They must match their rowNumber in the fields array. */
	public static final int
			SWIS = 0,
			PARCEL_ID = 1,
			PRINT_KEY = 2,
			SBL = 3,
			Loc_St_Nbr = 4,
			Street = 5,
			Loc_Muni_Name = 6,
			OwnerNames = 7,
			XML_LOCATION = 8;
	
	public final String[] values = new String[fields.length];
}
