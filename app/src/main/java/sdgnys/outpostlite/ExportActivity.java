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

public class ExportActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);
		
		findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		
		findViewById(R.id.exportButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				export();
			}
		});
	}
	
	private void onFinishExport() {
		findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
		findViewById(R.id.checkmark).setVisibility(View.VISIBLE);
		
		setStatus("Successfully Exported.");
	}
	
	private void export() {
		findViewById(R.id.exportButton).setVisibility(View.INVISIBLE);
		findViewById(R.id.status).setVisibility(View.VISIBLE);
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		
		setStatus("Exporting...");
		StorageAccess storage = new StorageAccess(this);
		String[] fileNames = new String[storage.exportDirectory.listFiles().length];
		for (int i = 0; i < storage.exportDirectory.listFiles().length; i ++)
			fileNames[i] = storage.exportDirectory.listFiles()[i].getPath();
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
	
	
	private void copyFile(String inputPath, String inputFile, String outputPath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			
			//create output directory if it doesn't exist
			File dir = new File (outputPath);
			if (!dir.exists())
				dir.mkdirs();
			
			in = new FileInputStream(inputPath + inputFile);
			out = new FileOutputStream(outputPath + "/" + inputFile);
			
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
				out.write(buffer, 0, read);
			
			in.close();
			
			// write the output file (You have now copied the file)
			out.flush();
			out.close();
			
		}  catch (FileNotFoundException fnfe1) {
			logE(fnfe1.getMessage());
		}
		catch (Exception e) {
			logE(e.getMessage());
		}
	}
	
	private void setStatus(String status) {
		((TextView) findViewById(R.id.status)).setText(status);
	}
	
	private void setPercentLoaded(int progress) {
		((ProgressBar) findViewById(R.id.progressBar)).setProgress(progress);
	}
}
