package sdgnys.outpostlite.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import sdgnys.outpostlite.R;
import sdgnys.outpostlite.ViewParcel;

import static sdgnys.outpostlite.Logger.log;
import static sdgnys.outpostlite.search.RowData.*;

/** This is the activity that does the searching through the SQLite database
 * and is also responsible for displaying the results of the search.
 * Created by jforce on 8/10/2017.
 *
 * Requires that there be extras placed in the intent to be used as search terms.
 */
public class SearchActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		// Pull in the input parameters that will be used as search terms.
		String
				municipality = getIntent().getStringExtra("municipality"),
				taxMapID = getIntent().getStringExtra("taxMapID"),
				streetNumber = getIntent().getStringExtra("streetNumber"),
				streetName = getIntent().getStringExtra("streetName");
		
		// Populate the search terms into an object to be sent to the search program.
		RowData searchTerms = new RowData();
		searchTerms.values[LOC_MUNI_NAME] = municipality;
		searchTerms.values[PRINT_KEY] = taxMapID;
		searchTerms.values[LOC_ST_NBR] = streetNumber;
		searchTerms.values[LOC_ST_NAME] = streetName;
		
		// Launch a search with the specified terms and get the results.
		ArrayList<RowData> searchResults = new Search(this, searchTerms).getResults();
		
		if (searchResults.size() > 0) {
			// Set the adapter of the ListView to be a search results adapter.
			// This is so the ListView can display the search results.
			getListView().setAdapter(new SearchResultsAdapter(this, R.layout.search_result, searchResults));
			
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3)
				{
					Intent intent = new Intent(SearchActivity.this, ViewParcel.class);
					intent.putExtra("SWIS", getDataFromView(view, R.id.SWIS));
					intent.putExtra("PRINT_KEY", getDataFromView(view, R.id.PRINT_KEY));
					intent.putExtra("PARCEL_ID", getDataFromView(view, R.id.PARCEL_ID));
					startActivity(intent);
				}
			});
		} else {
			findViewById(R.id.noResultsLabel).setVisibility(View.VISIBLE);
		}
	}
	
	/** @return the list view. */
	private ListView getListView() {
		return (ListView) this.findViewById(R.id.listView);
	}
	
	private CharSequence getDataFromView(View view, int subViewID) {
		return ((TextView) view.findViewById(subViewID)).getText();
	}
}