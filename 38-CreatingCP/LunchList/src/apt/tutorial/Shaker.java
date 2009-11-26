package apt.tutorial;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Shaker {
	private SensorManager sensor=null;
	private long lastShakeTimestamp=0;
	private double threshold=1.0d;
	private long gap=0;
	private Shaker.Callback cb=null;
	
	public Shaker(Context ctxt, double threshold, long gap,
									Shaker.Callback cb) {
		this.threshold=threshold;
		this.gap=gap;
		this.cb=cb;
		
		sensor=(SensorManager)ctxt.getSystemService(Context.SENSOR_SERVICE);
		sensor.registerListener(listener,
														SensorManager.SENSOR_ACCELEROMETER);
	}
	
	public void close() {
		sensor.unregisterListener(listener);
	}
	
	private void isShaking() {
		long now=SystemClock.uptimeMillis();
		
		if (lastShakeTimestamp==0) {
			lastShakeTimestamp=now;
			
			if (cb!=null) {
				cb.shakingStarted();
			}
		}
		else {
			lastShakeTimestamp=now;
		}
	}
	
	private void isNotShaking() {
		long now=SystemClock.uptimeMillis();
		
		if (lastShakeTimestamp>0) {
			if (now-lastShakeTimestamp>gap) {
				lastShakeTimestamp=0;
				
				if (cb!=null) {
					cb.shakingStopped();
				}
			}
		}
	}
	
	public interface Callback {
		void shakingStarted();
		void shakingStopped();
	}
	
	private SensorListener listener=new SensorListener() {
		public void onSensorChanged(int sensor, float[] values) {
			if (sensor==SensorManager.SENSOR_ACCELEROMETER) {
				double netForce=Math.pow(values[SensorManager.DATA_X], 2.0);
				
				netForce+=Math.pow(values[SensorManager.DATA_Y], 2.0);
				netForce+=Math.pow(values[SensorManager.DATA_Z], 2.0);
				
				if (threshold<(Math.sqrt(netForce)/SensorManager.GRAVITY_EARTH)) {
					isShaking();
				}
				else {
					isNotShaking();
				}
			}
		}
		
		public void onAccuracyChanged(int sensor, int accuracy) {
			// unused
		}
	};
}