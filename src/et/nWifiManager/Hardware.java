package et.nWifiManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

public class Hardware {

	/**
	 * Gets the state of Airplane Mode.
	 * 
	 * @param context
	 * @return true if enabled.
	 */
	public static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	public static String getSSID(Context context) {
		try {
			return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
					.getConnectionInfo().getBSSID();
		} catch (Exception ex) {
			return "";
		}
	}

	@SuppressLint("DefaultLocale")
	public static String getIP(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();

			return String.format("%d.%d.%d.%d", (ipAddress & 0xff),
					(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
					(ipAddress >> 24 & 0xff));
		} catch (Exception ex) {
			// TODO Notify me
			return "";
		}
	}

	public static String WifiName(Context context) {
		try {
			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
			return wifiInfo.getSSID();
		} catch (Exception ex) {
			return "";
		}
	}
	
}
