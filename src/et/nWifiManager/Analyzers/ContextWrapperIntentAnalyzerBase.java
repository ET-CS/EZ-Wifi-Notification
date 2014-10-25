package et.nWifiManager.Analyzers;

import et.nWifiManager.Constants;
import et.nWifiManager.R;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;

public abstract class ContextWrapperIntentAnalyzerBase extends ContextWrapper implements IntentAnalyzerInterface {


	// Log TAG
	protected final static String TAG = "Analyzer";
	
	public ContextWrapperIntentAnalyzerBase(Context base) {
		super(base);
	}


	protected NotificationPreferences getPreferences() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		NotificationPreferences prefs = new NotificationPreferences();
		String BSSIDkey = getString(R.string.pre_ssid_key);
		String IPkey = getString(R.string.pre_ip_key);
		String onlyconnectedsoundkey = getString(R.string.pre_notificationsoundonlywhenconnected_key);
		String onlyconnectedvibratekey = getString(R.string.pre_notificationvibrateonlywhenconnected_key);

		prefs.notificationSoundOnlyAtConnected = sp.getBoolean(
				onlyconnectedsoundkey,
				Constants.DefaultSettingNotificationSoundOnlyOnConnected);
		prefs.notificationVibrateOnlyAtConnected = sp.getBoolean(
				onlyconnectedvibratekey,
				Constants.DefaultSettingNotificationSoundOnlyOnConnected);
		prefs.isBSSIDEnabled = sp.getBoolean(BSSIDkey,
				Constants.DefaultSettingNotificationSSID);
		prefs.isIPEnabled = sp.getBoolean(IPkey,
				Constants.DefaultSettingNotificationIP);

		String fkey = getString(R.string.pre_event_flight_key);
		boolean fdef = (getString(R.bool.pre_event_flight_Default) == "true") ? true
				: false;
		prefs.nFlight = sp.getBoolean(fkey, fdef);

		return prefs;
	}

	class NotificationPreferences {
		/*
		 * User wants to see flight mode
		 */
		public boolean nFlight;
		/*
		 * User wants to see IP
		 */
		public boolean isIPEnabled;
		/*
		 * User want to see SSIED
		 */
		public boolean isBSSIDEnabled;
		/*
		 * User want Vibration only on connected
		 */
		public boolean notificationVibrateOnlyAtConnected;
		/*
		 * User Want to hear sound only on connected
		 */
		public boolean notificationSoundOnlyAtConnected;
	}


	/**
	 * For a disconnect event, the boolean extra EXTRA_NO_CONNECTIVITY is set to
	 * true if there are no connected networks at all.
	 * 
	 * @param intent
	 * @return
	 */
	protected boolean getNoConnectivity(Intent intent) {
		return intent.getBooleanExtra(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
	}

	protected String getReason(Intent intent) {
		return intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
	}

	protected NetworkInfo getOtherNetworkInfo(Intent intent) {
		return (NetworkInfo) intent
				.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
	}

	/**
	 * this is a connection that was the result of failing over from a
	 * disconnected network
	 * 
	 * @param intent
	 * @return
	 */
	protected boolean isFailover(Intent intent) {
		return intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER,
				false);
	}

	@SuppressWarnings("deprecation")
	protected NetworkInfo getNetworkInfoFromIntent(Intent intent) {
		// the affected network
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// TODO Fix deprecation
			return (NetworkInfo) intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		} else {
			return (NetworkInfo) intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
		}
	}

	
}
