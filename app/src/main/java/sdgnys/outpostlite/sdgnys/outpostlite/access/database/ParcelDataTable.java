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
			"PRINT_KEY",
			"PARCEL_ID",
			"LOC_ST_NBR",
			"LOC_ST_NAME",
			"LOC_MAIL_ST_SUFF",
			"LOC_MUNI_NAME"
	},
	columnDataTypes = new String[] {
			"char(6)",
			"varchar(25)",
			"integer",
			"varchar(10)",
			"varchar(30)",
			"char(4)",
			"varchar(30)"
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