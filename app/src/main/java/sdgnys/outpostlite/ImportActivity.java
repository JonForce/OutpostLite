package sdgnys.outpostlite;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.sdgnys.outpostlite.access.CSVParser;
import sdgnys.outpostlite.sdgnys.outpostlite.access.Callback;
import sdgnys.outpostlite.sdgnys.outpostlite.access.ParcelXmlParser;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.DataTable;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ImageDataTable;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ParcelDataTable;

public class ImportActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);
		
		final StorageAccess storage = new StorageAccess(this);
		final Database database = new Database(this);
		
		// On the click of the import button,
		findViewById(R.id.importButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Begin the importing of the package.
				importPackage(storage, database);
				
				// Show the progress bar ans status text, but hide the import button.
				findViewById(R.id.importButton).setVisibility(View.INVISIBLE);
				findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
				findViewById(R.id.Status).setVisibility(View.VISIBLE);
				
				findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
			}
		});
		
		// On click of the back button,
		final Intent intent = new Intent(this, MainActivity.class);
		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});
	}
	
	private void importPackage(final StorageAccess storage, final Database database) {
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				setStatus("Importing package...");
				
				setStatus("(1/10) Deleting current package data...");
				storage.deleteLocalFiles();
				
				setStatus("(2/10) Unzipping incoming package.");
				storage.loadPackage(new Callback<Float>() {
					@Override
					public void callback(Float data) {
						setPercentLoaded(data);
					}
				});
				
				setStatus("(3/10) Parsing the package's parcel data...");
				ParcelXmlParser parcelXmlParser = new ParcelXmlParser(storage);
				parcelXmlParser.beginParsing();
				
				setStatus("(4/10) Parsing the package's image data...");
				CSVParser imageDataParser = new CSVParser(
						storage.getInternalFile("image_data.txt"), "|", "\n");
				imageDataParser.beginParsing(new Callback<Float>() {
					@Override
					public void callback(Float data) {
						setPercentLoaded(data);
					}
				});
				
				
				setStatus("(5/10) Inserting package's parcel data into database...");
				database.resetDatabase(database.getWritableDatabase());
				
				fillDatabase(database, parcelXmlParser.getParcels());
				
				setStatus("(5/10) Inserting package's image data into database...");
				fillImageDatabase(database, imageDataParser, new ImageDataTable());
				
				setStatus("Successfully finished!");
				showCheckmark();
			}
		});
	}
	
	/** This method takes in parsed parcel data and inputs the stuff that's needed into the database. */
	private void fillDatabase(Database database, ArrayList<HashMap<String, Object>> parcels) {
		int XML_LOCATION = 0;
		for (HashMap<String, Object> parcel : parcels) {
			setPercentLoaded(((float) XML_LOCATION) / parcels.size());
			database.addRecord(
					new ParcelDataTable(),
					(String) parcel.get("SWIS"),
					(String) parcel.get("PRINT_KEY"),
					(String) parcel.get("Parcel_Id"),
					(String) ((HashMap<String, Object>) parcel.get("Location")).get("Loc_St_Nbr"),
					(String) ((HashMap<String, Object>) parcel.get("Location")).get("Street"),
					(String) ((HashMap<String, Object>) parcel.get("Location")).get("Loc_Muni_Name"),
					(String) ((HashMap<String, Object>) parcel.get("Assessment")).get("TotalAV"),
					(String) ((HashMap<String, Object>) parcel.get("Assessment")).get("LandAV"),
					XML_LOCATION + ""
					);
			XML_LOCATION ++;
		}
	}
	
	private void fillImageDatabase(Database database, CSVParser parser, DataTable table) {
		ArrayList<String[]> parcelRecords = parser.getRecords();
		for (int i = 0; i < parcelRecords.size(); i ++) {
			setPercentLoaded(((float) i) / parcelRecords.size());
			database.addRecord(table, parcelRecords.get(i));
		}
	}
	
	private void showCheckmark() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				findViewById(R.id.checkmark).setVisibility(View.VISIBLE);
				
				findViewById(R.id.backButton).setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void setPercentLoaded(float percent) {
		// Convert the progress to be out of 100.
		final int progress = (int) (percent * 100);
		
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Update the actual progress bar.
				((ProgressBar) findViewById(R.id.progressBar)).setProgress(progress);
			}
		});
	}
	
	private void setStatus(final String status) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Set the text of the UI status.
				((TextView) findViewById(R.id.Status)).setText(status);
			}
		});
	}
}
