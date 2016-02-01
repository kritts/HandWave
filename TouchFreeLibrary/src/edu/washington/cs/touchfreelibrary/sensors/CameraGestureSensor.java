package edu.washington.cs.touchfreelibrary.sensors;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

/**
 * <p><code>CameraGestureSensor</code> takes input data from the camera and uses that to sense
 * four gesture commands: up, down, left, and right.</p>
 * 
 * <p><strong>Important: The static function {@link #loadLibrary()} must be called after
 * OpenCV is initiated and before {@link #start()} is called!</strong></p>
 * 
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 */
public class CameraGestureSensor extends ClickSensor {
	private static final String TAG = "CameraGestureSensor";
	
	/**
	 * To receive messages from CameraGestureSensor, classes must implement the <code>CameraGestureSensor.Listener</code>
	 * interface.
	 * 
	 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
	 */
	public interface Listener {
		/**
		 * Called when an up gesture is triggered
		 * @param caller the CameraGestureSensor object that made the call
		 * @param gestureLength the amount of time the gesture took in milliseconds
		 */
		public void onGestureUp(CameraGestureSensor caller, long gestureLength);
		
		/**
		 * Called when a down gesture is triggered
		 * @param caller the CameraGestureSensor object that made the call
		 * @param gestureLength the amount of time the gesture took in milliseconds
		 */
		public void onGestureDown(CameraGestureSensor caller, long gestureLength);
		
		/**
		 * Called when a left gesture is triggered
		 * @param caller the CameraGestureSensor object that made the call
		 * @param gestureLength the amount of time the gesture took in milliseconds
		 */
		public void onGestureLeft(CameraGestureSensor caller, long gestureLength);
		
		/**
		 * Called when a right gesture is triggered
		 * @param caller the CameraGestureSensor object that made the call
		 * @param gestureLength the amount of time the gesture took in milliseconds
		 */
		public void onGestureRight(CameraGestureSensor caller, long gestureLength);
	}
	
	private enum Direction {
		Left(0), Down(1), Right(2), Up(3), None(4);
		
		private int numVal;
		
		Direction(int numVal) {
			this.numVal = numVal;
		}
		
		public int toInt() {
			return numVal;
		}
	} ;
	
	private List<Listener> mGestureListeners;
	
	private VideoCapture mCamera;
	private int mCameraId;
	private Size mPreviewSize;
	private Thread mFrameProcessor;
	
	private boolean mIsRunning;
	
	private static final double MIN_FRACTION_SCREEN_MOTION = 0.1;
	private final static double MIN_MILLISECONDS_BETWEEN_GESTURES = 500;
	
	private double mMinDirectionalMotionX;
	private double mMinDirectionalMotionY;
	private double mMinGestureLength;
	//private double mWidthToHeight;
	
	private Mat mPreviousFrame;
	private Mat mCurrentFrame;
	
	private Point mStartPos;
	private Point mEndPos;
	
	private boolean mIsHorizontalScrollEnabled;
	private boolean mIsVerticalScrollEnabled;
	private boolean mIsClickByColorEnabled;
	
	private boolean ignoreNext = false;
	
	private Context mContext;

	int frameCount = 0;
	int framesToWaitForGesture = 20;

	String resultCsv = "";
	boolean bufferFilled = false;
	boolean recordGraph = false;
	
	double [] last100Intensities = new double [100];
	double runningIntensityAverage = 0;
	double currentIntensityValue = 120;
	double currentGestureLength = 0;
	
	boolean gestureStartDetected = false;
	boolean gestureEndDetected = false;
	
	MotionDetectionReturnValue mdret = null;
	
	long lastGestureEndTime = 0;
	long lastGestureStartTime = 0;
	long lastClickTime = 0;
	
	/**
	 * To use a <code>CameraGestureSensor</code> object, this must be called some time after 
	 * OpenCV is initiated.
	 */
	static public void loadLibrary() {
		System.loadLibrary("touch_free_library");
	}
	
