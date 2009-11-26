package apt.tutorial.three;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import winterwell.jtwitter.Twitter;
import apt.tutorial.ITwitterListener;
import apt.tutorial.ITwitterMonitor;

public class TwitterMonitor extends Service {
	public static final String STATUS_UPDATE="apt.tutorial.three.STATUS_UPDATE";
	public static final String FRIEND="apt.tutorial.three.FRIEND";
	public static final String STATUS="apt.tutorial.three.STATUS";
	public static final String CREATED_AT="apt.tutorial.three.CREATED_AT";
	private static final int NOTIFY_ME_ID=1337;
	private static final int POLL_PERIOD=60000;
	private static final int INITIAL_POLL_PERIOD=1000;
	private static PowerManager.WakeLock lock=null;
	private Set<Long> seenStatus=new HashSet<Long>();
	private Map<ITwitterListener, Account> accounts=
					new ConcurrentHashMap<ITwitterListener, Account>();
	private List<String> bff=new CopyOnWriteArrayList<String>();
	private NotificationManager mgr=null;
	private AtomicBoolean isBatteryLow=new AtomicBoolean(false);
	private WorkQueue q=null;
	private AlarmManager alarm=null;
	private PendingIntent pi=null;
	private final ITwitterMonitor.Stub binder=new ITwitterMonitor.Stub() {
		public void registerAccount(String user, String password,
																ITwitterListener callback) {
			if (user!=null) registerAccountImpl(user, password, callback);
		}
		
		public void removeAccount(ITwitterListener callback) {
			removeAccountImpl(callback);
		}
		
		public void setBestFriends(ITwitterListener callback,
																List<String> newBff) {
			bff.clear();
			bff.addAll(newBff);
		}
		
		public void updateTimeline() {
			new Thread(new Runnable() {
				public void run() {
					for (Account l : accounts.values()) {
						poll(l);
					}
				}
			}).start();
		}
	};
	
	synchronized public static PowerManager.WakeLock getLock(Context context) {
		if (lock==null) {
			PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			
			lock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
														"apt.tutorial.three.TMonitorLock");
			lock.setReferenceCounted(true);
		}
		
		return(lock);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		alarm=(AlarmManager)getSystemService(Context.ALARM_SERVICE);

		Intent i=new Intent(this, OnAlarmReceiver.class);
		
		pi=PendingIntent.getBroadcast(this, 0, i, 0);
		
		q=new WorkQueue(this, "apt.tutorial.three.TMonitorLock2",
										onStop);
		q.start();
		
		mgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		registerReceiver(onBatteryChanged,
											new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		setAlarm(INITIAL_POLL_PERIOD);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return(binder);
	}
	
	@Override
	public void onStart(Intent intent, final int startId) {
		enqueueJob();
		getLock(this).release();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(onBatteryChanged);
		
		alarm.cancel(pi);
		q.stop();
	}
	
	public void registerAccountImpl(String user, String password,
																	ITwitterListener callback) {
		Account l=new Account(user, password, callback);
		
		accounts.put(callback, l);
	}
	
	public void removeAccountImpl(ITwitterListener callback) {
		accounts.remove(callback);
	}
	
	synchronized private void poll(Account l) {
		try {
			Twitter client=new Twitter(l.user, l.password);
			List<Twitter.Status> timeline=client.getFriendsTimeline();
			
			for (Twitter.Status s : timeline) {
				if (!seenStatus.contains(s.id)) {
					try {
						Intent broadcast=new Intent(STATUS_UPDATE);
						
						broadcast.putExtra(FRIEND, s.user.screenName);
						broadcast.putExtra(STATUS, s.text);
						broadcast.putExtra(CREATED_AT,
																s.createdAt.toString());
						
						sendBroadcast(broadcast);
	
						seenStatus.add(s.id);
					}
					catch (Throwable t) {
						Log.e("TwitterMonitor", "Exception in callback", t);
					}
					
					if (bff.contains(s.user.screenName)) {
						notify(s.user.screenName);
					}
				}
			}
		}
		catch (Throwable t) {
			Log.e("TwitterMonitor", "Exception in poll()", t);
		}
	}
	
	private void notify(String friend) {
		Notification note=new Notification(R.drawable.red_ball,
																				"Tweet!",
																				System.currentTimeMillis());
		Intent i=new Intent();
		
		i.setClassName("apt.tutorial.two",
									 "apt.tutorial.two.Patchy");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent pi=PendingIntent.getActivity(this, 0, i,
																								0);
		
		note.setLatestEventInfo(this, "Tweet!",
														friend+" updated their Twitter status",
														pi);
		
		mgr.notify(NOTIFY_ME_ID, note);
	}
	
	private void pollAll() {
		for (Account l : accounts.values()) {
			poll(l);
		}
		
		setAlarm(isBatteryLow.get() ? POLL_PERIOD*10 : POLL_PERIOD);
	}
	
	private void setAlarm(long period) {
		alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
							SystemClock.elapsedRealtime()+period,
							pi);
	}
	
	private void enqueueJob() {
		q.enqueue(new Runnable() {
			public void run() {
				pollAll();
			}
		});
	}
	
	BroadcastReceiver onBatteryChanged=new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int pct=100*intent.getIntExtra("level", 1)/intent.getIntExtra("scale", 1);
			
			isBatteryLow.set(pct<=25);
		}
	};
	
	private Runnable onStop=new Runnable() {
		public void run() {
			// being destroyed, nothing useful to do
		}
	};
	
	class Account {
		String user=null;
		String password=null;
		ITwitterListener callback=null;
		
		Account(String user, String password,
						 ITwitterListener callback) {
			this.user=user;
			this.password=password;
			this.callback=callback;
		}
	}
}
