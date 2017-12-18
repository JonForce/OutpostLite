package sdgnys.outpostlite;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;

import static sdgnys.outpostlite.Logger.logE;
import static sdgnys.outpostlite.Logger.logI;

/** This activity is responsible for creating the outgoing package that will go to the PC. */
public class ExportActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);
		
		// First hide the progress bar. We don't need it yet.
		findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		
		// When the export button is pressed, export.
		findViewById(R.id.exportButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				export();
			}
		});
	}
	
	/** This method contains all of the things that need to happen when we're all done exporting. */
	private void onFinishExport() {
		// Hide the progress bar.
		findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		// Show the checkmark because we are all done :)
		findViewById(R.id.checkmark).setVisibility(View.VISIBLE);
		
		setStatus("Successfully Exported.");
	}
	
	/** This method begins the export process synchronously. */
	private void export() {
		// Hide the export button, they're already exporting.
		findViewById(R.id.exportButton).setVisibility(View.INVISIBLE);
		// Show the status and progress bar.
		findViewById(R.id.status).setVisibility(View.VISIBLE);
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		
		setStatus("Exporting...");
		// Gain access to the storage where we'll put the package.
		StorageAccess storage = new StorageAccess(this);
		// Get an array of all of the files we want to zip up into an export package.
		String[] fileNames = new String[storage.exportDirectory.listFiles().length];
		for (int i = 0; i < storage.exportDirectory.listFiles().length; i ++)
			fileNames[i] = storage.exportDirectory.listFiles()[i].getPath();
		// Zip all the files into the export package.
		zip(fileNames, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/TabletToPC.zip");
		
		onFinishExport();
	}
	
	/** This method was cruelly and blatantly stolen from Stack Overflow.
	 * It didn't even have that many upvotes.
	 * https://stackoverflow.com/questions/25562262/how-to-compress-files-into-zip-folder-in-android */
	public void zip(String[] _files, String zipFileName) {
		try {
			final int BUFFER = 512;
			
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			byte data[] = new byte[BUFFER];
			
			for (int i = 0; i < _files.length; i++) {
				logI("Adding: " + _files[i]);
				FileInputStream fi = new FileInputStream(_files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				
				ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
				out.putNextEntry(entry);
				int count;
				
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** This method sets the status text on the screen so the user knows what's up. */
	private void setStatus(String status) {
		((TextView) findViewById(R.id.status)).setText(status);
	}
	
	@Deprecated
	private void setPercentLoaded(int progress) {
		((ProgressBar) findViewById(R.id.progressBar)).setProgress(progress);
	}
}
