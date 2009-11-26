package apt.tutorial.three;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnAlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		TwitterMonitor.getLock(context).acquire();
		
		context.startService(new Intent(context,
																		TwitterMonitor.class));
	}
}
