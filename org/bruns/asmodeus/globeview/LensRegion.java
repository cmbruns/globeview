//
//  LensRegion.java
//  globeview
//
//  Created by Christopher Bruns on 2/22/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.5  2005/03/13 22:07:43  cmbruns
// added changeValuesOf() method so that new parameter files can change the parent canvas's viewLens.
//
// Revision 1.4  2005/03/11 00:12:21  cmbruns
// fixed several bugs in LensRegion methods
//
// Revision 1.3  2005/03/05 00:11:01  cmbruns
// Added new functions:
//   getPlanePoint()
//   addBoundingLens()
//   lonLatRange()
//   planeHeight()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//
// Lens-shaped region of the planet formed by cutting the sphere with a plane.
// This shape is extended to include a round-based cone with apex at the center of the planet.
// Intended to be used to compare whether particular elements need to be drawn.
// One lens corresponding to the current view can be back transformed to the standard 
// orientation, then quickly compared to lenses containing various geographic elements.
// Each lens has an angleRadius, with respect to the center of the planet.
// Each lens has a unitVector, pointing from the center of the planet to the center of the 
// circle on the planet surface.
// Two lenses overlap if the sum of their angleRadii is greater than the angle between their
// unit vectors.
// This is more quickly computed using cosines of the angles.
//
// Lens1 overlaps Lens2 iff cos(angleRadius1 + angleRadius2) < cos(unitVector1 angle with unitVector2)
//
// i.e. iff [cos(aR1)*cos(aR2) - sin(aR1)*sin(aR2)] < uV1.dot.uV2
// these sines and cosines can be precomputed for each lens
// So the total marginal cost of each comparison is 5 multiplications and 3 additions/subtractions
// And the inverse view rotation can be applied to the view lens, so no rotation need be
// applied to each geographical element until the lens culling has occured.
package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;

public class LensRegion {
	
	private double angleRadius;
	private double cosAngleRadius;
	private double nearCosAngleRadius; // Cutoff for things to be "near" the lens
	private double sinAngleRadius;
	
	private Vector3D unitVector;

	// Define a LensRegion using two parameters
	// 1) The closest point in the plane that cuts the sphere to the origin/sphere center
	//      this point should be within the unit sphere
	// 2) A unit vector showing the direction from the center of the sphere to the first point
	//      this vector should be colinear with the first one, but may point in the other direction
	LensRegion(Vector3D planeCenterPoint, Vector3D direction) {
		// TODO sanity checks
		setUnitVector(direction);
		setAngleRadius(Math.acos(planeCenterPoint.dot(direction)));
	}

	// Same object, different values
	void changeValues(LensRegion l2) {
		setUnitVector(l2.getUnitVector());
		setAngleRadius(l2.getAngleRadius());
	}
	
	void setUnitVector(Vector3D v) {
		unitVector = v;
	}
	
	void setAngleRadius(double r) {
		angleRadius = r;
		cosAngleRadius = Math.cos(r);
		sinAngleRadius = Math.sin(r);

		double nearAngle = r * 2.0;
		if (nearAngle > Math.PI) nearAngle = Math.PI;
		nearCosAngleRadius = Math.cos(nearAngle);
	}
	double getAngleRadius() {return angleRadius;}
	double getCosAngleRadius() {return cosAngleRadius;}
	double getNearCosAngleRadius() {return nearCosAngleRadius;}
	double getSinAngleRadius() {return sinAngleRadius;}
	Vector3D getUnitVector() {return unitVector;}
	Vector3D getPlanePoint() {return getUnitVector().mult(getCosAngleRadius());}
	
