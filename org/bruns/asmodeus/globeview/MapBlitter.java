//
// $Id$
// $Header$
// $Log$
// Revision 1.7  2005/03/28 01:40:40  cmbruns
// Try to use MeasuredInputStream to keep track of load progress
//
// Revision 1.6  2005/03/13 22:09:56  cmbruns
// Changes to permit dynamic loading and unloading of images to and from memory.
// Changes to permit blurring of boundaries between pixels at hyper zoom levels.
// Minor improvement to the accuracy of pixel lookup.
//
// Revision 1.5  2005/03/11 00:14:43  cmbruns
// Removed some unused code and structures in preparation for more careful image loading
// Changed calling signature of readMap to have longitude precede latitude
//
// Revision 1.4  2005/03/05 00:14:05  cmbruns
// Made MapBlitter a derived class of GeoObject
// Many changes to permit varied latitude/longitude ranges in MapBlitters
//
// Revision 1.3  2005/03/02 01:56:25  cmbruns
// Wrapped getColor() around getPixel(), since they were so similar
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import javax.imageio.*;
import org.bruns.asmodeus.globeview.*;

// Class to deliver colors from a bitmap image to the GlobeView program
public class MapBlitter extends GeoObject
{
	GeoCanvas canvas;
    int width;
    int height;
    int pixelArray[] = null;
	URL imageURL; // Use to reconstruct after memory flushes?

    double dX; // coordinate of long 0
    double dY; // coordinate of lat 0
    double kX; // radians per pixel north/south
    double kY; // radians per pixel east/west
	double minLat, maxLat, minLon, maxLon;
	
	long timeOfPreviousUse = 0; // For manual garbage collection

    MapBlitter(URL url, 
			   GeoCanvas geoCanvas,
			   double minLong, double maxLong, double minLati, double maxLati) {
		
		minLat = minLati;
		maxLat = maxLati;
		minLon = minLong;
		maxLon = maxLong;
		imageURL = url;

		canvas = geoCanvas;

		// Populate parameters
		pixelArray = null;
		
		setLonLatRange(minLon, maxLon, minLat, maxLat);
    }
	
	void loadImagePixels() throws InterruptedException, IOException {
		canvas.loadProgress = 0;
		canvas.setWait("BUSY: Loading Image...");

		Image image;
		try {
			// This way the progress can be measured
			MeasuredInputStream mis = new MeasuredInputStream(imageURL, canvas);
			image = javax.imageio.ImageIO.read(mis);
		}
		catch (NoClassDefFoundError e) {
			// TODO I don't know how to get file size for progress indicator
			image = canvas.getToolkit().getImage(imageURL);
			canvas.loadProgress = 22; // To give the illusion that the bar might be working
		}
		catch (NoSuchMethodError e) {
			// TODO I don't know how to get file size for progress indicator
			image = canvas.getToolkit().getImage(imageURL);
			canvas.loadProgress = 22; // To give the illusion that the bar might be working
		}
		
		pixelArray = null;
		
		MediaTracker mt = new MediaTracker(canvas);
		mt.addImage(image,1);
		mt.waitForAll(); // InterruptedException could be thrown here
		canvas.loadProgress = 100;

		// Set image parameters
		width = image.getWidth(null);
		height = image.getHeight(null);
		// System.out.println("Image width = "+width+", height = "+height);
		kX = width / (maxLon - minLon);
		kY = height / (maxLat - minLat);
		dX = -minLon * kX - 0.5; // 0.5 is half a pixel to the left of the middle of pixel 0
		dY = maxLat * kY - 0.5;
		
		pixelArray = new int[height * width];
		PixelGrabber pixelGrabber = new PixelGrabber
			(image, 0, 0, width, height, 
			 pixelArray, 0, width);
		try {
			pixelGrabber.grabPixels();
		}
		catch (InterruptedException e) {
			pixelArray = null; // Did not complete load
			canvas.loadProgress = 100;
			throw e;
		}

		canvas.setWait("BUSY: Drawing Image...");
    }

    Color getColor(Vector3D v) throws InterruptedException, IOException {
		int intColor = getPixel(v);
		if (intColor < 0) return null;
		return new Color(intColor);
    }

	// pixel Size is used to blur hyper zoomed pixels
    int getPixel(Vector3D v, double pixelSize) throws InterruptedException, IOException {

		double phi = v.getLatitude();
		if (phi < minLat) return 0;
		if (phi > maxLat) return 0;
		
		double lambda = v.getLongitude();
		if (lambda < minLon) return 0;
		if (lambda > maxLon) return 0;
		
		return getPixel(lambda, phi, pixelSize);
	}
		
    int getPixel(Vector3D v) throws InterruptedException, IOException {		
		return getPixel(v, 0);
	}
		
    int getPixel(double lambda, double phi) throws InterruptedException, IOException {
		return getPixel(lambda, phi, 0);
	}
	
    int getPixel(double lambda, double phi, double pixelSize) 
		throws InterruptedException, IOException 
	{

		// Only load image if we get to this point
		if (pixelArray == null) {
			try {loadImagePixels();}
			catch (IOException e) {
				canvas.loadProgress = 100;
				throw e;
				// return 0;
			}				
			canvas.loadProgress = 100;
		}
		
		double fx = dX + lambda * kX;
		double fy = dY - phi * kY;

		if (fx < -0.5) return 0;
		if (fx > (width - 0.5)) return 0;
		if (fy < -0.5) return 0;
		if (fy > (height - 0.5)) return 0;
		
		// Blur hyper zoomed pixels
		if (pixelSize > 0) {
			// How far from the center of the bitmap pixel are we?
			// Want chance of shift to go from zero at center to 0.5 at edge
			if (1/kY > (2 * pixelSize))
				fy += (Math.random() - 0.5);
			if (Math.cos(phi)/kX > (2 * pixelSize))
				fx += (Math.random() - 0.5);
		}
		
		int x, y;
		
		x = (int) Math.round(fx);		
		y = (int) Math.round(fy);
		
		if (x < 0) x = 0;
		if (x >= width) x = width - 1;
		if (y < 0) y = 0;
		if (y >= height) y = height - 1;

		int intColor = pixelArray[x + y * width];
		
		return intColor;
    }
	
	static MapBlitter readMap(URL url, GeoCanvas geoCanvas) {
		double d2r = Math.PI / 180.0;
		// Default is entire planet surface
		return readMap(url, geoCanvas, 
					   -180.0*d2r, 180.0*d2r, -90.0*d2r, 90.0*d2r);
	}
	
	static MapBlitter readMap(URL url, GeoCanvas geoCanvas,
							  double minLon, double maxLon, double minLat, double maxLat) {
		MapBlitter mapBlitter = null;
		// try {
			// mapURL = new URL("file:/home/bruns/public_html/pacbell/images/bs_0.3deg.jpg");
			mapBlitter = new MapBlitter(url, geoCanvas,
										minLon, maxLon, minLat, maxLat);
		// } catch (Exception exception) {
		// 	System.out.println(exception);
		// 	System.out.println("Could not find image " + url);
		// }
		return mapBlitter;
	}

}
