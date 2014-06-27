package edu.washington.cs.touchfreelibrary.touchemulation;

import android.app.Instrumentation;
import android.graphics.Point;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import edu.washington.cs.touchfreelibrary.sensors.CameraGestureSensor;

/**
 * Class <code>GestureScroller</code> emulates the swipe-to-scroll touch-screen
 * gesture with {@link CameraGestureSensor}'s gestures. Users of this class must set
 * <code>this</code> as a Listener to an external <code>GestureSensor</code>
 * class.
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 */
public class GestureScroller implements CameraGestureSensor.Listener {
	// Points used for horizontal scroll:
	private Point mLeftPoint;
	private Point mRightPoint;
	
	// Points used for vertical scroll:
	private Point mTopPoint;
	private Point mBottomPoint;
	
	private boolean mVerticalScrollEnabled;
	private boolean mHorizontalScrollEnabled;
	
	private boolean mInvertVerticalScroll;
	private boolean mInvertHorizontalScroll;
	
	private Instrumentation mInstrumentation;
	
	private boolean mIsRunning;
	
	private boolean mCancelMotion;
	
	/**
	 * Creates a new instance of <code>GestureScroller</code>. In order to function, this must be
	 * set as a listener to a {@link CameraGestureSensor} object. Also, {@link #start()} must be called to
	 * activate.
	 */
	public GestureScroller() {
		mLeftPoint = new Point(-1, -1);
		mRightPoint = new Point(-1, -1);
		mTopPoint = new Point(-1, -1);
		mBottomPoint = new Point(-1, -1);
		
		mVerticalScrollEnabled = true;
		mHorizontalScrollEnabled = true;
		
		mInvertVerticalScroll = false;
		mInvertHorizontalScroll = false;
		
		mInstrumentation = new Instrumentation();
		mIsRunning = false;
		
		mCancelMotion = false;
	}
	
	/**
     * Activates this instance of <code>GestureScroller</code>.
     */
    public void start() {
    	mIsRunning = true;
    }

    /**
     * Stops the instance from acting as a cursor, which will no longer be drawn on the view.
     */
    public void stop() {
    	mIsRunning = false;
    }
    
    /**
     * Get whether or not this instance is currently activated
     * @return true if this is running, false otherwise.
     */
    public boolean isRunning() {
    	return mIsRunning;
    }
	
	/**
	 * Set whether this scroller responds to vertical scrolling commands.
	 * @param enabled true if vertical scrolling should be enabled, false otherwise.
	 */
	public void setVerticalScrollEnabled(boolean enabled) {
		mVerticalScrollEnabled = enabled;
	}
	
	/**
	 * Get whether or not this scroller responds to vertical scrolling commands.
	 * @return true if vertical scrolling is enabled, false otherwise.
	 */
	public boolean getVerticalScrollEnabled() {
		return mVerticalScrollEnabled;
	}
	
	/**
	 * Set whether this scroller responds to horizontal scrolling commands.
	 * @param enabled true if horizontal scrolling should be enabled, false otherwise.
	 */
	public void setHorizontalScrollEnabled(boolean enabled) {
		mHorizontalScrollEnabled = enabled;
	}
	
	/**
	 * Get whether or not this scroller responds to horizontal scrolling commands.
	 * @return true if horizontal scrolling is enabled, false otherwise.
	 */
	public boolean getHorizontalScrollEnabled() {
		return mHorizontalScrollEnabled;
	}
	
	/**
	 * Set whether this scroller inverts vertical scrolling commands.
	 * @param inverted true if vertical scrolling should be inverted, false otherwise.
	 */
	public void setInvertVerticalScroll(boolean inverted) {
		mInvertVerticalScroll = inverted;
	}
	
	/**
	 * Get whether or not this scroller inverts vertical scrolling commands.
	 * @return true if vertical scrolling is inverted, false otherwise.
	 */
	public boolean getInvertVerticalScroll() {
		return mInvertVerticalScroll;
	}
	
	/**
	 * Set whether this scroller inverts horizontal scrolling commands.
	 * @param inverted true if horizontal scrolling should be inverted, false otherwise.
	 */
	public void setInvertHorizontalScroll(boolean inverted) {
		mInvertHorizontalScroll = inverted;
	}
	
	/**
	 * Get whether or not this scroller inverts vertical scrolling commands.
	 * @return true if vertical horizontal is inverted, false otherwise.
	 */
	public boolean getInvertHorizontalScroll() {
		return mInvertHorizontalScroll;
	}
	
	/**
	 * Sets the position, in screen space, of the left point that is used in horizontal scrolling
	 * @param x the x coordinate, in screen space
	 * @param y the y coordinate, in screen space
	 */
	public void setLeftPosition(int x, int y) {
		mLeftPoint.x = x;
		mLeftPoint.y = y;
	}
	
	/**
	 * Sets the position, in screen space, of the right point that is used in horizontal scrolling
	 * @param x the x coordinate, in screen space
	 * @param y the y coordinate, in screen space
	 */
	public void setRightPosition(int x, int y) {
		mRightPoint.x = x;
		mRightPoint.y = y;
	}
	
	/**
	 * Sets the position, in screen space, of the top point that is used in vertical scrolling
	 * @param x the x coordinate, in screen space
	 * @param y the y coordinate, in screen space
	 */
	public void setTopPosition(int x, int y) {
		mTopPoint.x = x;
		mTopPoint.y = y;
	}
	
