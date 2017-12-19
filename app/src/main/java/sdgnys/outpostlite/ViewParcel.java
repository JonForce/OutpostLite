package sdgnys.outpostlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.improvements.ImprovementsActivity;
import sdgnys.outpostlite.sdgnys.outpostlite.access.ModFileAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.ParcelDataTable;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.SqlQueries;
import sdgnys.outpostlite.search.SearchActivity;

import static sdgnys.outpostlite.Logger.logE;

/**
 * @author jforce
 * This Activity is used for viewing a parcels images and relevant data. It is also
 * the Activity that launces the Image Capture Activity and saves images to the internal
 * export directory.
 */
public class ViewParcel extends ParcelDataActivity {
	
	private static final int
			REQUEST_IMAGE_CAPTURE = 1;
	private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.sdgnys.outpostlite.fileprovider";
	
	/** These constants define the width and height of the thumbnails that the user can scroll through */
	private static final int
		THUMBNAIL_WIDTH = 900, THUMBNAIL_HEIGHT = 600;
	
	private StorageAccess storage;
	private ModFileAccess modFile;
	private Database database;
	private String SWIS, PRINT_KEY, PARCEL_ID;
	
	private File imageToCompress;
	private ImageView zoomedInOn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_parcel);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		// Update the UI with data about the Parcel we recieved.
		super.updateUI(parcelData);
		
		storage = new StorageAccess(this);
		modFile = new ModFileAccess(this);
		database = new Database(this);
		
		// Pull in the input parameters that will be used to identify the parcel.
		SWIS = (String) parcelData.get("SWIS");
		PRINT_KEY = (String) parcelData.get("PRINT_KEY");
		PARCEL_ID = (String) parcelData.get("Parcel_Id");
		
		loadImages();
		
		findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomOut();
			}
		});
		
		findViewById(R.id.addPhotoButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			launchPhotoCapture();
			}
		});
		
		findViewById(R.id.makeDefaultButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeDefault(zoomedInOn);
			}
		});
		
		findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteImage(zoomedInOn);
			}
		});
		
		findViewById(R.id.notesButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, NotesActivity.class);
				intent.putExtra("SWIS", SWIS);
				intent.putExtra("PRINT_KEY", PRINT_KEY);
				intent.putExtra("PARCEL_ID", PARCEL_ID);
				intent.putExtra("address", (String) parcelData.get("Street"));
				startActivity(intent);
			}
		});
		
		findViewById(R.id.improvementsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, ImprovementsActivity.class);
				intent.putExtra("improvements", getImprovements());
				startActivity(intent);
			}
		});
		
		findViewById(R.id.saleButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ViewParcel.this, SaleActivity.class);
				intent.putExtra("parcelData", getSale());
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
		
		
		// We need to set the LandAV and TotalAV text boxes to have the values from the DB.
		updateAVTextBoxes();
		
		database.printDatabase(ParcelDataTable.TABLE_NAME);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// If the result was an image capture, load the images onto screen.
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			if (imageToCompress != null) {
				storage.compressJPG(imageToCompress, 100, 4);
				imageToCompress = null;
			}
			
			loadImages();
		}
		
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	/** This method queries the database to get the current average land and total values. It then
	 * puts those into the text boxes on the screen. */
	private void updateAVTextBoxes() {
		// Make a query for both the land and the total average values.
		Cursor LandAVCursor =
				database.getReadableDatabase().rawQuery(
						SqlQueries.get(SWIS, PRINT_KEY, PARCEL_ID, ParcelDataTable.TABLE_NAME, "LandAV"), null);
		Cursor TotalAVCursor =
				database.getReadableDatabase().rawQuery(
						SqlQueries.get(SWIS, PRINT_KEY, PARCEL_ID, ParcelDataTable.TABLE_NAME, "TotalAV"), null);
		
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
					"PRINT_KEY = \"" + PRINT_KEY + "\" AND " +
					"PARCEL_ID = " + PARCEL_ID;
			
			// Make the change.
			database.getWritableDatabase().update(ParcelDataTable.TABLE_NAME, values, whereClause, null);
			
			modFile.addSetValue(SWIS, PRINT_KEY, PARCEL_ID, name, newValue);
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
	
	/** Delete the image that is associated with the specified View. */
	private void deleteImage(View view) {
		File file = (File) view.getTag();
		
		if (storage.getFileIsDefault(file))
			Toast.makeText(this,
					"Warning! You deleted the default image.\n" +
					"Make another image the default", Toast.LENGTH_LONG).show();
		
		if (file.getParent().equals(storage.exportDirectory.getPath())) {
			// File is in export directory. We need only delete it from there.
		} else {
			// File is stored in RPS already. We need to tell the PC to delete from RPS.
			modFile.addDeleteImage(SWIS, PRINT_KEY, PARCEL_ID, storage.getFileID(file));
		}
		
		file.delete();
		
		loadImages();
		zoomOut();
	}
	
	/** Makes the image that is associated with the specified View the new default image for
	 * this parcel. */
	private void makeDefault(View view) {
		// Clear any current defaults
		for (File f : images())
			if (storage.getFileIsDefault(f))
				storage.setFileIsDefault(f, false);
		
		// Set the new default
		File newDefault = (File) view.getTag();
		
		storage.setFileIsDefault(newDefault, true);
		
		modFile.addSetDefaultImage(SWIS, PRINT_KEY, PARCEL_ID, storage.getFileID(newDefault));
		
		loadImages();
	}
	
	/** Launches the photo capture intent for result.
	 * Result will be stored in the internal export directory. */
	private void launchPhotoCapture() {
		// Get the path to the export directory.
		File path = new File(getFilesDir(), "export/");
		
		// Get a reference to the new image file.
		File image = new File(path, storage.getNewImageFileName(SWIS, PRINT_KEY, PARCEL_ID));
		imageToCompress = image;
		// Use our FileProvider to get a Uri for our new image file.
		Uri imageUri = FileProvider.getUriForFile(ViewParcel.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
		
		// Launch the photo capture intent for result.
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}
	
	/** This method zooms out if there is an image zoomed in on. */
	private void zoomOut() {
		zoomedInOn = null;
		findViewById(R.id.zoomedImage).setVisibility(View.INVISIBLE);
		findViewById(R.id.closeButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.makeDefaultButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.deleteButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.setLand).setVisibility(View.VISIBLE);
		findViewById(R.id.setTotal).setVisibility(View.VISIBLE);
	}
	
	/** This method brings the specified ImageView into focus by displaying it in the center
	 * of the screen.
	 * @param imageView The image to zoom in on.
	 */
	private void zoomInOn(ImageView imageView) {
		zoomedInOn = imageView;
		
		if (storage.getFileIsDefault((File) imageView.getTag()))
			findViewById(R.id.makeDefaultButton).setVisibility(View.INVISIBLE);
		else
			findViewById(R.id.makeDefaultButton).setVisibility(View.VISIBLE);
		
		ImageView zoomView = (ImageView) findViewById(R.id.zoomedImage);
		zoomView.setVisibility(View.VISIBLE);
		
		zoomView.setImageDrawable(imageView.getDrawable());
		findViewById(R.id.closeButton).setVisibility(View.VISIBLE);
		findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
		findViewById(R.id.setLand).setVisibility(View.INVISIBLE);
		findViewById(R.id.setTotal).setVisibility(View.INVISIBLE);
	}
	
	/** Clears all the images from the view. */
	private void clearImages() {
		((LinearLayout)findViewById(R.id.imageLayout)).removeAllViews();
	}
	
	/** Load all the images that belong with this parcel from disk and onto the screen. */
	private void loadImages() {
		clearImages();
		
		ArrayList<File> images = images();
		
		File defaultImage = null;
		for (File f : images)
			if (storage.getFileIsDefault(f)) {
				if (defaultImage != null)
					logE("More than one default image! Oh No. Tell a programmer.");
				
				defaultImage = f;
			}
		
		if (defaultImage != null) {
			// Put the default image at the beginning of the list.
			images.remove(defaultImage);
			images.add(0, defaultImage);
		} else logE("Not even one default image. Oh no.");
		
		addImages(images);
	}
	
	/** Load all the images from disk onto screen that have the PRINT_KEY of this parcel. */
	private void addImages(ArrayList<File> files) {
		// Find the layout where we'll be adding all the images.
		LinearLayout layout = ((LinearLayout)findViewById(R.id.imageLayout));
		
		// For every file in internal storage,
		for (File f : files) {
			// Create a new ImageView to put the found image in.
			final ImageView image = new ImageView(this);
			Bitmap map = storage.getImageBitmap(f);
			map = Bitmap.createScaledBitmap(map, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
			image.setImageBitmap(map);
			image.setTag(f);
			image.setPadding(0, 8, 0, 0);
			// Ensure when the image is clicked, the activity zooms in on it.
			image.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					zoomInOn(image);
				}
			});
			// Add the image to the layout.
			layout.addView(image);
		}
	}
	
	/** @rerturn all the images associated with this parcel. */
	private ArrayList<File> images() {
		ArrayList<File> images = new ArrayList<>();
		// Add all from regular storage.
		for (File f : getFilesDir().listFiles())
			if (storage.isImage(f) && f.getName().contains(PRINT_KEY))
				images.add(f);
		// Add all from the export directory.
		for (File f : storage.exportDirectory.listFiles())
			if (storage.isImage(f) && f.getName().contains(PRINT_KEY))
				images.add(f);
		
		return images;
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
	
	/** This will make the keyboard go away. Stolen shamelessly from
	 * https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear */
	private void hideKeyboard(EditText editText) {
		InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}
