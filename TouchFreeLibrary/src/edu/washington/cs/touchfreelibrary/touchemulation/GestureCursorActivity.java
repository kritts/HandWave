package edu.washington.cs.touchfreelibrary.touchemulation;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import edu.washington.cs.touchfreelibrary.sensors.AccelerometerClickSensor;
import edu.washington.cs.touchfreelibrary.sensors.CameraGestureSensor;
import edu.washington.cs.touchfreelibrary.sensors.ClickSensor;
import edu.washington.cs.touchfreelibrary.sensors.MicrophoneClickSensor;
import android.app.Activity;

/**
 * This class provides the simplest way to add a touch-free interface to an activity. Simply
 * extend your activity with <code>GestureCursorActivity</code> and call
 * {@link #initializeTouchFree(ClickSensorType)} in your <code>onCreate</code> method, and
 * you will get a touch-free cursor to play with.
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 *
 */
public abstract class GestureCursorActivity extends Activity {
	/**
	 * Defines the three types of click sensors that can be used:
	 * <code>Microphone</code>, <code>Accelerometer</code>, and <code>Camera</code>.
	 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
	 */
	public enum ClickSensorType {
		Microphone, Accelerometer, Camera
	}
	
	private CameraGestureSensor mGestureSensor;
	private ClickSensor mClickSensor;
	private GestureCursorController mCursor;
	
	private boolean mOpenCVInitiated = false;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					mOpenCVInitiated = true;
					
					CameraGestureSensor.loadLibrary();
					mGestureSensor.start();
					mClickSensor.start();
					mCursor.start();
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};
	
	/**
	 * This method should be called to enable the cursor.
	 * @param clickSensorType the type of click sensor to be used.
	 */
	protected void initializeTouchFree(ClickSensorType clickSensorType) {
		mGestureSensor = new CameraGestureSensor(this);
		mCursor = new GestureCursorController(this);
		switch(clickSensorType) {
			case Microphone:
				mClickSensor = new MicrophoneClickSensor();
				break;
			case Accelerometer:
				mClickSensor = new AccelerometerClickSensor(this);
				break;
			case Camera:
				mClickSensor = mGestureSensor;
				mGestureSensor.enableClickByColor(true);
				break;
		}
		
		mGestureSensor.addGestureListener(mCursor);
		mClickSensor.addClickListener(mCursor);
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

		mCursor.attachToActivity(this);
	}

	/**
	 * If overriding, make sure to call <code>super.onWindowFocusChanged(hasFocus)</code>.
	 */
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
		if(!mOpenCVInitiated)
			return;
		
		if(hasFocus) {
			mGestureSensor.start();
			if(mGestureSensor != mClickSensor)
				mClickSensor.start();
		}
		else {
			mGestureSensor.stop();
			if(mGestureSensor != mClickSensor)
				mClickSensor.stop();
		}
	}
	
	/**
	 * If overriding, make sure to call <code>super.onResume()</code>.
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		if(!mOpenCVInitiated)
			return;
		
		mGestureSensor.start();
		if(mGestureSensor != mClickSensor)
			mClickSensor.start();
	}
	
	
	/**
	 * If overriding, make sure to call <code>super.onPause()</code>.
	 */
	@Override
	public void onPause() {
		super.onPause();
		
		if(!mOpenCVInitiated)
			return;
		
		mGestureSensor.stop();
		if(mGestureSensor != mClickSensor)
			mClickSensor.stop();
	}
	
	/**
	 * If overriding, make sure to call <code>super.onDestroy()</code>.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(!mOpenCVInitiated)
			return;
		
		mGestureSensor.stop();
		mClickSensor.stop();
		mCursor.stop();
	}
}
