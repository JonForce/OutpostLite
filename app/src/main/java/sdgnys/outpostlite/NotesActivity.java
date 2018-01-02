package sdgnys.outpostlite;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
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
		// Hide keyboard. Without this, the keyboard pops up automatically.
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		// Pull in the input parameters that will be used to identify the parcel.
		SWIS = getIntent().getStringExtra("SWIS");
		PRINT_KEY = getIntent().getStringExtra("PRINT_KEY");
		PARCEL_ID = getIntent().getStringExtra("PARCEL_ID");
		String address = getIntent().getStringExtra("address");
		
		// NotesAccess will provide an interface between this activity and the notes files.
		notes = new NotesAccess(this);
		
		setText(R.id.SWIS, SWIS);
		setText(R.id.PRINT_KEY, PRINT_KEY);
		setText(R.id.PARCEL_ID, PARCEL_ID);
		setText(R.id.address, address);
		
		// Load the current notes for this parcel from the notes file.
		load();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// Whenever we pause, save.
		save();
	}
	
	/** This method saves the changes we have made to the notes in the notes file. */
	private void save() {
		notes.writeNotes(SWIS, PRINT_KEY, PARCEL_ID, getText());
	}
	
	/** This method loads all the text from the notes file onto the screen. */
	private void load() {
		setText(R.id.editArea, notes.getNotes(SWIS, PRINT_KEY, PARCEL_ID));
	}
	
	/** This method gets the text from the screen. */
	private String getText() {
		return ((EditText) findViewById(R.id.editArea)).getText().toString();
	}
	
	/** This method sets the text on the screen. */
	private void setText(int id, String text) {
		((TextView) findViewById(id)).setText(text);
	}
}
