package et.nWifiManager.Analyzers;

import java.util.Locale;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import et.nWifiManager.Constants;
import et.nWifiManager.Hardware;
import et.nWifiManager.PreferencesActivity;
import et.nWifiManager.R;
import et.nWifiManager.Message.Message;
import et.nWifiManager.Message.Messages;
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
	

	/**
	 * 
	 * @param status
	 * @return
	 */
	public Message GenerateMessage(ConnectionStatusEnum status) {
		boolean short_title = PreferencesActivity.isShortTitle(this);
		switch (status) {
		case Wifi:
			return Messages.Wifi(WifiName(),
					(isSSIDEnabled()) ? getSSID() : "",
					(isIPEnabled()) ? getIP() : "", false, short_title);
		case Mobile:
			return Messages.Mobile(short_title);
		case NoWifi:
		case Disconnected:
			return Messages.NoConnectivity();
		case Airplane:
			return Messages.Flight();
		case AirplaneWithWifi:
			return Messages.Wifi(WifiName(),
					(isSSIDEnabled()) ? getSSID() : "",
					(isIPEnabled()) ? getIP() : "", true, short_title);
		default:
			return Messages.Unknown();
		}
	}

	// --------------------- Connectivity ----------------------

	/**
	 * 
	 * @return
	 */
	private String getSSID() {
		try {
			return ((WifiManager) getSystemService(WIFI_SERVICE))
					.getConnectionInfo().getBSSID();
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * 
	 * @return
	 */
	private String getIP() {
		try {
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			return String.format(Locale.getDefault(), "%d.%d.%d.%d",
					(ipAddress & 0xff), (ipAddress >> 8 & 0xff),
					(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
			/*
			 * for (Enumeration<NetworkInterface> en =
			 * NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			 * NetworkInterface intf = en.nextElement(); for
			 * (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
			 * enumIpAddr.hasMoreElements();) { InetAddress inetAddress =
			 * enumIpAddr.nextElement(); if (!inetAddress.isLoopbackAddress()) {
			 * return inetAddress.getHostAddress().toString(); } } }
			 */
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * 
	 * @return
	 */
	private String WifiName() {
		try {
			WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
			return wifiInfo.getSSID();
		} catch (Exception ex) {
			return "";
		}
	}

	// --------------------- Helpers ------------------------

	/**
	 * 
	 * @return
	 */
	private boolean isSSIDEnabled() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String key = getString(R.string.pre_ssid_key);
		// get settings
		return sp.getBoolean(key, Constants.DefaultSettingNotificationSSID);
	}

	/**
	 * 
	 * @return
	 */
	private boolean isIPEnabled() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String key = getString(R.string.pre_ip_key);
		// get settings
		return sp.getBoolean(key, Constants.DefaultSettingNotificationIP);
	}

	
}
