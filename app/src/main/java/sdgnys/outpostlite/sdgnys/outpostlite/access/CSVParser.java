package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.telecom.Call;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static sdgnys.outpostlite.Logger.log;

/**
 * Created by jforce on 8/9/2017.
 * This class is a tool for parsing the CSV data contained in file.
 */
public class CSVParser {
	
	private final String VALUE_DELIMITER, RECORD_DELIMITER;
	
	private final File file;
	private ArrayList<String[]> records;
	
	/** Create a new parser given a specific file to parse. */
	public CSVParser(File file, String valueDelimiter, String recordDelimiter) {
		if (!file.exists())
			throw new RuntimeException("File to parse doesn't exist");
		
		VALUE_DELIMITER = Pattern.quote(valueDelimiter);
		RECORD_DELIMITER = Pattern.quote(recordDelimiter);
		
		this.file = file;
	}
	
	public ArrayList<String[]> getRecords() {
		if (records == null)
			throw new RuntimeException("You must call beginParsing before accessing parsed data.");
		return records;
	}
	
	public void beginParsing(Callback<Float> progressCallback) {
		records = new ArrayList<String[]>();
		
		// All this junk just opens the file and begins reading it line by line.
		FileInputStream is = null;
		BufferedReader reader;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			line = reader.readLine();
			while (line != null){
				// Parse the line.
				String[] all = line.split(RECORD_DELIMITER);
				for (int i = 0; i < all.length; i ++) {
					String record = all[i];
					records.add(record.split(VALUE_DELIMITER));
					progressCallback.callback(((float) i) / all.length);
				}
				
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
