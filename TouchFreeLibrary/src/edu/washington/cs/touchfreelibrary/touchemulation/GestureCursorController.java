package edu.washington.cs.touchfreelibrary.touchemulation;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import edu.washington.cs.touchfreelibrary.sensors.ClickSensor;
import edu.washington.cs.touchfreelibrary.sensors.CameraGestureSensor;

/**
 * <p>Class <code>GestureCursorController</code> can be used to create the visual and functionality of a
 * cursor that is controlled via gestures. To use this class, one must create a {@link CameraGestureSensor}
 * object and a {@link ClickSensor} object and set <code>this</code> as a listener to both.</p>
 * 
 * <p>For an object of this class to be functional, the view returned by {@link #getView()} must attached to
 * a parent object and drawn on the screen somewhere. You can use {@link #attachToActivity(Activity)} to
 * easily attach this to an <code>Activity</code>. Clicks will be registered to whatever is below the view.</p>
 * 
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 *
 */
public class GestureCursorController implements CameraGestureSensor.Listener, ClickSensor.Listener {
	protected static final String TAG = "GestureCursorController";
	
	private static final int DEFAULT_CURSOR_RADIUS = 20;
	
	private static final int MILLISECONDS_PER_FRAME = 7;
	private static final int NUM_CLICK_FRAMES = 50;
	
	private GestureCursorView mView;
	
	private Point mSize;
	private Point mPosition;
	private Point mVelocity;
	
	private int mCursorRadius;
	
	private boolean mIsRunning;
	
	private boolean mDisableInjection;
	
	private int mClickCounter;
	
	private Instrumentation mInstrumentation;
	
	private long mMinimumGestureLength;
	
	/** This click sensor sets the cursors velocity to 0 instead of triggering a click */
	private ClickSensor mStopClickSensor;
	
	/** create the animation timer */
	private class AnimationTimerTask extends TimerTask {
		@Override
		public void run() {
			synchronized(GestureCursorController.this) {
				if(mVelocity.x != 0 || mVelocity.y != 0 || mClickCounter != 0)
					mView.postInvalidate();
				
				if(mSize.x == 0 || mSize.y == 0) {
					mPosition.x = mPosition.y = 0;
					mVelocity.x = mVelocity.y = 0;
				} else {
					if(mPosition.x < 0) {
						mPosition.x = 0;
						mVelocity.x = 0;
					}
					else if(mPosition.x >= mSize.x) {
						mPosition.x = mSize.x-1;
						mVelocity.x = 0;
					}
					
					if(mPosition.y < 0) {
						mPosition.y = 0;
						mVelocity.y = 0;
					}
					else if(mPosition.y >= mSize.y) {
						mPosition.y = mSize.y - 1;
						mVelocity.y = 0;
					}
					
					mPosition.x += mVelocity.x;
					mPosition.y += mVelocity.y;
				}
				
				if(mClickCounter > 0) {
					mClickCounter--;
				}
				
			}
		}
    };
    private Timer mAnimationTimer;
	
    /**
     * Creates a new <code>GestureCursorController</code> object.
     * @param context A context from the application.
     */
    public GestureCursorController(Context context) {
        mView = new GestureCursorView(context);
        
        mSize = new Point(0, 0);
        
        mPosition = new Point(0, 0);
        mVelocity = new Point(0, 0);
        
        mClickCounter = 0;
        
        mCursorRadius = DEFAULT_CURSOR_RADIUS;
        
        mIsRunning = false;
        
        mInstrumentation = new Instrumentation();
        
        mStopClickSensor = null;
        
        mDisableInjection = false;
        
        mMinimumGestureLength = 0;
    }
    
    /**
     * Starts this instance of <code>GestureCursorController</code>, meaning the cursor will be drawn
     * onto the view, and clicks will be sent to the application below.
     */
    public synchronized void start() {
    	if(!mIsRunning) {
	    	mIsRunning = true;
	    	mAnimationTimer = new Timer();
	        mAnimationTimer.schedule(new AnimationTimerTask(), 0, MILLISECONDS_PER_FRAME);
	        
	        mPosition = new Point(mSize.x / 2, mSize.y / 2);
	        mVelocity = new Point(0, 0);
	        mClickCounter = 0;
    	}
    }

    /**
     * Stops the instance from acting as a cursor, which will no longer be drawn on the view.
     */
    public synchronized void stop() {
    	if(mIsRunning) {
			mAnimationTimer.cancel();
			mIsRunning = false;
    	}
    }
    
    /**
     * Set the color of the cursor when not clicking.
     * @param c the color-int that the cursor will be set to
     */
    public void setIdleColor(int c) {
		mView.mNormalPaint.setColor(c);
	}
	
    /**
     * Set the color of the cursor when clicking.
     * @param c the color-int that the cursor will be set to when clicking
     */
	public void setClickColor(int c) {
		mView.mClickPaint.setColor(c);
	}
	
	/**
	 * Set the radius of the cursor
	 * @param r the radius of the cursor
	 */
	public void setCursorRadius(int r) {
		mCursorRadius = r;
	}
	
	/**
	 * Returns the radius of the cursor.
	 * @return the radius of the cursor
	 */
	public int getCursorRadius() {
		return mCursorRadius;
	}
	
	/**
	 * @return the view that contains the cursor (clicking won't work unless the view is attached to something)
	 */
	public View getView() {
		return mView;
	}
	
	/**
	 * Sets whether or not injection will be disabled.
	 * @param disableInjection if true, injection will not occur. If false, it will occur as per usual.
	 */
	public void setDisableInjection(boolean disableInjection) {
		mDisableInjection = disableInjection;
	}
	
