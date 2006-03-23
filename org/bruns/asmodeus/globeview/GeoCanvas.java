/*
 * Copyright (c) 2005, Christopher Bruns. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions
 * are met: 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 *  - Neither the name of Christopher Bruns nor the names other 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.bruns.asmodeus.globeview;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;
import java.net.*; // URL
import org.bruns.asmodeus.globeview.*;
import java.lang.*;
import java.text.*;

// $Id$
// $Header$
// $Log$
// Revision 1.8  2005/03/28 01:03:20  cmbruns
// Reorganized header area
// Added methods to start and stop progressBarThread
// Propagate OutOfMemoryError when caught
// Created updateOnScreen method to copy offscreen buffer to screen
// Created updateOffScreen method to handle some of the complexity of drawing stereoscopic images
// Reorganized paint() method to use new updateOffScreen() and updateOnScreen() methods
// Added and used "left eye" off screen buffers for stereoscopic use
// Added projection and genglobe arguments to paintBackground() and paintOutline() methods.  TODO - the use of new genglobe members has not been fully implemented in these methods.
// Created getCurrentGenglobe() method for use in stereoscopic display
// Created rasterToSphere() and rasterToUnrotatedSphere() methods to abstract some of the genglobe calculations that were being done.
// Fixed bug in setting of whatTheModifiedMouse does
//
// Revision 1.7  2005/03/14 04:19:14  cmbruns
// Created mechanism for turning nightUpdateThread on and off
// Wrapped shadeBoxColor initialization so that it goes to black if the Java version does not have transparency
//
// Revision 1.6  2005/03/13 21:51:18  cmbruns
// Created outOfMemoryDialog
// Made rendering of crosshair optional
// Created 3 levels of DirectionGraticule detail
// flushSomeImages() after full repaint
// Catch buggy AccessControlException in stopBitmapThread()
// Catch OutOfMemoryError in update() [still needs to do smarter stuff here though TODO]
// Update viewLens after loading parameter files [may not be necessary, as parameter file loading should take care of this]
// Don't do full repaint on resizing [TODO this is not very nice]
// Replaced all generic exception catch statements with specific ones.
// Improved support for mouse rotateZ action
//
// Revision 1.5  2005/03/11 00:03:17  cmbruns
// Created distance scale bar
// Added more tests to prevent crazy zoom changes
// Repaint foreground before time consuming redraws
//
// Revision 1.4  2005/03/05 00:55:25  cmbruns
// Implemented separate DrawBitmapThread to handle drawing the satellite and day/night images.  Gave public
// access to offscreen buffers for this purpose.  Made some
// variables volatile for this, but am not sure that is
// necessary.
//
// Created container objects for parameter files, graticules,
// coastlines, labels
//
// Created reference to parent frame, so that menu checks can be modified from ParameterFiles.  Several new
// routines to change local variables and reflect this in
// parent frame menus.  e.g. setGraticules(boolean)
//
// Removed parameters for separate mapURL, etc.  These can now be passed in ParameterFiles.
//
// Removed rusty benchmarking code.
//
// Created getViewLens() routine
//
// Created setWait(String) and unsetWait() to centralize feedback on time consuming operations.
//
// swapped update() and paint() routines.  I don't think this makes a difference.
//
// most of the content of paintBitmap() has been moved to ImageCollection
//
// created centerOnPosition() routine
//
// Revision 1.3  2005/03/02 01:48:23  cmbruns
// Changed data type of coasts and labels to GeoCollection, to make it easier for the new ParameterFiles to add to them.
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
// Revision 1.1.1.1  2005/03/01 01:52:01  cmbruns
// Initial cvs import of globeview on koobi
//
// Revision 1.5  2004/02/12 18:27:38  bruns
// new comment about anntenna lines plans
//
// Revision 1.4  2004/02/08 05:33:58  bruns
// Many changes
//   label colors
//   moved some parameters to genglobe object
//   updated day/night calculation?
//
// Revision 1.3  2003/03/16 21:59:14  bruns
// Added use of NightUpdateThread to periodically update image
// Added several city labels to default display
// Created fullRepaint and fastRepaint subroutines to
//   a) compartmentalize use of boolean drawBitmap
//   b) isolate sending of interrupt events to NightUpdateThread
// Sharpened day/night terminator from 0.1 radians to 0.02 radians
//
// Revision 1.2  2001/11/24 00:31:56  bruns
// Added cvs tags
//

// FIXME - Implement smooth coloring for vector objects

public class GeoCanvas extends Canvas 
implements MouseListener, MouseMotionListener
{
    double planetRadius = 6371; // Radius of earth in kilometers

	// Variables associated with ancillary process threads
    NightUpdateThread nightUpdateThread;
	volatile boolean keepNightUpdate = true;
	
	DrawBitmapThread drawBitmapThread;
	volatile boolean drawNothing = false; // For incremental drawing
	
	ProgressBar progressBar;
	ProgressBarThread progressBarThread;
	volatile double loadProgress = 100.0;
	volatile double drawProgress = 100.0;
	volatile boolean keepProgressBar = true;
	
	String busyString = "";
	InfoDialog outOfMemoryDialog;
	
	// Colors
	int oceanColorInt = 0xFF3060E0;
	int oceanColorInt2 = oceanColorInt - 0xFF000000;
    Color oceanColor = new Color(oceanColorInt2);
    Color outlineColor = new Color(40, 40, 180);
    Color crosshairColor = Color.white;
	int backgroundColorInt = 0xFF000000;
    Color backgroundColor = Color.black;
	Color coastColor = new Color(70,100,30);
	Color riverColor = new Color(5,10,140);
	Color scaleBarColor = new Color(255, 220, 220); // Pink
	Color shadeBoxColor;
	Color borderColor = new Color(50, 50, 50); // Dark Grey
	
    // Define what kinds of objects will be drawn
	boolean drawStereoscopic = false; // For perspective projection only	
    volatile boolean fullUpdateNow = true; // Want to draw satellite image on startup
    boolean drawSatelliteImage = true; // Want to draw satellite image in general
    boolean dayNight = true; // Draw day/night terminator
    boolean drawLabels = true;
	boolean drawGraticule = true;
	boolean drawBearing = false;
	boolean drawBorders = true;
	boolean drawCoastLines = true;
	boolean drawRivers = true;
	boolean drawScaleBar = true;
	boolean drawCrosshair = true;
	
    int canvasWidth, canvasHeight;	
	
    volatile GenGlobe genGlobe = new GenGlobe(planetRadius); // Radius in kilometers
	volatile GenGlobe leftGenGlobe = genGlobe;
	volatile GenGlobe rightGenGlobe = genGlobe;
	
	// Objects whose sole purpose is to communicate resolution ranges
	GeoObject allResolution = new GeoObject(-1,-1,-1,-1);
	// Alpha on the graticules is too slow
	GeoObject lowResolution = new GeoObject(0.005, 0.005, 0.150, 0.150);
	GeoObject highResolution = new GeoObject(0.150, 0.150, -1, -1);
	
	// Container objects for map display elements
	GeoCollection directionGraticule = new GeoCollection();
	GeoCollection graticule = new GeoCollection();
	GeoCollection siteLabels = new GeoCollection();	
	GeoCollection paramFiles = new GeoCollection();
	ImageCollection images = new ImageCollection(this);
	GeoCollection coasts = new GeoCollection();
	GeoCollection rivers = new GeoCollection();
	GeoCollection borders = new GeoCollection();
    Label messageArea = new Label("");
		
    Projection projection = Projection.ORTHOGRAPHIC;
	
    // For Sinusoidal Projection outline shape
    int numOutlinePoints = 100;
    int xOutlinePoints[] = new int[numOutlinePoints];
    int yOutlinePoints[] = new int[numOutlinePoints];
	
    Method whatTheMouseDoes;
    Method whatTheModifiedMouseDoes;
	
    // Whole gang of objects to represent one off-screen image buffer
    int offScreenPixels[];
    public MemoryImageSource offScreenSource;
    public Image memOffScreenImage;	
    public Image offScreenImage;
    volatile public Graphics offScreenGraphics;
	
    int leftEyePixels[];
    public MemoryImageSource leftEyeSource;
    public Image memLeftEyeImage;	
    public Image leftEyeImage;
    volatile public Graphics leftEyeGraphics;
	
	GlobeView globeViewFrame;
	
    GeoCanvas(int width, int height, GlobeView parent) {
		setSize(width, height);
		canvasWidth = width;
		canvasHeight = height;
		
		globeViewFrame = parent;
		
		// Automatically updates image periodically, if dayNight is set
		progressBar = new ProgressBar(this);
		
		startNightUpdateThread();
		startProgressBarThread();
		
		try {shadeBoxColor = new Color(0, 0, 0, 128);} // transparent black
		catch (NoSuchMethodError e) {shadeBoxColor = new Color(0,0,0);} // solid black
		
		genGlobe.setPixelRadius((width < height) ? width/2.0 : height/2.0);
		
		genGlobe.centerX = width / 2;
		genGlobe.centerY = height / 2;

		addMouseListener(this);
		addMouseMotionListener(this);

		DirectionGraticule directionGraticule1 = new DirectionGraticule(2000, 15, genGlobe, genGlobe.getKmRadius()*Math.PI);
		DirectionGraticule directionGraticule2 = new DirectionGraticule(500, 15, genGlobe, 5000);
		DirectionGraticule directionGraticule3 = new DirectionGraticule(100, 15, genGlobe, 500);
		directionGraticule1.setResolution(0.003, 0.003, 1.0, 1.0);
		directionGraticule2.setResolution(0.100, 0.100, 4.0, 4.0);
		directionGraticule3.setResolution(0.500, 0.500, 20.0, 20.0);
		directionGraticule.addElement(directionGraticule1);
		directionGraticule.addElement(directionGraticule2);
		directionGraticule.addElement(directionGraticule3);
		
		outOfMemoryDialog = new InfoDialog(globeViewFrame, "GlobeView out of Memory Error!", true);
		outOfMemoryDialog.okButton.setLabel("Bummer!");
		outOfMemoryDialog.addLine("GlobeView ran out of memory!");
		outOfMemoryDialog.finalizeDialog();
		
		checkOffScreenImage();
		// createOffScreen(width, height);		
    }
	
	boolean stopNightUpdateThread() {
		keepNightUpdate = false;
		if ((nightUpdateThread != null) && (nightUpdateThread.isAlive())) {
			// Java bug causes exception to be thrown when thread is not running
			try {nightUpdateThread.interrupt();}
			// catch (java.security.AccessControlException e) {} // netscape has a conniption
			catch (Exception e) {}

			try {nightUpdateThread.join(2000); // Wait up to two seconds for it to die
			} catch (InterruptedException e) {}
		}
		if ((nightUpdateThread != null) && (nightUpdateThread.isAlive())) return false; // Did not stop
		return true;
	}
	
	void startNightUpdateThread() {
		stopNightUpdateThread();
		keepNightUpdate = true;
		nightUpdateThread = new NightUpdateThread(this);
		nightUpdateThread.start();
	}
	
	boolean stopProgressBarThread() {
		keepProgressBar = false;
		if ((progressBarThread != null) && (progressBarThread.isAlive())) {
			// Java bug causes exception to be thrown when thread is not running
			try {progressBarThread.interrupt();}
			// catch (java.security.AccessControlException e) {} // netscape has a conniption
			catch (Exception e) {}
			
			try {progressBarThread.join(2000); // Wait up to two seconds for it to die
			} catch (InterruptedException e) {}
		}
		if ((progressBarThread != null) && (progressBarThread.isAlive())) return false; // Did not stop
		return true;
	}
	
	void startProgressBarThread() {
		stopProgressBarThread();
		keepProgressBar = true;
		progressBarThread = new ProgressBarThread(this);
		progressBarThread.start();
	}
	
	public boolean stopBitmapThread() {
		images.keepDrawing = false; // set variable first, then interrupt
		if ((drawBitmapThread != null) && (drawBitmapThread.isAlive())) {
			
			// Java bug causes exception to be thrown when thread is not running
			try {drawBitmapThread.interrupt();}
			// catch (AccessControlException e) {}
			catch (Exception e) {}
			
			try {drawBitmapThread.join(2000); // Wait up to two seconds for it to die
			} catch (InterruptedException e) {}
		}
		if ((drawBitmapThread != null) && (drawBitmapThread.isAlive())) return false; // Did not stop
		return true;
	}
	
	public void startBitmapThread(Graphics g, LensRegion viewLens) {
		if (!stopBitmapThread()) return;
		images.keepDrawing = true;
		drawBitmapThread = new DrawBitmapThread("DrawBitmap", this, g, viewLens);
		drawNothing = true; // Until bitmap returns
		drawBitmapThread.start();
	}
				
    public void fastRepaint() {
		if (drawNothing) return;
		// Most repaints should postpone auto-painting
		if (nightUpdateThread != null && nightUpdateThread.isAlive()) {
			nightUpdateThread.interrupt();
		}
		fullUpdateNow = false;
		repaint();
		// nightUpdateThread.interrupt();
    }
	
    public void fullRepaint() {
		if (drawNothing) return;
		// Most repaints should postpone auto-painting
		if (nightUpdateThread != null && nightUpdateThread.isAlive()) {
			nightUpdateThread.interrupt();
		}	
		if (!stopBitmapThread()) return; // Painting thread would not stop
		fullUpdateNow = true;
		repaint();
		images.flushSomeImages(); // Try to reclaim some memory
		// nightUpdateThread.interrupt();
    }
	
    // Projection-specific outline of map
    public void paintOutline(Graphics g, Projection p, GenGlobe gg) {
		int type = p.getBackgroundType();
		
		double r = gg.getPixelRadius();
		// double param = p.getBackgroundParameter() * r;
		double param = p.getBackgroundParameter();
		
		double diameter = r + r;
		Dimension d = getSize(); // (size of canvas)
		
		if (type == Projection.BKGD_INFPLANE) {
			// Infinite plane, there is no "outline"
		}
		if (type == Projection.BKGD_VSTRIPE) {
			// Vertical stripe, like Mercator
			// g.drawLine(gg.centerX - (int) param, 0, 
			// 		   gg.centerX - (int) param, d.height);
			// g.drawLine(gg.centerX + (int) param, 0, 
			// 		   gg.centerX + (int) param, d.height);
			g.drawLine(gg.screenX(-param), 0, 
					   gg.screenX(-param), d.height);
			g.drawLine(gg.screenX(param), 0, 
					   gg.screenX(param), d.height);
		}
		else if (type == Projection.BKGD_RECTANGLE) {
			// Rectangle, (2 x 1 aspect ratio)
			g.drawRect(gg.screenX(-param), gg.screenY(param / 2.0),
					   (int) (r * (param + param)), (int) (r * param));
			// g.drawRect(gg.centerX - (int) param, gg.centerY - (int) (param / 2.0), 
			//		   (int) (param + param), (int) param);
		}
		else if (type == Projection.BKGD_CIRCLE) {
			// Full projection is shaped like a circle
			// g.drawArc(gg.centerX - (int) param, gg.centerY - (int) param, 
			// 		  (int) (param + param), (int) (param + param), 
			// 		  0, 360); 
			g.drawArc(gg.screenX(-param), gg.screenY(param), 
					  (int) (r*(param + param)), (int) (r*(param + param)), 
					  0, 360); 
		}
		else if (type == Projection.BKGD_SINUSOID) {
			// Full projection is shaped like a sinusoid (2 x 1 aspect ratio)
			int i;
			for (i = 0; i < numOutlinePoints; ++i) {
				double halfHeight = param / 2.0;
				xOutlinePoints[i] = gg.screenX(
					param * Math.sin(2.0 * Math.PI * i / numOutlinePoints));
				
				yOutlinePoints[i] = gg.screenY(-halfHeight + halfHeight * 
										   4.0 * i / numOutlinePoints);
				if (yOutlinePoints[i] < gg.screenY(halfHeight))
					yOutlinePoints[i] = 2 * gg.screenY(halfHeight) - yOutlinePoints[i];
				
				// xOutlinePoints[i] = gg.centerX;
				// yOutlinePoints[i] += gg.centerY;
			}
			g.drawPolygon(xOutlinePoints, yOutlinePoints, numOutlinePoints);
		}
    }
	
    // FILLED Projection-specific outline of map
    public void paintOcean(Graphics g, Projection p, GenGlobe gg) {
		int type = p.getBackgroundType();
		
		double r = gg.getPixelRadius();
		double param = p.getBackgroundParameter() * r;
		
		double diameter = r + r;
		Dimension d = getSize(); // (size of canvas)
		
		if (type == Projection.BKGD_INFPLANE) {
			g.fillRect(0,0,getSize().width,getSize().height);
		}
		if (type == Projection.BKGD_VSTRIPE) {
			// Vertical stripe, like Mercator
			g.fillRect(gg.centerX - (int) param, 0,
					   (int) (2 * param + 0.5), d.height);
		}
		else if (type == Projection.BKGD_RECTANGLE) {
			// Rectangle, (2 x 1 aspect ratio)
			g.fillRect(gg.centerX - (int) param, gg.centerY - (int) (param / 2.0), 
					   (int) (param + param), (int) param);
		}
		else if (type == Projection.BKGD_CIRCLE) {
			// Full projection is shaped like a circle
			// g.fillArc(gg.centerX - (int) param, gg.centerY - (int) param, 
			// 		  (int) (param + param), (int) (param + param), 
			// 		  0, 360); 
			g.fillArc(gg.screenX(-param/r), gg.screenY(param/r), 
					  (int) ((param + param)), (int) ((param + param)), 
					  0, 360); 
		}
		else if (type == Projection.BKGD_SINUSOID) {
			// Full projection is shaped like a sinusoid (2 x 1 aspect ratio)
			int i;
			for (i = 0; i < numOutlinePoints; ++i) {
				double halfHeight = param / 2.0;
				xOutlinePoints[i] = 
					(int) (param * Math.sin(2.0 * Math.PI * i / numOutlinePoints));
				yOutlinePoints[i] = (int) (-halfHeight + halfHeight * 
										   4.0 * i / numOutlinePoints);
				if (yOutlinePoints[i] > halfHeight)
					yOutlinePoints[i] = (int) ((2.0 * halfHeight) - yOutlinePoints[i]);
				
				xOutlinePoints[i] += gg.centerX;
				yOutlinePoints[i] += gg.centerY;
			}
			g.fillPolygon(xOutlinePoints, yOutlinePoints, numOutlinePoints);
		}
    }
	
    // Temporary for reuse
    Vector2D tempV2 = new Vector2D();
    Vector3D tempV3;
    Vector3D utilityV3 = new Vector3D(); // NEVER let this get set to null
	
    boolean continueLine = false;
    // Need two actual points at once
    Vector2D lastScreenTemp = new Vector2D();
    Vector2D newScreenTemp = new Vector2D();
	
    // Everything IN FRONT OF the texture map
    synchronized public void paintForeground(Graphics g, Projection p, GenGlobe gg, LensRegion viewLens) {
		double r = gg.getPixelRadius();
		// double resolution = r / planetRadius; // Pixels per kilometer
		// double resolution = gg.getResolution(); // Pixels per kilometer
													  // System.out.println("" + resolution);

		if (drawBorders) borders.paint(g, gg, projection, viewLens);
		if (drawGraticule) graticule.paint(g, gg, projection, viewLens);
		if (drawRivers) rivers.paint(g, gg, projection, viewLens);
		if (drawCoastLines) coasts.paint(g, gg, projection, viewLens);
		
		// Draw the center-focussed elements, such as antenna directions
		if (drawBearing) directionGraticule.paint(g, gg, projection, null);
		
		// Draw outline (again, to clean up edges)
		g.setColor(outlineColor);
		paintOutline(g, p, gg);
		
		// Draw city labels
		if (drawLabels) siteLabels.paint(g, gg, projection, viewLens);
		
		if (drawScaleBar) paintScaleBar(g, gg, projection, viewLens);
		
		// Cross-hair at center
		if (drawCrosshair) {
			g.setColor(crosshairColor);
			g.drawLine(gg.centerX, gg.centerY - 5, gg.centerX, gg.centerY + 5);
			g.drawLine(gg.centerX - 5, gg.centerY, gg.centerX + 5, gg.centerY);
		}
    }
	
	// Paint a scale bar in the lower right corner
	void paintScaleBar(Graphics g, GenGlobe gg, Projection projection, LensRegion viewLens) {
		// What is the longest round distance less than 150 pixels?
		int maxPixelsPerBar = 120;
		double kilometersPerPixel = 1.0 / gg.getResolution();
		double maxBarDistance = kilometersPerPixel * maxPixelsPerBar;
		double logMaxDistance = Math.log(maxBarDistance) / Math.log(10.0);

		int exponent = (int) logMaxDistance;
		double logMantissa = logMaxDistance - exponent;
		if (logMaxDistance < 0) {
			exponent -= 1;
			logMantissa = 2.0 - logMantissa;
		}

		int mantissa = (int) (Math.pow(10.0, logMantissa)); // Should be between 1 and 9;
		// Restrict to 1, 2, and 5
		if (mantissa >= 5) mantissa = 5;
		else if (mantissa >= 2) mantissa = 2;
		else mantissa = 1;
		
		double barDistance = mantissa * Math.pow(10.0, exponent);
		int barPixels = (int) (barDistance/kilometersPerPixel);

		int unitCount = (int)(barDistance + 0.5);
		String units = "km";
		if (exponent < 0) {
			units = "m";
			unitCount = (int)(barDistance*1000 + 0.5);
		}
		if (exponent >=6) {
			units = "*10^6km";
			unitCount = (int)(barDistance/1000000 + 0.5);
		}
		
		int cWidth = offScreenImage.getWidth(null);
		int cHeight = offScreenImage.getHeight(null);
		
		int startX = cWidth - 40 - maxPixelsPerBar;
		int startY = cHeight - 20;
		
		// Shade box for light backgrounds
		g.setColor(shadeBoxColor);
		g.fillRect(startX - 8, startY - 8, cWidth - startX + 8, cHeight - startY + 8);
		
		g.setColor(scaleBarColor);
		
		g.drawLine(startX, startY, startX + barPixels, startY); // line
		g.drawLine(startX, startY - 5, startX, startY + 2); // left pip
		g.drawLine(startX + barPixels, startY - 5, startX + barPixels, startY + 2); // right pip
		
		if (mantissa == 5) {
			double interval = barPixels / 5.0;
			for (int i = 1; i <= 4; i++) {
				int ticX = (int)(startX + i * interval);
				g.drawLine(ticX, startY, ticX, startY - 2);
			}
		}
		else { // mantissa = 1 or 2
			int ticX = (int)(startX + barPixels/2.0);
			g.drawLine(ticX, startY, ticX, startY - 2);
		}
		
		g.drawString("0", startX - 4, startY + 16); // left distance
		g.drawString("" + unitCount + units, startX + barPixels - 4, startY + 16); // right distance
	}
	
	LensRegion getViewLens() {
		// For clipping
		// Compute viewable LensArea
		// Get the z value at each of the 4 corners of the display
		// If any come up null, use the minimum z for that projection
		double lensZ = projection.getMinimumZ(); // Start with worst case
		
		double farthestPointZ = 2.0;
		Vector3D cornerPoint = new Vector3D();
		
		// Upper left corner - all projections are symmetric at the corners
		// This one point should be enough
		// But not for Mercator?
		cornerPoint = projection.vec2DTo3D(genGlobe.planeX(0),
										   genGlobe.planeY(0),
										   cornerPoint);
		if (cornerPoint != null) {
			if (cornerPoint.z() < farthestPointZ) farthestPointZ = cornerPoint.z();
		} else {farthestPointZ = lensZ;}
		// left middle point
		cornerPoint = new Vector3D();
		cornerPoint = projection.vec2DTo3D(genGlobe.planeX(0),
										   0,
										   cornerPoint);
		if (cornerPoint != null) {
			if (cornerPoint.z() < farthestPointZ) farthestPointZ = cornerPoint.z();
		} else {farthestPointZ = lensZ;}
		
		if ((farthestPointZ < 1.0) && (farthestPointZ > lensZ))
			lensZ = farthestPointZ;
		
		Vector3D lensUnitVector = new Vector3D(0.0, 0.0, 1.0);
		Vector3D lensPlanePoint = new Vector3D(0.0, 0.0, lensZ);
		lensUnitVector = genGlobe.unrotate(lensUnitVector);
		lensPlanePoint = genGlobe.unrotate(lensPlanePoint);
		return new LensRegion(lensPlanePoint, lensUnitVector);
	}
	
	// *** Set/Unset methods below *** //
	
    void setBearing(boolean state) {
		drawBearing = state;
		if ((globeViewFrame != null) && (globeViewFrame.bearingButton != null)) {
			globeViewFrame.bearingButton.setState(state);
		}
    }
	
    void setBorders(boolean state) {
		drawBorders = state;
		if ((globeViewFrame != null) && (globeViewFrame.bordersButton != null)) {
			globeViewFrame.bordersButton.setState(state);
		}
    }
	
    void setCoastLines(boolean state) {
		drawCoastLines = state;
		if ((globeViewFrame != null) && (globeViewFrame.coastsButton != null)) {
			globeViewFrame.coastsButton.setState(state);
		}
    }
	
    void setCrosshair(boolean state) {
		drawCrosshair = state;
		if ((globeViewFrame != null) && (globeViewFrame.crosshairButton != null)) {
			globeViewFrame.crosshairButton.setState(state);
		}
    }
	
    void setDayNight(boolean state) {
		dayNight = state;
		if ((globeViewFrame != null) && (globeViewFrame.dayNightButton != null)) {
			globeViewFrame.dayNightButton.setState(state);
		}
    }
	
    void setGraticules(boolean state) {
		drawGraticule = state;
		if ((globeViewFrame != null) && (globeViewFrame.graticulesButton != null)) {
			globeViewFrame.graticulesButton.setState(state);
		}
    }
	
    void setNorthUp(boolean state) {
		genGlobe.setNorthUp(state);
		if ((globeViewFrame != null) && (globeViewFrame.northUpButton != null)) {
			globeViewFrame.setNorthUp(state);
		}
    }
	
    void setProjection(Projection p) {
		projection = p;
		if (p == Projection.CROSSEYE3D)
			drawStereoscopic = true;
		else drawStereoscopic = false;
		checkOffScreenImage();
		if (globeViewFrame != null) {
			globeViewFrame.setProjection(p);
		}
    }
	
    void setRivers(boolean state) {
		drawRivers = state;
		if ((globeViewFrame != null) && (globeViewFrame.riversButton != null)) {
			globeViewFrame.riversButton.setState(state);
		}
    }
	
    void setSatellites(boolean state) {
		drawSatelliteImage = state;
		if ((globeViewFrame != null) && (globeViewFrame.imagesButton != null)) {
			globeViewFrame.imagesButton.setState(state);
		}
    }
	
    void setScaleBar(boolean state) {
		drawScaleBar = state;
		if ((globeViewFrame != null) && (globeViewFrame.scaleBarButton != null)) {
			globeViewFrame.scaleBarButton.setState(state);
		}
    }
	
    void setSiteLabels(boolean state) {
		drawLabels = state;
		if ((globeViewFrame != null) && (globeViewFrame.sitesButton != null)) {
			globeViewFrame.sitesButton.setState(state);
		}
    }
	
	// Make clear to the user that waiting is required
	void setWait(String msg) {
		setCursor(waitCursor); // Could be slow
		busyString = msg + "  ";
		messageArea.setText(busyString);
		messageArea.repaint();
	}
	void unsetWait() {
		setCursor(defaultCursor);
		busyString = "";
		messageArea.setText(busyString + "(ready)");
		messageArea.repaint();
	}
	
	long oldTime = 0;
	boolean timeStarted = false;
	int timeIncrement() {
		long newTime = (new Date()).getTime();
		if (!timeStarted) {
			oldTime = newTime;
		}
		int elapsedTime = (int) (newTime - oldTime);
		timeStarted = true;
		oldTime = newTime;
		return elapsedTime;
	}

    // Paint should only be called during infrequent window expose events
    // (because we have redefined update() to not call paint())
    // It is also called to paint the initial image
    public void update(Graphics g) {
		try {
			paint(g);
		}
		catch (OutOfMemoryError e) {
			respondToOutOfMemory(e);
			throw e;
		}
    }
	
	void respondToOutOfMemory(OutOfMemoryError e) {
		outOfMemoryDialog.show();
		images.flushSomeImages();
		throw e;
		// TODO - take more drastic measures
	}
	
	// Copy off screen data onto the screen
	public void updateOnScreen(Graphics g) {
		if (drawStereoscopic) {
			g.drawImage(leftEyeImage, 0, 0, this);
			g.drawImage(offScreenImage, canvasWidth/2, 0, this);
		}
		else 
			g.drawImage(offScreenImage, 0, 0, this);
	}
	
	// Update the offscreen image, without updating the memOffscreen data
	public void updateOffScreen(LensRegion viewLens) {
		checkOffScreenImage();
		
		// 3D stereo - paint two images
		if (drawStereoscopic) {

			double screenDist = 1500; // Viewer to screen in pixels
			double interpupillaryDist = 250; // Distance between eyes in pixels
			double parallaxAngle = Math.asin((interpupillaryDist/2)/(screenDist + genGlobe.getPixelRadius()));
			double parallaxPixels = (interpupillaryDist/2)*genGlobe.getPixelRadius()/(genGlobe.getPixelRadius()+screenDist);
								   
			leftGenGlobe = genGlobe.copy();
			leftGenGlobe.centerX = leftEyeImage.getWidth(null)/2;
			leftGenGlobe.rotateY(parallaxAngle);
			leftGenGlobe.offsetX = (int)Math.round(parallaxPixels);

			rightGenGlobe = genGlobe.copy();
			rightGenGlobe.centerX = offScreenImage.getWidth(null)/2;
			rightGenGlobe.rotateY(-parallaxAngle);
			rightGenGlobe.offsetX = (int)Math.round(-parallaxPixels);
			
			if (drawSatelliteImage || dayNight) {
				leftEyeGraphics.drawImage(memLeftEyeImage, 0, 0, this);
				offScreenGraphics.drawImage(memOffScreenImage, 0, 0, this);
			}
			else {
				paintBackground(leftEyeGraphics, projection, leftGenGlobe, 
								leftEyeImage.getWidth(null), leftEyeImage.getHeight(null));
				paintBackground(offScreenGraphics, projection, rightGenGlobe,
								offScreenImage.getWidth(null), offScreenImage.getHeight(null));
			}
			paintForeground(leftEyeGraphics, projection, leftGenGlobe, viewLens);
			paintForeground(offScreenGraphics, projection, rightGenGlobe, viewLens);
		}
		else { // non stereo
			if (drawSatelliteImage || dayNight) // Need memory pixel source for night and planet image
				offScreenGraphics.drawImage(memOffScreenImage, 0, 0, this);
			else // paint simple background otherwise
				paintBackground(offScreenGraphics, projection, genGlobe,
								offScreenImage.getWidth(null), offScreenImage.getHeight(null));

			paintForeground(offScreenGraphics, projection, genGlobe, viewLens);
		}		
	}
	
    public void paint(Graphics g) {
		if (!stopBitmapThread()) return; // thread did not stop

		LensRegion viewLens = getViewLens();
		updateOffScreen(viewLens); // Store old background off screen
		
		if (fullUpdateNow) { // Check for new data to load
			paramFiles.paint(g, genGlobe, projection, viewLens);
			viewLens = getViewLens(); // update in case scale or center has changed
			
			// In case something new was loaded - instant feedback of foreground
			updateOffScreen(viewLens); // Store old background off screen
		}
		updateOnScreen(g);
		drawProgress = 100;
		
		// Do we need time consuming draw operation?
		if (drawSatelliteImage || dayNight) {			
			if (fullUpdateNow) {
				drawProgress = 0;
				progressBar.show();
				startBitmapThread(g, viewLens);
				return;
			}
		}		
    }
	
    private void checkOffScreenImage() {
		
		// double/triple buffering
		Dimension d = getSize();

		if (drawStereoscopic) {
			if ( (offScreenImage == null) ||
				 (leftEyeImage == null) ||
				 ((d.width/2) != offScreenImage.getWidth(null)) ||
				 (d.height != offScreenImage.getHeight(null)) ) {

				createOffScreen(d.width/2, d.height);
				createLeftEye(d.width/2, d.height);
				canvasWidth = d.width;
				canvasHeight = d.height;
			}
		}
		else {
			leftEyeImage = null;
			if ( (offScreenImage == null) ||
				 (d.width != offScreenImage.getWidth(null)) ||
				 (d.height != offScreenImage.getHeight(null)) ) {

				createOffScreen(d.width, d.height);
				canvasWidth = d.width;
				canvasHeight = d.height;
			}
		}
	}
	
    void createOffScreen(int width, int height) {
		// Create one buffer for pixel-by-pixel drawing

		int arraySize = width * height;
		offScreenPixels = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			offScreenPixels[i] = 0xFF404040; // Initialize to grey
		}

		offScreenSource = new MemoryImageSource(width, height, 
												offScreenPixels, 
												0, width);
		offScreenSource.setAnimated(true);
		memOffScreenImage = createImage(offScreenSource);
		
		// And a second buffer for Graphics based drawing
		offScreenImage = createImage(width, height);
		try {
			offScreenGraphics = offScreenImage.getGraphics();
			offScreenGraphics.setColor(backgroundColor);
			offScreenGraphics.fillRect(0,0,width,height);
		}
		catch (NullPointerException e) {}
	}

    void createLeftEye(int width, int height) {
		// Create one buffer for pixel-by-pixel drawing

		int arraySize = width * height;
		leftEyePixels = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			leftEyePixels[i] = 0xFF404040; // Initialize to grey
		}
		
		leftEyePixels = new int[width * height];
		leftEyeSource = new MemoryImageSource(width, height, 
												leftEyePixels, 
												0, width);
		leftEyeSource.setAnimated(true);
		memLeftEyeImage = createImage(leftEyeSource);
		
		// And a second buffer for Graphics based drawing
		leftEyeImage = createImage(width, height);
		try {
			leftEyeGraphics = leftEyeImage.getGraphics();
			leftEyeGraphics.setColor(backgroundColor);
			leftEyeGraphics.fillRect(0,0,width,height);
		}
		catch (NullPointerException e) {}
	}
	

    // Everything BEHIND the texture map
    public void paintBackground(Graphics g, Projection p, GenGlobe genGlobe, int width, int height) {
		// Fill everything with black
		g.setColor(backgroundColor);
		g.fillRect(0, 0, width, height);
		
		// Draw background (ocean)
		g.setColor(oceanColor);
		paintOcean(g, p, genGlobe);
		
		// Draw outline
		g.setColor(outlineColor);
		paintOutline(g, p, genGlobe);
    }
	
	// TODO offscreengraphics can change if stereoscopic
    public void paintBitmap(Graphics offScreenGraphics, // always offScreenGraphics?
							Graphics onScreenGraphics, 
							GenGlobe genGlobe, 
							Projection projection, 
							LensRegion viewLens) {
		images.paint(onScreenGraphics, genGlobe, projection, viewLens);
	}
	
	// TODO resizing is slow
    public void setBounds(Rectangle r) {
		super.setBounds(r);
		
		stopBitmapThread();
		fullUpdateNow = false;
		
		// Resize event is captured here
		Dimension d = getSize();
		canvasWidth = d.width;
		canvasHeight = d.height;
		genGlobe.centerX = d.width / 2;
		genGlobe.centerY = d.height / 2;
		
		fullRepaint();
    }
	
    public void setBounds(int x, int y, int width,  int height) {
		super.setBounds(x,y,width,height);
		// Resize event is captured here

		stopBitmapThread();
		fullUpdateNow = false;
		
		Dimension d = getSize();
		canvasWidth = d.width;
		canvasHeight = d.height;
		genGlobe.centerX = d.width / 2;
		genGlobe.centerY = d.height / 2;
		
		fullRepaint();
    }
    
    // *********************
    // *** Mouse actions ***
    // *********************
    int oldMouseX, oldMouseY;
    boolean useOldMouse = false;
    boolean isDragging = false;
	
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
    Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
	
	public void centerOnPosition(double longitude, double latitude) {
		centerOnPosition(new Vector3D(longitude, latitude));
	}
	public void centerOnPosition(Vector3D newCenter) {
		if (newCenter == null) return;
		// newCenter = genGlobe.getInverseOrientation().mult(newCenter);
		newCenter = genGlobe.unrotate(newCenter);
		
		Matrix3D oldOrientation = genGlobe.getOrientation();
		Vector3D oldCenter = new Vector3D();
		oldCenter.element[0] = oldOrientation.element[2][0];
		oldCenter.element[1] = oldOrientation.element[2][1];
		oldCenter.element[2] = oldOrientation.element[2][2];
		
		Vector3D axis = oldCenter.cross(newCenter);
		double angle = Math.asin(axis.length());
		if (oldCenter.dot(newCenter) < 0)
			angle = Math.PI - angle;
		if (angle == 0) return;
		
		// axis = genGlobe.getOrientation().mult(axis);
		axis = genGlobe.rotate(axis);
		Matrix3D adjustment = new Matrix3D();
		adjustment.setAxisAngle(axis, angle);
		genGlobe.changeOrientation(adjustment.mult(genGlobe.getOrientation()));
	}
	
    public void mouseClicked(MouseEvent e) {
		// Recenter globe on clicked point

		tempV3 = rasterToSphere(e.getX(), e.getY(), tempV3);
		if (tempV3 == null) return;
		stopBitmapThread();
		
		tempV3 = genGlobe.rotate(tempV3);
		centerOnPosition(tempV3);
		
		useOldMouse = false;
		isDragging = false;
		
		// Immediate visual feedback of click
		// To avoid coalescing with the following repaint, use update(),
		// [usually a no-no]
		if (stopBitmapThread()) {
			fullUpdateNow = false;
			update(getGraphics());
		}
		
		fullRepaint();
    }
    public void mouseEntered(MouseEvent e) {
		// setCursor(crosshairCursor); // This is handled in mouseMoved
    }
    public void mouseExited(MouseEvent e) {
		setCursor(defaultCursor);
    }
    public void mousePressed(MouseEvent e) {
		oldMouseX = e.getX();
		oldMouseY = e.getY();
		useOldMouse = true;
		setCursor(handCursor);
    }
    public void mouseReleased(MouseEvent e) {
		useOldMouse = false;
		if (isDragging) {
			if (stopBitmapThread()) fastRepaint(); // For quick feedback
			fullRepaint();
			isDragging = false;
		}
		setCursor(crosshairCursor);
    }
    public void mouseDragged(MouseEvent e) {
		stopBitmapThread();
		
		isDragging = true;
		setCursor(handCursor);
		if (useOldMouse) {
			// mouseRotateXY(e);
			Method mouseAction = whatTheMouseDoes;
			// Modified mouse drags do alternate action
			int modifiers = e.getModifiers();
			if (e.isShiftDown() || 
				e.isControlDown() || 
				((modifiers & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) ||
				((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) ||
				e.isAltDown() || 
				e.isMetaDown()) {
				mouseAction = whatTheModifiedMouseDoes;
			}
			MouseEvent argArray[] = {e};
			try {
				mouseAction.invoke(this, argArray);
			} 
			catch (IllegalAccessException ex) {
			 	System.out.println("1" + ex);
			}
			catch (InvocationTargetException ex) {
			 	System.out.println("1" + ex);
			}
			if (stopBitmapThread()) fastRepaint();
		}
		
		oldMouseX = e.getX();
		oldMouseY = e.getY();
		
		useOldMouse = true;
    }
	
	// Convert mouse coordinates proper genglobe
	GenGlobe getCurrentGenGlobe(int rx, int ry) {
		GenGlobe gg;
		if (drawStereoscopic)
			if (rx < canvasWidth/2) gg = leftGenGlobe;
			else gg = rightGenGlobe;
		else gg = genGlobe;
		return gg;
	}

	// Convert mouse coordinates to XYZ
	Vector3D rasterToUnrotatedSphere(int rx, int ry, Vector3D v) {
		double x, y;
		GenGlobe gg = getCurrentGenGlobe(rx, ry);
		if (drawStereoscopic) {
			if (rx < canvasWidth/2) {
				x = gg.planeX(rx);
				y = gg.planeY(ry);
			}
			else {
				x = gg.planeX(rx - canvasWidth/2);
				y = gg.planeY(ry);
			}
		} else {
			x = gg.planeX(rx);
			y = gg.planeY(ry);
		}

		v = projection.vec2DTo3D(x, y, utilityV3);
		if (v == null) {
			setCursor(defaultCursor);
			return null;
		}
		return v;
	}
	
	// Convert mouse coordinates to XYZ
	Vector3D rasterToSphere(int rx, int ry, Vector3D v) {
		v = rasterToUnrotatedSphere(rx, ry, v);
		if (v == null) return null;
		GenGlobe gg = getCurrentGenGlobe(rx, ry);
		v = gg.unrotate(v);
		return v;
	}
	
    String latString, longString;
	String bearingString;
    double degree, minute, second;
    public void mouseMoved(MouseEvent e) {
		// Show the latitude and longitude of the mouse position

		// Save local mouse position for bearing calculation
		tempV3 = rasterToUnrotatedSphere(e.getX(), e.getY(), tempV3);
		if (tempV3 == null) return;
		double localX = tempV3.x();
		double localY = tempV3.y();
		double localZ = tempV3.z();
		
		// tempV3 = genGlobe.getInverseOrientation().mult(tempV3);
		tempV3 = rasterToSphere(e.getX(), e.getY(), tempV3); // includes urotate
		setCursor(crosshairCursor);
		
		// In radians
		double latitude = Math.asin(tempV3.y());
		double longitude = Math.atan2(tempV3.x(), tempV3.z());
		
		// In degrees
		double lambda = longitude * 180 / Math.PI;
		double phi = latitude * 180 / Math.PI;
		
		char latChar = 'N';
		char lonChar = 'E';
		if (lambda < 0) {
			lonChar = 'W';
			lambda = -lambda;
		}
		if (phi < 0) {
			latChar = 'S';
			phi = -phi;
		}
		
		char degreeChar = '\u00B0';

		bearingString = "";
		if (drawBearing) {
			// Distance
			double bearingDistance = Math.acos(localZ) * genGlobe.getKmRadius();
			
			// Angle to north
			double upAngle = (Math.atan2(localX, localY) - genGlobe.northAngle()) * 180/Math.PI;
			while (upAngle < 0) upAngle += 360.0;
			while (upAngle > 360) upAngle -= 360.0;
			
			bearingString += "" + (int) bearingDistance + " km";
			bearingString += "; " + (int) upAngle + degreeChar + " ";
		}
		
		if (tempV3 != null) {
			
			degree = lambda;
			minute = 60.0 * (degree - (int)degree);
			second = 60.0 * (minute - (int)minute);
			longString = "" + (int)(degree) + degreeChar;
			longString += (" " + (int)(minute) + '\'');
			longString += (" " + (int)(second) + '\"');
			longString += (" " + lonChar);
			
			degree = phi;
			minute = 60.0 * (degree - (int)degree);
			second = 60.0 * (minute - (int)minute);
			latString = "" + (int)(degree) + degreeChar;
			latString += (" " + (int)(minute) + '\'');
			latString += (" " + (int)(second) + '\"');
			latString += (" " + latChar);
			
			messageArea.setText(busyString + bearingString + "(" + 
								longString + ",  " + 
								latString + ")"
								);
		}
    }
	
    // The following are mine
	
    void setMouseAction2(String actionName) {
		Class canvasClass = this.getClass();
		Class mouseMethodArgs[] = {(new MouseEvent(this, 0, 0, 0, 0, 0, 0, 
												   false)).getClass()};
		try {
			whatTheMouseDoes = canvasClass.getMethod(actionName, 
													 mouseMethodArgs);
		} catch (NoSuchMethodException ex) {
			System.out.println("2" + ex);
		}
    }
	
    void setMouseAction(String actionName) {
		Class canvasClass = this.getClass();
		Class mouseMethodArgs[] = {(new MouseEvent(this, 0, 0, 0, 0, 0, 0, 
												   false)).getClass()};
		Method rotateXY;
		Method zoom;	
		Method rotateZ;
		try {
			whatTheMouseDoes = canvasClass.getMethod(actionName, 
													 mouseMethodArgs);
			whatTheModifiedMouseDoes = canvasClass.getMethod(actionName, 
															 mouseMethodArgs);
			
			rotateXY = canvasClass.getMethod("mouseRotateXY", mouseMethodArgs);
			zoom = canvasClass.getMethod("mouseZoom", mouseMethodArgs);
			rotateZ = canvasClass.getMethod("mouseRotateZ", mouseMethodArgs);

			if (actionName == "mouseZoom")
				whatTheModifiedMouseDoes = rotateXY;
			if (actionName == "mouseRotateXY")
				whatTheModifiedMouseDoes = zoom;
			if (actionName == "mouseRotateZ")
				whatTheModifiedMouseDoes = rotateXY;

		} catch (NoSuchMethodException ex) {
			System.out.println("2" + ex);
		}
    }
	
    public void mouseRotateXY(MouseEvent e) {
		double deltaX = e.getX() - oldMouseX;
		double deltaY = e.getY() - oldMouseY;
		
		double dragLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		
		if (dragLength < 1) return;
		deltaX = deltaX / dragLength;
		deltaY = deltaY / dragLength;
		
		// 1 - express world rotation in axis/angle format
		Vector3D axis = new Vector3D(-deltaY, -deltaX, 0.0);
		double angle = Math.atan(dragLength / genGlobe.getPixelRadius());
		Matrix3D rotMatrix = new Matrix3D();
		rotMatrix.setAxisAngle(axis, angle);
		
		genGlobe.updateOrientation(rotMatrix);
    }
	
    public void mouseRotateZ(MouseEvent e) {
		double deltaX = e.getX() - oldMouseX;
		double deltaY = e.getY() - oldMouseY;
		
		Vector3D mouseDragVector = new Vector3D(deltaX, -deltaY, 0);
		Vector3D centerToMouse = new Vector3D(e.getX() - genGlobe.centerX, 
											  genGlobe.centerY - e.getY(), 
											  0);
		Vector3D zPart = mouseDragVector.cross(centerToMouse);
		double adjacentSide = centerToMouse.length();
		double oppositeSide = zPart.element[2] / adjacentSide;
		if (adjacentSide < 10) adjacentSide = 10; // prevent blowup at center
		double angle = Math.atan(oppositeSide/adjacentSide);
		
		Vector3D axis = new Vector3D(0.0, 0.0, 1.0);
		Matrix3D rotMatrix = new Matrix3D();
		rotMatrix.setAxisAngle(axis, angle);
		
		genGlobe.updateOrientation(rotMatrix);
    }
    
    public void mouseRotateXYZ(MouseEvent e) {
    }
	
    public void mouseZoom(MouseEvent e) {
		double deltaX = e.getX() - oldMouseX;
		double deltaY = e.getY() - oldMouseY;
		
		double screenScale = getSize().width + getSize().height;
		double zoomScale = (deltaX - deltaY) * 3;
		
		if (zoomScale > screenScale) return; // Don't grow more than double
		if (zoomScale < -screenScale/2) return; // Don't shrink less than half
		
		double zoomRadius = 
			genGlobe.getPixelRadius() *
			(screenScale + zoomScale) / screenScale;
		// Don't let radius go negative, or even ridiculously small
		if (zoomRadius < 10) zoomRadius = 10;
		genGlobe.setPixelRadius(zoomRadius);
    }
}

