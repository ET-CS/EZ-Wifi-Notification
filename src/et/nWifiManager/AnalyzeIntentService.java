package et.nWifiManager;

import et.nWifiManager.Analyzers.BrutalAnalyzer;
import et.nWifiManager.Analyzers.IntentAnalyzer;
import et.nWifiManager.Message.Message;
import et.nWifiManager.Notificators.SystemNotificator;
import et.nWifiManager.conState.ConnectionStatusEnum;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Analyize Intents recieved from android when connectivity changes
 * 
 * @author ET
 */
public class AnalyzeIntentService extends IntentService {

	// *********** analyzer settings ********************
	
	/**
	 * Anaylize Intent in reciever can be disabled to test directly the
	 * Bruteforce analyzer
	 */
	public final static boolean AnalyzeExtras = true;

	private static final String TAG = "AnalyzeIntentService";

	/**
	 * Constructor. set worker thread name.
	 */
	public AnalyzeIntentService() {
		super("WNMService");
	}

	// ------------- Everything starts here... ----------

	/**
	 * start worker thread
	 * 
	 * @param intent
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if (AnalyzeExtras) {
				// Analyze intent extras
				if (intent != null) {
					analyzeIntent(intent);
				} else {
					Log.i(TAG, "Intent empty. failover to brute");
					ShowNotificationBrutaly();
				}
			} else {
				Log.i(TAG, "Analyzing Intent extra disabled");
				ShowNotificationBrutaly();
			}
		} catch (Exception ex) {
			Log.e(TAG, "unknown error", ex);
		}
	}

	protected void analyzeIntent(Intent intent) {
		if (intent.getAction() == null) {
			// Intent is empty
			Log.i(TAG, "Intent Action empty. failover to brute.");
			ShowNotificationBrutaly();
		} else {
			// Check Intent Action (source)
			String action = intent.getAction();
			Log.d(TAG, "Handling intent " + action);
			// TODO Support API 1.7+ switch-case for strings instead of if/else
			if (action.equals(
					"android.intent.action.AIRPLANE_MODE")) {
				ShowNotificationBrutaly();
			} else {
				if (action.equals(
						"android.net.conn.CONNECTIVITY_CHANGE")) {
					if (intent.getExtras() != null) {
						Message m = null;
						try {
							m = (new IntentAnalyzer()).AnalyzeIntent(
									this, intent);
						} catch (Exception ex) {
							Log.e(TAG, "unknown fatal error analyzing intent. failover to brute", ex);
							ShowNotificationBrutaly();
						}
						if (m != null) {
							if (m.isError) {
								Log.e(TAG, "unknown error analyzing intent. failover to brute");
								ShowNotificationBrutaly();
							} else {
								// create notification
								if (!m.isSkip)
									(new SystemNotificator(this)).showNotification(m);
							}
						} else {
							Log.e(TAG, "Analyzer returned null. failover to brute");
							ShowNotificationBrutaly();
						}
					} else {
						Log.w(TAG, "Extra missing in Intent. failover to brute notification");
						// called from activity
						ShowNotificationBrutaly();
					}
				} else {
					if (intent.getAction().equals(
							"android.intent.action.BOOT_COMPLETED")) {
						ShowNotificationBrutaly();
					}
				}
			}
		}
	}
	
	// --------------------- Logic Part ------------------------

	/* BRUTAL PART (OLD) */
	
	/**
	 * WifiService will remember last status and will not notify if nothing
	 * changed.
	 */
	public final static boolean DontRepeatNotification = false; // TODO Remove

	/**
	 * last status of the connection
	 */
	private static ConnectionStatusEnum lastStatus = null; // TODO Remove this too
	
	/**
	 * old way = scan what is now and not analyze message extras
	 */
	private void ShowNotificationBrutaly() {
		Log.d(TAG, "Showing notification brutally");
		BrutalAnalyzer analyzer = new BrutalAnalyzer(this);
		//ConnectionStatusEnum status = ConnectionStatus();
		ConnectionStatusEnum status = analyzer.getConnectivityStatus();
		if (Constants.Debug)
			Log.d(getString(R.string.log_tag), "brutal status = " + status);
		boolean Notify = true;
		if (DontRepeatNotification) {
			// check last status and save current
			if (lastStatus != null)
				if (lastStatus == status) {
					Notify = false;
				}
			lastStatus = status;
		}
		if (Notify) {
			(new SystemNotificator(this)).showNotification(analyzer.GenerateMessage(status));
		} else {
			Log.i(TAG, "Skipping identical notification");
		}
	}


}
