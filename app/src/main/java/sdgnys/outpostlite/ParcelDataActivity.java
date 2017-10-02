package sdgnys.outpostlite;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by jforce on 9/20/2017.
 * This is an activity where views that have names matching the field names in the parcel
 * data will automatically be filled in with that data.
 */
public abstract class ParcelDataActivity extends AppCompatActivity {
	
	protected HashMap<String, Object> parcelData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		parcelData =
				(HashMap<String, Object>) getIntent().getSerializableExtra("parcelData");
	}
	
	/** This method updates the user interface using the given parcel data. */
	protected void updateUI(HashMap<String, Object> data) {
		for (String dataKey : data.keySet()) {
			if (data.get(dataKey) instanceof HashMap) {
				updateUI((HashMap<String, Object>) data.get(dataKey));
				continue;
			}
			
			View view = getViewByName(dataKey, this);
			
			// Set the text of this view to have the data from the SQLite result.
			if (view != null) {
				((TextView) view).setText((String) data.get(dataKey));
			}
			
		}
	}
	
	public static View getViewByName(String name, AppCompatActivity context) {
		for (Field field : R.id.class.getFields()) {
			// If that GUI item matches the column name, we want to put column data in it.
			if (field.getName().equals(name)) {
				// Dynamically get the view of the GUI item.
				View view = null;
				try {
					view = context.findViewById(field.getInt(null));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
				return view;
			}
		}
		
		return null;
	}
}