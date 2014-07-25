package et.nWifiManager.Message;

import et.nWifiManager.conState.ConnectionStatusEnum;


public class Messages {
	
	public static Message Wifi(String WifiName, String ssid, String ip, boolean flight, boolean short_title) {
		Message m = new Message();		
		if (ssid!="") {			
			m.setContentTitle("Wi-Fi"+ ((flight) ? " (Flight): " : ": ") + ssid);
		} else {
			if (flight) { m.setContentTitle("Wi-Fi (Flight Mode)"); } else {
			m.setContentTitle((!short_title) ? "Connected via Wi-Fi" : "Wi-Fi");}
		}
		if (ip!="") {
			m.setContentText(WifiName + " (" + ip + ")");
		} else {
			m.setContentText("Connected to " + WifiName);
		}
		m.setTickerText(m.getContentText());
		m.setState(ConnectionStatusEnum.Wifi);
		return m;		
	}
	
	public static Message Flight() {
		return new Message(ConnectionStatusEnum.Airplane, 
				"No connectivity","Flight Mode","No connectivity");
	}

	public static Message Mobile(boolean short_title) {
		return new Message(ConnectionStatusEnum.Mobile, (!short_title) ? "Connected via Mobile" : "Mobile");
	}

	public static Message Mobile(String ip, boolean short_title) {
		Message m = new Message();
		m.setContentTitle((!short_title) ? "Connected via Mobile" : "Mobile");
		if (ip!=null && ip!="") {
			m.setContentText("Mobile (" + ip + ")");
		} else {
			m.setContentText((!short_title) ? "Connected via Mobile" : "Mobile");
		}
		m.setTickerText(m.getContentText());
		m.setState(ConnectionStatusEnum.Mobile);
		return m;
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
