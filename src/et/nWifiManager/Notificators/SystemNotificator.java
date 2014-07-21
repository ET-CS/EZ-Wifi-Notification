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

public class SystemNotificator extends ContextWrapper {

	private static final String TAG = "SystemNotificator";
	
	public SystemNotificator(Context base) {
		super(base);
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
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void showNotification(Message m) {
		Log.i(TAG, "Showing Notification for " + m.State.toString());
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
					Log.e(TAG, "Error canceling notification");
				}
			} else {
				Notification notification = CreateNotification(m);
				// Hide icon feature (for API 16+)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					if (PreferencesActivity.isHideIcon(this)) {
						notification.priority = Notification.PRIORITY_MIN;	
					}					
				}
				nm.notify(ID, notification);
			}
		} else {
			if (!hide)
				try {
					(new SystemNotificator(this)).FXOnly(m);
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
	 * 
	 * @param m
	 * @return
	 */
	public Notification CreateNotification(Message m) {
		// Instantiate the Notification

		Notification notification = new Notification(getIcon(m.State),
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
	 * @param message
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void FXOnly(Message message) throws IllegalArgumentException,
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
