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
// Changed data type of borders and labels to GeoCollection, to make it easier for the new ParameterFiles to add to them.
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
    // double planetRadius = 6371; // Radius of earth in kilometers
	
    NightUpdateThread nightUpdateThread;
	DrawBitmapThread drawBitmapThread;
	String busyString = "";
	
    int colorResolution = 30;
    // Colors of different transparency for labels
    Vector labelColors;
	
	int oceanColorInt = 0xFF3060E0;
	int oceanColorInt2 = oceanColorInt - 0xFF000000;
    Color oceanColor = new Color(oceanColorInt2);
    Color outlineColor = new Color(40, 40, 180);
    Color crosshairColor = Color.white;
    // Color equatorColor = new Color(220, 70, 70);
    Color backgroundColor = Color.black;
	Color borderColor = new Color(120,180,50);
	int backgroundColorInt = 0xFF000000;
	Color scaleBarColor = new Color(255, 220, 220); // Pink
	Color shadeBoxColor = new Color(0, 0, 0, 128); // transparent Black
	
    // Define what kinds of objects will be drawn
    volatile boolean fullUpdateNow = true; // Want to draw satellite image on startup
    boolean drawSatelliteImage = true; // Want to draw satellite image in general
    boolean dayNight = true; // Draw day/night terminator
    boolean drawLabels = true;
	boolean drawGraticule = true;
	boolean drawBearing = false;
	boolean drawCoastLines = true;
	boolean drawScaleBar = true;
	
    int canvasWidth, canvasHeight;	
	
    volatile GenGlobe genGlobe = new GenGlobe(6371); // Radius in kilometers
	
	// Objects whose sole purpose is to communicate resolution ranges
	GeoObject allResolution = new GeoObject(-1,-1,-1,-1);
	// Alpha on the graticules is too slow
	GeoObject lowResolution = new GeoObject(0.005, 0.005, 0.150, 0.150);
	GeoObject highResolution = new GeoObject(0.150, 0.150, -1, -1);
	
	DirectionGraticule directionGraticule = new DirectionGraticule(2000, 15, genGlobe);

	GeoCollection graticule = new GeoCollection();
	
    // Vector siteLabels; // All the city labels we are using
	GeoCollection siteLabels = new GeoCollection();
	SiteLabelCollection newSiteLabels;
	
	GeoCollection paramFiles = new GeoCollection();
	
    // For Sinusoidal Projection outline shape
    int numOutlinePoints = 100;
    int xOutlinePoints[] = new int[numOutlinePoints];
    int yOutlinePoints[] = new int[numOutlinePoints];
    
    Projection projection = Projection.ORTHOGRAPHIC;
	
	ImageCollection images = new ImageCollection(this);
    MapBlitter mapBlitter;

	GeoCollection borders = new GeoCollection();
	
    Method whatTheMouseDoes;
    Method whatTheModifiedMouseDoes;
	
    // Whole gang of objects to represent one off-screen image buffer
    int offScreenPixels[];
    public MemoryImageSource offScreenSource;
    public Image memOffScreenImage;
	
    public Image offScreenImage;
    volatile public Graphics offScreenGraphics;
	
    Label messageArea = new Label("");
	
	GlobeView globeViewFrame;
	
    // These two lines form the paradigm for converting from plane to screen
    // Turn abstract plane coordinates into screen pixel coordinates
    // int screenX(double x) {return (int) (x * genGlobe.getPixelRadius()) + centerX;}
    // int screenY(double y) {return centerY - (int) (y * genGlobe.getPixelRadius());}
    // Turn screen coordinates into plane coordinates
    // double planeX(int x) {return (double)(x - centerX) * genGlobe.getInvPixelRadius();}
    // double planeY(int y) {return (double)(centerY - y) * genGlobe.getInvPixelRadius();}
	
    GeoCanvas(int width, int height, GlobeView parent) {
		setSize(width, height);
		canvasWidth = width;
		canvasHeight = height;
		
		globeViewFrame = parent;
		
		// Automatically updates image periodically, if dayNight is set
		nightUpdateThread = new NightUpdateThread("nightUpdateThread");
		nightUpdateThread.start();
		
		labelColors = SiteLabel.fadeColors(new Color(100, 255, 255));
		
		genGlobe.setPixelRadius((width < height) ? width/2.0 : height/2.0);
		
		genGlobe.centerX = width / 2;
		genGlobe.centerY = height / 2;
		
		addMouseListener(this);
		addMouseMotionListener(this);

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
		if (!stopBitmapThread()) return; // Painting thread would not stop
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
	
    // Everything IN FRONT OF the texture map
    synchronized public void paintForeground(Graphics g, LensRegion viewLens) {
		double r = genGlobe.getPixelRadius();
		// double resolution = r / planetRadius; // Pixels per kilometer
		double resolution = genGlobe.getResolution(); // Pixels per kilometer
													  // System.out.println("" + resolution);

		if (drawCoastLines) borders.paint(g, genGlobe, projection, viewLens);
		if (drawGraticule) graticule.paint(g, genGlobe, projection, viewLens);
		
		// Draw the center-focussed elements, such as antenna directions
		if (drawBearing) directionGraticule.paint(g, genGlobe, projection, null);
		
		// Draw outline (again, to clean up edges)
		g.setColor(outlineColor);
		paintOutline(g);
		
		// Draw city labels
		if (drawLabels) siteLabels.paint(g, genGlobe, projection, viewLens);
		
		if (drawScaleBar) paintScaleBar(g, genGlobe, projection, viewLens);
		
		// Cross-hair at center
		g.setColor(crosshairColor);
		g.drawLine(genGlobe.centerX, genGlobe.centerY - 5, genGlobe.centerX, genGlobe.centerY + 5);
		g.drawLine(genGlobe.centerX - 5, genGlobe.centerY, genGlobe.centerX + 5, genGlobe.centerY);
    }
	
	// Paint a scale bar in the lower right corner
	void paintScaleBar(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {
		// What is the longest round distance less than 150 pixels?
		int maxPixelsPerBar = 120;
		double kilometersPerPixel = 1.0 / genGlobe.getResolution();
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
		
		int startX = canvasWidth - 40 - maxPixelsPerBar;
		int startY = canvasHeight - 20;
		
		// Shade box for light backgrounds
		g.setColor(shadeBoxColor);
		g.fillRect(startX - 8, startY - 8, canvasWidth - startX + 8, canvasHeight - startY + 8);
		
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
	
    void setNorthUp(boolean state) {
		genGlobe.setNorthUp(state);
		if ((globeViewFrame != null) && (globeViewFrame.northUpButton != null)) {
			globeViewFrame.northUpButton.setState(state);
		}
    }
	
    void setDayNight(boolean state) {
		dayNight = state;
		if ((globeViewFrame != null) && (globeViewFrame.dayNightButton != null)) {
			globeViewFrame.dayNightButton.setState(state);
		}
    }
	
    void setCoastLines(boolean state) {
		drawCoastLines = state;
		if ((globeViewFrame != null) && (globeViewFrame.coastsButton != null)) {
			globeViewFrame.coastsButton.setState(state);
		}
    }
	
    void setSiteLabels(boolean state) {
		drawLabels = state;
		if ((globeViewFrame != null) && (globeViewFrame.sitesButton != null)) {
			globeViewFrame.sitesButton.setState(state);
		}
    }
	
    void setGraticules(boolean state) {
		drawGraticule = state;
		if ((globeViewFrame != null) && (globeViewFrame.graticulesButton != null)) {
			globeViewFrame.graticulesButton.setState(state);
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
	
    void setBearing(boolean state) {
		drawBearing = state;
		if ((globeViewFrame != null) && (globeViewFrame.bearingButton != null)) {
			globeViewFrame.bearingButton.setState(state);
		}
    }
	
    void setProjection(Projection p) {
		projection = p;
		if (globeViewFrame != null) {
			globeViewFrame.setProjection(p);
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

	public boolean stopBitmapThread() {
		if ((drawBitmapThread != null) && (drawBitmapThread.isAlive())) {
			images.keepDrawing = false;
			drawBitmapThread.interrupt();
			try {drawBitmapThread.join(2000); // Wait up to two seconds for it to die
			} catch (InterruptedException e) {}
		}
		if ((drawBitmapThread != null) && (drawBitmapThread.isAlive())) return false; // Did not stop
		return true;
	}
	
    // Paint should only be called during infrequent window expose events
    // (because we have redefined update() to not call paint())
    // It is also called to paint the initial image
    public void update(Graphics g) {
		paint(g);
    }
	
    public void paint(Graphics g) {
		if (!stopBitmapThread()) return; // thread did not stop

		checkOffScreenImage();
		LensRegion viewLens = getViewLens();
		if (fullUpdateNow) { // Check for new data to load
			paramFiles.paint(g, genGlobe, projection, viewLens);
			
			// In case something new was loaded - instant feedback of foreground
			paintForeground(g, viewLens);
		}
		
		// Do we need time consuming draw operation?
		if (drawSatelliteImage || dayNight) {
			// Draw old one first
			offScreenGraphics.drawImage(memOffScreenImage, 0, 0, this);		
			if (fullUpdateNow) {
				images.keepDrawing = true;
				drawBitmapThread = new DrawBitmapThread("DrawBitmap", this, g, viewLens);
				drawBitmapThread.start();
			}
			else { // Fast update of foreground
				paintForeground(offScreenGraphics, viewLens);
				g.drawImage(offScreenImage, 0, 0, null);
			}
		}
		else { // Non-bitmap drawing
			paintBackground(offScreenGraphics);
			paintForeground(offScreenGraphics, viewLens);
			g.drawImage(offScreenImage, 0, 0, null);
		}
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
	
    public void paintBitmap(Graphics offScreenGraphics, // always offScreenGraphics?
							Graphics onScreenGraphics, 
							GenGlobe genGlobe, 
							Projection projection, 
							LensRegion viewLens) {
		images.paint(onScreenGraphics, genGlobe, projection, viewLens);
	}
	
    public void setBounds(Rectangle r) {
		super.setBounds(r);
		
		stopBitmapThread();
		
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
		stopBitmapThread();
		// Recenter globe on clicked point
		
		double x = genGlobe.planeX(e.getX());
		double y = genGlobe.planeY(e.getY());
		Vector3D newCenter = new Vector3D();
		newCenter = projection.vec2DTo3D(x, y, newCenter);
		centerOnPosition(newCenter);
		
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
			} catch (Exception ex) {
				System.out.println("1" + ex);
			}
			if (stopBitmapThread()) fastRepaint();
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
		
		if (zoomScale > screenScale) return;
		if (zoomScale < -screenScale/2) return;
		
		double zoomRadius = 
			genGlobe.getPixelRadius() *
			(screenScale + zoomScale) / screenScale;
		// Don't let radius go negative, or even ridiculously small
		if (zoomRadius < 10) zoomRadius = 10;
		genGlobe.setPixelRadius(zoomRadius);
    }
}

