//
//  BoundingBox.java
//  globeview
//
//  Created by Christopher Bruns on 2/23/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//  Rectangular box intersecting unit sphere that defines limits on a GeoObject
//  May be simpler to use than the LensRegion
//  We would like to have a comparison between a BoundingBox and a LensRegion

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;

public class BoundingBox {

	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private double minZ;
	private double maxZ;
	
	private int pointCount;	
	
	BoundingBox() {
		// Initialize to extreme values outside of unit sphere
		// These values will be overwritten by ANY point on the unit sphere sent to includePoint()
		minX = 1.1;
		maxX = -1.1;
		minY = 1.1;
		maxY = -1.1;
		minZ = 1.1;
		maxZ = -1.1;
		pointCount = 0;
	}
	
	int getPointCount() {return pointCount;}
	double getMinX() {return minX;}
	double getMaxX() {return maxX;}
	double getMinY() {return minY;}
	double getMaxY() {return maxY;}
	double getMinZ() {return minZ;}
	double getMaxZ() {return maxZ;}
	
	void addPoint(Vector3D v) {
		pointCount ++;
		if (v.getX() < minX) minX = v.getX();
		if (v.getX() > maxX) maxX = v.getX();
		if (v.getY() < minY) minY = v.getY();
		if (v.getY() > maxY) maxY = v.getY();
		if (v.getZ() < minZ) minZ = v.getZ();
		if (v.getZ() > maxZ) maxZ = v.getZ();
	}
	
	void addBoundingBox(BoundingBox b) {
		pointCount += b.getPointCount();
		if (b.getMinX() < minX) minX = b.getMinX();
		if (b.getMaxX() > maxX) maxX = b.getMaxX();
		if (b.getMinY() < minY) minY = b.getMinY();
		if (b.getMaxY() > maxY) maxY = b.getMaxY();
		if (b.getMinZ() < minZ) minZ = b.getMinZ();
		if (b.getMaxZ() > maxZ) maxZ = b.getMaxZ();
	}
	
	Vector3D extremeCorner = new Vector3D();
	boolean overlaps(LensRegion lens) {

		// If there is uncertainty, return true
		if (lens == null) return true;
		if (pointCount == 0) return true; // Empty BoundingBox -- cannot be certain of rejection

		// We only need to compare the corner of the BoundingBox that is in the same
		// general direction as the LensRegion unitVector
		Vector3D uV = lens.getUnitVector();

		if (uV.x() > 0) extremeCorner.setX(maxX);
		else extremeCorner.setX(minX);
		if (uV.y() > 0) extremeCorner.setY(maxY);
		else extremeCorner.setY(minY);
		if (uV.z() > 0) extremeCorner.setZ(maxZ);
		else extremeCorner.setZ(minZ);
		
		if (extremeCorner.dot(uV) > lens.getCosAngleRadius()) return true;
		else return false;
	}
}
