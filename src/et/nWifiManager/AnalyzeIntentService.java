package et.nWifiManager;

import java.io.IOException;
import java.util.Locale;

import et.nWifiManager.Message.Message;
import et.nWifiManager.Message.Messages;
import et.nWifiManager.conState.Analyzer;
import et.nWifiManager.conState.ConnectionStatusEnum;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Analyize Intents recieved from android when connectivity changes
 * 
 * @author ET
 */
public class AnalyzeIntentService extends IntentService {

	// *********** analyzer settings ********************
	/**
	 * WifiService will remember last status and will not notify if nothing
	 * changed.
	 */
	public final static boolean DontRepeatNotification = false; // TODO Remove
																// this feature

	/**
	 * last status of the connection
	 */
	private static ConnectionStatusEnum lastStatus = null; // TODO Remove this
															// field when
															// DontRepeatNotification
															// is removed.

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
			Log.w(TAG, "Intent Action is empty. failover to brute notification");
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
							m = (new Analyzer()).AnalyzeIntent(
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
									showNotification(m);
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

	/**
	 * old way = scan what is now and not analyze message extras
	 */
	private void ShowNotificationBrutaly() {
		Log.d(TAG, "Showing notification brutally");
		ConnectionStatusEnum status = ConnectionStatus();
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
		if (Notify)
			showNotification(GenerateMessage(status));
	}

	/**
	 * check Preferences if Notification is Enabled.
	 * 
	 * @return true if notification enabled.
	 */
	private boolean isNotificationEnabled() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String skey = getString(R.string.pre_notification_key);
		return sp.getBoolean(skey, Constants.DefaultSettingNotification);
	}

