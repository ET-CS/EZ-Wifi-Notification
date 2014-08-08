package et.nWifiManager;

// PreferencesActivity.java - Preferences screen.

//TODO remove all SupressWarnings and convert to API 16+ Fragement


import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

/**
 * Preferences Activity
 * @author ET
 */
public class PreferencesActivity extends PreferenceActivity {

	static final String TAG = "PreferencesActivity";
	
	/**
	 * Where it all begins.. load the UI from XML and call SetupButtons().
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (isLightTheme(this)) setTheme(R.style.LightThemeSelector);
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Load the service (if not loaded before (?!)
		((MyApplication) getApplication()).runOnce();
		
		// Setup Buttons handlers
		SetupButtons();			

	}

	OnPreferenceClickListener loadAndroidWirelessSettings = new OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			showWifiActivity();
			return true;
		}
		
	};
	
	OnPreferenceChangeListener	switchWifiState = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object isChecked) {
			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled((Boolean) isChecked);
			return true;
		}		
	};
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void PrepareWifiSwitch() {
		// Set the Wi-Fi Switch
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			SwitchPreference wifiSwitch = (SwitchPreference) findPreference(getString(R.string.pre_wifi_switch_key));
		    wifiSwitch.setOnPreferenceChangeListener(switchWifiState);
		    wifiSwitch.setChecked(getWifiState());
		} else {
			CheckBoxPreference wifiSwitch = (CheckBoxPreference) findPreference(getString(R.string.pre_wifi_switch_key));
		    wifiSwitch.setOnPreferenceChangeListener(switchWifiState);
			wifiSwitch.setChecked(getWifiState());
		}
	}

	private boolean getWifiState() {
		ConnectivityManager cm =
		        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return  activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	}

	/** 
	 * Helper: Setup all Buttons Handlers
	 */
	@SuppressWarnings("deprecation")
	private void SetupButtons() {
		// Load again notification on changes on:
		setListener(R.string.pre_ip_key);
		setListener(R.string.pre_ssid_key);
		setListener(R.string.pre_event_flight_key);
		setListener(R.string.pre_event_flight_key);
		setListener(R.string.pre_event_wifi_key);
		setListener(R.string.pre_event_none_key);
		setListener(R.string.pre_event_mobile_key);
		setListener(R.string.pre_notification_type_key);
		setListener(R.string.pre_notification_action_key);
		setListener(R.string.pre_notification_key);
		setListener(R.string.pre_short_title_key);
		setListener(R.string.pre_action_different_key);
		// TODO Check if 'Different Actions' enabled before refreshing notification 
		setListener(R.string.pre_notification_wifi_action_key);
		setListener(R.string.pre_notification_mobile_action_key);
		setListener(R.string.pre_notification_disconnected_action_key);
		setListener(R.string.pre_icon_nowifi_key);
		setListener(R.string.pre_notification_icon_key);
		setRestartListener(R.string.pre_notification_icon_key);

		// Wifi switch
		try {
			PrepareWifiSwitch();	
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		
		
		// Set refresh activity on Theme change
		CheckBoxPreference theme = (CheckBoxPreference) findPreference(getString(R.string.pre_light_theme_key));
		theme.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				refresh();
				return true;
			}
			
		});


		
		// Set the ringtone one button to set off the old rington selector
		try {
			CheckBoxPreference OneRingtone = (CheckBoxPreference) findPreference(getString(R.string.pre_one_ringtone_key));
			OneRingtone.setOnPreferenceClickListener(OneRingtoneCheckBoxListener);
			if (OneRingtone.isChecked() & OneRingtone.isEnabled()) {
				EnableDisablePref((Preference) findPreference(getString(R.string.pre_ringtone_key)), false);
			}

		} catch (Exception e) {
			
		}

		// Enable the notification hide icon setting on api 19+
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				CheckBoxPreference hideicon = (CheckBoxPreference) findPreference(getString(R.string.pre_notification_icon_key));
				hideicon.setEnabled(true);
				hideicon.setSummary(R.string.pre_notification_icon_summary_avail);
			}
		} catch (Exception e) {
			
		} 
		
		// Set up 'different actions' to disable old one action button.
		try {
			CheckBoxPreference diffactions = (CheckBoxPreference) findPreference(getString(R.string.pre_action_different_key));
			diffactions.setOnPreferenceClickListener(OneActionCheckBoxListener);
			if (diffactions.isChecked() & diffactions.isEnabled()) {
				EnableDisablePref((Preference) findPreference(getString(R.string.pre_notification_action_key)), false);
			}

		} catch (Exception e) {
			
		}
		
		// Set the version in the about section
		try {
			Preference About = (Preference) findPreference(getString(R.string.pre_about_key));
			String version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
			About.setTitle(getString(R.string.app_name) + " - v" + version);
			About.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					OpenMarket();
					return true;
				}
			});
		} catch (NameNotFoundException e) {
			// Log.e("tag", e.getMessage());
		}
		
		try {
			// Set GitHub Preference link
			Preference GitHub = (Preference) findPreference(getString(R.string.pre_github_key));
			GitHub.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					OpenGitHub();
					return true;
				}
			});			
		} finally {
			
		}

		try {
			// Set GitHub Preference link
			Preference Contact = (Preference) findPreference(getString(R.string.pre_contact_key));
			Contact.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					Contact();
					return true;
				}
			});			
		} finally {
			
		}
		
		//clear notification when disabled
		Preference NotificationSettingsPref = (Preference) findPreference(getString(R.string.pre_notification_key));
		NotificationSettingsPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						clearNotification();
						return true;
					}
				});
		
		Preference customPref = (Preference) findPreference(getString(R.string.pre_reset_key));
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						// Clear preferences and refresh to reload changes. 
						clearPreferences();
						refresh();
						return true;
					}
				});

		//networks
		Preference NetworkSettingsPref = (Preference) findPreference(getString(R.string.pre_networksettings_key));
		NetworkSettingsPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						showNetworkActivity();
						return true;
					}
				});


		
		//wireless
		Preference WifiSettingsPref = (Preference) findPreference(getString(R.string.pre_wifisettings_key));
		WifiSettingsPref
				.setOnPreferenceClickListener(loadAndroidWirelessSettings);
		
		/* Back button removed at version 2.0 
		Preference BackPref = (Preference) findPreference(getString(R.string.back));
		BackPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				PreferenceScreen ps = (PreferenceScreen) PreferencesActivity.this
						.getPreferenceScreen().findPreference(
								"pre_screen_service_key");
				ps.getDialog().dismiss();
				return true;
			}
		});
		*/
	}
	
	/**
	 * Helper: set listener to reload Notification on <resId>.onChange;
	 * @param resId - Resource Id of the UI object.  
	 */
	@SuppressWarnings("deprecation")
	private void setListener(int resId) {
		(findPreference(getString(resId))).setOnPreferenceChangeListener(overrider);
	}

	/**
	 * Helper: set listener to remove and show again Notification on <resId>.onChange;
	 * @param resId - Resource Id of the UI object.  
	 */
	@SuppressWarnings("deprecation")
	private void setRestartListener(int resId) {
		(findPreference(getString(resId))).setOnPreferenceChangeListener(restarter);
	}
	
	// --------  Gui helpers ----------
	
	/**
	 * Helper: reload Notification on <resId>.onChange.
	 * Set by setListener on resId of objects need to reset notification on changes.
	 */
	public OnPreferenceChangeListener overrider = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			((MyApplication) getApplication()).runOnce();
			return true;
		}
	};

	/**
	 * Helper: remove and reload Notification on <resId>.onChange.
	 * Set by setListener on resId of objects need to reset notification on changes.
	 */
	public OnPreferenceChangeListener restarter = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			clearNotification();
			((MyApplication) getApplication()).runOnce();
			return true;
		}
	};
	
	/**
	 * Disable notification sound checkboxPreference
	 * @param pref
	 * @param stats
	 */
	private void EnableDisablePref(Preference pref, boolean stats) {
		pref.setEnabled(stats);		
	}

	/**
	 * Lisener for onclick on seperate ringtone checkbox to disable notification sound checkbox
	 */
	private OnPreferenceClickListener OneRingtoneCheckBoxListener = new OnPreferenceClickListener() {
	    @SuppressWarnings("deprecation")
		public boolean onPreferenceClick(Preference preference) {
			// Disable old ringtone checkbox
			try {
				CheckBoxPreference cb = (CheckBoxPreference) preference;
				EnableDisablePref((Preference) findPreference(getString(R.string.pre_ringtone_key)),!cb.isChecked());
			} catch (Exception e) {
				
			}
	        return true;
	    }
	};

	/**
	 * Lisener for onclick on seperate actions checkbox to disable old one action preference
	 */
	private OnPreferenceClickListener OneActionCheckBoxListener = new OnPreferenceClickListener() {
	    @SuppressWarnings("deprecation")
		public boolean onPreferenceClick(Preference preference) {
			// Disable old ringtone checkbox
			try {
				CheckBoxPreference cb = (CheckBoxPreference) preference;
				EnableDisablePref((Preference) findPreference(getString(R.string.pre_notification_action_key)),!cb.isChecked());
			} catch (Exception e) {
				
			}
	        return true;
	    }
	};

	// --------  Notifications helpers ----------
	
	/**
	 * remove (cancel) Notification
	 */
	private void clearNotification() {
	    String ns = NOTIFICATION_SERVICE;
	    NotificationManager nMgr = (NotificationManager) getSystemService(ns);
	    nMgr.cancelAll();
	}

	// --------  Preferences helpers ----------
	
	/**
	 * Reset (clear) all preferences.
	 */
	public void clearPreferences() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		Editor editor = prefs.edit();
		editor.clear();
		editor.commit();
	}

	// --------  Activity helpers ----------
	
	/**
	 * Refresh this activity
	 */
	private void refresh() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}
	
	// --------  Create Intents to other screens helpers ----------

	/**
	 * Helper: Show Android Wireless Settings Activity
	 */
	private void showNetworkActivity() {
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}
	
	/**
	 * Helper: Show Android Wifi Settings Activity
	 */
	private void showWifiActivity() {
		startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
	}

	/**
	 * Open Market page on et.nWifiManager.
	 */
	public void OpenMarket() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=et.nWifiManager"));
		startActivity(intent);
	}

	/**
	 * Open GitHub Page
	 */
	public void OpenGitHub() {
		Log.println(Log.DEBUG, TAG, "Opening GitHub page");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("https://github.com/ET-CS/EZ-Wifi-Notification"));
		startActivity(intent);
	}
	
	/**
	 * Contact me
	 */
	public void Contact() {
		try {
			String version = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("mailto:et.programming@gmail.com?subject=EZ Wifi Notification v"+version));
			startActivity(intent);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// --------  Read from preferences helpers ----------
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isShortTitle(Context context) {
		// get settings
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String key = context.getString(R.string.pre_short_title_key);
		return sp.getBoolean(key,
				Constants.DefaultSettingNotificationShortTitle); 
	}

	// Get Setting from preferences
	private static boolean getSetting(Context context, SharedPreferences sp, int preferenceKey, int preferenceDefault) {
		return sp.getBoolean(
				context.getString(preferenceKey),
				(context.getString(preferenceDefault) == "true") ? true	: false);
	}

	@SuppressWarnings("unused")
	private static boolean getSetting(Context context, SharedPreferences sp,
			int preferenceKey, boolean defaultsetting) {
		return sp.getBoolean(
				context.getString(preferenceKey),
				defaultsetting );

	}
	
	public static boolean isLightTheme(Context context) {
		// get settings
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		return getSetting(context, sp, R.string.pre_light_theme_key, R.bool.pre_light_theme_default);
	}
	
	public static boolean isHideIcon(Context context) {
		// get settings
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		String key = context.getString(R.string.pre_notification_icon_key);
		return sp.getBoolean(key,
				Constants.DefaultSettingNotificationHideIcon);
	}

}
