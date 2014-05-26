package et.nWifiManager.Message;

import et.nWifiManager.conState.ConnectionStatusEnum;

public class Message extends NotificationSettings {

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
		
	public Message(){
		
	}
	
	public boolean isError=false;
	public boolean isSkip=false;
	
	public ConnectionStatusEnum State;

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

	public Message(ConnectionStatusEnum state, CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		Feed(TickerText, ContentTitle, ContentTitle);
		this.State = state;
	}
	
	
}
