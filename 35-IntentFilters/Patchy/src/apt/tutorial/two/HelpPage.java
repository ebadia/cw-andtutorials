package apt.tutorial.two;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class HelpPage extends Activity {
	private WebView browser;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.help);
		
		browser=(WebView)findViewById(R.id.webkit);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.addJavascriptInterface(new CustomHelp(),
																		"customHelp");
		browser.loadUrl("file:///android_asset/help.html");
		
		Button cast=(Button)findViewById(R.id.helpcast);
		
		cast.setOnClickListener(onCast);
	}
	
	private View.OnClickListener onCast=new View.OnClickListener() {
		public void onClick(View v) {
			startActivity(new Intent(HelpPage.this, HelpCast.class));
		}
	};
	
	public class CustomHelp {
		SharedPreferences prefs=null;
		
		CustomHelp() {
			prefs=PreferenceManager
								.getDefaultSharedPreferences(HelpPage.this);
		}
		
		public String getUserName() {
			return(prefs.getString("user", "<no account>"));
		}
	}
}
