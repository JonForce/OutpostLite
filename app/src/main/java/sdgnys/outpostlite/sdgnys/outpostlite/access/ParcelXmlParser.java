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

import static sdgnys.outpostlite.Logger.logE;

/**
 * Created by jforce on 8/31/2017.
 * This class is to be used to parse XML parcel data from the disk.
 * You should likely contact jforce before changing this code. It's pretty flexible,
 * but parsing xml in android is somewhat nuanced and contains lots of traps.
 *
 * New simple tags with no subtags require no changes to the code.
 * New tags that contain subtags require addition to the SUPER_TAGS array.
 *
 * WARNING : This class does NOT fail fast. If it encounters parsing errors, it will
 *              log the problem and then continue operating. This was a conscious choice made
 *              because of the volatile and rapid changing nature of the data set.
 */
public class ParcelXmlParser {
	
	private static final String
			// We don't use namespaces
			NAMESPACE = null,
			DOCUMENT_TAG = "Data",
			PARCEL_TAG = "Parcel";
	
	private StorageAccess storage;
	// Parcels are represented by HashMaps, so this is a list of all the parcels.
	private ArrayList<HashMap<String, Object>> parcels;
	
	// Put all tags that have sub-tags here.
	private final String[] SUPER_TAGS = {
			"Location", "Owners", "Owner", "Assessment", "Sites", "Site", "Improvements",
			"Improvement", "LANDS", "LAND", "Sales", "Sale"
	};
	
	// Put all tags that repeat and belong in a list in here.
	private final String[] LIST_TAGS = {
			"Improvement", "Owner"
	};
	
	public ParcelXmlParser(StorageAccess storage) {
		this.storage = storage;
	}
	
	/** Start parsing all of the parcels from disk. When the parsing is finished,
	 * you can use getParcels() to retrieve the results. This method is synchronous.
	 */
	public void beginParsing() {
		// Get the parcel data file.
		File parcelData = storage.getInternalFile("Parcel_Data.xml");
		try {
			parcels = parse(new FileInputStream(parcelData));
		} catch (XmlPullParserException e) {
			throw new RuntimeException("Couldn't parse the xml file : " + e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("Couldn't find the parcel data file : " + e.getMessage());
		}
	}
	
	/** @return the results of parsing the parcel data from disk. Only use after beginParsing() */
	public ArrayList<HashMap<String, Object>> getParcels() {
		return parcels;
	}
	
	/** This method is the highest level of the parsing process. It will fire lower level parsing
	 * processes. It is responsible for creating the parsing objects and setup.
	 * @param in The stream of XML data.
	 * @return A list of all of the parcels' data.
	 */
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
	
	/** This method is the second level of the parsing process. It is responsible for calling
	 * readParcel on every given parcel tag in the parser.
	 * @param parser The parser to use for parsing the XML data.
	 * @return a list of all the parcel data hashmaps.
	 */
	private ArrayList<HashMap<String, Object>> readParcels(XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<HashMap<String, Object>> entries = new ArrayList<>();
		
		// Require that the outermost start tag be correct.
		parser.require(XmlPullParser.START_TAG, NAMESPACE, DOCUMENT_TAG);
		// Skip that tag because we don't need to focus on that shit.
		parser.next();
		
		while (true) {
			// Try to skip to the next opening tag. Because of the fragile nature of all things,
			// if it fails, just return what we have so far and hope no one notices.
			if (!skipToStartTag(parser)) {
				logE("Issue when trying to read parcels. Couldn't skip to start tag. Returning gracefully.");
				return entries;
			}
			
			// Get the name of this current opening tag.
			String name = parser.getName();
			// If it's a parcel tag,
			if (name.equals(PARCEL_TAG)) {
				try {
					// Try to read a parcel and add it to the list.
					entries.add(readParcel(parser));
				} catch (Exception e) {
					// There should never be an error. But if there is, we're going to catch it so
					// that the assessor doesn't get upset.
					logE("!!!!!!!!!!!! Hard exception while reading parcel! Will try to recover. Error : "+ e.getMessage());
				}
			} else {
				// If it wasn't a parcel tag, just skip it.
				skip(parser);
			}
			
			// Skip null garbage. Sometimes there is whitespace after tags that we need to avoid.
			skipNullNames(parser);
			// If we are at the document end tag we should definitely stop parsing.
			if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(DOCUMENT_TAG))
				break;
		}
		
		return entries;
	}
	
