package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.support.v7.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * Created by jforce on 8/22/2017.
 *
 * The Mod File is a file that will be transferred to the computer with the export package.
 * The Mod File contains all of the changes that have been made on the tablet that need to occur on
 * the desktop / RPS. There should be additional documentation about what the Mod File looks like.
 */
public class ModFileAccess {
	
	private final static String
			MOD_FILE_NAME = "ModFile.txt",
			SET_DEFAULT_TAG = "SET_DEFAULT_IMAGE",
			DELETE_IMAGE_TAG = "DELETE_IMAGE",
			SET_TAG = "SET",
			ENTRY_DELIMITER = "|";
	
	private final StorageAccess storage;
	private final File modFile;
	
	public ModFileAccess(AppCompatActivity context) {
		// Gain access to storage.
		storage = new StorageAccess(context);
		
		// Create the reference to the Mod File.
		modFile = new File(storage.exportDirectory, MOD_FILE_NAME);
		
		// If the Mod File doesn't exist,
		if (!modFile.exists())
			// Create it. We need it to exist.
			try {
				modFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create mod file. " + e.getMessage());
			}
	}
	
	/** Add a record to the Mod File that instructs the PC to delete an image. */
	public void addDeleteImage(String SWIS, String PRINT_KEY, String PARCEL_ID, int IMAGE_ID) {
		write(DELETE_IMAGE_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + IMAGE_ID);
	}
	
	/** Add a record to the Mod File that instructs the PC to set an image to default. */
	public void addSetDefaultImage(String SWIS, String PRINT_KEY, String PARCEL_ID, int IMAGE_ID) {
		write(SET_DEFAULT_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + IMAGE_ID);
	}
	
	/** Add a record to the Mod File that instructs the PC to set the value of a parcel.
	 * This is either the Total or Land value. */
	public void addSetValue(String SWIS, String PRINT_KEY, String PARCEL_ID, String name, String VALUE) {
		write(SET_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + name + "," + VALUE);
	}
	
	/** This method simply writes a String to the Mod File. It is provided for convenience. */
	private void write(String entry) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(modFile, true)));
			out.println(entry + ENTRY_DELIMITER);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write to modFile. " + e.getMessage());
		}
	}
}
