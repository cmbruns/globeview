//
//  GeoPath.java
//  globeview
//
//  Created by Christopher Bruns on 12/23/04.
//  Copyright 2004 __MyCompanyName__. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.5  2005/03/13 21:52:42  cmbruns
// Added alternate drawLine method for observing the direction of coast lines (for debugging data)
//
// Revision 1.4  2005/03/04 23:59:22  cmbruns
// made overlap comparison to be more generic, using both LensRegions and BoundingBoxes
//
// Revision 1.3  2005/03/02 01:51:19  cmbruns
// Renamed checkResolution() to usableResolution()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

// Reimplement GeoShape
// A connected set of vectors, not necessarily closed
// For use in graticules and land boundaries
package org.bruns.asmodeus.globeview;

import java.util.*;
import java.awt.*; // Color

public class GeoPath extends GeoObject 
{
	public Vector point;
	Color color; // One color for the whole line
	boolean rotateNorthOnly = false;  // Whether to rotate this object with the rest of the globe, for direction Graticule
	
	GeoPath() {
		point = new Vector();
	}
	
	void addPoint(double lambda, double phi) {
		GeoPosition pos = new GeoPosition(lambda, phi);
		addPoint(pos);
	}

	void addPoint(double x, double y, double z) {
		GeoPosition pos = new GeoPosition(x, y, z);
		addPoint(pos);
	}
	
	void addPoint(GeoPosition p) {
		point.addElement(p);
		addBoxPoint(p.getSpherePoint());
	}
	
	void setColor(Color c) {
		color = c;
	}
	
	Vector3D getSpherePoint(int i) {
		return ((GeoPosition) point.elementAt(i)).getSpherePoint();
	}
	
	boolean continueLine = false;
	// Need two actual points at once
	Vector2D lastScreenTemp = new Vector2D();
	Vector2D newScreenTemp = new Vector2D();
	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {

		if (point.size() < 1) return;
				
		if (!usableResolution(genGlobe)) return;
		if (!overlaps(viewLens)) return;
		
		g.setColor(color);

		int alpha = alphaResolution(genGlobe);
		if (alpha <= 10) return;
		if (alpha < 240) {
			Color alphaColor = new Color(color.getRed(), 
										 color.getGreen(), 
										 color.getBlue(),
										 alpha);
			g.setColor(alphaColor);
		}
		
		int i;
		
		GeoPosition lastVec = (GeoPosition) point.elementAt(0);
		
		Vector3D lv = new Vector3D();
		lv = lv.copy(lastVec.getSpherePoint());
		if (rotateNorthOnly) lv = genGlobe.rotateNorthOnly(lv);
		else lv = genGlobe.rotate(lv);

		Vector2D lastScreen = 
			projection.vec3DTo2D(lv.getX(),
								 lv.getY(),
								 lv.getZ(),
								 lastScreenTemp);
		
		// Check clipping from projection
		if (lastScreen == null) continueLine = false;
		else continueLine = true;

		
		Vector3D nv = new Vector3D();
		// Start at zero, so even single points get plotted
		for (i = 0; i < point.size(); ++ i) {
			GeoPosition newVec = (GeoPosition) point.elementAt(i);
			
			// Clip individual point on viewLens
			Vector2D newScreen = null;
			if (newVec.nearlyOverlaps(viewLens)) {				
				// Vector3D nv = genGlobe.getOrientation().mult(newVec.getSpherePoint());
				nv = nv.copy(newVec.getSpherePoint());
				if (rotateNorthOnly) nv = genGlobe.rotateNorthOnly(nv);
				else nv = genGlobe.rotate(nv);
				newScreen = 
					projection.vec3DTo2D(nv.getX(),
										 nv.getY(),
										 nv.getZ(),
										 newScreenTemp);
			}
			
			// Check clipping from projection
			if (newScreen == null) continueLine = false;
			else if (continueLine) {
				int lsx = genGlobe.screenX(lastScreen.getX());
				int lsy = genGlobe.screenY(lastScreen.getY());
				int nsx = genGlobe.screenX(newScreen.getX());
				int nsy = genGlobe.screenY(newScreen.getY());
				// Don't cross seam (for cylindrical projections)
				if ((lastScreen.flag * newScreen.flag) < 0) {
					g.drawLine(lsx, lsy, lsx, lsy);
					g.drawLine(nsx, nsy, nsx, nsy);
				}
				else {
					g.drawLine(lsx, lsy, nsx, nsy);
					//  debug coast directions
					// drawDirectionalLine(g,lsx,lsy,nsx,nsy);
				}
				continueLine = true;
			}
			else continueLine = true;
			lastVec = newVec;
			// lastScreen = newScreen;
			lastScreen = lastScreenTemp.copy(newScreen); // Copy to keep lean number of objects OK
		}
		continueLine = false;
	}

	// For examining direction of lines - green->yellow->red
	Color green = new Color(0,255,0);
	Color yellow = new Color(255,255,0);
	Color red = new Color(255,0,0);
	void drawDirectionalLine(Graphics g, int x1, int y1, int x2, int y2) {
		int xa = (int)(x1 + (x2-x1)*0.33);
		int ya = (int)(y1 + (y2-y1)*0.33);
		int xb = (int)(x1 + (x2-x1)*0.67);
		int yb = (int)(y1 + (y2-y1)*0.67);
		g.setColor(green);
		g.drawLine(x1,y1,xa,ya);
		g.setColor(yellow);
		g.drawLine(xa,ya,xb,yb);
		g.setColor(red);
		g.drawLine(xb,yb,x2,y2);
	}
}