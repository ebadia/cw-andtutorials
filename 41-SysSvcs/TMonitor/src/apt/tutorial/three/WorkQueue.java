package apt.tutorial.three;

import android.content.Context;
import android.os.PowerManager;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkQueue {
	private PowerManager.WakeLock lock=null;
	private BlockingQueue<Runnable> workQueue=new LinkedBlockingQueue<Runnable>();
	private AtomicBoolean isStopping=new AtomicBoolean(false);
	private Runnable onStop=null;
	
	public WorkQueue(Context context, String name,
									 Runnable onStop) {
		PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
		
		lock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
													name);
		lock.setReferenceCounted(true);
		this.onStop=onStop;
	}
	
	/*
	 * Must be called within an outer active WakeLock!
	 */ 
	public void enqueue(Runnable r) {
		synchronized(this) {
			if (!isStopping.get()) {
				workQueue.add(r);
				lock.acquire();
			}
		}
	}
	
	/*
	 * Must be called within an outer active WakeLock!
	 */ 
	public void stop() {
		synchronized(this) {
			isStopping.set(true);
			workQueue.add(onStop);
			lock.acquire();
		}
	}
	
	public void start() {
		new Thread(new Runnable() {
			public void run() {
				process();
			}
		}).start();
	}
	
	/*
	 * Designed to run on its own thread!
	 */ 
	private void process() {
		try {
			while (true) {
				Runnable r=workQueue.take();
				
				r.run();
				
				if (r==onStop) {
					break;
				}
				
				synchronized(this) {
					lock.release();
				}
			}
		}
		catch (InterruptedException e) {
			synchronized(this) {
				// theory = we really want to wrap up, so we
				// try to force the WakeLock to fully release
				// when we get interrupted by forces unknown
				lock.setReferenceCounted(false);
			}
		}
		
		synchronized(this) {
			lock.release();		// from the onStop job
		}
	}
}