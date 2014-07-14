# HandWave
========

> An Android library that allows developers to add touch free capabilities to their mobile applications.

## Overview 
HandWave is a library that allows developers to enable touch-free interactions in their apps. 
HandWave uses the built-in, forward-facing camera on a device to recognize users’ in-air gestures. The API provides developers with easy access to a variety of touch-free gestures which invoke callback functions when detected. 

Example apps using the HandWave library can be found [here] (https://github.com/kritts/HandWave-Sample-Apps).

The code for the library is all available if you'd like to make changes. However, the .jar file is also available if not. Further details about how to use the library in your code are included below. 


A video demonstrating capabilities of the library can be found here:
[![Video demonstrating HandWave's function](http://img.youtube.com/vi/ws8UipMmJLE/0.jpg)](http://youtu.be/ws8UipMmJLE)


## Using the HandWave library 

I primarily use Eclipse for development therefore the instructions below are for Eclipse.
I may add additional instructions later for Android Studio.

1. Clone the repo: `git clone https://github.com/kritts/HandWave.git`
1. Import `TouchFreeLibrary` **as a library**
    1. Click **File | Import | Android | Existing Android Code into Workspace**
    1. Select the `TouchFreeLibrary` project
    1. Click **Finish**
    1. Right-click on `TouchFreeLibrary`, then click **Properties**
    1. In the project properties window, click the **Android** section
    1. Check the **Is Library** checkbox
    1. Add a reference to the `TouchFreeLibrary` project (click **Remove** to remove any broken references, then click **Add** to add the correct one)
	1. You will also need to need to add opencv as a library. Detailed instructions on how to do so can be found [here](https://github.com/Itseez/opencv/blob/master/doc/tutorials/introduction/java_eclipse/java_eclipse.rst).
		1. It's up to you which version of OpenCV you'd like to use (all of the recent versions should work just fine), but the 2.4.3 is the version I used during development. 
		1. A copy of the both libraries are available [here] (https://github.com/kritts/HandWave/tree/master/TouchFreeLibrary/bin).
1. Remember to add the required permissions to your application's AndroidManifest file: `<uses-permission android:name="android.permission.CAMERA" />`  


## Acknowledgements
The code for this library was initially created by Leeran Raphaely (leeran.raphaely@gmail.com). 
It has since been modified to fix bugs in the code and improve the overall speed of the algorithms.  
The changes were made by Krittika D'Silva (krittika.dsilva@gmail.com) and Nicola Dell (nixdell@cs.washington.edu).


