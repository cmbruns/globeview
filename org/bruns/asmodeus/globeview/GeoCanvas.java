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
    // double planetRadius = 6371; // Radius of earth in kilometers
	
    NightUpdateThread nightUpdateThread;
	
    int colorResolution = 30;
    // Colors of different transparency for labels
    Vector labelColors;
	
	double twilightWidth = 0.05; // size of the twilight zone, in radians

	int oceanColorInt = 0xFF3060E0;
	int oceanColorInt2 = oceanColorInt - 0xFF000000;
    Color oceanColor = new Color(oceanColorInt2);
    Color outlineColor = new Color(40, 40, 180);
    Color crosshairColor = Color.white;
    // Color equatorColor = new Color(220, 70, 70);
    Color backgroundColor = Color.black;
	Color borderColor = new Color(120,180,50);
	
    // Define what kinds of objects will be drawn
    boolean fullUpdateNow = true; // Want to draw satellite image on startup
    boolean drawSatelliteImage = true; // Want to draw satellite image in general
    boolean dayNight = true; // Draw day/night terminator
    boolean drawLabels = true;
	boolean drawGraticule = true;
	boolean drawBearing = false;
	boolean drawCoastLines = true;
	
    int canvasWidth, canvasHeight;	
	
    GenGlobe genGlobe = new GenGlobe(6371); // Radius in kilometers
	
	// Objects whose sole purpose is to communicate resolution ranges
	GeoObject allResolution = new GeoObject(-1,-1,-1,-1);
	// Alpha on the graticules is too slow
	GeoObject lowResolution = new GeoObject(0.005, 0.005, 0.150, 0.150);
	GeoObject highResolution = new GeoObject(0.150, 0.150, -1, -1);
	
	DirectionGraticule directionGraticule = new DirectionGraticule(2000, 15, genGlobe);
	LatitudeGraticule latitudeGraticule1 = new LatitudeGraticule(Math.PI/6, lowResolution);
	LatitudeGraticule latitudeGraticule2 = new LatitudeGraticule(Math.PI/18, highResolution);
	
    Vector siteLabels; // All the city labels we are using
	SiteLabelCollection newSiteLabels;
	
    // For Sinusoidal Projection outline shape
    int numOutlinePoints = 100;
    int xOutlinePoints[] = new int[numOutlinePoints];
    int yOutlinePoints[] = new int[numOutlinePoints];
    
    Projection projection = Projection.ORTHOGRAPHIC;
	
    MapBlitter mapBlitter;
	GeoPathCollection borders;
	
    Method whatTheMouseDoes;
    Method whatTheModifiedMouseDoes;
	
    // Whole gang of objects to represent one off-screen image buffer
    int offScreenPixels[];
    private MemoryImageSource offScreenSource;
    private Image memOffScreenImage;
	
    private Image offScreenImage;
    private Graphics offScreenGraphics;
	
    Label messageArea = new Label("");
	
    // These two lines form the paradigm for converting from plane to screen
    // Turn abstract plane coordinates into screen pixel coordinates
    // int screenX(double x) {return (int) (x * genGlobe.getPixelRadius()) + centerX;}
    // int screenY(double y) {return centerY - (int) (y * genGlobe.getPixelRadius());}
    // Turn screen coordinates into plane coordinates
    // double planeX(int x) {return (double)(x - centerX) * genGlobe.getInvPixelRadius();}
    // double planeY(int y) {return (double)(centerY - y) * genGlobe.getInvPixelRadius();}
	
    GeoCanvas(int width, int height, URL mapURL, URL siteURL, URL borderURL) {
		setSize(width, height);
		canvasWidth = width;
		canvasHeight = height;
		
		// Automatically updates image periodically, if dayNight is set
		nightUpdateThread = new NightUpdateThread("nightUpdateThread");
		nightUpdateThread.start();
		
		labelColors = SiteLabel.fadeColors(new Color(100, 255, 255));
		
		genGlobe.setPixelRadius((width < height) ? width/2.0 : height/2.0);
		
		genGlobe.centerX = width / 2;
		genGlobe.centerY = height / 2;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		mapBlitter = MapBlitter.readMap(mapURL, this);
		// Build some city labels		
		newSiteLabels = new SiteLabelCollection(siteURL);
		borders = new GeoPathCollection(borderURL, lowResolution, borderColor);
		
		createOffScreen(width, height);
		
    }
	
    public void fastRepaint() {
		// Most repaints should postpone auto-painting
		if (nightUpdateThread != null && nightUpdateThread.isAlive()) {
			nightUpdateThread.interrupt();
		}
		fullUpdateNow = false;
		repaint();
		// nightUpdateThread.interrupt();
    }
	
    public void fullRepaint() {
		// Most repaints should postpone auto-painting
		if (nightUpdateThread != null && nightUpdateThread.isAlive()) {
			nightUpdateThread.interrupt();
		}	
		fullUpdateNow = true;
		repaint();
		// nightUpdateThread.interrupt();
    }
	
    // Projection-specific outline of map
    public void paintOutline(Graphics g) {
		int type = projection.getBackgroundType();
		
		double r = genGlobe.getPixelRadius();
		double param = projection.getBackgroundParameter() * r;
		
		double diameter = r + r;
		Dimension d = getSize(); // (size of canvas)
		
		if (type == Projection.BKGD_INFPLANE) {
			// Infinite plane, there is no "outline"
		}
		if (type == Projection.BKGD_VSTRIPE) {
			// Vertical stripe, like Mercator
			g.drawLine(genGlobe.centerX - (int) param, 0, 
					   genGlobe.centerX - (int) param, d.height);
			g.drawLine(genGlobe.centerX + (int) param, 0, 
					   genGlobe.centerX + (int) param, d.height);
		}
		else if (type == Projection.BKGD_RECTANGLE) {
			// Rectangle, (2 x 1 aspect ratio)
			g.drawRect(genGlobe.centerX - (int) param, genGlobe.centerY - (int) (param / 2.0), 
					   (int) (param + param), (int) param);
		}
		else if (type == Projection.BKGD_CIRCLE) {
			// Full projection is shaped like a circle
			g.drawArc(genGlobe.centerX - (int) param, genGlobe.centerY - (int) param, 
					  (int) (param + param), (int) (param + param), 
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
				
				xOutlinePoints[i] += genGlobe.centerX;
				yOutlinePoints[i] += genGlobe.centerY;
			}
			g.drawPolygon(xOutlinePoints, yOutlinePoints, numOutlinePoints);
		}
    }
	
    // FILLED Projection-specific outline of map
    public void paintOcean(Graphics g) {
		int type = projection.getBackgroundType();
		
		double r = genGlobe.getPixelRadius();
		double param = projection.getBackgroundParameter() * r;
		
		double diameter = r + r;
		Dimension d = getSize(); // (size of canvas)
		
		if (type == Projection.BKGD_INFPLANE) {
			g.fillRect(0,0,getSize().width,getSize().height);
		}
		if (type == Projection.BKGD_VSTRIPE) {
			// Vertical stripe, like Mercator
			g.fillRect(genGlobe.centerX - (int) param, 0,
					   (int) (2 * param + 0.5), d.height);
		}
		else if (type == Projection.BKGD_RECTANGLE) {
			// Rectangle, (2 x 1 aspect ratio)
			g.fillRect(genGlobe.centerX - (int) param, genGlobe.centerY - (int) (param / 2.0), 
					   (int) (param + param), (int) param);
		}
		else if (type == Projection.BKGD_CIRCLE) {
			// Full projection is shaped like a circle
			g.fillArc(genGlobe.centerX - (int) param, genGlobe.centerY - (int) param, 
					  (int) (param + param), (int) (param + param), 
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
				
				xOutlinePoints[i] += genGlobe.centerX;
				yOutlinePoints[i] += genGlobe.centerY;
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
	
	// For benchmarking
	int updateCount = 0;
	int fullUpdateCount = 0;
	
	int otherTime = 0; // 5 - 20 ms
	int checkTime = 0;
	int paintBackgroundTime = 0;
	int paintBitmapTime = 0;
	int paintForegroundTime = 0; // 65 - 180 ms
	int drawImageTime = 0; // 2 - 6 ms
	
	int foreOtherTime = 0;
	int foreCoastTime = 0;
	int foreGratTime = 0;
	int foreGrat2Time = 0;
	int foreBearingTime = 0;
	int foreLabelTime = 0;
	int foreOutlineTime = 0;
	int foreCrossHairTime = 0;
	
    // Everything IN FRONT OF the texture map
    public void paintForeground(Graphics g) {
		double r = genGlobe.getPixelRadius();
		// double resolution = r / planetRadius; // Pixels per kilometer
		double resolution = genGlobe.getResolution(); // Pixels per kilometer
													  // System.out.println("" + resolution);
		int i;
		
		// Compute viewable LensArea
		// Get the z value at each of the 4 corners of the display
		// If any come up null, use the minimum z for that projection
		double lensZ = projection.getMinimumZ(); // Start with worst case
		// Upper left corner - all projections are symmetric at the corners
		// This one point should be enough
		Vector3D cornerPoint = new Vector3D();
		cornerPoint = projection.vec2DTo3D(genGlobe.planeX(0),
										   genGlobe.planeY(0),
										   cornerPoint);
		if (cornerPoint != null) lensZ = cornerPoint.z();		
		Vector3D lensUnitVector = new Vector3D(0.0, 0.0, 1.0);
		Vector3D lensPlanePoint = new Vector3D(0.0, 0.0, lensZ);
		lensUnitVector = genGlobe.unrotate(lensUnitVector);
		lensPlanePoint = genGlobe.unrotate(lensPlanePoint);
		LensRegion viewLens = new LensRegion(lensPlanePoint, lensUnitVector);
		
		if (countBenchMark) foreOtherTime += timeIncrement();

		if (drawCoastLines) borders.paint(g, genGlobe, projection, viewLens);
		if (countBenchMark) foreCoastTime += timeIncrement();

		if (drawGraticule) latitudeGraticule1.paint(g, genGlobe, projection, viewLens);
		if (countBenchMark) foreGratTime += timeIncrement();
		if (drawGraticule) latitudeGraticule2.paint(g, genGlobe, projection, viewLens);
		if (countBenchMark) foreGrat2Time += timeIncrement();
		
		// Draw the center-focussed elements, such as antenna directions
		if (drawBearing) directionGraticule.paint(g, genGlobe, projection, null);
		if (countBenchMark) foreBearingTime += timeIncrement();
		
		// Draw outline (again, to clean up edges)
		g.setColor(outlineColor);
		// projection.paintOutline(g);
		paintOutline(g);
		if (countBenchMark) foreOutlineTime += timeIncrement();
		
		// Matrix3D m3 = genGlobe.getOrientation();
		
		// Draw city labels
		if (drawLabels) {
			newSiteLabels.paint(g, genGlobe, projection, viewLens);
		}
		if (countBenchMark) foreLabelTime += timeIncrement();
		
		// Cross-hair at center
		g.setColor(crosshairColor);
		g.drawLine(genGlobe.centerX, genGlobe.centerY - 5, genGlobe.centerX, genGlobe.centerY + 5);
		g.drawLine(genGlobe.centerX - 5, genGlobe.centerY, genGlobe.centerX + 5, genGlobe.centerY);
		if (countBenchMark) foreCrossHairTime += timeIncrement();
    }
	
    void setNorthUp(boolean state) {
		genGlobe.setNorthUp(state);
    }
	
    void createOffScreen(int width, int height) {
		// Create one buffer for pixel-by-pixel drawing
		offScreenPixels = new int[width * height];
		offScreenSource = new MemoryImageSource(width, height, 
												offScreenPixels, 
												0, width);
		offScreenSource.setAnimated(true);
		memOffScreenImage = createImage(offScreenSource);
		
		// And a second buffer for Graphics based drawing
		offScreenImage = createImage(width, height);
    }
	
    // Paint should only be called during infrequent window expose events
    // (because we have redefined update() to not call paint())
    // It is also called to paint the initial image
    public void paint(Graphics g) {
		update(g);
    }
	
	// TODO - watch time variables for benchmarking
	// number of milliseconds since this function was last called
	boolean countBenchMark = false;
	boolean timeStarted = false;
	long oldTime = 0;
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

	void flushBenchMarks() {
		if (updateCount > 0) {
			System.out.println();
			System.out.println("otherTime = " + otherTime / updateCount);
			System.out.println("checkTime = " + checkTime / updateCount);
			System.out.println("paintBackgroundTime = " + paintBackgroundTime / updateCount);
		}
		if (fullUpdateCount > 0) {
			System.out.println("paintBitmapTime = " + paintBitmapTime / fullUpdateCount);
		}		
		if (updateCount > 0) {
			System.out.println("paintForegroundTime = " + paintForegroundTime / updateCount);
			System.out.println("drawImageTime = " + drawImageTime / updateCount);
			
			System.out.println();
			System.out.println("foreOtherTime = " + foreOtherTime / updateCount);
			System.out.println("foreCoastTime = " + foreCoastTime / updateCount);
			System.out.println("foreGratTime = " + foreGratTime / updateCount);
			System.out.println("foreGrat2Time = " + foreGrat2Time / updateCount);
			System.out.println("foreBearingTime = " + foreBearingTime / updateCount);
			System.out.println("foreLabelTime = " + foreBearingTime / updateCount);
			System.out.println("foreOutlineTime = " + foreOutlineTime / updateCount);
			System.out.println("foreCrossHairTime = " + foreCrossHairTime / updateCount);
		}
		
		updateCount = 0;
		fullUpdateCount = 0;
		otherTime = 0;
		checkTime = 0;
		paintBackgroundTime = 0;
		paintBitmapTime = 0;
		paintForegroundTime = 0;
		drawImageTime = 0;

		foreOtherTime = 0;
		foreCoastTime = 0;
		foreGratTime = 0;
		foreGrat2Time = 0;
		foreBearingTime = 0;
		foreLabelTime = 0;
		foreOutlineTime = 0;
		foreCrossHairTime = 0;		
	}

    // This is the "cheap" update that should occur during mouse-drag animation
    public void update(Graphics g) {
		if (countBenchMark) {
			otherTime += timeIncrement();
			updateCount ++;
		}

		checkOffScreenImage();
		if (countBenchMark) checkTime += timeIncrement();
		
		// 1 - paint background to OSI
		paintBackground(offScreenGraphics);
		if (countBenchMark) paintBackgroundTime += timeIncrement();
		
		// Handle full image updates here
		if (fullUpdateNow) {
			// 1.5 paint background to screen
			// paintBackground(g);
			
			// Paint bitmap simultaneously to screen and memOffScreenImage
			if (drawSatelliteImage || dayNight) {
				paintBitmap(offScreenGraphics, g);
				// 4 - copy raw memOSI to OSI
				offScreenGraphics.drawImage(memOffScreenImage, 0, 0, null);
				if (countBenchMark) {
					paintBitmapTime += timeIncrement();
					fullUpdateCount ++;
				}
			}
		}
		
		// 5 - paint frontal stuff to OSI
		paintForeground(offScreenGraphics);
		if (countBenchMark) paintForegroundTime += timeIncrement();
		
		// 6 - copy to screen
		g.drawImage(offScreenImage, 0, 0, null);
		if (countBenchMark) drawImageTime += timeIncrement();
		
		if (countBenchMark && ((updateCount % 10) == 0)) flushBenchMarks();
    }
	
    private void checkOffScreenImage() {
		// double/triple buffering
		Dimension d = getSize();
		if ( (offScreenImage == null) ||
			 (d.width != offScreenImage.getWidth(null)) ||
			 (d.height != offScreenImage.getHeight(null)) ) {
			
			createOffScreen(d.width, d.height);
			offScreenGraphics = offScreenImage.getGraphics();
			canvasWidth = d.width;
			canvasHeight = d.height;
		}
    }
	
	
    // Everything BEHIND the texture map
    public void paintBackground(Graphics g) {
		// Fill everything with black
		g.setColor(backgroundColor);
		g.fillRect(0,0,getSize().width,getSize().height);
		
		// Draw background (ocean)
		g.setColor(oceanColor);
		paintOcean(g);
		
		// Draw outline
		g.setColor(outlineColor);
		paintOutline(g);
    }
	
    // The texture map, simultaneously to two destinations
    // g1 is the off-screen image
    // g2 is the on-screen image
    // Try for faster version -
    //  No new objects
    //  No inner loop stack variables
    //  No function calls
    int stripWidth = 10; // How often to update progress on-screen
    Vector3D sunVector;
    public void paintBitmap(Graphics g1, Graphics onScreenGraphics) {
		setCursor(waitCursor); // Could be slow
		
		if (dayNight) {
			// Measure elapsed time
			// Date t1 = new Date();
			// sunVector = getSunVector2();
			Date currentDate = new Date();
			try {
				SimpleDateFormat dateFormat = 
				new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a z");
				// currentDate = dateFormat.parse("01-Jun-2008 12:00:00 PM GMT");
				// currentDate = dateFormat.parse("09-May-2003 07:05:02 PM PST");	  
			} catch (Exception e) {}
			sunVector = Sun.getVector(currentDate);
		}
		
		Vector3D tempv1 = new Vector3D(); // These two are NEVER null
		Vector3D v1 = tempv1; // These may be null
		
		Color color;
		int pixel, darkPixel, shadePixel;
		int width = memOffScreenImage.getWidth(null);
		int height = memOffScreenImage.getHeight(null);
		
		int x, y; // Actual screen coordinates
		double cx, cy; // Scaled, translated screen coordinates
		
		int tx, ty; // Coordinates of texture map pixel
		
		Dimension d = getSize();
		int xMax = d.width;
		int yMax = d.height;
		
		double r = genGlobe.getPixelRadius();
		double rInv = 1.0 / r;
		
		// Image textureMap = mapBlitter.rawImage;
		double dX, dY, kX, kY; // Scale parameters for texture map
		if (mapBlitter != null) {
			kX = mapBlitter.kX;
			kY = mapBlitter.kY;
			dX = mapBlitter.dX;
			dY = mapBlitter.dY;
		}
		double lambda, phi; // longitude, latitude
		
		int mapIndex;
		
		double sinSunElevation = 0;
		// "civil twilight" is six degrees below horizon (0.2 radians)
		double alpha; // Day/night ratio for twilight zone
		double fakeAngle;
		
		for (x = 0; x < xMax; ++x) {
			cx = (x - genGlobe.centerX) * rInv;
			for (y = 0; y < yMax; ++y) {
				mapIndex = x + y * width;
				pixel = 0;
				offScreenPixels[mapIndex] = pixel;
				
				cy = (genGlobe.centerY - y) * rInv;
				
				v1 = projection.vec2DTo3D(cx, cy, tempv1);
				if (v1 == null) continue; // Clipping (function failed)
				
				// Oblique transform (centers globe on point of interest)
				v1 = genGlobe.unrotate(v1);
				
				if ((mapBlitter != null) && (mapBlitter.rawImage != null) && drawSatelliteImage) pixel = mapBlitter.getPixel(v1, 0);
				else pixel = oceanColorInt; // Blue
				
				// Check for day/night
				if (dayNight) {
					sinSunElevation = 
					v1.x() * sunVector.x() +
					v1.y() * sunVector.y() +
					v1.z() * sunVector.z();
					// Make it bright everywhere sun shines 
					// (i.e. twilight is all in shadow)
					// if (sinSunElevation < twilightWidth ) { // not quite day
					if (sinSunElevation < 0) { // not quite day
						darkPixel = pixel >> 1; // Shift to darken
						darkPixel = darkPixel & 0xFF7F7F7F; // High bit zero in each color
						darkPixel = darkPixel | 0xFF000000; // Restore opacity
						
						if (sinSunElevation < -twilightWidth) { // definitely night
							pixel = darkPixel;
						}
						else { // in twilight zone
							   // Make Twilight Zone really smooth by using SINE function
							   // fakeAngle = (sinSunElevation / twilightWidth)  * (Math.PI / 2.0);
							fakeAngle = (2*(sinSunElevation + twilightWidth/2)/ twilightWidth)  * (Math.PI / 2.0);
							alpha = (Math.sin(fakeAngle) + 1.0) / 2.0;
							// alpha = (sinSunElevation + twilightWidth) /
							//     (2.0 * twilightWidth);
							if (alpha > 1.0) alpha = 1.0;
							if (alpha < 0.0) alpha = 0.0;
							
							// Red
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x00FF0000) + 
												alpha * (pixel & 0x00FF0000));
							shadePixel = shadePixel & 0x00FF0000;
							pixel = pixel & 0xFF00FFFF; // clear red
							pixel = pixel + shadePixel; // add shaded red
							
							// Green
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x0000FF00) + 
												alpha * (pixel & 0x0000FF00));
							shadePixel = shadePixel & 0x0000FF00;
							pixel = pixel & 0xFFFF00FF; // clear green
							pixel = pixel + shadePixel; // add shaded green
							
							// Blue
							shadePixel = (int) ((1.0 - alpha) * (darkPixel & 0x000000FF) + 
												alpha * (pixel & 0x000000FF));
							shadePixel = shadePixel & 0x000000FF;
							pixel = pixel & 0xFFFFFF00; // clear blue
							pixel = pixel + shadePixel; // add shaded blue
							
						}
					}
					}
				
				offScreenPixels[mapIndex] = pixel;
				}
			// When bitmap is slow, update on-screen image
			if (((x % stripWidth) == 0) && (x > 0)){
				offScreenSource.newPixels(x - stripWidth,0, stripWidth,yMax);
				onScreenGraphics.drawImage(memOffScreenImage,
										   x - stripWidth,0, x,yMax,
										   x - stripWidth,0, x,yMax,
										   null);
			}
			}
		offScreenSource.newPixels(0,0,width,height);
		setCursor(defaultCursor); // Could be slow
		}
	
    public void setBounds(Rectangle r) {
		super.setBounds(r);
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
	
    public void mouseClicked(MouseEvent e) {
		// Recenter globe on clicked point
		
		double x = genGlobe.planeX(e.getX());
		double y = genGlobe.planeY(e.getY());
		Vector3D newCenter = new Vector3D();
		newCenter = projection.vec2DTo3D(x, y, newCenter);
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
		
		useOldMouse = false;
		isDragging = false;
		
		fullRepaint();
    }
    public void mouseEntered(MouseEvent e) {
		setCursor(crosshairCursor);
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
			fullRepaint();
			isDragging = false;
		}
		setCursor(crosshairCursor);
    }
    public void mouseDragged(MouseEvent e) {
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
			} catch (Exception ex) {
				System.out.println("1" + ex);
			}
			fastRepaint();
			setCursor(moveCursor);
		}
		
		oldMouseX = e.getX();
		oldMouseY = e.getY();
		
		useOldMouse = true;
    }
	
    String latString, longString;
	String bearingString;
    double degree, minute, second;
    public void mouseMoved(MouseEvent e) {
		// Show the latitude and longitude of the mouse position
		double x = genGlobe.planeX(e.getX());
		double y = genGlobe.planeY(e.getY());
		// Vector3D tempV3 = new Vector3D();
		tempV3 = projection.vec2DTo3D(x, y, utilityV3);
		if (tempV3 == null) {
			setCursor(defaultCursor);
			return;
		}
		
		// Save local mouse position for bearing calculation
		double localX = tempV3.x();
		double localY = tempV3.y();
		double localZ = tempV3.z();
		
		// tempV3 = genGlobe.getInverseOrientation().mult(tempV3);
		tempV3 = genGlobe.unrotate(tempV3);
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
			
			messageArea.setText(bearingString + "(" + 
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
		} catch (Exception ex) {
			System.out.println("2" + ex);
		}
    }
	
    void setMouseAction(String actionName) {
		Class canvasClass = this.getClass();
		Class mouseMethodArgs[] = {(new MouseEvent(this, 0, 0, 0, 0, 0, 0, 
												   false)).getClass()};
		Method rotateXY;
		Method zoom;	
		try {
			whatTheMouseDoes = canvasClass.getMethod(actionName, 
													 mouseMethodArgs);
			whatTheModifiedMouseDoes = canvasClass.getMethod(actionName, 
															 mouseMethodArgs);
			
			rotateXY = canvasClass.getMethod("mouseRotateXY", mouseMethodArgs);
			zoom = canvasClass.getMethod("mouseZoom", mouseMethodArgs);
			if (actionName == "mouseZoom")
				whatTheModifiedMouseDoes = rotateXY;
			if (actionName == "mouseRotateXY")
				whatTheModifiedMouseDoes = zoom;
		} catch (Exception ex) {
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
		
		double zoomRadius = 
			genGlobe.getPixelRadius() *
			(screenScale + zoomScale) / screenScale;
		// Don't let radius go negative, or even ridiculously small
		if (zoomRadius < 1) zoomRadius = 1;
		genGlobe.setPixelRadius(zoomRadius);
    }
	}

