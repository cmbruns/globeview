//
// $Id$
// $Header$
// $Log$
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
import org.bruns.asmodeus.globeview.*;

// Class to deliver colors from a bitmap image to the GlobeView program
public class MapBlitter extends GeoObject
{
	GeoCanvas canvas;
    Image rawImage;
    int width;
    int height;
    Color colorArray[][];
    int pixelArray[];

    // Efficiency variables to minimize computations in getColor
    int xPixelIndex[]; // 
    double yPixelIndex[];

    double dX; // coordinate of long 0
    double dY; // coordinate of lat 0
    double kX;
    double kY;
    double yScale;
	double minLat, maxLat, minLon, maxLon;

    MapBlitter(String imageFileName, GeoCanvas geoCanvas, 
		double minLati, double maxLati, double minLong, double maxLong) {

		minLat = minLati;
		maxLat = maxLati;
		minLon = minLong;
		maxLon = maxLong;

		setResolution(0.001, 0.001, 100.0, 100.0); // TODO for testing
		canvas = geoCanvas;
		try {
			rawImage = Toolkit.getDefaultToolkit().getImage(imageFileName);
		} catch (Exception ex) {
			rawImage = null;
			pixelArray = null;
			return;
		}
		setLonLatRange(minLon, maxLon, minLat, maxLat);
		processImage(rawImage, canvas);
    }
	
    MapBlitter(URL imageURL, 
			   GeoCanvas geoCanvas,
			   double minLati, double maxLati, double minLong, double maxLong) {
		
		minLat = minLati;
		maxLat = maxLati;
		minLon = minLong;
		maxLon = maxLong;

		setResolution(0.001, 0.001, 100.0, 100.0); // TODO for testing
		canvas = geoCanvas;
		try {
			rawImage = Toolkit.getDefaultToolkit().getImage(imageURL);
		} catch (Exception ex) {
			System.err.println(ex);
			rawImage = null;
			pixelArray = null;
			return;
		}
		setLonLatRange(minLon, maxLon, minLat, maxLat);
		processImage(rawImage, canvas);
    }
	
	void processImage(Image image, GeoCanvas geoCanvas) {
		canvas = geoCanvas;
		MediaTracker mt = new MediaTracker(canvas);
		mt.addImage(image,1);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			System.err.println(e);
			image = null;
		}
		width = image.getWidth(null);
		height = image.getHeight(null);
		
		pixelArray = new int[height * width];
		PixelGrabber pixelGrabber = new PixelGrabber
			(image, 0, 0, width, height, 
			 pixelArray, 0, width);
		try {
			pixelGrabber.grabPixels();
		} catch (Exception e) {
			System.out.println(e);
		}

		// Efficiency variables
		//   Scale in pixels per radian
		kX = width / (maxLon - minLon);
		kY = height / (maxLat - minLat);
		//   Coordinates of 0,0
		// dX = width/2; // -minLon * kX;
		// dY = height/2; // -minLat * kY;
		dX = -minLon * kX;
		dY = maxLat * kY;

		yScale = 4 * height;
		yPixelIndex = new double[(int)(2 * yScale)];
		int i;
		for (i = 0; i < 2 * yScale; ++i) {
			// index of color at yScale*y() + yScale
			yPixelIndex[i] = ((height - 1) / 2.0) * 
			Math.asin((i - yScale)/yScale) * 2.0 / (maxLat - minLat);
		}
    }

    Color getColor(Vector3D v, double resolution) {
		int intColor = getPixel(v, resolution);
		if (intColor < 0) return null;
		return new Color(intColor);
    }

    int getPixel(Vector3D v, double resolution) {
		if (pixelArray == null) return 0;
		try {
			int x, y;
			
			double lambda = Math.atan2(v.x(), v.z());
			if (lambda < minLon) return 0;
			if (lambda > maxLon) return 0;

			double fx = dX + lambda * kX;
			// if (fx < 0) fx -= 0.5; // So integer truncation is as accurate as possible
			// else fx += 0.5;
			x = (int) (fx);
			
			double phi = Math.asin(v.y());
			if (phi < minLat) return 0;
			if (phi > maxLat) return 0;

			double fy = dY - phi * kY;
			// if (fy < 0) fy -= 0.5; // So integer truncation is as accurate as possible
			// else fy += 0.5;
			y = (int) (fy);
			
			if (x < 0) return 0;
			if (x >= width) return 0;
			if (y < 0) return 0;
			if (y >= height) return 0;
			
			int intColor = pixelArray[x + y * width];
			
			return intColor;
			
		} catch (Exception e) {
			return 0;
		}
    }
	
	static MapBlitter readMap(URL mapURL, GeoCanvas geoCanvas) {
		double d2r = Math.PI / 180.0;
		// Default is entire planet surface
		return readMap(mapURL, geoCanvas, 
					   -90.0*d2r, 90.0*d2r, -180.0*d2r, 180.0*d2r);
	}
	
	static MapBlitter readMap(URL mapURL, GeoCanvas geoCanvas,
							  double minLat, double maxLat, double minLon, double maxLon) {
		MapBlitter mapBlitter = null;
		try {
			// mapURL = new URL("file:/home/bruns/public_html/pacbell/images/bs_0.3deg.jpg");
			mapBlitter = new MapBlitter(mapURL, geoCanvas,
										minLat, maxLat, minLon, maxLon);
		} catch (Exception exception) {
			System.out.println(exception);
			System.out.println("Could not find image " + mapURL);
		}
		return mapBlitter;
	}

}
