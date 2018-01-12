package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

import android.provider.BaseColumns;

/**
 * Created by jforce on 8/14/2017.
 *
 */
public class ImageDataTable extends DataTable {
	public static final String
			TABLE_NAME = "ImageData";
	
	public static final String[]
		columns = new String[] {
			"IMAGE_LOC",
			"SWIS",
			"SBL",
			"PARCEL_ID",
			"IS_DEFAULT_IMAGE",
			"SEQUENCE"
		},
		columnDataTypes = new String[] {
				"varchar(100)",
				"char(6)",
				"varchar(25)",
				"integer",
				"integer",
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
