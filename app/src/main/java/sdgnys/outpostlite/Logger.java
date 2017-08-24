package sdgnys.outpostlite;

import android.util.Log;

/**
 * Created by jforce on 8/9/2017.
 * A badass utility for logging all the dopest info.
 */
public class Logger {
	
	private static final String TAG = "OutpostLite";
	
	public static void log(String message) {
		Log.d(TAG, message);
	}
	
	public static void logE(String message) {
		Log.e(TAG, message);
	}
	
	public static void logI(String message) {
		Log.d(TAG, "~~~~~~~~~~ " + message);
	}
}