	// a quick utility function to find the camera id
	int getFrontCameraId() {
		CameraInfo ci = new CameraInfo();
		for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
	        Camera.getCameraInfo(i, ci);
	        if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) 
	        {
	        	return i;
	        }
	    }
	    return 0; // No front-facing camera found
	}
	
	private void setCameraSettings(int cameraId)
	{
		Camera mCam = Camera.open(cameraId);
		Camera.Parameters params = mCam.getParameters();

		params.set("iso", "400"); // values can be "auto", "100", "200", "400", "800", "1600"
		params.setExposureCompensation(2);
		params.setWhiteBalance("fluorescent");
		
		mCam.setParameters(params);
		mCam.release();
	}
	
	/**
	 * Creates a new instance of CameraGestureSensor. Remember to call {@link loadLibrary} 
	 * @param context A functional Context object needed to get the screen rotation.
	 */
	public CameraGestureSensor(Context context) {
		mIsHorizontalScrollEnabled = true;
		mIsVerticalScrollEnabled = true;
		//mIsClickByColorEnabled = false;
		mIsRunning = false;		
		mGestureListeners = new LinkedList<Listener>();
		
		// find the front facing camera id
		mCameraId = getFrontCameraId();
		
		//Try manipulate some settings on the camera
		//setCameraSettings(mCameraId);
		
		mContext = context;
	}
	
	/**
	 * Adds listener to the list of gesture listeners.
	 * @param listener This object will have its call-back methods called when a gesture is recognized
	 */
	public void addGestureListener(Listener listener) {
		mGestureListeners.add(listener);
	}
	
	/**
	 * Removes listener from the list of gesture listeners
	 * @param listener The object will no longer have its call-back mehtods called by this gesture sensor.
	 */
	public void removeGestureListener(Listener listener) {
		mGestureListeners.remove(listener);
	}
	
	/**
	 * Removes all gesture listeners.
	 */
	public void clearGestureListeners() {
		mGestureListeners.clear();
	}
	
	// these methods invoke gesture call backs on all listeners
	private void onGestureUp(long gestureLength) {
		for(Listener l : mGestureListeners) {
			l.onGestureUp(this, gestureLength);
		}
	}
	
	private void onGestureLeft(long gestureLength) {
		for(Listener l : mGestureListeners) {
			l.onGestureLeft(this, gestureLength);
		}
	}
	
	private void onGestureRight(long gestureLength) {
		for(Listener l : mGestureListeners) {
			l.onGestureRight(this, gestureLength);
		}
	}
	
	private void onGestureDown(long gestureLength) {
		for(Listener l : mGestureListeners) {
			l.onGestureDown(this, gestureLength);
		}
	}
	
	/**
	 * Enable/disable horizontal scroll.
	 * @param enabled When true, onGestureLeft/onGestureRight are called, when false, they are not.
	 */
	public void enableHorizontalScroll(boolean enabled) {
		mIsHorizontalScrollEnabled = enabled;
	}
	
	/**
	 * Test if horizontal scroll is enabled.
	 * @return true if horizontal scroll is enabled, false otherwise.
	 */
	public boolean isHorizontalScrollEnabled() {
		return mIsHorizontalScrollEnabled;
	}
	
	/**
	 * Enable/disable vertical scroll.
	 * @param enabled When true, onGestureUp/onGestureDown are called, when false, they are not.
	 */
	public void enableVerticalScroll(boolean enabled) {
		mIsVerticalScrollEnabled = enabled;
	}
	
	/**
	 * Test if vertical scroll is enabled.
	 * @return true if vertical scroll is enabled, false otherwise.
	 */
	public boolean isVerticalScrollEnabled() {
		return mIsVerticalScrollEnabled;
	}
	
	/**
	 * When enabled, an onSensorClick command is sent to any click listeners when a large enough
	 * percentage of the screen goes black.
	 * 
	 * @param enabled Set whether click-by-color is enabled
	 */
	public void enableClickByColor(boolean enabled) {
		mIsClickByColorEnabled = enabled;
	}
	
	/**
	 * Test if click by color is enabled.
	 * @return true if click by color is enabled, false otherwise.
	 */
	public boolean isClickByColorEnabled() {
		return mIsClickByColorEnabled;
	}
	
	/**
	 * <p>Causes this to start reading camera input and looking for gestures. The camera must be available
	 * for this method to be successful.</p>
	 * <p>Warning! CameraGestureSensor will seize control of the front facing camera, even if the activity loses focus.
	 * If you would like to let other applications use the camera, you must call stop() when the activity loses
	 * focus.</p>
	 */
	public void start() {
		if(mIsRunning)
			return;
		
		//mPeakPos = null;
		mStartPos = null;
		mEndPos = null;
		
		if (mCamera != null) {
			VideoCapture camera = mCamera;
			mCamera = null; // Make it null before releasing...
			camera.release();
		}
		
		mCamera = new VideoCapture(mCameraId);
		if(!mCamera.isOpened()) {
			// the camera was not available
			VideoCapture camera = mCamera;
			mCamera = null; // Make it null before releasing...
			camera.release();
			
			return;
		}
		
		List<Size> previewSizes = mCamera.getSupportedPreviewSizes();
		double smallestPreviewSize = 640 * 480; // We should be smaller than this...
		double smallestWidth = 320; // Let's not get smaller than this...
		
		for (Size previewSize : previewSizes) {
			if (previewSize.area() < smallestPreviewSize && previewSize.width >= smallestWidth) {
				mPreviewSize = previewSize;
			}
		}
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mPreviewSize.width);
		mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mPreviewSize.height);
		
		mPreviousFrame = new Mat((int)mPreviewSize.height, (int)mPreviewSize.width, CvType.CV_8U);
		mCurrentFrame = new Mat((int)mPreviewSize.height, (int)mPreviewSize.width, CvType.CV_8U);
  	     
		//w x h = 320 x 240
		mMinDirectionalMotionX = mPreviewSize.width / 5;
  	    mMinDirectionalMotionY = mPreviewSize.height / 5;
  	    mMinGestureLength = 100;
  	    //mWidthToHeight = mPreviewSize.width / mPreviewSize.height;
  	    mIsRunning = true;
  	    
  	    // run the frame processor now
  	    mFrameProcessor = new Thread(mProcessFramesRunnable);
  	    mFrameProcessor.start();
	}
	
	/**
	 * Stops this from looking at camera input for gestures, thus freeing the camera for other uses.
	 */
	public void stop() 
	{	
		if (recordGraph)
		{
			try {
				String filename = Environment.getExternalStorageDirectory().toString() + "/resultCSV.csv";
				writeFile(filename, resultCsv);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!mIsRunning)
			return;
		
		mIsRunning = false;
		if (mCamera != null) {
			synchronized (mProcessFramesRunnable) {
				VideoCapture camera = mCamera;
				mCamera = null; // Make it null before releasing...
				camera.release();
			}
		}
	}
	
	private int adjustDirectionForScreenRotation(Direction d) {
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		int dNum = d.toInt();
		
		switch(display.getRotation()) {
		case Surface.ROTATION_0:
			dNum += 3;
			break;
		case Surface.ROTATION_90:
			break;
		case Surface.ROTATION_180:
			dNum += 1;
			break;
		case Surface.ROTATION_270:
			dNum += 2;
			break;
		}
		
		return dNum % 4;
	}
	
	private boolean isHorScrollAdjustForScreen() {
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		switch(display.getRotation()) {
		case Surface.ROTATION_0:
			return mIsVerticalScrollEnabled;
		case Surface.ROTATION_90:
			return mIsHorizontalScrollEnabled;
		case Surface.ROTATION_180:
			return mIsVerticalScrollEnabled;
		default:
			return mIsHorizontalScrollEnabled;
		}
	}
	
	private boolean isVertScrollAdjustForScreen() {
		Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		switch(display.getRotation()) {
		case Surface.ROTATION_0:
			return mIsHorizontalScrollEnabled;
		case Surface.ROTATION_90:
			return mIsVerticalScrollEnabled;
		case Surface.ROTATION_180:
			return mIsHorizontalScrollEnabled;
		default:
			return mIsVerticalScrollEnabled;
		}
	}
	
	ArrayList <Double> gestureValues = new ArrayList <Double>();
	//ArrayList<Point> gesturePoints = new ArrayList<Point>();
	//ArrayList <Double> gestureFractions = new ArrayList<Double>();
	
	private Runnable mProcessFramesRunnable = new Runnable() 
	{
		@Override
		public void run() 
		{
			while (mCamera != null && mIsRunning) 
			{
				synchronized (this) 
				{
					boolean grabbed = mCamera.grab();
					if(!grabbed)
						continue;
					
					mCamera.retrieve(mCurrentFrame, Highgui.CV_CAP_ANDROID_GREY_FRAME);
					
					currentIntensityValue = Core.mean(mCurrentFrame).val[0];
					last100Intensities[frameCount] = currentIntensityValue;
										
					//KEEP A RUNNING AVERAGE OF THE INTENSITIES OF THE LAST 100 FRAMES
					double runningTotal = 0;
					if ((frameCount < 100) && (!bufferFilled))
					{
						for (int i=0;i<frameCount+1;i++)
						{
							runningTotal += last100Intensities[i];
						}
						runningIntensityAverage = runningTotal/frameCount;
						frameCount++;
					}
					else if (frameCount < 100)
					{
						for (int i=0;i<100;i++)
						{
							runningTotal += last100Intensities[i];
						}
						runningIntensityAverage = runningTotal/100;
						frameCount++;
					}
					
					if (frameCount == 100)
					{
						frameCount = 0;
						bufferFilled = true;
					}
					
					//DETECT GESTURES
					mdret = DetectMovementPosition(mCurrentFrame.getNativeObjAddr(), mPreviousFrame.getNativeObjAddr());
					
					//Are we already in a gesture?
					if (gestureStartDetected)
					{	
						currentGestureLength ++;
						gestureValues.add(currentIntensityValue);
						//gesturePoints.add(mdret.averagePosition);
						//gestureFractions.add(mdret.fractionOfScreenInMotion);
						
						//Are we seeing the end of the gesture?
						if (mdret.fractionOfScreenInMotion < MIN_FRACTION_SCREEN_MOTION)
						{
							//This is now the last gesture so record the time
							lastGestureEndTime = System.currentTimeMillis();							
							gestureEndDetected = true;
							mEndPos = mdret.averagePosition;	

						}
						
						//If the gesture lasts too long without finishing then ignore it
						if (currentGestureLength > framesToWaitForGesture)
						{
							Log.e("CameraGestureSensor", "GESTURE TIMED OUT " + currentGestureLength);
							
							//Reset everything
							mStartPos = null;
							mEndPos = null;
							
							gestureStartDetected = false;
							gestureEndDetected = false;
							currentGestureLength = 0;
						}
					}
					//Figure out if we need to start a gesture
					else
					{
						//Are we seeing a spike in intensity? 
						if (mdret.fractionOfScreenInMotion > MIN_FRACTION_SCREEN_MOTION)
						{				
							//Enforce a certain amount of time between gestures
							long time = System.currentTimeMillis();
							if (time - lastGestureEndTime > MIN_MILLISECONDS_BETWEEN_GESTURES)
							{
								lastGestureStartTime = time;
								mStartPos = mdret.averagePosition;
								gestureStartDetected = true;
								currentGestureLength ++;
								
								gestureValues = new ArrayList<Double>();
								gestureValues.add(currentIntensityValue);
								/*gesturePoints = new ArrayList<Point>();
								gesturePoints.add(mdret.averagePosition);
								gestureFractions = new ArrayList<Double>();
								gestureFractions.add(mdret.fractionOfScreenInMotion);*/
								
								if (time-lastClickTime > 1.5*MIN_MILLISECONDS_BETWEEN_GESTURES)
								{
									ignoreNext = false;
								}
							}
						}
					}
					
					// Did we find a gesture?
					if (gestureEndDetected)
					{
						//Ignore a gesture if it comes right after a click
						if (ignoreNext) 
						{
							Log.e("", "IGNORED");
							ignoreNext = false;
						}
						
						else
						{
							long gestureLength = lastGestureEndTime - lastGestureStartTime;	
							
							//Figure out if it's a gesture or a click
							double minValue = runningIntensityAverage;
							double maxValue = runningIntensityAverage;
							int minIndex = 0, maxIndex = 0;
								
							for (int i=0;i<gestureValues.size();i++)
							{
								double thisValue = gestureValues.get(i);
								
								//Figure out max and min values
								if (thisValue < minValue)
								{
									minValue = thisValue;
									minIndex = i;
								}
								if (thisValue > maxValue)
								{
									maxValue = thisValue;
									maxIndex = i;
								}
								
							}
							
							double peakToPeak = maxIndex-minIndex;
							double amountAbove = maxValue - runningIntensityAverage;
							
							Log.e("Detected", "peakToPeak:" + peakToPeak + " length:" + gestureLength + " amountAbove:" + amountAbove);
							
							if ((peakToPeak < -1 && amountAbove == 0) || (peakToPeak < -3 && amountAbove < 10))
							{
								lastClickTime = lastGestureEndTime;
								Log.e("CLICK", "CLICK");
								onSensorClick();
								ignoreNext = true;
							}
							else
							{							
								Direction movementDirection = getGestureDirection(gestureLength);
								if (movementDirection == Direction.None)
									Log.e("", "NO DIRECTION!");
								
								// see if we should call a callback based on movementDirection
								if(mGestureListeners.size() != 0 && movementDirection != Direction.None) 
								{					
									int adjustedDirection = adjustDirectionForScreenRotation(movementDirection);
	
									if(adjustedDirection == Direction.Left.toInt())
									{	
										//Log.e("LEFT", "LEFT");
										onGestureLeft(gestureLength);
									}
									else if(adjustedDirection == Direction.Right.toInt())
									{
										//Log.e("RIGHT", "RIGHT");
										onGestureRight(gestureLength);
									}
									else if(adjustedDirection == Direction.Up.toInt())
									{
										//Log.e("UP", "UP");
										onGestureUp(gestureLength);
									}
									else if(adjustedDirection == Direction.Down.toInt())
									{
										//Log.e("DOWN", "DOWN");
										onGestureDown(gestureLength);
									}
								}
							}
						}
						
						//Reset everything
						mStartPos = null;
						mEndPos = null;
						
						gestureStartDetected = false;
						gestureEndDetected = false;
						currentGestureLength = 0;
					}
	
					//Record data so we can make pretty graphs of gestures
					//double perCentFraction = mdret.fractionOfScreenInMotion*100;
					//resultCsv += currentIntensityValue + "," + perCentFraction + ",\n";
					
					// set the previous frame to the current frame
					mCurrentFrame.copyTo(mPreviousFrame);
				}
			}
		}

		private Direction getGestureDirection(double gestureLength)
		{
			Direction movementDirection = Direction.None;
			
            double diffY = mEndPos.y - mStartPos.y;
            double diffX = mEndPos.x - mStartPos.x;
            
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > mMinDirectionalMotionX && Math.abs(gestureLength) > mMinGestureLength) 
                {
                    if (diffX > 0) 
                    {
                    	movementDirection = Direction.Left;
                    } 
                    else 
                    {
                    	movementDirection = Direction.Right;
                    }
                }
            } 
            else 
            {
                if (Math.abs(diffY) > mMinDirectionalMotionY && Math.abs(gestureLength) > mMinGestureLength) 
                {
                    if (diffY > 0) 
                    {
                    	movementDirection = Direction.Down;
                    } 
                    else 
                    {
                    	movementDirection = Direction.Up;
                    }
                }
            }
			
            /*
			if(isHorScrollAdjustForScreen()) 
			{	
				if(mEndPos.x - mStartPos.x > mMinDirectionalMotionX) 
				{
					movementDirection = Direction.Left;
					//Log.e("ZERO", "LEFT");
				}
				else if(mStartPos.x - mEndPos.x > mMinDirectionalMotionX) {
					movementDirection = Direction.Right;
					//Log.e("ZERO", "RIGHT");
				}
			}
			if(isVertScrollAdjustForScreen()) 
			{
				double verticalMotion = Math.abs(mEndPos.y - mStartPos.y);
				if(verticalMotion > mMinDirectionalMotionY) {
					if(movementDirection == Direction.None || verticalMotion * mWidthToHeight > Math.abs(mEndPos.x - mStartPos.x) ) {
						if(mEndPos.y < mStartPos.y)
						{	
							movementDirection = Direction.Up;
							//Log.e("ZERO", "UP");
						}
						else
						{
							movementDirection = Direction.Down;
							//Log.e("ZERO", "DOWN");
						}
					}
				}
			}
            */
            
			return movementDirection;
		}
				
	};
	
	/**
	 * If ClickByColor is enabled, then when the mean color of the pixels is below c, register a click.
	 * @param c the maximum average color of the pixels received by the camera for a click to be registered
	 */
	public void setAverageColorMaxForClick(double c) {
		//No longer necessary
	}
	
	private native MotionDetectionReturnValue DetectMovementPosition(long currentFrame, long previousFrame);
	
	//Adding capability to try and see if we can differentiate clicks from gestures
	public static void writeFile(String filename, String text) throws IOException 
	{
	    Writer out = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");
	    try {
	      out.write(text);
	    }
	    finally {
	      out.close();
	    }
	}
}