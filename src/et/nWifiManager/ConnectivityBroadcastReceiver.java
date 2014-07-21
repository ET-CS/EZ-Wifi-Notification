package et.nWifiManager;

// ConnectivityBroadcastReceiver.java - Global reciever for all intent-filters
// Reciever for handling: 
// 		android.net.conn.CONNECTIVITY_CHANGE, 
// 		android.intent.action.AIRPLANE_MODE
// 		android.intent.action.BOOT_COMPLETED
// then fires up WifiService with Intent

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/** Receiver for CONNECTIVITY_CHANGE, AIRPLANE_MODE, BOOT_COMPLETED
 * that fires up WifiService on start 
 * Everything runs inside a try/catch block to prevent non catched exceptions.
 *   
 * @author ET
 */
public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

	/**
	 * onReceive must be very quick and not block, so it just fires up AnalyzeIntentService
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			// Create Intent to AnalyzeIntentService
			final Intent intentService = new Intent(context, AnalyzeService.class);
			// Set action to recieved intent action.
			intentService.setAction(intent.getAction());
			// Pass Intent as Extra to AnalyzeIntentService
			if (AnalyzeService.AnalyzeExtras) intentService.putExtras(intent);				
			try {
				// Start service (AnalyzeIntentService)
				ComponentName component = context.startService(intentService);
				if (component != null) {
					// the service is being started or is already running,
					// the ComponentName of the actual service that was started
					// is returned;
					// TODO Figure out what to do with that
				}
			} catch (SecurityException ex) {
				// TODO Check why it happens and under what conditions
			}
		} catch (Exception ex) {			
			// TODO Notify me of the exception
		}
	}
	
}