	/**
	 * Sets the minimum amount of time, in milliseconds, a gesture has to be to be registered.
	 * @param clickLength Gestures shorter than clickLength milliseconds will be ignored.
	 */
	public void setMinimumGestureLength(long gestureLength) {
		mMinimumGestureLength = gestureLength;
	}
	
	/**
	 * Set the view stored in GestureCursorController to draw on top of a given <code>Activity</code>
	 * @param activity the <code>Activity</code> that will get this <code>GestureCursorController</code> attached
	 * to it.
	 */
	public void attachToActivity(Activity activity) {
		activity.addContentView(mView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mView.bringToFront();
		mView.setVisibility(View.VISIBLE);
		mView.setWillNotDraw(false);
	}
	
	/**
	 * Removes the cursor from its parent.
	 */
	public void removeFromParent() {
		ViewGroup vg = (ViewGroup)(getView().getParent());
		vg.removeView(getView());
	}
	
	/**
	 * Get the position of the cursor in the view's coordinate system.
	 * @return the position of the cursor in the view's space
	 */
	public Point getPositionInViewSpace() {
		return new Point(mPosition.x, mPosition.y);
	}
	
	
	
	/**
	 * Get the position of the cursor in the screen's coordinate system
	 * @return the position of the cursor in screen space
	 */
	public Point getPositionInScreenSpace() {
		Point screenPos = new Point(mPosition.x, mPosition.y);
		int [] screenCoords = new int[2];
		mView.getLocationOnScreen(screenCoords);
		
		screenPos.x += screenCoords[0];
		screenPos.y += screenCoords[1];
		
		return screenPos;
	}
	
	/**
	 * Assign one click sensor to set the cursor's velocity to 0 as opposed to actually triggering
	 * a tap.
	 * @param stopSensor The click sensor that will cause the cursor to stop. This can be set to
	 * null if no stop click sensor is wanted.
	 */
	public void setStopClickSensor(ClickSensor stopSensor) {
		mStopClickSensor = stopSensor;
	}
    
	private class GestureCursorView extends ViewGroup {
    	public Paint mNormalPaint;
    	public Paint mClickPaint;

	    public GestureCursorView(Context context) {
	    	super(context);

	    	mNormalPaint = new Paint();
	    	mNormalPaint.setAntiAlias(true);
	    	mNormalPaint.setARGB(255, 255, 0, 0);
	    	
	    	mClickPaint = new Paint();
	    	mClickPaint.setAntiAlias(true);
	    	mClickPaint.setARGB(255, 0, 255, 0);
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	    	synchronized(GestureCursorController.this) {
	    		super.onDraw(canvas);
	    		if(mClickCounter == 0)
	    			canvas.drawCircle(mPosition.x, mPosition.y, mCursorRadius, mNormalPaint);
	    		else 
	    			canvas.drawCircle(mPosition.x, mPosition.y, mCursorRadius, mClickPaint);
	    	}
	    }

	    @Override
	    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
	    	if(changed) {
		    	mSize.x = right - left; mSize.y = bottom - top;
		        mPosition.x = mSize.x / 2; mPosition.y = mSize.y / 2;
		        mVelocity.x = mVelocity.y = 0;
	    	}
	    }
	    
	    @Override
	    public boolean onInterceptTouchEvent (MotionEvent ev) {
	    	onTouchEvent(ev);
	    	return false;
	    }
    }

	@Override
	public synchronized void onGestureUp(CameraGestureSensor caller, long gestureLength) {
		if(mClickCounter == 0 && gestureLength > mMinimumGestureLength) {
			mVelocity.x = 0;
			if(mVelocity.y > 0)
				mVelocity.y = 0;
			else
				mVelocity.y -= 1;
		}
	}

	@Override
	public synchronized void onGestureDown(CameraGestureSensor caller, long gestureLength) {
		if(mClickCounter == 0 && gestureLength > mMinimumGestureLength) {
			mVelocity.x = 0;
			if(mVelocity.y < 0)
				mVelocity.y = 0;
			else
				mVelocity.y += 1;
		}
	}

	@Override
	public synchronized void onGestureLeft(CameraGestureSensor caller, long gestureLength) {
		if(mClickCounter == 0 && gestureLength > mMinimumGestureLength) {
			mVelocity.y = 0;
			if(mVelocity.x > 0)
				mVelocity.x = 0;
			else
				mVelocity.x -= 1;
		}
	}

	@Override
	public synchronized void onGestureRight(CameraGestureSensor caller, long gestureLength) {
		if(mClickCounter == 0 && gestureLength > mMinimumGestureLength) {
			mVelocity.y = 0;
			if(mVelocity.x < 0)
				mVelocity.x = 0;
			else
				mVelocity.x += 1;
		}
	}

	@Override
	public void onSensorClick(ClickSensor caller) {
		mVelocity.x = mVelocity.y = 0;
		
		if(caller != mStopClickSensor) {
			if(mDisableInjection)
				mClickCounter = NUM_CLICK_FRAMES;
			else
				new Thread(mInjectTapSequence).start();
		}
	}
	
	private Runnable mInjectTapSequence = new Runnable() {
		@Override
		public void run() {
			Point screenPos = getPositionInScreenSpace();
			MotionEvent downAction = MotionEvent.obtain(
					SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_DOWN, screenPos.x, screenPos.y, 0);
			MotionEvent upAction = MotionEvent.obtain(
					SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_UP, screenPos.x, screenPos.y, 0);
			
			try {
				mInstrumentation.sendPointerSync(downAction);
				mInstrumentation.sendPointerSync(upAction);
				mClickCounter = NUM_CLICK_FRAMES;
			} catch (SecurityException e) {
				// security exception occurred, but we can pretty much ignore it.
			}
			
			downAction.recycle();
			upAction.recycle();
		}
	};
}
