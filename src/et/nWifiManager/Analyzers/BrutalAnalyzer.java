package et.nWifiManager.Analyzers;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import et.nWifiManager.Hardware;
import et.nWifiManager.R;
import et.nWifiManager.R.string;
import et.nWifiManager.conState.ConnectionStatusEnum;

public class BrutalAnalyzer extends ContextWrapper implements Analyzer {

	private static final String TAG = "BrutalAnalyzer";
	
	public BrutalAnalyzer(Context base) {
		super(base);
	};

	/**
	 * 
	 * @return
	 */
	public ConnectionStatusEnum getConnectivityStatus() {
		final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		String name = wifiInfo.getSSID();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		String fkey = getString(R.string.pre_event_flight_key);
		boolean fdef = (getString(R.string.pre_event_flight_Default) == "true") ? true
				: false;
		boolean nFlight = sp.getBoolean(fkey, fdef);
		Log.v(TAG, "testing FlightMode: " + nFlight);
		boolean inFlightMode = false;
		if (nFlight) {
			inFlightMode = Hardware.isAirplaneModeOn(getBaseContext());
			Log.d(TAG, "inFlightMode: " + inFlightMode);
		}
		try {
			if (wifi.isAvailable()) {
				// wifi
				if (name == null) {
					return ConnectionStatusEnum.NoWifi;
				} else {
					if (inFlightMode) {
						return ConnectionStatusEnum.AirplaneWithWifi;
					} else
						return ConnectionStatusEnum.Wifi;
				}
			} else if (mobile.isAvailable()) {
				if (mobile.isConnected()) {
					if (inFlightMode) {
						return ConnectionStatusEnum.Airplane;
					} else {
						// 3g
						return ConnectionStatusEnum.Mobile;
					}
				} else {
					// no network
					return ConnectionStatusEnum.Disconnected;
				}
			} else {
				if (inFlightMode) {
					return ConnectionStatusEnum.Airplane;
				} else {
					// no network
					return ConnectionStatusEnum.Disconnected;
				}
			}
		} catch (Exception ex) {
			return ConnectionStatusEnum.Error;
		}
	}
	
}