	/**
	 * Sets the position, in screen space, of the bottom point that is used in vertical scrolling
	 * @param x the x coordinate, in screen space
	 * @param y the y coordinate, in screen space
	 */
	public void setBottomPosition(int x, int y) {
		mBottomPoint.x = x;
		mBottomPoint.y = y;
	}
	
	/**
	 * Sets the top and bottom points based on a view. The view must already be placed
	 * somewhere on the screen for this method to work successfully.
	 * @param view The view that will be used to set the scrolling points.
	 * @param margins The number of pixels within the view that the points will be offset by
	 * vertically.
	 */
	public void setVerticalPointsWithView(View view, int margins) {
		int [] viewScreenCoords = new int[2];
		view.getLocationOnScreen(viewScreenCoords);
		int xPos = view.getWidth() / 2 + viewScreenCoords[0];

		setTopPosition(xPos, viewScreenCoords[1] + margins);
		setBottomPosition(xPos, viewScreenCoords[1] + view.getHeight() - margins);
	}
	
	/**
	 * Sets the left and right points based on a view. The view must already be placed
	 * somewhere on the screen for this method to work successfully.
	 * @param view The view that will be used to set the scrolling points.
	 * @param margins The number of pixels within the view that the points will be offset by
	 * horizontally.
	 */
	public void setHorizontalPointsWithView(View view, int margins) {
		int [] viewScreenCoords = new int[2];
		view.getLocationOnScreen(viewScreenCoords);
		int yPos = view.getHeight() / 2 + viewScreenCoords[1];

		setLeftPosition(viewScreenCoords[0] + margins, yPos);
		setRightPosition(viewScreenCoords[0] + view.getWidth() - margins, yPos);
	}
	
	/**  Called when an upwards gesture is detected - scrolls upwards. 	 */
	@Override
	public void onGestureUp(CameraGestureSensor caller, long gestureLength) {
		if(mVerticalScrollEnabled && mIsRunning && mTopPoint.x >= 0 && mBottomPoint.x >= 0) {
			if(!mInvertVerticalScroll)
				sendCursorDragEvent(mTopPoint, mBottomPoint, (int)(gestureLength / 4));
			else
				sendCursorDragEvent(mBottomPoint, mTopPoint, (int)(gestureLength / 4));
		}
	}

	/** Called when a downwards gesture is detected - scrolls downwards. */
	@Override
	public void onGestureDown(CameraGestureSensor caller, long gestureLength) {
		if(mVerticalScrollEnabled && mIsRunning && mTopPoint.x >= 0 && mBottomPoint.x >= 0) {
			if(!mInvertVerticalScroll)
				sendCursorDragEvent(mBottomPoint, mTopPoint, (int)(gestureLength / 4));
			else
				sendCursorDragEvent(mTopPoint, mBottomPoint, (int)(gestureLength / 4));
		}
	}

	/** Called when a leftwards gesture is detected - scrolls to the left. */
	@Override
	public void onGestureLeft(CameraGestureSensor caller, long gestureLength) {
		if(mHorizontalScrollEnabled && mIsRunning && mLeftPoint.x >= 0 && mRightPoint.x >= 0) {
			if(!mInvertHorizontalScroll)
				sendCursorDragEvent(mLeftPoint, mRightPoint, (int)(gestureLength / 6));
			else
				sendCursorDragEvent(mRightPoint, mLeftPoint, (int)(gestureLength / 6));
		}
	}
	
	/** Called when a rightwards gesture is detected - scrolls right. */
	@Override
	public void onGestureRight(CameraGestureSensor caller, long gestureLength) {
		if(mHorizontalScrollEnabled && mIsRunning && mLeftPoint.x >= 0 && mRightPoint.x >= 0) {
			if(!mInvertHorizontalScroll)
				sendCursorDragEvent(mRightPoint, mLeftPoint, (int)(gestureLength / 6));
			else
				sendCursorDragEvent(mLeftPoint, mRightPoint, (int)(gestureLength / 6));
		}
	}
	
	/** Uses a simplified case of a cubic Hermite spline to smoothly calculate the position */
	private Point calculateSplinePosition(Point p1, Point p2, int time, int steps) {
		double percentDone = (double)time / (double)steps;
		
		double s = 3 * percentDone * percentDone - 2 * percentDone * percentDone * percentDone;
		return new Point((int)((1 - s) * p1.x + s * p2.x), (int)((1 - s) * p1.y + s * p2.y));
	}

	/** Invokes a fake drag using instrumentation. p1 and p2 are in screen space */
	private void sendCursorDragEvent(final Point p1, final Point p2, final int stepCount) {
		// run the touch event
		new Thread() {
			@Override
			public void run() {
				Point p = p1;
				
				long downTime = SystemClock.uptimeMillis();
				
				MotionEvent event = null;
				
				synchronized(GestureScroller.this) {
					try {
						event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, p.x, p.y, 0);
						mInstrumentation.sendPointerSync(event);
						event.recycle();
						
						for (int i = 0; i < stepCount; i++) {
							p = calculateSplinePosition(p1, p2, i, stepCount);
							
							event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, p.x, p.y, 0);
							mInstrumentation.sendPointerSync(event);
							event.recycle();
							
							if(mCancelMotion) break;
						}
						
						event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, p.x, p.y, 0);
						mInstrumentation.sendPointerSync(event);
						event.recycle();
					} catch (SecurityException e) {
						// security exception occurred, but we can pretty much ignore it.
					}
					mCancelMotion = false;
				}
			}
		}.start();
	}
}
