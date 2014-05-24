package et.nWifiManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/***
 * Launcer activity - help page and link to preferencesActivity. 
 * launced first after installation. 
 * @author ET
 */
public class LauncerActivity extends Activity implements OnClickListener {
	
	@SuppressWarnings("unused")
	private static final String TAG = "LauncerActivity";
	
	/**
	 * Initialize launcer
	 * Setup UI / Start Service / register buttons handlers 
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init UI xml
		try {
			setContentView(R.layout.launcer);
		} catch (Exception e) {
			// Never should have happen...
			String msg = "could not load GUI.";
			Toast.makeText(this, "EZ-WIFI: Sorry. " + msg, Toast.LENGTH_SHORT).show();
			this.finish();	// Close Activity.
		}
		
		/**
		 * runOnce the Listener Service. Normally, should be loaded on boot, 
		 * and loads here for other cases (after installation).
		 * The service will only be loaded once anyway.
		 */
		((MyApplication) getApplication()).runOnce();

		// Register buttons to send onClick events to Activity.
		RegisterButtonsToSelf();

	}

	/**
	 * Helper: Register All Buttons Using registerButton() to Self.
	 */
	private void RegisterButtonsToSelf() {
		try {
			registerButton(R.id.Preferences);	// register 'Preferences' button
		} catch (Exception e) {
			// Never should have happen...
			String msg = "registering Preferences button failed.";
			Toast.makeText(this, "EZ-WIFI: Sorry. " + msg, Toast.LENGTH_SHORT).show();
		}
		
		try {
			registerButton(R.id.Close);			// register 'Close' button
		} catch (Exception e) {
			// Never should have happen...
			String msg = "registering Close button failed.";
			Toast.makeText(this, "EZ-WIFI: Sorry. " + msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * private helper to Register onClick with this class onClick(View v)
	 */
	private void registerButton(int id) {
		((Button) findViewById(id)).setOnClickListener(this);
	}
	
	/**
	 * Buttons onClick handler:
	 * 'Preferences' button onClick handler - Loads preferences page.
	 * 'Close' button onClick handler - close the LauncherActivity
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.Preferences:
			// load preferenced
			Intent myIntent = new Intent(getBaseContext(),
					PreferencesActivity.class);
			try {
				startActivityForResult(myIntent, 0);
			} catch (Exception ex) {
				Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.Close:
			// close launcer
			this.finish();
			break;
		default:
		}
	}

}
