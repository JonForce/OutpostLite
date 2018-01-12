package sdgnys.outpostlite.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.R;
import sdgnys.outpostlite.ViewParcel;
import sdgnys.outpostlite.sdgnys.outpostlite.access.ParcelXmlParser;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;

import static sdgnys.outpostlite.search.RowData.*;
import static sdgnys.outpostlite.search.RowData.PRINT_KEY;

/** This is the activity that does the searching through the SQLite database
 * and is also responsible for displaying the results of the search.
 * Created by jforce on 8/10/2017.
 *
 * Requires that there be extras placed in the intent to be used as search terms.
 */
public class SearchActivity extends AppCompatActivity {
	
	private ArrayList<RowData> searchResults;
	private ArrayList<HashMap<String, Object>> parcelData;
	private boolean
			numberSortAscending = true,
			nameSortAscending = true;
	private String municipality, PRINT_KEY, SBL, streetNumber, streetName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		// Pull in the input parameters that will be used as search terms.
		municipality = getIntent().getStringExtra("municipality");
		SBL = getIntent().getStringExtra("SBL");
		PRINT_KEY = getIntent().getStringExtra("PRINT_KEY");
		streetNumber = getIntent().getStringExtra("streetNumber");
		streetName = getIntent().getStringExtra("streetName");
		
		// Pull all of the parcel data from disk. This is expensive.
		ParcelXmlParser parser = new ParcelXmlParser(new StorageAccess(this));
		parser.beginParsing();
		parcelData = parser.getParcels();
		
		findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideZoomedImage();
			}
		});
		
		setupSortButtons();
		search();
		
		hideZoomedImage();
	}
	
	/** This event should be activated when a search result is pressed or selected. It will
	 * launch the ViewParcel activity with the selected parcel's data.
	 */
	public void selectSearchResult(int position) {
		Intent intent = new Intent(SearchActivity.this, ViewParcel.class);
		
		int XML_LOCATION =
				Integer.parseInt(searchResults.get(position).values[RowData.XML_LOCATION]);
		HashMap<String, Object> parcel = parcelData.get(XML_LOCATION);
		
		intent.putExtra("parcelData", parcel);
		
		startActivity(intent);
	}
	
	private void setupSortButtons() {
		final ImageButton
				stNumberSortButton = (ImageButton) findViewById(R.id.stNumberSortButton),
				stNameSortButton = (ImageButton) findViewById(R.id.stNameSortButton);
		
		stNumberSortButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				numberSortAscending = !numberSortAscending;
				if (numberSortAscending)
					stNumberSortButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_up_float));
				else
					stNumberSortButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_down_float));
				search();
			}
		});
		
		stNameSortButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nameSortAscending = !nameSortAscending;
				if (nameSortAscending)
					stNameSortButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_up_float));
				else
					stNameSortButton.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_down_float));
				search();
			}
		});
	}
	
	private void search() {
		// Populate the search terms into an object to be sent to the search program.
		RowData searchTerms = new RowData();
		searchTerms.values[Loc_Muni_Name] = municipality;
		searchTerms.values[RowData.PRINT_KEY] = PRINT_KEY;
		searchTerms.values[Loc_St_Nbr] = streetNumber;
		searchTerms.values[Street] = streetName;
		
		// Launch a search with the specified terms and get the results.
		searchResults = new Search(this, searchTerms, nameSortAscending, numberSortAscending).getResults();
		
		if (searchResults.size() > 0) {
			// Set the adapter of the ListView to be a search results adapter.
			// This is so the ListView can display the search results.
			getListView().setAdapter(new SearchResultsAdapter(this, R.layout.search_result, searchResults) {
				@Override
				protected void onImagePress(ImageView pressedView) {
					((ImageView) findViewById(R.id.zoomedImage)).setImageDrawable(pressedView.getDrawable());
					showZoomedImage();
				}
			});
			
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3)
				{
					selectSearchResult(position);
				}
			});
		} else {
			findViewById(R.id.noResultsLabel).setVisibility(View.VISIBLE);
		}
	}
	
	private void showZoomedImage() {
		findViewById(R.id.zoomedImage).setVisibility(View.VISIBLE);
		findViewById(R.id.closeButton).setVisibility(View.VISIBLE);
	}
	private void hideZoomedImage() {
		findViewById(R.id.zoomedImage).setVisibility(View.INVISIBLE);
		findViewById(R.id.closeButton).setVisibility(View.INVISIBLE);
	}
	
	/** @return the list view. */
	private ListView getListView() {
		return (ListView) this.findViewById(R.id.listView);
	}
	
	private CharSequence getDataFromView(View view, int subViewID) {
		return ((TextView) view.findViewById(subViewID)).getText();
	}
}