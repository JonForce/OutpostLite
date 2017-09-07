package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static sdgnys.outpostlite.Logger.log;
import static sdgnys.outpostlite.Logger.logE;

/**
 * Created by jforce on 8/31/2017.
 */

public class PackageParser {
	
	// We don't use namespaces
	private static final String NAMESPACE = null;
	
	private StorageAccess storage;
	private ArrayList<HashMap<String, Object>> parcels;
	
	// Put all tags that have sub-tags here.
	private final String[] SUPER_TAGS = {
			"Location", "Owners", "Owner", "Assessment", "Sites", "Site", "Improvements",
			"Improvement", "LANDS", "LAND", "Sales", "Sale"
	};
	
	public PackageParser(StorageAccess storage) {
		this.storage = storage;
	}
	
	public void beginParsing() {
		File parcelData = storage.getInternalFile("Parcel_Data.xml");
		try {
			parcels = parse(new FileInputStream(parcelData));
		} catch (XmlPullParserException e) {
			throw new RuntimeException("Couldn't parse the xml file : " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find the parcel data file : " + e.getMessage());
		}
	}
	
	public ArrayList<HashMap<String, Object>> getParcels() {
		return parcels;
	}
	
	private ArrayList<HashMap<String,Object>> parse(InputStream in) throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readParcels(parser);
		} finally {
			in.close();
		}
	}
	
	private ArrayList<HashMap<String, Object>> readParcels(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<HashMap<String, Object>> entries = new ArrayList<>();
		
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Data");
		parser.next();
		while (true) {
			if (!skipToStartTag(parser)) {
				logE("Issue when trying to read parcels. Couldn't skip to start tag. Returning gracefully.");
				return entries;
			}
			
			String name = parser.getName();
			if (name.equals("Parcel")) {
				try {
					entries.add(readParcel(parser));
				} catch (Exception e) {
					logE("!!!!!!!!!!!! Hard exception while reading parcel! Will try to recover. Error : "+ e.getMessage());
				}
			} else {
				skip(parser);
			}
			
			skipNullNames(parser);
			if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("Data"))
				break;
		}
		return entries;
	}
	
	private HashMap<String, Object> readParcel(XmlPullParser parser) throws XmlPullParserException, IOException {
		log("Reading Parcel..");
		
		parser.require(XmlPullParser.START_TAG, NAMESPACE, "Parcel");
		HashMap<String, Object> parcelData = new HashMap<>();
		
		while (true) {
			parser.next();
			if (!skipNullNames(parser)) {
				logE("Issue during parcel parse. Failed to skip null names.Will try to fail gracefully.");
				return parcelData;
			}
			if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("Parcel"))
				break;
			dynamicParse(parser, parcelData);
		}
		
		return parcelData;
	}
	
	private void dynamicParse(XmlPullParser parser, HashMap<String, Object> map) throws IOException, XmlPullParserException {
		if (skipToStartTag(parser) == false) {
			logE("Issue during dynamic parse. Couldn't skip to start tag. Aborted.");
			return;
		}
		
		
		
		String name = parser.getName();
		log("Dynamic parse of " + name);
	
		if (contains(name, SUPER_TAGS)) {
			HashMap<String, Object> subMap = new HashMap<>();
			parser.next();
			while (true) {
				parser.next();
				skipNullNames(parser);
				
				// In case of fuck-up try to recover by exiting this parsing loop if we hit a
				//      parcel tag. This would ideally never be true though.
				if (parser.getName().equals("Parcel"))
					return;
				
				if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(name))
					break;
				dynamicParse(parser, subMap);
			}
			map.put(name, subMap);
			parser.next();
		} else {
			parser.next();
			map.put(name, parser.getText());
			parser.next();
		}
	}
	
	private boolean skipNullNames(XmlPullParser parser) throws IOException, XmlPullParserException {
		int count = 0;
		while (parser.getName() == null) {
			log("Skipping null name ");
			if (count++ > 100)
				return false;
			
			parser.next();
		}
		return true;
	}
	
	private boolean skipToStartTag(XmlPullParser parser) throws XmlPullParserException, IOException {
		int count = 0;
		while (parser.getEventType() != XmlPullParser.START_TAG) {
			// Add provision for failure to prevent freezing.
			if (count++ > 1000) {
				logE("Could not skip to start tag, probably is no more start tags.");
				return false;
			}
			
			parser.next();
		}
		return true;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG)
			throw new IllegalStateException();
		
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}
	
	private boolean contains(String s, String[] array) {
		for (String a : array)
			if (a.equals(s))
				return true;
		return false;
	}
	
}