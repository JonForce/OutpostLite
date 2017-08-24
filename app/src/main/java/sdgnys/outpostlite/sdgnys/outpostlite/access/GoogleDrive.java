package sdgnys.outpostlite.sdgnys.outpostlite.access;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;

/**
 * Created by jforce on 7/27/2017.
 *
 * This class is a tool that wraps up the disgusting awful garbage that is the
 * Google Drive android API into a friendly, intelligent system.
 */
public class GoogleDrive {
	
	private GoogleApiClient driveClient;
	
	/** Create a new tool for accessing Google Drive. */
	public GoogleDrive(GoogleApiClient driveClient) {
		this.driveClient = driveClient;
	}
	
	/** This method is used for getting all the data from a file on google drive.
	 * The data will be returned in String format in the callback method that is passed as a parameter.
	 * @param title The title of the file you want the data for.
	 * @param callback The callback function that will receive the File's data.
	 */
	public void getFileData(final String title, final Callback<String> callback) {
		// Make a request to read the content of the file.
		getDriveContents(title, DriveFile.MODE_READ_ONLY, new Callback<DriveContents>() {
			@Override
			public void callback(DriveContents data) {
				// Once we have the drive's contents, convert to a String,
				String fileData = convertStreamToString(data.getInputStream());
				// And send it to the callback.
				callback.callback(fileData);
			}
		});
	}
	
	/** This method will write a String to the given file on Google Drive.
	 * The content will be appended at the bottom of the file.
	 * @param title The title of the file that will be written to.
	 * @param content The content that will be appended at the bottom of the file.
	 */
	public void writeToFile(String title, String content) {
		writeToFile(title, new String(content).getBytes());
	}
	
	/** This method will write data to the given file on Google Drive.
	 * The data will be appended at the bottom of the file.
	 * @param title The title of the file that will be written to.
	 * @param content The data that will be appended at the bottom of the file.
	 */
	public void writeToFile(final String title, final byte[] content) {
		// Make a request to get a stream for writing to the file,
		getDriveContents(title, DriveFile.MODE_WRITE_ONLY, new Callback<DriveContents>() {
			@Override
			public void callback(DriveContents contents) {
				// Write the desired data to the end of the file.
				try {
					contents.getOutputStream().write(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Upload the changes to the drive client.
				// null because there are no metadata changes.
				contents.commit(driveClient, null);
			}
		});
	}
	
	/** This method will create an empty file in the Google Drive.
	 * @param title The file's name.
	 */
	public void createEmptyFile(String title) {
		// Define the new File's metadata.
		MetadataChangeSet changeSet =
				new MetadataChangeSet.Builder()
						.setTitle(title)
						.setMimeType("text/plain")
						.build();
		
		// Get a reference to Google Drive's root folder,
		Drive.DriveApi.getRootFolder(driveClient)
				// Create the new file.
				.createFile(driveClient, changeSet, null /* DriveContents */)
				.setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
					@Override
					public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
						if (!driveFileResult.getStatus().isSuccess()) {
							throw new RuntimeException("Couldn't create the file.");
						}
					}
				});
	}
	
	/** This method will list all of the files accessible in Google Drive's
	 * root folder. Because of the nature of Google Drive's API, there is no guarantee
	 * about when the files will be listed.
	 */
	public void listAllFilesInDrive() {
		// Get the drive's root folder, request it to list its children, and wait for the result.
		Drive.DriveApi.getRootFolder(driveClient).listChildren(driveClient).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(DriveApi.MetadataBufferResult result) {
				Log.d("OutpostLite", "\nListing all files in Google Drive");
				// Iterate over the matching Metadata instances in mdResultSet
				for (Metadata r : result.getMetadataBuffer()) {
					Log.d("OutpostLite", " ------------" + r.getTitle());
				}
			}
		});
	}
	
	/** This method can be used to retrieve the DriveContents of a particular file.
	 * The DriveContents can be used to read and write to a file on Google Drive.
	 * @param title The title of the file you want the DriveContents of.
	 * @param mode The access mode you'd like to use. Ex : DriveFile.MODE_WRITE_ONLY
	 * @param callback The callback function that will be sent the DriveContents.
	 */
	public void getDriveContents(final String title, final int mode, final Callback<DriveContents> callback) {
		// Make a request for the DriveFile.
		getFile(title, new Callback<DriveFile>() {
			@Override
			public void callback(DriveFile file) {
				if (file == null)
					throw new RuntimeException("Couldn't get file " + title);
				
				// Open the DriveFile so we can get at its meat.
				// If its on Google's servers, it'll be downloaded automatically.
				file.open(driveClient, mode, new DriveFile.DownloadProgressListener() {
					@Override
					public void onProgress(long downloaded, long expected) {
						// Log the download progress.
						Log.d("OutpostLite", "Downloading file, " + downloaded +":"+ expected);
					}
				}).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
					@Override
					public void onResult(@NonNull DriveApi.DriveContentsResult result) {
						if (result.getStatus().isSuccess()) {
							DriveContents contents = result.getDriveContents();
							
							callback.callback(contents);
						} else {
							throw new RuntimeException("Error getting file content.");
						}
					}
				});
			}
		});
	}
	
	/** Gets a DriveFile by name from Google Drive. The DriveFile will be sent to the callback
	 * function.
	 * @param title The title of the file to get the DriveFile reference for.
	 * @param callback The callback function to send the DriveFile to.
	 */
	public void getFile(final String title, final Callback<DriveFile> callback) {
		// Build a search query that will look for the DriveFile.
		Query query = new Query.Builder()
				.addFilter(Filters.eq(SearchableField.TITLE, title))
				.build();
		
		// Use the query to search for the DriveFile.
		Drive.DriveApi.query(driveClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
			@Override
			public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
				// For every file returned by the query,
				for (Metadata m : metadataBufferResult.getMetadataBuffer()) {
					// If the file matches our target file's name,
					if (m.getTitle().equals(title)) {
						// Send it to the callback function.
						DriveFile file = Drive.DriveApi.getFile(driveClient, m.getDriveId());
						callback.callback(file);
						return;
					}
				}
				throw new RuntimeException("Couldn't find the DriveFile " + title);
			}
		});
	}
	
	/** This method converts an inputstream to a String.
	 * Found Here : https://stackoverflow.com/questions/8652804/how-to-convert-string-to-byte-in-java  */
	private String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
