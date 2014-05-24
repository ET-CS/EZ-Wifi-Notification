package et.nWifiManager.conState;


public class Message {

	public Message(CharSequence Text){
		this.tickerText = Text;
		this.contentTitle = Text;
		this.contentText = Text;
	}

	public Message(ConnectionStatusEnum state, CharSequence Text){
		this.tickerText = Text;
		this.contentTitle = Text;
		this.contentText = Text;
		this.State = state;
	}
	
	public Message(CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		this.tickerText = TickerText;
		this.contentTitle = ContentTitle;
		this.contentText = ContentText;
	}
	
	public Message(boolean error, CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		if (error==false) {
			isSkip=true;
		}
		else{
			isError = error;	
		}
		this.tickerText = TickerText;
		this.contentTitle = ContentTitle;
		this.contentText = ContentText;		
	}
		
	public Message(){
		
	}
	
	public boolean isError=false;
	public boolean isSkip=false;
	public CharSequence tickerText="";
	public CharSequence contentTitle="";
	public CharSequence contentText="";
	public boolean sound=true;
	public boolean vibrate=true;
	
	public ConnectionStatusEnum State;
	
	public void Feed(String string) {
		// TODO Auto-generated method stub
		tickerText=string;
		contentTitle=string;
		contentText=string;
	}

	public void Feed(ConnectionStatusEnum state, String string) {
		// TODO Auto-generated method stub
		tickerText=string;
		contentTitle=string;
		contentText=string;
		this.State = state;
	}

	public Message(ConnectionStatusEnum state, CharSequence TickerText, CharSequence ContentTitle, CharSequence ContentText){
		this.tickerText = TickerText;
		this.contentTitle = ContentTitle;
		this.contentText = ContentText;
		this.State = state;
	}
	
	
}
