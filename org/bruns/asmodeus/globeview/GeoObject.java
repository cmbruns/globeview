package org.bruns.asmodeus.globeview;

// GeoObject.java
// March 20, 2001  Chris Bruns
// Meant to be the new parent class of SiteLabel, BitMap, and PolyVector

// Resolution ranges are provided in order to permit resolution dependend display of objects
// "Full" resolution ranges permit full rendering of object.  Outside of this range, objects should be rendered semi-transparently
//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/03/04 23:58:53  cmbruns
// made overlap comparison to be more generic, using both LensRegions and BoundingBoxes
//
// Revision 1.3  2005/03/02 01:51:19  cmbruns
// Renamed checkResolution() to usableResolution()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

import java.awt.*;

public class GeoObject
{
    // Useful resolution, in units of pixels-per-kilometer
    double minResolution; // default to zero
    double minFullResolution; // Resolution at which this object becomes fully distinct
    double maxFullResolution; // Resolution at which this object becomes fully distinct
    double maxResolution; // default to 100
	
	private BoundingBox boundingBox = null;
	private LensRegion boundingLens = null;
	
    // Constructor
    GeoObject() {
		setMinResolution(0.001);
		setMinFullResolution(0.002);
		
		setMaxFullResolution(50.0);
		setMaxResolution(100.0);
    }
	
	GeoObject(double r1, double r2, double r3, double r4) {
		setMinResolution(r1);
		setMinFullResolution(r2);
		setMaxResolution(r3);
		setMaxFullResolution(r4);
    }
	
    // accessors
    double getMaxResolution() {return maxResolution;}
    double getMinResolution() {return minResolution;}
    double getMaxFullResolution() {return maxFullResolution;}
    double getMinFullResolution() {return minFullResolution;}
	BoundingBox getBoundingBox() {return boundingBox;}
	
    // modifiers
    void setMaxResolution(double d) {maxResolution = d;}
    void setMinResolution(double d) {minResolution = d;}
    void setMaxFullResolution(double d) {maxFullResolution = d;}
    void setMinFullResolution(double d) {minFullResolution = d;}
	
    void setResolution(double r1, double r2, double r3, double r4) {
		setMinResolution(r1);
		setMinFullResolution(r2);
		setMaxResolution(r3);
		setMaxFullResolution(r4);
    }

	void setResolution(GeoObject resolutionObject) {
		setMinResolution(resolutionObject.getMinResolution());
		setMaxResolution(resolutionObject.getMaxResolution());
		setMinFullResolution(resolutionObject.getMinFullResolution());
		setMaxFullResolution(resolutionObject.getMaxFullResolution());		
	}
	
	void extendResolution(GeoObject resolutionObject) {
		if (resolutionObject.getMinResolution() < getMinResolution()) 
			setMinResolution(resolutionObject.getMinResolution());
		if (resolutionObject.getMaxResolution() > getMaxResolution()) 
			setMaxResolution(resolutionObject.getMaxResolution());
		if (resolutionObject.getMinFullResolution() < getMinFullResolution()) 
			setMinFullResolution(resolutionObject.getMinFullResolution());
		if (resolutionObject.getMaxResolution() > getMaxResolution()) 
			setMaxFullResolution(resolutionObject.getMaxFullResolution());
	}

	boolean usableResolution(GenGlobe genGlobe) {
		if ((minResolution > 0) && (minResolution > genGlobe.getResolution())) return false;
		if ((maxResolution > 0) && (maxResolution < genGlobe.getResolution())) return false;
		return true;
	}

	// To avoid popups, include alpha component near resolution boundaries
	// Range is 0-255
	int alphaResolution(GenGlobe genGlobe) {
		if ((minFullResolution <= genGlobe.getResolution()) && (maxFullResolution >= genGlobe.getResolution())) {
			return 255; // solidly within resolution range
		}
		if (!usableResolution(genGlobe)) return 0; // outside of resolution range
		double alpha = 0;
		if (genGlobe.getResolution() < minFullResolution) {
			alpha = (genGlobe.getResolution() - minResolution)/(minFullResolution - minResolution);
		}
		else {alpha = (genGlobe.getResolution() - maxResolution)/(maxFullResolution - maxResolution);}
		if (alpha < 0) return 0;
		if (alpha > 1) return 255;
		int intAlpha = (int) (255.0 * alpha);
		
		return intAlpha;
	}

	// Paint does nothing, but is not abstract so that resolution objects do not need their own class
	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {
	}
	
	boolean overlaps(LensRegion viewLens) {
		if ((viewLens == null) && (boundingBox == null)) return true;  // Uncertainty means truth
		if ((boundingBox != null) &&
			boundingBox.overlaps(viewLens))
			return true;
		if ((viewLens != null) && viewLens.overlaps(viewLens))
			return true;
		return false;
	}
	
	void addBoxPoint(Vector3D p) {
		if (boundingBox == null) boundingBox = new BoundingBox();
		boundingBox.addPoint(p);
	}
	
	void addBoundingBox(GeoObject o) {
		if (o.boundingBox == null) return;
		if (boundingBox == null) boundingBox = new BoundingBox();
		boundingBox.addBoundingBox(o.boundingBox);
	}
	void addBoundingLens(GeoObject o) {
		if (o.boundingLens == null) return;
		LensRegion l2 = o.boundingLens;
		if (boundingLens == null) boundingLens = new LensRegion(l2.getPlanePoint(), l2.getUnitVector());
		else boundingLens.addBoundingLens(o.boundingLens);
	}
	void setLonLatRange(double minLon, double maxLon, double minLat, double maxLat) {
		boundingLens = LensRegion.lonLatRange(minLon, maxLon, minLat, maxLat);
	}
}
