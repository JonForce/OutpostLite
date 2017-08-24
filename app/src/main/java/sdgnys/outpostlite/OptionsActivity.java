package sdgnys.outpostlite;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.File;

import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;

import static sdgnys.outpostlite.Logger.log;

public class OptionsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		
		findViewById(R.id.deleteInternalButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new StorageAccess(OptionsActivity.this).deleteLocalFiles();
				logGUI("Deleted all internal files.");
			}
		});
		
		findViewById(R.id.listInternalButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				logGUI("Printing out files into logcat...");
				listFiles(getFilesDir());
			}
		});
		
		findViewById(R.id.deleteExportButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StorageAccess storage = new StorageAccess(OptionsActivity.this);
				for (File f : storage.exportDirectory.listFiles())
					f.delete();
				storage.exportDirectory.delete();
				logGUI("Deleted export directory.");
			}
		});
		
		findViewById(R.id.listExportButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listFiles(new StorageAccess(OptionsActivity.this).exportDirectory);
			}
		});
	}
	
	private void listFiles(File directory) {
		for (File f : directory.listFiles())
			if (f.isDirectory()) listFiles(f);
			else log(f.getPath());
	}
	
	private void logGUI(String text) {
		EditText log = ((EditText)findViewById(R.id.log));
		log.setText(log.getText() + "\n" + text);
	}
	
}
