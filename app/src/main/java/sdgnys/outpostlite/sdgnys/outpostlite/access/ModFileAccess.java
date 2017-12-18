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
		storage = new StorageAccess(context);
		
		modFile = new File(storage.exportDirectory, MOD_FILE_NAME);
		
		if (!modFile.exists())
			try {
				modFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create mod file. " + e.getMessage());
			}
	}
	
	public void addDeleteImage(String SWIS, String PRINT_KEY, String PARCEL_ID, int IMAGE_ID) {
		write(DELETE_IMAGE_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + IMAGE_ID);
	}
	
	public void addSetDefaultImage(String SWIS, String PRINT_KEY, String PARCEL_ID, int IMAGE_ID) {
		write(SET_DEFAULT_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + IMAGE_ID);
	}
	
	public void addSetValue(String SWIS, String PRINT_KEY, String PARCEL_ID, String name, String VALUE) {
		write(SET_TAG + "," + SWIS + "," + PRINT_KEY + "," + PARCEL_ID + "," + name + "," + VALUE);
	}
	
	private void write(String line) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(modFile, true)));
			out.println(line + ENTRY_DELIMITER);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write to modFile. " + e.getMessage());
		}
	}
}
