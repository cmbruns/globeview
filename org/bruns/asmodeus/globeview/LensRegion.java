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
	
	boolean overlaps(LensRegion otherLens) {
		double cosAngleRadiusSum = getCosAngleRadius() * otherLens.getCosAngleRadius() - getSinAngleRadius() * otherLens.getSinAngleRadius();
		double cosVectorAngle = getUnitVector().dot(otherLens.getUnitVector());
		if (cosAngleRadiusSum <= cosVectorAngle) return true;
		else return false;
	}
}
