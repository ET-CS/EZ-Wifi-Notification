package et.nWifiManager.Analyzers;

import android.content.Intent;
import et.nWifiManager.Message.Message;

public interface IntentAnalyzerInterface {
	public Message AnalyzeIntent(Intent intent);
}
