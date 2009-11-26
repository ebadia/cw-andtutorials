package apt.tutorial.three;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import winterwell.jtwitter.Twitter;
import apt.tutorial.ITwitterListener;
import apt.tutorial.ITwitterMonitor;

public class TwitterMonitor extends Service {
	public static TwitterMonitor singleton=null;
	private static final int POLL_PERIOD=60000;
	private static final int INITIAL_POLL_PERIOD=1000;
	private int pollPeriod=INITIAL_POLL_PERIOD;
	private AtomicBoolean active=new AtomicBoolean(true);
	private Set<Long> seenStatus=new HashSet<Long>();
	private Map<ITwitterListener, Account> accounts=
					new ConcurrentHashMap<ITwitterListener, Account>();
	private final ITwitterMonitor.Stub binder=new ITwitterMonitor.Stub() {
		public void registerAccount(String user, String password,
																ITwitterListener callback) {
			if (user!=null) registerAccountImpl(user, password, callback);
		}
		
		public void removeAccount(ITwitterListener callback) {
			removeAccountImpl(callback);
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		singleton=this;
		new Thread(threadBody).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return(binder);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		active.set(false);
		singleton=null;
	}
	
	public void registerAccountImpl(String user, String password,
																	ITwitterListener callback) {
		Account l=new Account(user, password, callback);
		
		accounts.put(callback, l);
	}
	
	public void removeAccountImpl(ITwitterListener callback) {
		accounts.remove(callback);
	}
	
	private void poll(Account l) {
		try {
			Twitter client=new Twitter(l.user, l.password);
			List<Twitter.Status> timeline=client.getFriendsTimeline();
			
			for (Twitter.Status s : timeline) {
				if (!seenStatus.contains(s.id)) {
					try {
						l.callback.newFriendStatus(s.user.screenName, s.text,
																			 s.createdAt.toString());
						seenStatus.add(s.id);
					}
					catch (Throwable t) {
						Log.e("TwitterMonitor", "Exception in callback", t);
					}
				}
			}
		}
		catch (Throwable t) {
			Log.e("TwitterMonitor", "Exception in poll()", t);
		}
	}
	
	private Runnable threadBody=new Runnable() {
		public void run() {
			while (active.get()) {
				for (Account l : accounts.values()) {
					poll(l);
					pollPeriod=POLL_PERIOD;
				}
				
				SystemClock.sleep(pollPeriod);
			}
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
