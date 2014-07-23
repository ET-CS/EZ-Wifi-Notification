package et.nWifiManager.Notificators;

import java.io.IOException;

import et.nWifiManager.Constants;
import et.nWifiManager.PreferencesActivity;
import et.nWifiManager.R;
import et.nWifiManager.Message.Message;
import et.nWifiManager.conState.ConnectionStatusEnum;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class SystemNotificator extends ContextWrapper implements Notificator {

	private static final String TAG = "SystemNotificator";
	
	public SystemNotificator(Context base) {
		super(base);
	}

	public void Show(Message m) {
		showNotification(m);
	}
	
	// Get Setting from preferences
	private boolean getSetting(SharedPreferences sp, int preferenceKey, int preferenceDefault) {
		return sp.getBoolean(
				getString(preferenceKey),
				(getString(preferenceDefault) == "true") ? true	: false);
	}
	
	/**
	 * Show norification based on message
	 * 
	 * @param m
	 *            - message
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showNotification(Message m) {
		Log.i(TAG, "Showing Notification for " + m.getState().toString());

		// Check whetever notification needed to be hided on non notification event
		boolean hide = isNotificationNeeded(m);
		if (hide) Log.i(TAG,"Notification not needed");
		if (isNotificationEnabled()) {
			// Get a reference to the NotificationManager
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			final int ID = 1;
			if (hide) {
				try {
					nm.cancel(ID);
				} catch (Exception ex) {
					Log.e(TAG, "Error canceling notification");
				}
			} else {
				Log.i(TAG, "Creating notification");
				Notification notification = null;
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					// use the depreceated notification creation api.
					// Used for debuging of deprecated algorithm on newer api (16+) 
					boolean forceDepreceated = true;
					if (forceDepreceated) {
						Log.w(TAG, "Using depreceated notification builder");
						notification = CreateNotification(m);							
					} else {
						notification = CreateNotificationUsingBuilder(m);
					}
					if (PreferencesActivity.isHideIcon(this)) {
						// Hide icon feature (only API 16+)
						notification.priority = Notification.PRIORITY_MIN;
					}					
				} else {
					// force old api on lower API then 16 (JELLY BEAN)
					notification = CreateNotification(m);
				}					
				// Send notification to notificationManager
				nm.notify(ID, notification);
			}
		} else {
			if (!hide)
				try {
					(new SystemNotificator(this)).FXOnly(m);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				} catch (SecurityException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				}
		}
	}

	/**
	 * Check if Notification is set to show on current connectivity event
	 * @param m
	 * @return
	 */

	private boolean isNotificationNeeded(Message m) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		// get settings
		boolean notificationOnWifi = getSetting(sp,
				R.string.pre_event_wifi_key, 
				R.bool.pre_event_wifi_Default);
		boolean notificationOnMobile = getSetting(sp,
				R.string.pre_event_mobile_key, 
				R.bool.pre_event_mobile_Default);
		boolean notificationOnDisconnected = getSetting(sp,
				R.string.pre_event_none_key, 
				R.bool.pre_event_none_default);

		boolean hide = false;
		if (!(m.getState() == null))
			switch (m.getState()) {
			case Mobile:
				if (!notificationOnMobile)
					hide = true;
				break;
			case Airplane:
			case Disconnected:
				if (!notificationOnDisconnected)
					hide = true;
				break;
			case Wifi:
			case AirplaneWithWifi:
				if (!notificationOnWifi)
					hide = true;
				break;
			default:
				break;
			}
		return hide;
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
	
	private int getIcon(ConnectionStatusEnum state) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		
		String key = getString(R.string.pre_icon_nowifi_key);

		boolean noWifiIcon = sp.getBoolean(key,
				Constants.DefaultSettingNoWifiIcon);
		if (noWifiIcon) {
			switch (state) {
			case Wifi:
			case AirplaneWithWifi:
			case WiMax:
				break;  
			default:
				return R.drawable.icon_nowifi;
			}
		}
		return R.drawable.icon1;		
	}
	
	/** 
	 * Used for API 16+
	 * @param m
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Notification CreateNotificationUsingBuilder(Message m) {
		Notification notification = new Notification.Builder(this)
		.setTicker(m.getTickerText())
        .setContentTitle(m.getContentTitle())
        .setContentIntent(CreateIntent(m))
        .setContentText(m.getContentText())
        .setSmallIcon(getIcon(m.getState()))
        .setOngoing(isOngoing())
        .build();		
		setVisualFlags(notification, m);
		// return the object
		return notification;
	}
	
	/**
	 * Used for API 8-15
	 * @param m
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public Notification CreateNotification(Message m) {
		// Instantiate the Notification

		Notification notification = new Notification(getIcon(m.getState()),
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
			switch (m.getState()) {
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

		// Different sound for each state
		if (diff == true) {
			// Check state
			switch (m.getState()) {
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
		
		if (notificationSound) Log.d(TAG, "Notificaion Sound enabled");
		if (notificationVibrate) Log.d(TAG, "Notificaion Vibrate enabled");
		if (notificationLights) Log.d(TAG, "Notificaion Lights enabled");
		
		String strRingtonePreference = sp.getString(
				getString(SoundToUse(message)), "DEFAULT_SOUND");

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
			switch (message.getState()) {
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
	 * @param message
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void FXOnly(Message message) throws IllegalArgumentException,
			SecurityException, IllegalStateException, IOException {
		Log.d(TAG, "Playing FX");
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
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
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
	
}