	/**
	 * Show norification based on message
	 * 
	 * @param m
	 *            - message
	 */
	private void showNotification(Message m) {
		Log.d(TAG, "Showing Notification for " + m.State.toString());
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		String wkey = getString(R.string.pre_event_wifi_key);
		boolean wdef = (getString(R.string.pre_event_wifi_Default) == "true") ? true
				: false;
		String mkey = getString(R.string.pre_event_mobile_key);
		boolean mdef = (getString(R.string.pre_event_mobile_Default) == "true") ? true
				: false;
		String nkey = getString(R.string.pre_event_none_key);
		boolean ndef = (getString(R.string.pre_event_none_default) == "true") ? true
				: false;
		// get settings
		boolean nWifi = sp.getBoolean(wkey, wdef);
		boolean nMobile = sp.getBoolean(mkey, mdef);
		boolean nNone = sp.getBoolean(nkey, ndef);

		boolean hide = false;
		if (!(m.State == null))
			switch (m.State) {
			case Mobile:
				if (!nMobile)
					hide = true;
				break;
			case Airplane:
			case Disconnected:
				if (!nNone)
					hide = true;
				break;
			case Wifi:
			case AirplaneWithWifi:
				if (!nWifi)
					hide = true;
				break;
			default:
				break;
			}

		if (isNotificationEnabled()) {
			// Get a reference to the NotificationManager
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			final int ID = 1;
			if (hide) {
				try {
					nm.cancel(ID);
				} catch (Exception ex) {
					// TODO Notify me
				}
			} else {
				Notification notification = CreateNotification(m);
				nm.notify(ID, notification);
			}
		} else {
			if (!hide)
				try {
					FXOnly(m);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 
	 * @param message
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private void FXOnly(Message message) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {
		// Notification Sound And Vibrate
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		String skey = getString(R.string.pre_notificationsound_key);
		String vkey = getString(R.string.pre_notificationvib_key);
		// String lkey = getString(R.string.pre_notification_lights_key);
		// get settings
		boolean notificationSound = sp.getBoolean(skey,
				Constants.DefaultSettingNotificationSound);
		boolean notificationVibrate = sp.getBoolean(vkey,
				Constants.DefaultSettingNotificationVibrate);
		// boolean notificationLights = sp.getBoolean(lkey,
		// Constants.DefaultSettingNotificationLights);

		String strRingtonePreference = sp.getString(
				getString(SoundToUse(message)), "DEFAULT_SOUND");

		// define notification
		if (notificationSound) {
			Uri myUri = Uri.parse(strRingtonePreference); // initialize Uri here
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
			mediaPlayer.prepare();
			mediaPlayer.start();

		}
		if (notificationVibrate) {
			// Get instance of Vibrator from current Context
			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			long[] pattern = VibrationToUse(message);
			// Only perform this pattern one time (-1 means "do not repeat")
			v.vibrate(pattern, -1);
		}
	}

	/**
	 * 
	 * @param m
	 * @return
	 */
	private Notification CreateNotification(Message m) {
		// Instantiate the Notification
		Notification notification = new Notification(R.drawable.icon1,
				m.getTickerText(), System.currentTimeMillis());
		// Create Intent
		PendingIntent contentIntent = CreateIntent(m);
		// Set flags
		if (isOngoing())
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		setVisualFlags(notification, m);
		notification.setLatestEventInfo(this, m.getContentTitle(),
				m.getContentText(), contentIntent);
		// return the object
		return notification;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isOngoing() {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String key = getString(R.string.pre_notification_type_key);
		// get settings
		return (sp.getString(key,
				getString(R.string.pre_notification_type_default)).equals("1"));
	}

	/**
	 * Generate the PendingIntent to perform action on notification click
	 * 
	 * @param action
	 * @return
	 */
	private PendingIntent GeneratePendingIntent(String action, Message m) {
		if (action.equals("EZ"))
			return PendingIntent.getActivity(this, 0, new Intent(this,
					PreferencesActivity.class), 0);
		if (action.equals("WIRELESS"))
			return PendingIntent.getActivity(this, 0, new Intent(
					Settings.ACTION_WIRELESS_SETTINGS), 0);
		if (action.equals("WIFI"))
			return PendingIntent.getActivity(this, 0, new Intent(
					Settings.ACTION_WIFI_SETTINGS), 0);
		if (action.equals("MOBILE"))
			return PendingIntent.getActivity(this, 0, new Intent(
					Settings.ACTION_DATA_ROAMING_SETTINGS), 0);
		if (action.equals("SMART"))
			switch (m.State) {
			case WiMax:
				return PendingIntent.getActivity(this, 0, new Intent(
						Settings.ACTION_WIFI_SETTINGS), 0);
			case Mobile:
				return PendingIntent.getActivity(this, 0, new Intent(
						Settings.ACTION_DATA_ROAMING_SETTINGS), 0);
			case Airplane:
			case Disconnected:
				return PendingIntent.getActivity(this, 0, new Intent(this,
						PreferencesActivity.class), 0);
			case Wifi:
			case AirplaneWithWifi:
				return PendingIntent.getActivity(this, 0, new Intent(
						Settings.ACTION_WIFI_SETTINGS), 0);
			default:
				return PendingIntent.getActivity(this, 0, new Intent(this,
						PreferencesActivity.class), 0);
			}
		return PendingIntent.getActivity(this, 0, new Intent(this,
				PreferencesActivity.class), 0);
	}

	/**
	 * 
	 * @param m
	 * @return
	 */
	private PendingIntent CreateIntent(Message m) {
		// get the PreferenceManager object
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		// check if different actions preference enabled
		String dkey = getString(R.string.pre_action_different_key);
		boolean diff = sp.getBoolean(dkey,
				Constants.DefaultSettingDifferentActions);

		if (diff == true) {
			// Different sound for each state

			// Check state
			switch (m.State) {
			case Mobile:
				return GeneratePendingIntent(
						sp.getString(
								getString(R.string.pre_notification_mobile_action_key),
								getString(R.string.pre_notification_mobile_action_default)),
						m);
			case Airplane:
			case Disconnected:
				return GeneratePendingIntent(
						sp.getString(
								getString(R.string.pre_notification_disconnected_action_key),
								getString(R.string.pre_notification_disconnected_action_default)),
						m);
			case WiMax:
			case Wifi:
			case AirplaneWithWifi:
				return GeneratePendingIntent(
						sp.getString(
								getString(R.string.pre_notification_wifi_action_key),
								getString(R.string.pre_notification_wifi_action_default)),
						m);
			default:
				return GeneratePendingIntent("EZ", m);
			}

		} else {
			// Same preference for all

			// check the normal notification action preference
			String key = getString(R.string.pre_notification_action_key);
			String action = sp.getString(key,
					getString(R.string.pre_notification_action_default));
			return GeneratePendingIntent(action, m);
		}

	}

	/**
	 * 
	 * @param notification
	 * @param message
	 */
	private void setVisualFlags(Notification notification, Message message) {
		// Notification Sound And Vibrate

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		String skey = getString(R.string.pre_notificationsound_key);
		String vkey = getString(R.string.pre_notificationvib_key);
		String lkey = getString(R.string.pre_notification_lights_key);
		// get settings
		boolean notificationSound = sp.getBoolean(skey,
				Constants.DefaultSettingNotificationSound);
		boolean notificationVibrate = sp.getBoolean(vkey,
				Constants.DefaultSettingNotificationVibrate);
		boolean notificationLights = sp.getBoolean(lkey,
				Constants.DefaultSettingNotificationLights);

		String strRingtonePreference = sp.getString(
				getString(SoundToUse(message)), "DEFAULT_SOUND");
		// Toast.makeText(this, "I'm stuck!", Toast.LENGTH_SHORT).show();
		// mHandler.post(new DisplayToast(strRingtonePreference));
		// mHandler.post(new DisplayToast("did something"));
		// define notification
		if (notificationSound) {
			if (message.isSound())
				notification.sound = Uri.parse(strRingtonePreference);
		}
		if (notificationVibrate) {
			if (message.isVibrate())
				notification.vibrate = VibrationToUse(message);
		}
		if (notificationLights) {
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledARGB = 0xff00ff00;
			notification.ledOnMS = 300;
			notification.ledOffMS = 1000;
		}
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	private long[] VibrationToUse(Message message) {
		return new long[] { 0, 100, 200, 100, 200 };
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	private int SoundToUse(Message message) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		// get settings
		boolean OneRingtone = sp.getBoolean(
				getString(R.string.pre_one_ringtone_key),
				Constants.DefaultSettingNotificationSound);
		if (OneRingtone) {
			// different sounds
			switch (message.State) {
			case WiMax:
				return R.string.pre_ringtone_wimax_key;
			case Mobile:
				return R.string.pre_ringtone_mobile_key;
			case Airplane:
			case Disconnected:
				return R.string.pre_ringtone_offline_key;
			case Wifi:
			case AirplaneWithWifi:
				return R.string.pre_ringtone_wifi_key;
			default:
				return R.string.pre_ringtone_offline_key;
			}
		} else {
			return R.string.pre_ringtone_key;
		}
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	private Message GenerateMessage(ConnectionStatusEnum status) {
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
	public ConnectionStatusEnum ConnectionStatus() {
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
		boolean inFlightMode = false;
		if (!nFlight)
			inFlightMode = Hardware.isAirplaneModeOn(getBaseContext());

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
				// no network
				return ConnectionStatusEnum.Disconnected;
			}
		} catch (Exception ex) {
			return ConnectionStatusEnum.Error;
		}
	}

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
