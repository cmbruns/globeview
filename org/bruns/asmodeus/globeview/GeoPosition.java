//
// $Id$
// $Header$
// $Log$
// Revision 1.3  2005/03/05 00:01:03  cmbruns
// Changed nearlyOverlaps() routine to better handle things that are on the far side of the planet.
//
// Revision 1.2  2005/03/01 02:13:13  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

public class GeoPosition
{
    private double lambda; // longitude, in Radians
    private double phi; // latitute, in Radians
    private Vector3D coord = new Vector3D();

    GeoPosition() {}
    GeoPosition(double lon, double lat) {
	set(lon, lat);
    }
    GeoPosition(double a, double b, double c) {
	set(a, b, c);
    }
    
    Vector3D getSpherePoint() {
	return coord;
    }
    void set(double lon, double lat) {
	lambda = lon;
	phi = lat;
	double latcoeff = Math.cos(phi);
	coord.set (
		   Math.sin(lambda) * latcoeff, 
		   Math.sin(phi),
		   // Changed sign on Math.cos... to positive
		   // March 19, 2001, to fix label location
		   Math.cos(lambda) * latcoeff);
    }
    void set(double x, double y, double z) {
	coord.set (x, y, z);
	lambda = Math.atan2(x, z);
	phi = Math.asin(y);
    }
    double getLatitude() {return phi;}
    double getLongitude() {return lambda;}

	boolean overlaps(LensRegion lens) {
		// If there is uncertainty, return true
		if (lens == null) return true;		
		if (getSpherePoint().dot(lens.getUnitVector()) > lens.getCosAngleRadius()) return true;
		else return false;
	}
	boolean nearlyOverlaps(LensRegion lens) {
		// If there is uncertainty, return true
		if (lens == null) return true;
		if (getSpherePoint().dot(lens.getUnitVector()) > (lens.getNearCosAngleRadius())) return true;
		if (lens.getNearCosAngleRadius() < -0.60) return true; // If almost the whole planet is shown, everything is "near"
		else return false;
	}
}
