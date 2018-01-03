package sdgnys.outpostlite;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/** The activity that shows the user all of the sale information. */
public class SaleActivity extends ParcelImageActivity {
	
	public SaleActivity() {
		super(R.layout.activity_sale, 900, 600, 740, 493);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Update the UI with data about the Parcel we received.
		super.updateUI(parcelData);
	}
	
}
