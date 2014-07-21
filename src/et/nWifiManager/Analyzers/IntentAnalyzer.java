package et.nWifiManager.Analyzers;

import et.nWifiManager.Constants;
import et.nWifiManager.Hardware;
import et.nWifiManager.PreferencesActivity;
import et.nWifiManager.R;
import et.nWifiManager.Message.Message;
import et.nWifiManager.Message.Messages;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;
import android.util.Log;

public class IntentAnalyzer implements Analyzer {

	private static String TAG = "Analyzer";
	
	public boolean noConnection = true;
	public boolean isConnected = false;
	public State affectedState = null;
	public DetailedState detailedState = null;

	/**
	 * Analyze extras in reciever
	 * 
	 * @param context
	 * @param intent
	 * @return current status as ConnectionStatusEnum
	 */
	@SuppressWarnings("incomplete-switch")
	public Message AnalyzeIntent(Context context, Intent intent) {		
		try {			
			// get preferences
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(context);
			String BSSIDkey = context.getString(R.string.pre_ssid_key);
			String IPkey = context.getString(R.string.pre_ip_key);
			String onlyconnectedsoundkey = 
					context.getString(R.string.pre_notificationsoundonlywhenconnected_key);
			String onlyconnectedvibratekey = 
					context.getString(R.string.pre_notificationvibrateonlywhenconnected_key);
			boolean notificationSoundOnlyAtConnected = sp.getBoolean(onlyconnectedsoundkey,
					Constants.DefaultSettingNotificationSoundOnlyOnConnected);
			boolean notificationVibrateOnlyAtConnected = sp.getBoolean(onlyconnectedvibratekey,
					Constants.DefaultSettingNotificationSoundOnlyOnConnected);
			boolean isBSSIDEnabled = sp.getBoolean(BSSIDkey,
					Constants.DefaultSettingNotificationSSID);			
			boolean isIPEnabled = sp.getBoolean(IPkey,
					Constants.DefaultSettingNotificationIP);
			
			// the affected network
			NetworkInfo mNetworkInfo = (NetworkInfo) intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			// the new network
			NetworkInfo mOtherNetworkInfo = (NetworkInfo) intent
					.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

			String mReason = intent
					.getStringExtra(ConnectivityManager.EXTRA_REASON);
			// this is a connection that was the result of failing over from a
			// disconnected network
			boolean mIsFailover = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_IS_FAILOVER, false);
			// For a disconnect event, the boolean extra EXTRA_NO_CONNECTIVITY
			// is set to true
			// if there are no connected networks at all.
			noConnection = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			
			Log.d(TAG, "EXTRA_REASON Flag: "+mReason);
			Log.d(TAG, "EXTRA_IS_FAILOVER Flag: "+mIsFailover);
			Log.d(TAG, "EXTRA_NO_CONNECTIVITY Flag: "+noConnection);
			
			isConnected = !noConnection;
			if (mNetworkInfo!=null) {
				affectedState = mNetworkInfo.getState();
				detailedState = mNetworkInfo.getDetailedState();
				Log.d(TAG, "mNetworkInfo.getState(): "+affectedState);
				Log.d(TAG, "mNetworkInfo.getDetailedState(): "+detailedState);
			}
			
			String fkey = context.getString(R.string.pre_event_flight_key);
			boolean fdef = (context.getString(R.string.pre_event_flight_Default)=="true") ? true : false;
			boolean nFlight = sp.getBoolean(fkey, fdef);
			boolean flight = false;
			if (nFlight) flight = Hardware.isAirplaneModeOn(context);
				
			
			if (noConnection) {
				if (flight) return Messages.Flight();
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
								return new Message("Connected to mobile", "Wi-Fi connection lost", "Conneced to mobile");
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
									if (notificationSoundOnlyAtConnected) m.setSound(false);
									if (notificationVibrateOnlyAtConnected) m.setVibrate(false);									
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
			}
			else
			{
				// device connected
				switch (affectedState) {
					case CONNECTED:
						if (mNetworkInfo!=null){
							if (mNetworkInfo.isConnected()) {					
								switch (mNetworkInfo.getType()) {
									case ConnectivityManager.TYPE_MOBILE_DUN:
									case ConnectivityManager.TYPE_MOBILE_HIPRI:
									case ConnectivityManager.TYPE_MOBILE_MMS:
									case ConnectivityManager.TYPE_MOBILE_SUPL:									
									case ConnectivityManager.TYPE_MOBILE:										 
										return Messages.Mobile(PreferencesActivity.isShortTitle(context));
									case ConnectivityManager.TYPE_WIMAX:
										return Messages.WiMax(PreferencesActivity.isShortTitle(context));
									case ConnectivityManager.TYPE_WIFI:
										if (mIsFailover) {											
											return new Message(false, "","","");
										}
										else
										{
											// new message to be sent to the user
											return Messages.Wifi(Hardware.WifiName(context), 
													(isBSSIDEnabled) ? Hardware.getSSID(context) : "",
													(isIPEnabled) ? Hardware.getIP(context) : "", flight, PreferencesActivity.isShortTitle(context));	
											/* Message m = new Message();
											m.contentTitle = (isBSSIDEnabled) ? (flight) ? "Flight Mode" : ("Wi-Fi") + " (" + getSSID(context) + ")"											
													: (flight) ? "Wi-Fi (Flight mode)" : "Connected via Wi-Fi";
											m.contentText = (isIPEnabled) ? WifiName(context) + " (" + getIP(context)
													+ ")" : "Connected to " + WifiName(context);
											m.tickerText=m.contentText;
											m.State = ConnectionStatusEnum.Wifi;
											return m;*/
										}
								}
							}
						}
					case DISCONNECTED:
						//something disconnected in background
						return new Message(false, "","","");
				}				
			}
			
			// For a disconnect event, the boolean extra EXTRA_NO_CONNECTIVITY
			// is set to true
			// if there are no connected networks at all.
			boolean noConnectivity = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			State mState = (noConnectivity) ? State.DISCONNECTED
					: State.CONNECTED;			

			NetworkInfo.State affectedState = (mNetworkInfo!=null) ? mNetworkInfo.getState() : null;
			DetailedState detailedState = (mNetworkInfo!=null) ? mNetworkInfo.getDetailedState() : null;

			if (Constants.Debug) {
				String TAG = context.getString(R.string.log_tag);
				Log.d(TAG, "------ Reciever Called -----------");
				Log.d(TAG, "noConn=" + noConnectivity + ", AffectedState: "
						+ affectedState + ", DetailedState: " + detailedState);
				Log.d(TAG, "mNetworkInfo: " + mNetworkInfo);
				Log.d(TAG, "mOtherNetworkInfo: "
						+ (mOtherNetworkInfo == null ? "[none]"
								: mOtherNetworkInfo));
				Log.d(TAG, " mState=" + mState.toString() + ", mReason="
						+ mReason + ", mIsFailover=" + mIsFailover);
			}
		} catch (Exception ex) {
			return new Message(true, "", "", "");
		}
		return null;
	}

}
