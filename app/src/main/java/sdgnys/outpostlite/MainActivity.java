package sdgnys.outpostlite;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import sdgnys.outpostlite.sdgnys.outpostlite.access.ParcelXmlParser;
import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;
import sdgnys.outpostlite.sdgnys.outpostlite.access.database.Database;
import sdgnys.outpostlite.search.SearchActivity;

import static sdgnys.outpostlite.Logger.*;

public class MainActivity extends AppCompatActivity {
	
	private static final int CHECK_FOR_PACKAGE_FREQUENCY = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    
        final ImageButton button = (ImageButton) findViewById(R.id.navigate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search();
            }
        });
	    
	    findViewById(R.id.importButton).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    importPackage();
		    }
	    });
	    findViewById(R.id.exportButton).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
				export();
		    }
	    });
	
	    findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
			    startActivity(intent);
		    }
	    });
		
	    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
	
//	    new Timer().scheduleAtFixedRate(new TimerTask() {
//		    @Override
//		    public void run() {
//
//		    }
//	    }, /** Delay */ 0, CHECK_FOR_PACKAGE_FREQUENCY);
	    
	    log("Launched OutpostLite");
    }
    
    private void export() {
	    Intent intent = new Intent(this, ExportActivity.class);
	    startActivity(intent);
    }
	
    private void importPackage() {
	    Intent intent = new Intent(this, ImportActivity.class);
	    startActivity(intent);
	}
	
    private void search() {
	    String
			    municipality = getInputText(R.id.municipality),
			    taxMapID = getInputText(R.id.taxMapID),
	            streetNumber = getInputText(R.id.streetNumber),
	            streetName = getInputText(R.id.street);
		
	    Intent intent = new Intent(this, SearchActivity.class);
	    intent.putExtra("municipality", municipality);
	    intent.putExtra("taxMapID", taxMapID);
	    intent.putExtra("streetNumber", streetNumber);
	    intent.putExtra("streetName", streetName);
	    startActivity(intent);
    }
    
    private String getInputText(int id) {
	    return ((EditText) findViewById(id)).getText().toString();
    }
}