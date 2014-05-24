package et.nWifiManager.conState;


public class Messages {
	
	public static Message Wifi(String WifiName, String ssid, String ip, boolean flight, boolean short_title) {
		Message m = new Message();		
		if (ssid!="") {			
			m.contentTitle = "Wi-Fi"+ ((flight) ? " (Flight): " : ": ") + ssid;
		} else {
			if (flight) { m.contentTitle = "Wi-Fi (Flight Mode)"; } else {
			m.contentTitle = (!short_title) ? "Connected via Wi-Fi" : "Wi-Fi";}
		}
		if (ip!="") {
			m.contentText = WifiName + " (" + ip + ")";
		} else {
			m.contentText = "Connected to " + WifiName;
		}
		m.tickerText = m.contentText;
		m.State = ConnectionStatusEnum.Wifi;
		return m;		
	}
	
	public static Message Flight() {
		return new Message(ConnectionStatusEnum.Airplane, 
				"No connectivity","Flight Mode","No connectivity");
	}

	public static Message Mobile(boolean short_title) {
		return new Message(ConnectionStatusEnum.Mobile, (!short_title) ? "Connected via Mobile" : "Mobile");
	}

	public static Message NoConnectivity() {
		return new Message(ConnectionStatusEnum.Disconnected, "No network connection");
	}

	public static Message Unknown() {
		return new Message(ConnectionStatusEnum.Unsupported, "Unsupported Connection");
	}
	

	public static Message WiMax(boolean short_title) {
		return new Message(ConnectionStatusEnum.WiMax, (!short_title) ? "Connected via WIMAX" : "WIMAX");		
	}
	
}
