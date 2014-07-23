package et.nWifiManager.Message;

import et.nWifiManager.conState.ConnectionStatusEnum;

/** 
 * Message container. includes corrent Connectivity State & output Notification settings
 * @author ET-CS 
 */
public class Message extends NotificationSettings {

	private ConnectionStatusEnum State;
	/**
	 * @return the state
	 */
	public ConnectionStatusEnum getState() {
		return State;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(ConnectionStatusEnum state) {
		State = state;
	}
	
	public boolean isError=false;
	public boolean isSkip=false;
	
	public Message(){
	}
	public Message(CharSequence Text){ 
		Feed(Text); 
	}
	public Message(ConnectionStatusEnum state, CharSequence Text){
		this(Text);
		this.State = state;
	}	
	public Message(CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		setTickerText(TickerText);
		setContentTitle(ContentTitle);
		setContentText(ContentText);
	}	
	public Message(boolean error, CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		this(TickerText, ContentTitle, ContentText);
		if (error==false) {
			isSkip=true;
		}
		else{
			isError = error;	
		}
	}
	public Message(ConnectionStatusEnum state, CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		Feed(TickerText, ContentTitle, ContentTitle);
		this.State = state;
	}
	public void Feed(CharSequence Text) {
		setTickerText(Text);
		setContentTitle(Text);
		setContentText(Text);
	}
	public void Feed(String string) {
		setTickerText(string);
		setContentTitle(string);
		setContentText(string);
	}
	public void Feed(CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText) {
		setTickerText(TickerText);
		setContentTitle(ContentTitle);
		setContentText(ContentText);
	}
	public void Feed(ConnectionStatusEnum state, String string) {
		Feed(string);
		this.State = state;
	}

	
}
