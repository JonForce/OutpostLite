package sdgnys.outpostlite;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

import sdgnys.outpostlite.sdgnys.outpostlite.access.StorageAccess;
import sdgnys.outpostlite.search.SearchActivity;

import static sdgnys.outpostlite.Logger.*;


/** This is the launching point of the program. It is where you can perform a search, or go to
 * the import or export activities.
 */
public class MainActivity extends AppCompatActivity {
	
	/** How often (in ms) should we check for a package? */
	private static final int CHECK_FOR_PACKAGE_FREQUENCY = 700;
	
	private boolean displayedPackageNotice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	    // Hide the keyboard.
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	    
	    // When they press the search button, search.
        final ImageButton button = (ImageButton) findViewById(R.id.navigate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search();
            }
        });
	    
	    // When they press the import / export buttons, import / export.
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
		
	    // When the options button is pressed, launch the options activity.
	    findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
			    startActivity(intent);
		    }
	    });
	
	    findViewById(R.id.mapsButton).setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    Uri gmmIntentUri = Uri.parse("geo:0,0");
			    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
			    mapIntent.setPackage("com.google.android.apps.maps");
			    startActivity(mapIntent);
		    }
	    });
		
	    // Request permission to use the camera (in case we don't already have it).
	    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
		
	    // We need to set up a reoccuring task that will check for an incoming package.
	    new Timer().scheduleAtFixedRate(new TimerTask() {
		    @Override
		    public void run() {
			    // Gain access to the storage of the device.
			    StorageAccess access = new StorageAccess(MainActivity.this);
			    // If we have an incoming package,
			    if (access.getIncomingPackage() != null)
				    runOnUiThread(new Runnable() {
					    @Override
					    public void run() {
						    if (!displayedPackageNotice) {
							    Toast.makeText(MainActivity.this,
									    "Package is ready to import!",
									    Toast.LENGTH_LONG).show();
							    displayedPackageNotice = true;
						    }
					    }
				    });
		    }
	    }, /** Delay */ 0, CHECK_FOR_PACKAGE_FREQUENCY);
	    
	    log("Launched OutpostLite");
    }
    
    /** This method launches the export activity. */
    private void export() {
	    Intent intent = new Intent(this, ExportActivity.class);
	    startActivity(intent);
    }
	
	/** This method launches the import activity. */
    private void importPackage() {
	    Intent intent = new Intent(this, ImportActivity.class);
	    startActivity(intent);
	}
	
	/** This method pulls all of the search data from the screen and then launches the search
	 * activity with that data. */
    private void search() {
	    // Get all of the search parameters from the screen.
	    String
			    municipality = getInputText(R.id.municipality),
			    SBL = getInputText(R.id.SBL),
			    PRINT_KEY = getInputText(R.id.PRINT_KEY),
	            streetNumber = getInputText(R.id.streetNumber),
	            streetName = getInputText(R.id.street);
		
	    // Start the search activity with all of the parameters in the intent.
	    Intent intent = new Intent(this, SearchActivity.class);
	    intent.putExtra("municipality", municipality);
	    intent.putExtra("SBL", SBL);
	    intent.putExtra("PRINT_KEY", PRINT_KEY);
	    intent.putExtra("streetNumber", streetNumber);
	    intent.putExtra("streetName", streetName);
	    startActivity(intent);
    }
    
    /** @return the text of the input field on the screen with the specified id. */
    private String getInputText(int id) {
	    return ((EditText) findViewById(id)).getText().toString();
    }
}