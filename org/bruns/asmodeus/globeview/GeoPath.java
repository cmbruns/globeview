//
//  GeoPath.java
//  globeview
//
//  Created by Christopher Bruns on 12/23/04.
//  Copyright 2004 __MyCompanyName__. All rights reserved.
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
		boundingBox.addPoint(p.getSpherePoint());
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
				
		if (!checkResolution(genGlobe)) return;
		if (!boundingBox.overlaps(viewLens)) return;
		
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
			if (newVec.overlaps(viewLens)) {				
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
}