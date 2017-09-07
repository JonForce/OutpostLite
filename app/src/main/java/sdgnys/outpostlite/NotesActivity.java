package sdgnys.outpostlite;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import sdgnys.outpostlite.sdgnys.outpostlite.access.NotesAccess;

import static sdgnys.outpostlite.Logger.log;

public class NotesActivity extends AppCompatActivity {
	
	private String SWIS, PRINT_KEY, PARCEL_ID;
	private NotesAccess notes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notes);
		
		// Pull in the input parameters that will be used to identify the parcel.
		SWIS = getIntent().getStringExtra("SWIS");
		PRINT_KEY = getIntent().getStringExtra("PRINT_KEY");
		PARCEL_ID = getIntent().getStringExtra("PARCEL_ID");
		String address = getIntent().getStringExtra("address");
		
		notes = new NotesAccess(this);
		
		setText(R.id.SWIS, SWIS);
		setText(R.id.PRINT_KEY, PRINT_KEY);
		setText(R.id.PARCEL_ID, PARCEL_ID);
		setText(R.id.address, address);
		
		load();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		save();
	}
	
	private void save() {
		notes.writeNotes(SWIS, PRINT_KEY, PARCEL_ID, getText());
	}
	
	private void load() {
		setText(R.id.editArea, notes.getNotes(SWIS, PRINT_KEY, PARCEL_ID));
	}
	
	private String getText() {
		return ((EditText) findViewById(R.id.editArea)).getText().toString();
	}
	
	private void setText(int id, String text) {
		((TextView) findViewById(id)).setText(text);
	}
}
