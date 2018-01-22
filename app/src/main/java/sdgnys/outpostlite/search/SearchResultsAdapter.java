package sdgnys.outpostlite.search;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import sdgnys.outpostlite.R;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ImageDataTable;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static sdgnys.outpostlite.search.SearchResult.OwnerNames;
import static sdgnys.outpostlite.search.SearchTerms.*;

/**
 * This class is an adapter that will allow search results to be displayed in a ListView.
 *
 * Created by jforce on 8/10/2017.
 */
abstract class SearchResultsAdapter extends ArrayAdapter<SearchResult> {
	
	protected abstract void onImagePress(ImageView view);
	
	private static final int
		DARK_BG_R = 232, DARK_BG_G = 240, DARK_BG_B = 255;
	
	private SearchActivity activity;
	private int layout;
	private ArrayList<SearchResult> data;
	private StorageAccess storage;
	private Database database;
	private int ownerNameCharacterLimit;
	
	/** Create the tool for displaying search results in a ListView.
	 * @param activity The context of the Activity.
	 * @param layout The layout that will be used for each row in the results.
	 * @param data The search result data that will be displayed in the ListView.
	 */
	public SearchResultsAdapter(SearchActivity activity, int layout, ArrayList<SearchResult> data) {
		super(activity, layout, data);
		this.storage = new StorageAccess(activity);
		this.activity = activity;
		this.layout = layout;
		this.data = data;
		this.database = new Database(activity);
		
		if (activity.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE)
			ownerNameCharacterLimit = 45;
		else
			ownerNameCharacterLimit = 18;
	}
	
	/** We overwrite this method to provide a custom implementation for how views will
	 * appear on each row in the ListView. */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder = null;
		
		// If they are requesting the view of a new row,
		if(row == null)
		{
			LayoutInflater inflater = activity.getLayoutInflater();
			row = inflater.inflate(layout, parent, false);
			
			// Create a new ViewHolder, which is a container for the views in the row that we will
			// populate with search data.
			holder = new ViewHolder();
			holder.SWIS = (TextView)row.findViewById(R.id.SWIS);
			holder.PRINT_KEY = (TextView)row.findViewById(R.id.PRINT_KEY);
			holder.Loc_St_Nbr = (TextView)row.findViewById(R.id.Street);
			holder.Street = (TextView)row.findViewById(R.id.LOC_ST_NAME);
			holder.Loc_Muni_Name = (TextView)row.findViewById(R.id.LOC_MUNI_NAME);
			holder.viewButton = (Button)row.findViewById(R.id.viewButton);
			holder.image = (ImageView)row.findViewById(R.id.thumbnail);
			holder.Owner0 = (TextView)row.findViewById(R.id.Owner0);
			holder.Owner1 = (TextView)row.findViewById(R.id.Owner1);
			
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		
		// Get the search data for the row they're requesting.
		SearchResult searchResult = data.get(position);
		
		// Update the Views to contain the search data.
		holder.SWIS.setText(searchResult.values[SWIS]);
		holder.PRINT_KEY.setText(searchResult.values[PRINT_KEY]);
		holder.Loc_St_Nbr.setText(searchResult.values[Loc_St_Nbr]);
		holder.Street.setText(searchResult.values[Street]);
		holder.Loc_Muni_Name.setText(searchResult.values[Loc_Muni_Name]);
		// If we have owner names,
		if (searchResult.values[OwnerNames] != null && searchResult.values[OwnerNames].length() > 0) {
			// Put them on the screen.
			String[] split = searchResult.values[OwnerNames].split(",");
			holder.Owner0.setText(limit(split[0]));
			if (split.length > 1)
				holder.Owner1.setText(limit(split[1]));
		}
		
		// Load the thumbnail and apply it to the image on screen.
		
		// Get the sequence # for the default image.
		String SEQUENCE =
				getSequence(searchResult.values[SWIS], searchResult.values[PRINT_KEY], searchResult.values[PARCEL_ID]) + "";
		// Get the image file of the default image we want to display.
		File imageFile = storage.getDefaultImage(
				searchResult.values[SWIS], searchResult.values[PRINT_KEY], searchResult.values[SBL], searchResult.values[PARCEL_ID], SEQUENCE);
		// If we managed to do this successfully,
		if (imageFile != null && imageFile.exists()) {
			Bitmap map = storage.getImageBitmap(imageFile);
			map = Bitmap.createScaledBitmap(map, 740, 493, true);
			holder.image.setVisibility(View.VISIBLE);
			holder.image.setImageBitmap(map);
			holder.image.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onImagePress((ImageView) v);
				}
			});
		} else {
			holder.image.setVisibility(View.INVISIBLE);
		}
		
		holder.viewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.selectSearchResult(position);
			}
		});
		
		// Apply dark background to every other row.
		if (position % 2 == 0)
			row.setBackgroundColor(Color.rgb(DARK_BG_R, DARK_BG_G, DARK_BG_B));
		else
			row.setBackgroundColor(Color.WHITE);
		
		return row;
	}
	
	/** This method will query the image data table and find the sequence # of the image that is
	 * the default. It will return -1 if it couldn't find it. */
	private int getSequence(String SWIS, String PRINT_KEY, String PARCEL_ID) {
		String query =
				"SELECT * FROM " + ImageDataTable.TABLE_NAME +
						" WHERE SBL = '" + PRINT_KEY +
						"' AND PARCEL_ID = '" + PARCEL_ID + "'" +
						" AND SWIS = '" + SWIS + "'" +
						" AND IS_DEFAULT_IMAGE = 1";
		
		Cursor cursor = database.getReadableDatabase().rawQuery(query, null);
		if (cursor.getCount() == 0)
			return -1;
		
		cursor.moveToFirst();
		
		return cursor.getInt(cursor.getColumnIndex("SEQUENCE"));
	}
	
	/** A simple object that contains the Views that need to be populated with search data. */
	static class ViewHolder {
		TextView SWIS, PRINT_KEY, Loc_St_Nbr, Street, Loc_Muni_Name, Owner0, Owner1;
		Button viewButton;
		ImageView image;
	}
	
	private String limit(String source) {
		return source.substring(0, Math.min(ownerNameCharacterLimit, source.length() - 1));
	}
}