package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jforce on 8/23/2017.
 * This class serves as an access point for reading and writing notes
 * to the file system regarding any parcel.
 */
public class NotesAccess {
	
	private final StorageAccess storage;
	
	public NotesAccess(AppCompatActivity context) {
		// Establish a connection to storage.
		this.storage = new StorageAccess(context);
	}
	
	/** This method will write to the notes for the specified parcel. It overwrites whatever
	 * is currently there in that notes file. */
	public void writeNotes(String SWIS, String SBL, String PARCEL_ID, String notes) {
		File file = getNotesFile(SWIS, SBL, PARCEL_ID);
		
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		try{
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.print(notes);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("IO Error in writing notes. " + e.getMessage());
		}
	}
	
	/** @return the notes for the specified parcel. */
	public String getNotes(String SWIS, String SBL, String PARCEL_ID) {
		// Get a reference to where that file should be.
		File file = getNotesFile(SWIS, SBL, PARCEL_ID);
		
		// If it doesn't exist,
		if (!file.exists()) {
			// Create it and return "".
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create notes file. " + e.getMessage());
			}
			return "";
		} else {
			// Else we need to read everything in that notes file.
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				
				return sb.toString();
			} catch (IOException e) {
				throw new RuntimeException("IO Error when trying to get Notes File : " + e.getMessage());
			}
		}
	}
	
	/** @return a reference to the notes file for a specified parcel. The file may or may not exist. */
	private File getNotesFile(String SWIS, String SBL, String PARCEL_ID) {
		return new File(storage.exportDirectory, "NOTES-FOR_"+SWIS+"_"+SBL+"_"+PARCEL_ID+".txt");
	}
	
}
