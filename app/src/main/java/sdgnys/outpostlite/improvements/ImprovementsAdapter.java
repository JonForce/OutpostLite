package sdgnys.outpostlite.improvements;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.ParcelDataActivity;
import sdgnys.outpostlite.R;


/**
 * This class is an adapter that will allow imrovements to be displayed.
 *
 * Created by jforce on 9/27/2017.
 */
class ImprovementsAdapter extends ArrayAdapter<HashMap<String, Object>> {
	
	private AppCompatActivity context;
	private int layout;
	private ArrayList<HashMap<String, Object>> data;
	
	/** Create the tool for displaying search results in a ListView.
	 * @param context The context of the Activity.
	 * @param layout The layout that will be used for each row in the results.
	 * @param data The search result data that will be displayed in the ListView.
	 */
	public ImprovementsAdapter(AppCompatActivity context, int layout, ArrayList<HashMap<String, Object>> data) {
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
		HashMap<String, View> holder = null;
		
		// If they are requesting the view of a new row,
		if(row == null)
		{
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layout, parent, false);
			
			// Create a new ViewHolder, which is a container for the views in the row that we will
			// populate with search data.
			holder = new HashMap<>();
			
			// Add views to the holder
			HashMap<String, Object> improvement = data.get(position);
			for (String key : improvement.keySet()) {
				for (Field field : R.id.class.getFields()) {
					// If that GUI item matches the column name, we want to put column data in it.
					if (field.getName().equals(key)) {
						// Dynamically get the view of the GUI item.
						View view = null;
						try {
							view = row.findViewById(field.getInt(null));
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
						
						if (view != null)
							holder.put(key, view);
					}
				}
			}
			
			row.setTag(holder);
		} else {
			holder = (HashMap<String, View>) row.getTag();
		}
		
		// Get the data for the row they're requesting.
		HashMap<String, Object> improvement = data.get(position);
		
		// Update the Views to contain the data.
		for (String key : improvement.keySet()) {
			if (holder.get(key) != null)
				((TextView) holder.get(key)).setText((String) improvement.get(key));
		}
		
		return row;
	}
}