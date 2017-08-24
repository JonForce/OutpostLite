package sdgnys.outpostlite.sdgnys.outpostlite.access.database;

import android.provider.BaseColumns;

/**
 * Created by jforce on 8/15/2017.
 */

public abstract class DataTable implements BaseColumns {
	public abstract String name();
	public abstract String[] columns();
	public abstract String[] columnDataTypes();
}
