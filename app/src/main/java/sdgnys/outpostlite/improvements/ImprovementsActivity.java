package sdgnys.outpostlite.improvements;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import sdgnys.outpostlite.R;

public class ImprovementsActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_improvements);
		
		// Get the improvement data from the intent.
		ArrayList<HashMap<String, Object>> improvements =
				(ArrayList<HashMap<String, Object>>) getIntent().getSerializableExtra("improvements");
		
		// Use the improvements data.
		getListView().setAdapter(new ImprovementsAdapter(this, R.layout.improvement, improvements));
	}
	
	private ListView getListView() { return (ListView) findViewById(R.id.improvementsView); }
	
}
