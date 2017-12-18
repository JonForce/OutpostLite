package sdgnys.outpostlite.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sdgnys.outpostlite.R;

import static sdgnys.outpostlite.search.RowData.*;

/**
 * This class is an adapter that will allow search results to be displayed in a ListView.
 *
 * Created by jforce on 8/10/2017.
 */
class SearchResultsAdapter extends ArrayAdapter<RowData> {
	
	private static final int
		DARK_BG_R = 232, DARK_BG_G = 240, DARK_BG_B = 255;
	
	private Context context;
	private int layout;
	private ArrayList<RowData> data;
	
	/** Create the tool for displaying search results in a ListView.
	 * @param context The context of the Activity.
	 * @param layout The layout that will be used for each row in the results.
	 * @param data The search result data that will be displayed in the ListView.
	 */
	public SearchResultsAdapter(Context context, int layout, ArrayList<RowData> data) {
		super(context, layout, data);
		this.context = context;
		this.layout = layout;
		this.data = data;
	}
	
	/** We overwrite this method to provide a custom implementation for how views will
	 * appear on each row in the ListView. */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		
		// If they are requesting the view of a new row,
		if(row == null)
		{
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layout, parent, false);
			
			// Create a new ViewHolder, which is a container for the views in the row that we will
			// populate with search data.
			holder = new ViewHolder();
			holder.SWIS = (TextView)row.findViewById(R.id.SWIS);
			holder.PRINT_KEY = (TextView)row.findViewById(R.id.PRINT_KEY);
			holder.Loc_St_Nbr = (TextView)row.findViewById(R.id.Street);
			holder.Street = (TextView)row.findViewById(R.id.LOC_ST_NAME);
			holder.Loc_Muni_Name = (TextView)row.findViewById(R.id.LOC_MUNI_NAME);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		
		// Get the search data for the row they're requesting.
		RowData rowData = data.get(position);
		
		// Update the Views to contain the search data.
		holder.SWIS.setText(rowData.values[SWIS]);
		holder.PRINT_KEY.setText(rowData.values[PRINT_KEY]);
		holder.Loc_St_Nbr.setText(rowData.values[Loc_St_Nbr]);
		holder.Street.setText(rowData.values[Street]);
		holder.Loc_Muni_Name.setText(rowData.values[Loc_Muni_Name]);
		
		// Apply dark background to every other row.
		if (position % 2 == 0)
			row.setBackgroundColor(Color.rgb(DARK_BG_R, DARK_BG_G, DARK_BG_B));
		else
			row.setBackgroundColor(Color.WHITE);
		
		return row;
	}
	
	/** A simple object that contains the Views that need to be populated with search data. */
	static class ViewHolder {
		TextView SWIS, PRINT_KEY, Loc_St_Nbr, Street, Loc_Muni_Name;
	}
}