package et.nWifiManager.Analyzers;

import et.nWifiManager.Constants;
import et.nWifiManager.Hardware;
import et.nWifiManager.PreferencesActivity;
import et.nWifiManager.Message.Message;
import et.nWifiManager.Message.Messages;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.util.Log;

// TODO remove all SupressWarnings and fix

/**
 * Default IntentAnalyzer for EZ Wifi. Analyze Intent received by
 * AnalyzeService.
 * 
 * @author ET-CS
 */
public class IntentAnalyzer extends ContextWrapperIntentAnalyzerBase {

	public IntentAnalyzer(Context base) {
		super(base);
	}

	/**
	 * Analyze extras in reciever
	 * 
	 * @param intent
	 */
	@Override
	public Message AnalyzeIntent(Intent intent) {
		try {
			// get preferences
			NotificationPreferences prefs = getPreferences();
			return analyze(prefs, intent);
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
			return new Message(true, "", "", "");
		}
	}

	@SuppressWarnings({ "incomplete-switch" })
	private Message analyze(NotificationPreferences prefs, Intent intent) {
		// extract intent
		NetworkInfo mNetworkInfo = getNetworkInfoFromIntent(intent);
		State affectedState = null;
		DetailedState detailedState = null;
		if (mNetworkInfo != null) {
			affectedState = mNetworkInfo.getState();
			detailedState = mNetworkInfo.getDetailedState();
		}
		NetworkInfo mOtherNetworkInfo = getOtherNetworkInfo(intent);
		String mReason = getReason(intent);
		boolean mIsFailover = isFailover(intent);
		boolean noConnectivity = getNoConnectivity(intent);
		boolean flight = (prefs.nFlight) ? Hardware.isAirplaneModeOn(this) : false;

		// Log
		log(noConnectivity, affectedState, detailedState, mNetworkInfo, mOtherNetworkInfo, mReason, mIsFailover);

		// Analyze
		if (noConnectivity) {
			if (flight)
				return Messages.Flight();
			// device disconnecting...
			if (mNetworkInfo != null) {
				switch (affectedState) {
				case CONNECTING:
					switch (detailedState) {
					case OBTAINING_IPADDR:
						Message m = new Message();
						m.setTickerText("Connecting Wi-Fi...");
						m.setContentText("Connecting to mobile");
						m.setContentTitle("Connecting Wi-Fi...");
						m.setVibrate(false);
						m.setSound(false);
						return m;
					default:
						return new Message("Connected to mobile",
								"Wi-Fi connection lost",
								"Conneced to mobile");
					}

				}
				if (mNetworkInfo.getState() == State.DISCONNECTED) {
					switch (mNetworkInfo.getType()) {
					case ConnectivityManager.TYPE_WIFI:
						Message m = new Message();
						m.Feed("Wi-Fi disconnecting");
						// wifi disconnected
						if (mOtherNetworkInfo != null) {
							switch (mOtherNetworkInfo.getType()) {
							case ConnectivityManager.TYPE_MOBILE:
								m.setContentText("Switching to mobile...");
								m.setTickerText(m.getContentText());
								if (prefs.notificationSoundOnlyAtConnected)
									m.setSound(false);
								if (prefs.notificationVibrateOnlyAtConnected)
									m.setVibrate(false);
							}
						} else {
							return Messages.NoConnectivity();
						}
						return m;
					case ConnectivityManager.TYPE_MOBILE:
					case ConnectivityManager.TYPE_MOBILE_DUN:
					case ConnectivityManager.TYPE_MOBILE_HIPRI:
					case ConnectivityManager.TYPE_MOBILE_MMS:
					case ConnectivityManager.TYPE_MOBILE_SUPL:
						// mobile disconnected
						return new Message(false, "", "", "");
					case ConnectivityManager.TYPE_WIMAX:
						CharSequence c = "WiMAX Disconnecting";
						return new Message(c, c, c);
					}
				}
			} else {
				return Messages.NoConnectivity();
			}
		} else {
			// device connected
			switch (affectedState) {
			case CONNECTED:
				if (mNetworkInfo != null) {
					if (mNetworkInfo.isConnected()) {
						switch (mNetworkInfo.getType()) {
						case ConnectivityManager.TYPE_MOBILE_DUN:
						case ConnectivityManager.TYPE_MOBILE_HIPRI:
						case ConnectivityManager.TYPE_MOBILE_MMS:
						case ConnectivityManager.TYPE_MOBILE_SUPL:
						case ConnectivityManager.TYPE_MOBILE:
							return Messages.Mobile(PreferencesActivity
									.isShortTitle(this));
						case ConnectivityManager.TYPE_WIMAX:
							return Messages.WiMax(PreferencesActivity
									.isShortTitle(this));
						case ConnectivityManager.TYPE_WIFI:
							if (mIsFailover) {
								return new Message(false, "", "", "");
							} else {
								// new message to be sent to the user
								return Messages.Wifi(
										Hardware.WifiName(this),
										(prefs.isBSSIDEnabled) ? Hardware
												.getSSID(this) : "",
										(prefs.isIPEnabled) ? Hardware
												.getIP(this) : "", flight,
										PreferencesActivity
												.isShortTitle(this));
							}
						}
					}
				}
			case DISCONNECTED:
				// something disconnected in background
				return new Message(false, "", "", "");
			}
		}
		return null;
	}

	private void log(boolean noConnectivity, State affectedState,
			DetailedState detailedState, NetworkInfo mNetworkInfo,
			NetworkInfo mOtherNetworkInfo, String mReason, boolean mIsFailover) {

		// Log
		if (Constants.Debug) {
			//Log.d(TAG, "EXTRA_REASON Flag: " + mReason);
			//Log.d(TAG, "EXTRA_IS_FAILOVER Flag: " + mIsFailover);
			//Log.d(TAG, "EXTRA_NO_CONNECTIVITY Flag: " + noConnectivity);
			// Log.d(TAG, "mNetworkInfo.getState(): "+affectedState);
			// Log.d(TAG,
			// "mNetworkInfo.getDetailedState(): "+detailedState);
			Log.d(TAG, "------ Reciever Called -----------");
			Log.d(TAG, "noConn=" + noConnectivity + ", AffectedState: "
					+ affectedState + ", DetailedState: " + detailedState);
			Log.d(TAG, "mNetworkInfo: " + mNetworkInfo);
			Log.d(TAG, "mOtherNetworkInfo: "
					+ (mOtherNetworkInfo == null ? "[none]"
							: mOtherNetworkInfo));
			State mState = (noConnectivity) ? State.DISCONNECTED
					: State.CONNECTED;
			Log.d(TAG, " mState=" + mState.toString() + ", mReason="
					+ mReason + ", mIsFailover=" + mIsFailover);
		}		
	}

}
