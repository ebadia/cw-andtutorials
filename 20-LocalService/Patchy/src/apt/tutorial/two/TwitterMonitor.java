package apt.tutorial.two;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import winterwell.jtwitter.Twitter;

public class TwitterMonitor extends Service {
	public static TwitterMonitor singleton=null;
	private static final int POLL_PERIOD=60000;
	private AtomicBoolean active=new AtomicBoolean(true);
	private Set<Long> seenStatus=new HashSet<Long>();
	private Map<ITwitterListener, Account> accounts=
					new ConcurrentHashMap<ITwitterListener, Account>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		singleton=this;
		new Thread(threadBody).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return(null);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		active.set(false);
		singleton=null;
	}
	
	public void registerAccount(String user, String password,
																ITwitterListener callback) {
		Account l=new Account(user, password, callback);
		
		poll(l);
		accounts.put(callback, l);
	}
	
	public void removeAccount(ITwitterListener callback) {
		accounts.remove(callback);
	}
	
	private void poll(Account l) {
		try {
			Twitter client=new Twitter(l.user, l.password);
			List<Twitter.Status> timeline=client.getFriendsTimeline();
			
			for (Twitter.Status s : timeline) {
				if (!seenStatus.contains(s.id)) {
					l.callback.newFriendStatus(s.user.screenName, s.text,
																		 s.createdAt.toString());
					seenStatus.add(s.id);
				}
			}
		}
		catch (Throwable t) {
			android.util.Log.e("TwitterMonitor",
												 "Exception in poll()", t);
		}
	}
	
	private Runnable threadBody=new Runnable() {
		public void run() {
			while (active.get()) {
				for (Account l : accounts.values()) {
					poll(l);
				}
				
				SystemClock.sleep(POLL_PERIOD);
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
