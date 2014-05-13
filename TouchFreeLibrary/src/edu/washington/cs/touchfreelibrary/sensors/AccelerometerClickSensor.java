package edu.washington.cs.touchfreelibrary.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class <code>AccelerometerClickSensor</code> looks for sudden perturbations in the z-axis of the
 * accelerometer to detect clicks. Currently, the phone must be perfectly level rotation-wise for this to work well.
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 *
 */
public class AccelerometerClickSensor extends ClickSensor {
	
	private boolean mIsStarted;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	private static final double MAX_ACCELERATIONX_FOR_CLICK = 0.35;
	private static final double MIN_ACCELERATIONX_FOR_CLICK = -0.35;
	
	private static final double MAX_ACCELERATIONY_FOR_CLICK = 0.35;
	private static final double MIN_ACCELERATIONY_FOR_CLICK = -0.35;
	
	private static final double MAX_ACCELERATIONZ_FOR_CLICK = 8.5;
	private static final double MIN_ACCELERATIONZ_FOR_CLICK = -8.5;
	private static final int BREAK_TIME = 10;
	
	private int mBreakTimer;
	
	/**
	 * Creates a new <code>AccelerometerClickSensor</code>.
	 * @param context A valid context is required to fetch data from the accelerometer.
	 */
	public AccelerometerClickSensor(Context context) {
		mIsStarted = false;
		mBreakTimer = 0;
		
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	/**
	 * Causes this to start retrieving data from the accelerometer and sending <code>onSesnorClick()</code>
	 * messages to any listeners.
	 */
	public void start() {
		if(!mIsStarted) {
			mIsStarted = true;
			mSensorManager.registerListener(eventListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	/**
	 * Stop retrieving data from the accelerometer and sending <code>onSesnorClick()</code> to listeners.
	 */
	public void stop() {
		if(mIsStarted) {
			mIsStarted = false;
			mSensorManager.unregisterListener(eventListener);
		}
	}
	
	/**
	 * Check whether this is looking at sensor data.
	 * @return true if this is looking at sensor data, false otherwise.
	 */
	public boolean isStarted() {
		return mIsStarted;
	}

	private SensorEventListener eventListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	
		@Override
		public void onSensorChanged(SensorEvent event) {
			// get the acceleration coordinates
			float ax = event.values[0];
			float ay = event.values[1];
			float az = event.values[2];
			
			if(mBreakTimer == 0) {
				if(ax < MAX_ACCELERATIONX_FOR_CLICK && ay < MAX_ACCELERATIONY_FOR_CLICK && az < MAX_ACCELERATIONZ_FOR_CLICK &&
				   ax > MIN_ACCELERATIONX_FOR_CLICK && ay > MIN_ACCELERATIONY_FOR_CLICK && az > MIN_ACCELERATIONZ_FOR_CLICK) {
					onSensorClick();
					mBreakTimer = BREAK_TIME;
				}
			} else mBreakTimer--;
		}
	};
}
