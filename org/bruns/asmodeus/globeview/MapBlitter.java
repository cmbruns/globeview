//
// $Id$
// $Header$
// $Log$
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
public class MapBlitter
{
    Image rawImage;
    int width;
    int height;
    Color colorArray[][];
    int pixelArray[];

    // Efficiency variables to minimize computations in getColor
    int xPixelIndex[]; // 
    double yPixelIndex[];

    int dX; // width/2
    int dY; // height/2
    double kX;
    double kY;
    double yScale;

    MapBlitter(String imageFileName, Component parent) {
	try {
	    rawImage = Toolkit.getDefaultToolkit().getImage(imageFileName);
	} catch (Exception ex) {
	    rawImage = null;
	    pixelArray = null;
	    return;
	}
	processImage(rawImage, parent);
    }

    MapBlitter(URL imageURL, Component parent) {
	try {
	    rawImage = Toolkit.getDefaultToolkit().getImage(imageURL);
	} catch (Exception ex) {
	    System.err.println(ex);
	    rawImage = null;
	    pixelArray = null;
	    return;
	}
	processImage(rawImage, parent);
    }

    void processImage(Image image, Component parent) {
	MediaTracker mt = new MediaTracker(parent);
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
	dX = width/2;
	dY = height/2;
	kX = width / (2 * Math.PI);
	kY = height / Math.PI;

	yScale = 4 * height;
	yPixelIndex = new double[(int)(2 * yScale)];
	int i;
	for (i = 0; i < 2 * yScale; ++i) {
	    // index of color at yScale*y() + yScale
	    yPixelIndex[i] = ((height - 1) / 2.0) * 
		Math.asin((i - yScale)/yScale) * 2.0 / Math.PI;
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
			x = (int) (dX + lambda * kX);
			
			// double phi = Math.asin(v.y());
			y = (int) (dY - yPixelIndex[(int)((1.0 + v.y())*yScale)]);
			
			int intColor = pixelArray[x + y * width];
			
			return intColor;
			
		} catch (Exception e) {
			return 0;
		}
    }
	
	static MapBlitter readMap(URL mapURL, GeoCanvas geoCanvas) {
		MapBlitter mapBlitter = null;
		try {
			// mapURL = new URL("file:/home/bruns/public_html/pacbell/images/bs_0.3deg.jpg");
			mapBlitter = new MapBlitter(mapURL, geoCanvas);
		} catch (Exception exception) {
			System.out.println(exception);
			System.out.println("Could not find image " + mapURL);
		}
		return mapBlitter;
	}
}
