package sdgnys.outpostlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.improvements.ImprovementsActivity;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ParcelDataTable;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.SqlQueries;

/**
 * @author jforce
 * This Activity is used for viewing a parcels images and relevant data. It is also
 * the Activity that launces the Image Capture Activity and saves images to the internal
 * export directory.
 */
public class ViewParcel extends ParcelImageActivity {
	
	private Database database;
	
	public ViewParcel() { super(R.layout.activity_view_parcel); }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide keyboard. Without this, the keyboard pops up automatically.
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// Update the UI with data about the Parcel we received.
		super.updateUI(parcelData);
		
		database = new Database(this);
		
		findViewById(R.id.notesButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, NotesActivity.class);
				intent.putExtra("SWIS", SWIS);
				intent.putExtra("SBL", SBL);
				intent.putExtra("PARCEL_ID", PARCEL_ID);
				intent.putExtra("address",
						(String) ((HashMap<String, Object>)parcelData.get("Location")).get("Loc_St_Nbr") + "" +
						(String) ((HashMap<String, Object>)parcelData.get("Location")).get("Street"));
				startActivity(intent);
			}
		});
		
		findViewById(R.id.improvementsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, ImprovementsActivity.class);
				intent.putExtra("improvements", getImprovements());
				intent.putExtra("SWIS", SWIS);
				intent.putExtra("SBL", SBL);
				intent.putExtra("PRINT_KEY", (String) parcelData.get("PRINT_KEY"));
				intent.putExtra("PARCEL_ID", PARCEL_ID);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.saleButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, SaleActivity.class);
				intent.putExtra("parcelData", getSale());
				intent.putExtra("SWIS", SWIS);
				intent.putExtra("SBL", SBL);
				intent.putExtra("PRINT_KEY", (String) parcelData.get("PRINT_KEY"));
				intent.putExtra("PARCEL_ID", PARCEL_ID);
				startActivity(intent);
			}
		});
		
		findViewById(R.id.setLand).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String landValueText = ((EditText) findViewById(R.id.LandAVText)).getText().toString();
				setValue("LandAV", landValueText);
			}
		});
		
		findViewById(R.id.setTotal).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String totalValueText = ((EditText) findViewById(R.id.TotalAVText)).getText().toString();
				setValue("TotalAV", totalValueText);
			}
		});
		
		findViewById(R.id.directionsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String address =
						((HashMap<String, Object>) parcelData.get("Location")).get("Loc_St_Nbr")
						+ " " +
						((HashMap<String, Object>) parcelData.get("Location")).get("Street")
						+ " " +
						((HashMap<String, Object>) parcelData.get("Location")).get("Loc_Muni_Name");
				
				launchNavigationToAddress(address);
			}
		});
		
		// We need to set the LandAV and TotalAV text boxes to have the values from the DB.
		updateAVTextBoxes();
	}
	
	@Override
	protected void zoomInOn(ImageView view) {
		super.zoomInOn(view);
		
		findViewById(R.id.setLand).setVisibility(View.INVISIBLE);
		findViewById(R.id.setTotal).setVisibility(View.INVISIBLE);
		findViewById(R.id.improvementsButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.saleButton).setVisibility(View.INVISIBLE);
	}
	
	@Override
	protected void zoomOut() {
		super.zoomOut();
		
		findViewById(R.id.setLand).setVisibility(View.VISIBLE);
		findViewById(R.id.setTotal).setVisibility(View.VISIBLE);
		findViewById(R.id.improvementsButton).setVisibility(View.VISIBLE);
		findViewById(R.id.saleButton).setVisibility(View.VISIBLE);
	}
	
	/** This method queries the database to get the current average land and total values. It then
	 * puts those into the text boxes on the screen. */
	private void updateAVTextBoxes() {
		// Make a query for both the land and the total average values.
		Cursor LandAVCursor =
				database.getReadableDatabase().rawQuery(
						SqlQueries.get(SWIS, SBL, PARCEL_ID, ParcelDataTable.TABLE_NAME, "LandAV"), null);
		Cursor TotalAVCursor =
				database.getReadableDatabase().rawQuery(
						SqlQueries.get(SWIS, SBL, PARCEL_ID, ParcelDataTable.TABLE_NAME, "TotalAV"), null);
		
		// Next we need to move to the beginning of the results of those queries.
		LandAVCursor.moveToFirst();
		TotalAVCursor.moveToFirst();
		
		// If we have a result, put it on the screen.
		if (LandAVCursor.getCount() > 0)
			((EditText) findViewById(R.id.LandAVText)).setText(LandAVCursor.getString(0));
		if (TotalAVCursor.getCount() > 0)
			((EditText) findViewById(R.id.TotalAVText)).setText(TotalAVCursor.getString(0));
	}
	
	/** This method can be used to set the TotalAV and the LandAV in the database. */
	private void setValue(String name, String newValue) {
		try {
			// Declare the value that we want to change.
			ContentValues values = new ContentValues();
			values.put(name, Integer.parseInt(newValue));
			
			// Create the where clause that selects the correct record.
			String whereClause =
					"SWIS = " + SWIS + " AND " +
					"SBL = \"" + SBL + "\" AND " +
					"PARCEL_ID = " + PARCEL_ID;
			
			// Make the change.
			database.getWritableDatabase().update(ParcelDataTable.TABLE_NAME, values, whereClause, null);
			
			modFile.addSetValue(SWIS, SBL, PARCEL_ID, name, newValue);
		} catch (Exception e) {
			e.printStackTrace();
			
			// Let the user know about the error.
			Toast.makeText(this,
					"Couldn't set the " + name + " to " + newValue + "!",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		// Let the user know.
		Toast.makeText(this,
				"Successfully set the " + name + " to " + newValue + "!",
				Toast.LENGTH_LONG).show();
		
		hideKeyboard((EditText) getViewByName(name + "Text", this));
	}
	
	private ArrayList<HashMap<String, Object>> getImprovements() {
		return
				((ArrayList<HashMap<String, Object>>)
				((HashMap<String, Object>)
				((HashMap<String, Object>)
				((HashMap<String, Object>) parcelData.get("Sites") )
				.get("Site") )
				.get("Improvements") )
				.get("Improvement") );
	}
	
	private HashMap<String, Object> getSale() {
		return  ((HashMap<String, Object>)
				((HashMap<String, Object>)
				parcelData.get("Sales"))
				.get("Sale"));
	}
	
	/** Launch google maps with the intent of navigating to the given address. */
	private void launchNavigationToAddress(String address) {
		Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address);
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
		mapIntent.setPackage("com.google.android.apps.maps");
		startActivity(mapIntent);
	}
	
	/** This will make the keyboard go away. Stolen shamelessly from
	 * https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear */
	private void hideKeyboard(EditText editText) {
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}