	/** Reads one Parcel from the parser. This method should only be called when the parser is on a
	 * Parcel start tag.
	 * @param parser The parser to read the Parcel from.
	 * @return The new Parcel object (in the form of a HashMap)
	 */
	private HashMap<String, Object> readParcel(XmlPullParser parser) throws XmlPullParserException, IOException {
		// Require that we be starting on a Parcel tag.
		parser.require(XmlPullParser.START_TAG, NAMESPACE, PARCEL_TAG);
		// Create the map where we will store data about the parcel.
		HashMap<String, Object> parcelData = new HashMap<>();
		
		while (true) {
			parser.next();
			// Try to skip whitespace. If we fail,
			if (!skipNullNames(parser)) {
				// Log the problem and return any data we managed to get already.
				logE("Issue during parcel parse. Failed to skip null names.Will try to fail gracefully.");
				return parcelData;
			}
			
			// If we have reached the end of our Parcel's data, break our parsing loop.
			if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(PARCEL_TAG))
				break;
			
			// Parse one tag into the parcelData map.
			dynamicParse(parser, parcelData);
		}
		
		return parcelData;
	}
	
	/** This is the lowest level of parsing. It reads the current tag, if it's a simple tag then it
	 * adds the data to the map. If the tag has sub tags, it behaves recursively.
	 * @param parser The parser to read one tag from.
	 * @param map The map to add the data to.
	 */
	private void dynamicParse(XmlPullParser parser, HashMap<String, Object> map) throws IOException, XmlPullParserException {
		// Try to skip whitespace and stuff, if we fail to do so,
		if (skipToStartTag(parser) == false) {
			// Log the issue and return gracefully.
			logE("Issue during dynamic parse. Couldn't skip to start tag. Aborted.");
			return;
		}
		
		// First get the name of the tag we are going to parse.
		String name = parser.getName();
	
		// If the tag we are going to parse has children,
		if (contains(name, SUPER_TAGS)) {
			// Create a sub map. This will hold of the data for the tag.
			HashMap<String, Object> subMap = new HashMap<>();
			// Move past the start tag.
			parser.next();
			
			while (true) {
				// Skip past the whitespace to the next tag.
				parser.next();
				skipNullNames(parser);
				
				// In case of fuck-up try to recover by exiting this parsing loop if we hit a
				//      parcel tag. This would ideally never be true though.
				if (parser.getName().equals("Parcel"))
					return;
				
				// If it's the end tag of the super tag we are parsing for, break the loop.
				if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(name))
					break;
				
				// Otherwise, it's either simple data or another sub tag. Either way, recurse.
				dynamicParse(parser, subMap);
			}
			
			if (contains(name, LIST_TAGS)) {
				ArrayList<Object> list = getOrCreateList(name, map);
				list.add(subMap);
			} else
				map.put(name, subMap);
			
			// Not so sure about this but it's definitely needed. Sorry.
			parser.next();
		} else {
			// We have a simple tag
			
			// First skip the opening tag.
			parser.next();
			// Next read the text from the tag and put it into the map.
			map.put(name, parser.getText());
			// Next skip past the closing tag.
			parser.next();
		}
	}
	
	/** This method skips all of the tags until we get to a tag with a name. This has the effect
	 * of skipping all the whitespace. */
	private boolean skipNullNames(XmlPullParser parser) throws IOException, XmlPullParserException {
		// We need a count to provide us with a timeout feature.
		int count = 0;
		while (parser.getName() == null) {
			// If we've already skipped 100 null names, we're probably in trouble.
			// Return false to indicate the problem.
			if (count++ > 100)
				return false;
			
			parser.next();
		}
		
		return true;
	}
	
	private ArrayList<Object> getOrCreateList(String listName, HashMap<String, Object> data) {
		for (String key : data.keySet()) {
			Object o = data.get(key);
			if (o instanceof ArrayList && key.equals(listName))
				return (ArrayList<Object>) o;
		}
		
		// ELSE
		ArrayList<Object> newList = new ArrayList<>();
		data.put(listName, newList);
		return newList;
	}
	
	/** This method will skip the parser until it reaches a start tag. */
	private boolean skipToStartTag(XmlPullParser parser) throws XmlPullParserException, IOException {
		// The count is used in case there is no more start tags, to prevent freezing.
		int count = 0;
		while (parser.getEventType() != XmlPullParser.START_TAG) {
			// Add provision for failure to prevent freezing.
			if (count++ > 100) {
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
	
	/** @return true if the String s is contained in the array. */
	private boolean contains(String s, String[] array) {
		for (String a : array)
			if (a.equals(s))
				return true;
		
		return false;
	}
	
}