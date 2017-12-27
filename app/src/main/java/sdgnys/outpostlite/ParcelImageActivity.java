package sdgnys.outpostlite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import sdgnys.outpostlite.sdgnys.outpostlite.access.ModFileAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static sdgnys.outpostlite.Logger.logE;
import static sdgnys.outpostlite.R.id.view;

/**
 * Created by jforce on 12/27/2017.
 */
public class ParcelImageActivity extends ParcelDataActivity {
	
	private static final int
			REQUEST_IMAGE_CAPTURE = 1;
	
	/** These constants define the width and height of the thumbnails that the user can scroll through */
	private int
			LANDSCAPE_THUMBNAIL_WIDTH = 900, LANDSCAPE_THUMBNAIL_HEIGHT = 600,
			PORTRAIT_THUMBNAIL_WIDTH = 750, PORTRAIT_THUMBNAIL_HEIGHT = 500;
	private int THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT;
	
	private static final String CAPTURE_IMAGE_FILE_PROVIDER = "com.sdgnys.outpostlite.fileprovider";
	private ImageView zoomedInOn;
	
	private File imageToCompress;
	
	protected StorageAccess storage;
	protected ModFileAccess modFile;
	
	protected String SWIS, PRINT_KEY, PARCEL_ID;
	
	private final int layout;
	
	public ParcelImageActivity(int layout,
	        int LANDSCAPE_THUMBNAIL_WIDTH, int LANDSCAPE_THUMBNAIL_HEIGHT,
	         int PORTRAIT_THUMBNAIL_WIDTH, int PORTRAIT_THUMBNAIL_HEIGHT) {
		this.layout = layout;
		this.LANDSCAPE_THUMBNAIL_WIDTH = LANDSCAPE_THUMBNAIL_WIDTH;
		this.LANDSCAPE_THUMBNAIL_HEIGHT = LANDSCAPE_THUMBNAIL_HEIGHT;
		this.PORTRAIT_THUMBNAIL_WIDTH = PORTRAIT_THUMBNAIL_WIDTH;
		this.PORTRAIT_THUMBNAIL_HEIGHT = PORTRAIT_THUMBNAIL_HEIGHT;
	}
	
	public ParcelImageActivity(int layout) {
		this(layout, 900, 600, 750, 500);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout);
		
		// We need to set the size of the thumbnails depending on the orientation of the device
		if (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE) {
			THUMBNAIL_WIDTH = LANDSCAPE_THUMBNAIL_WIDTH;
			THUMBNAIL_HEIGHT = LANDSCAPE_THUMBNAIL_HEIGHT;
		} else {
			THUMBNAIL_WIDTH = PORTRAIT_THUMBNAIL_WIDTH;
			THUMBNAIL_HEIGHT = PORTRAIT_THUMBNAIL_HEIGHT;
		}
		
		// We need the swis, print key, and parcel id.
		// Try to get them from the parcel data if we can.
		if (parcelData != null) {
			SWIS = (String) parcelData.get("SWIS");
			PRINT_KEY = (String) parcelData.get("PRINT_KEY");
			PARCEL_ID = (String) parcelData.get("Parcel_Id");
		} else {
			// If not, we can get them from the intent.
			SWIS = getIntent().getExtras().getString("SWIS");
			PRINT_KEY = getIntent().getExtras().getString("PRINT_KEY");
			PARCEL_ID = getIntent().getExtras().getString("PARCEL_ID");
		}
		
		storage = new StorageAccess(this);
		modFile = new ModFileAccess(this);
		
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
		
		loadImages();
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
		Uri imageUri = FileProvider.getUriForFile(ParcelImageActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
		
		// Launch the photo capture intent for result.
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
	}
	
	/** This method zooms out if there is an image zoomed in on. */
	protected void zoomOut() {
		zoomedInOn = null;
		findViewById(R.id.zoomedImage).setVisibility(View.INVISIBLE);
		findViewById(R.id.closeButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.makeDefaultButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.deleteButton).setVisibility(View.INVISIBLE);
	}
	
	/** This method brings the specified ImageView into focus by displaying it in the center
	 * of the screen.
	 * @param imageView The image to zoom in on.
	 */
	protected void zoomInOn(ImageView imageView) {
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
	
}
