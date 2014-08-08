package et.nWifiManager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MyApplication extends Application {
	
	private ConnectivityManager cMgr;
	private SharedPreferences prefs;

	public SharedPreferences getPrefs() {
		return this.prefs;
	}

	//
	// lifecycle
	//
    /* (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
	@Override
	public void onCreate() {
		super.onCreate();
		this.cMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onTerminate() {
		// not guaranteed to be called
		super.onTerminate();
	}

	public boolean connectionPresent() {
		NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		if ((netInfo != null) && (netInfo.getState() != null)) {
			return netInfo.getState().equals(State.CONNECTED);
		}
		return false;
	}

	BroadcastReceiver receiver;

	/** Register for broadcast reciever */
	public void registerReciever() {
		// Register Broadcast Receiver
		if (receiver == null) {
			receiver = new ConnectivityBroadcastReceiver();
			registerReceiver(receiver, new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION));
			// registerReceiver(receiver, new IntentFilter(
			// WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			Toast.makeText(this, "Reciever listens", Toast.LENGTH_SHORT).show();
		} else {
			unregisterReceiver(receiver);
			receiver = null;
			Toast.makeText(this, "Reciever stopped", Toast.LENGTH_LONG).show();
		}
	}

	public boolean runOnce() {
		try {
			startService(new Intent(this, AnalyzeService.class));
		} catch (Exception ex) {
			Toast.makeText(this, "Reciever Error: error in runOnce",
					Toast.LENGTH_SHORT).show();
			// TODO Notify me of the exception
			return false;
		}
		return true;
	}
	
}
