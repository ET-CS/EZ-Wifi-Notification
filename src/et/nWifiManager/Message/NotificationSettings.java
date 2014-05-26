package et.nWifiManager.Message;

import android.app.PendingIntent;
import android.net.Uri;

/**
 * Wrapper for Android notification settings 
 * @author ET
 *
 */
public class NotificationSettings {

	// Settings fields
	
	private CharSequence tickerText;
	private CharSequence contentTitle;
	private CharSequence contentText;
	private PendingIntent resultIntent;
	private boolean onGoing;
	private boolean vibrate;
	private long[] vibratePattern;
	private boolean showLights;
	private boolean sound;
	private Uri soundUri;
	
	// Getters and Setters
	
	/**
	 * @return the tickerText
	 */
	public CharSequence getTickerText() {
		return tickerText;
	}

	/**
	 * @param tickerText the tickerText to set
	 */
	public void setTickerText(CharSequence tickerText) {
		this.tickerText = tickerText;
	}

	/**
	 * @return the pendingIntent
	 */
	public PendingIntent getResultIntent() {
		return resultIntent;
	}

	/**
	 * @param pendingIntent the pendingIntent to set
	 */
	public void setResultIntent(PendingIntent pendingIntent) {
		this.resultIntent = pendingIntent;
	}

	/**
	 * @return the onGoing
	 */
	public boolean isOnGoing() {
		return onGoing;
	}

	/**
	 * @param onGoing the onGoing to set
	 */
	public void setOnGoing(boolean onGoing) {
		this.onGoing = onGoing;
	}

	/**
	 * @return the contentTitle
	 */
	public CharSequence getContentTitle() {
		return contentTitle;
	}

	/**
	 * @param contentTitle the contentTitle to set
	 */
	public void setContentTitle(CharSequence contentTitle) {
		this.contentTitle = contentTitle;
	}

	/**
	 * @return the contentText
	 */
	public CharSequence getContentText() {
		return contentText;
	}

	/**
	 * @param contentText the contentText to set
	 */
	public void setContentText(CharSequence contentText) {
		this.contentText = contentText;
	}

	/**
	 * @return the vibrate
	 */
	public boolean isVibrate() {
		return vibrate;
	}

	/**
	 * @param vibrate the vibrate to set
	 */
	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}

	/**
	 * @return the vibratePattern
	 */
	public long[] getVibratePattern() {
		return vibratePattern;
	}

	/**
	 * @param vibratePattern the vibratePattern to set
	 */
	public void setVibratePattern(long[] vibratePattern) {
		this.vibratePattern = vibratePattern;
	}
	
	/**
	 * @return the showLights
	 */
	public boolean isShowLights() {
		return showLights;
	}

	/**
	 * @param showLights the showLights to set
	 */
	public void setShowLights(boolean showLights) {
		this.showLights = showLights;
	}

	/**
	 * @return the sound
	 */
	public boolean isSound() {
		return sound;
	}

	/**
	 * @param sound the sound to set
	 */
	public void setSound(boolean sound) {
		this.sound = sound;
	}

	/**
	 * @return the soundUri
	 */
	public Uri getSoundUri() {
		return soundUri;
	}

	/**
	 * @param soundUri the soundUri to set
	 */
	public void setSoundUri(Uri soundUri) {
		this.soundUri = soundUri;
	}



}
