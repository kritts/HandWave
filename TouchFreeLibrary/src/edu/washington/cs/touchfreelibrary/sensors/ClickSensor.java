package edu.washington.cs.touchfreelibrary.sensors;

import java.util.LinkedList;
import java.util.List;

/**
 * Class <code>ClickSensor</code> is an abstract base class to all other click
 * sensors. It defines a list of click listeners, as well as the basic methods
 * used by outsiders to add and remove listeners. Classes derived from <code>
 * ClickSensor</code> may use the protected {@link #onSensorClick()}
 * to call all the listeners at once.
 * 
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 *
 */
public abstract class ClickSensor {
	/**
	 * Listener for classes that derive from ClickSensor.
	 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
	 */
	public interface Listener {
		/**
		 * Called by a {@link ClickSensor} when a click is sensed.
		 * @param caller The <code>ClickSensor</code> that called the click.
		 */
		public void onSensorClick(ClickSensor caller);
	}
	
	private List<Listener> mListeners;
	
	/**
	 * <code>ClickSensor</code>'s constructor. All derived classes are required to
	 * call this.
	 */
	protected ClickSensor() {
		mListeners = new LinkedList<Listener>();
	}
	
	/**
	 * Adds a listener whose onSensorClick method will be called when a click is
	 * perceived.
	 * @param listener the <code>ClickSensor.Listener</code> to be added
	 */
	public void addClickListener(Listener listener) {
		mListeners.add(listener);
	}
	
	/**
	 * Removes a listener so it will no longer be called when a click is perceived.
	 * @param listener the <code>ClickSensor.Listener</code> to be removed
	 */
	public void removeClickListener(Listener listener) {
		mListeners.remove(listener);
	}
	
	/**
	 * Returns true if <code>listener</code> is currently listening for clicks
	 * from <code>this</code>.
	 * @param listener the <code>ClickSensor.Listener</code> who we are checking for local listener status
	 * @return
	 */
	public boolean isClickListener(Listener listener) {
		return mListeners.contains(listener);
	}
	
	/**
	 * Removes all listeners from <code>this</code>.
	 */
	public void clearClickListeners() {
		mListeners.clear();
	}
	
	/**
	 * Causes this <code>ClickSensor</code> to start listening for clicks.
	 */
	abstract public void start();
	
	/**
	 * Stops this <code>ClickSensor</code> from listening for clicks.
	 */
	abstract public void stop();
	
	/**
	 * To be called by a derived class all listeners should have their <code>onSensorClick</code>
	 * methods called.
	 */
	protected void onSensorClick() {
		for(Listener listener : mListeners) {
			listener.onSensorClick(this);
		}
	}
}
