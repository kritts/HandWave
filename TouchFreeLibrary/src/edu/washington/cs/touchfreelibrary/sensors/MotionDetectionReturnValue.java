package edu.washington.cs.touchfreelibrary.sensors;

import org.opencv.core.Point;

/**
 * Only used within the package to get data from JNI.
 * @author Leeran Raphaely <leeran.raphaely@gmail.com>
 */
public class MotionDetectionReturnValue {
	public Point averagePosition;
	public double fractionOfScreenInMotion;
	double bottomRightFraction;
	double bottomLeftFraction;
	double topRightFraction;
	double topLeftFraction;
	
	public MotionDetectionReturnValue(double x, double y, double fraction, double bottomRightFraction, double bottomLeftFraction, double topRightFraction, double topLeftFraction) 
	{
		averagePosition = new Point(x, y);
		fractionOfScreenInMotion = fraction;
		this.bottomRightFraction = bottomRightFraction;
		this.bottomLeftFraction = bottomLeftFraction;
		this.topRightFraction = topRightFraction;
		this.topLeftFraction = topLeftFraction;
	}
}