	boolean overlaps(LensRegion otherLens) {
		if (otherLens == null) return true;
		
		// Sum of radii must be less than PI for the following to work
		if ((getAngleRadius() + otherLens.getAngleRadius()) > Math.PI)
			return true;  // They must overlap

		// This only holds when the sum of the angles is < PI
		double cosAngleRadiusSum = getCosAngleRadius() * otherLens.getCosAngleRadius() - getSinAngleRadius() * otherLens.getSinAngleRadius();
		double cosVectorAngle = getUnitVector().dot(otherLens.getUnitVector());
		if (cosAngleRadiusSum <= cosVectorAngle) return true;
		else return false;
	}
	
	void addBoundingLens(LensRegion otherLens) {
		// Identify the smaller angle radius a1, and the larger a2
		// The angle between the unitVectors is a3
		// If a2 > (a1 + a3), then LensRegion2 contains both LensRegions
		// Otherwise, the angle radius of the combined regions is (a1 + a2 + a3) / 2
		// And the central vector is v2 rotated by (a1 - a2 + a3)/2 toward v1
		double a1, a2, a3;
		Vector3D v1, v2;
		if (getAngleRadius() >= otherLens.getAngleRadius()) {
			a1 = otherLens.getAngleRadius();
			v1 = otherLens.getUnitVector();
			a2 = getAngleRadius();
			v2 = getUnitVector();
		}
		else {
			a2 = otherLens.getAngleRadius();
			v2 = otherLens.getUnitVector();
			a1 = getAngleRadius();
			v1 = getUnitVector();			
		}
		a3 = Math.acos(v1.dot(v2));

		if ((a2 >= (a1 + a3)) || 
			(a2 >= Math.PI)) {
			setUnitVector(v2);
			setAngleRadius(a2);
			return;
		}
		
		else {
			double newAngleRadius = (a1 + a2 + a3) / 2.0;

			Matrix3D rotMat = new Matrix3D();
			Vector3D axis = v1.cross(v2).unit(); // Is this backwards? (I don't think so)
			double shiftAngle = (a1 - a2 + a3) / 2.0;
			rotMat.setAxisAngle(axis, shiftAngle);
			Vector3D newUnitVector = rotMat.mult(v2).unit();

			// Did I get the rotation backwards?
			if (newUnitVector.dot(v1) < v2.dot(v1)) {
				System.out.println("ERROR: rotation is backwards for combining LensRegions!!!");
				System.out.println("v2  :" + v2 + a2);
				System.out.println("v1  :" + v1 + a1);
				System.out.println("new :" + newUnitVector + newAngleRadius);
			}
			
			setAngleRadius(newAngleRadius);
			setUnitVector(newUnitVector);
			
			return;
		}
	}
	
	// Generate a lensRegion enclosing a range of latitudes/longitudes
	static LensRegion lonLatRange(double minLon, double maxLon, double minLat, double maxLat) {

		// Though not perfect, this should be OK as the central vector of the lens
		double centerLon = (maxLon + minLon) / 2.0;
		double centerLat = (maxLat + minLat) / 2.0;
		Vector3D centerVec = new Vector3D(centerLon, centerLat);

		double height = 1.0; // Start with infinitesimal lens
		double testHeight = 2.0;

		// Corners
		testHeight = planeHeight(minLon, minLat, centerVec);
		if (testHeight < height) height = testHeight;

		testHeight = planeHeight(minLon, maxLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		testHeight = planeHeight(maxLon, minLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		testHeight = planeHeight(maxLon, maxLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		// Middles
		testHeight = planeHeight(centerLon, minLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		testHeight = planeHeight(centerLon, maxLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		testHeight = planeHeight(minLon, centerLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		testHeight = planeHeight(maxLon, centerLat, centerVec);
		if (testHeight < height) height = testHeight;
		
		// For very large ranges (greater than PI) that are asymmetric, the
		// above points are not enough, but ehhh, close enough
		
		Vector3D planePoint = centerVec.mult(height);
		
		return new LensRegion(planePoint, centerVec);
	}
	
	// For use by LatLonRange
	static double planeHeight(double lon, double lat, Vector3D center) {
		Vector3D testVec = new Vector3D(lon, lat);
		return testVec.dot(center);
	}
}
