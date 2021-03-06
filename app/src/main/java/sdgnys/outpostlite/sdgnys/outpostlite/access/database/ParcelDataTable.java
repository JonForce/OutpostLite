package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

import android.provider.BaseColumns;

/**
 * Created by jforce on 8/8/2017.
 */
public class ParcelDataTable extends DataTable {
	public static final String
			TABLE_NAME = "ParcelData";
	
	public static final String[]
			columns = new String[] {
			"SWIS",
			"SBL",
			"PRINT_KEY",
			"PARCEL_ID",
			"Loc_St_Nbr",
			"Street",
			"Loc_Muni_Name",
			"TotalAV",
			"LandAV",
			"OwnerNames",
			"XML_LOCATION"
	},
	columnDataTypes = new String[] {
			"char(6)",
			"varchar(25)",
			"varchar(25)",
			"integer",
			"varchar(10)",
			"varchar(30)",
			"varchar(30)",
			"integer",
			"integer",
			"varchar(25)",
			"integer"
	};
	
	@Override
	public String name() {
		return TABLE_NAME;
	}
	
	@Override
	public String[] columns() {
		return columns;
	}
	
	@Override
	public String[] columnDataTypes() {
		return columnDataTypes;
	}
}