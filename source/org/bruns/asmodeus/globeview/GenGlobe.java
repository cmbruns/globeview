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

// GenGlobe.java
// March 22, 2001  Chris Bruns
// Generating Globe for Projections
// Intended to replace GeneratingGlobeTransform (which is internal to GeoCanvas)
//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/03/27 21:44:53  cmbruns
// Added rotateY method for use in 3D
// Added copy() method for use in creating left and right eye views
// Added detailLevel and getAdjustedResolution to support changing the level of detail
// Added offsetX to kludge drawing two images in stereo window.  TODO - this should be made a more general viewport operation.
//
// Revision 1.3  2005/03/04 23:56:50  cmbruns
// created setResolution(d) function
//
// Revision 1.2  2005/03/01 02:13:13  cmbruns
// added cvs headers
//
//

class GenGlobe {
    private Matrix3D obliqueOrientation;
    private Matrix3D inverseOrientation;
    private double planetRadius; // in kilometers
    int centerX;
    int centerY;
	private double detailLevel = 1.0; // Factor to bias detail level
	
	int offsetX = 0; // translate result this many pixels, for stereoscopic

    // Local matrix variables for speed
    double i00;
    double i01;
    double i02;
    double i10;
    double i11;
    double i12;
    double i20;
    double i21;
    double i22;

    double r00;
    double r01;
    double r02;
    double r10;
    double r11;
    double r12;
    double r20;
    double r21;
    double r22;

    boolean northUp = true;

    // Local variables for the NorthUp calculation
    private Vector3D zAxis;
    private Vector3D xAxis;
	private Vector3D yAxis = new Vector3D(0,1,0);
    private Vector3D axis;
    private Matrix3D diffMat;
    double angle;

    // This important variable serves several important functions
    // 1 - It is the radius, in pixels, of the generating globe
    // 2 - It is the resolution, in radians per pixel, of the raster image
    //   (in the center anyway)
    private double pixelRadius;
    private double invPixelRadius;
    private double resolution; // Pixels per kilometer at center

    GenGlobe(double radius) {
        planetRadius = radius;
		obliqueOrientation = new Matrix3D();
		inverseOrientation = new Matrix3D();
		
		diffMat = new Matrix3D();
		zAxis = new Vector3D(0.0, 0.0, 1.0);
		xAxis = new Vector3D(1.0, 0.0, 0.0);
		axis = new Vector3D(0,0,0);
		updateLocals();
    }

	GenGlobe copy() {
		GenGlobe answer = new GenGlobe(getKmRadius());

		answer.obliqueOrientation = obliqueOrientation.copy();
		answer.inverseOrientation = inverseOrientation.copy();
		answer.northUp = northUp;
		answer.centerX = centerX;
		answer.centerY = centerY;
		answer.detailLevel = detailLevel;
		answer.pixelRadius = pixelRadius;
		answer.invPixelRadius = invPixelRadius;
		answer.resolution = resolution;

		answer.updateLocals();
		return answer;
	}

	// For stereoscopic display, rotate about Y axis
	void rotateY(double angle) {
		Matrix3D yRot = new Matrix3D();
		yRot.setAxisAngle(yAxis, angle);
		obliqueOrientation = yRot.mult(obliqueOrientation);
		inverseOrientation = obliqueOrientation.transpose();
		updateLocals();
	}
	
	double getPlanetRadius() {return planetRadius;}
	
	double northAngle() // angle to north, from center point, in radians
	{
		double angle = 0;
		
	    if ((obliqueOrientation.element[0][1] > 0.001) ||
			(obliqueOrientation.element[0][1] < -0.001)) {
			double nX = obliqueOrientation.element[0][1];
			double nY = obliqueOrientation.element[1][1];
			angle = Math.atan2(nX,nY);
	    }
		return angle;
	}
	
    int screenX(double x) {return (int) (x * getPixelRadius()) + centerX + offsetX;}
    int screenY(double y) {return centerY - (int) (y * getPixelRadius());}
    double planeX(int x) {return (double)(x - centerX - offsetX) * getInvPixelRadius();}
    double planeY(int y) {return (double)(centerY - y) * getInvPixelRadius();}
 
    Vector3D utilityVector3D = new Vector3D();
    // Turn standard orientation point into rotated point
    Vector3D rotate(Vector3D v) {
	utilityVector3D.element[0] = 
	    r00*v.element[0] + 
	    r01*v.element[1] + 
	    r02*v.element[2];
	utilityVector3D.element[1] = 
	    r10*v.element[0] + 
	    r11*v.element[1] + 
	    r12*v.element[2];
	utilityVector3D.element[2] = 
	    r20*v.element[0] + 
	    r21*v.element[1] + 
	    r22*v.element[2];
	v.element[0] = utilityVector3D.element[0];
	v.element[1] = utilityVector3D.element[1];
	v.element[2] = utilityVector3D.element[2];
	return v;
    }

    // Rotate about Z axis only
    Vector3D rotateNorthOnly(Vector3D v) {
		double angle = northAngle();
		diffMat.setAxisAngle(zAxis, angle);
		v = diffMat.mult(v);
		return v;
    }
	
    // Rotate transformed point to standard orientation
    Vector3D unrotate(Vector3D v) {
	// Oblique transform (centers globe on point of interest)
	utilityVector3D.element[0] = 
	    i00*v.element[0] + 
	    i01*v.element[1] + 
	    i02*v.element[2];
	utilityVector3D.element[1] = 
	    i10*v.element[0] + 
	    i11*v.element[1] + 
	    i12*v.element[2];
	utilityVector3D.element[2] = 
	    i20*v.element[0] + 
	    i21*v.element[1] + 
	    i22*v.element[2];
	v.element[0] = utilityVector3D.element[0];
	v.element[1] = utilityVector3D.element[1];
	v.element[2] = utilityVector3D.element[2];
	return v;
    }

    void updateOrientation(Matrix3D m) {

	if (northUp) {

	    // 1 - if north pole is off-center, this is a brand new "north up"
	    //   situation, so rotate about Z-axis, to preserve center point
	    if ((obliqueOrientation.element[0][1] > 0.001) ||
		(obliqueOrientation.element[0][1] < -0.001)) {
		double nX = obliqueOrientation.element[0][1];
		double nY = obliqueOrientation.element[1][1];
		angle = -Math.atan2(nX,nY);
		diffMat.setAxisAngle(zAxis, angle);
		obliqueOrientation = diffMat.mult(obliqueOrientation);
	    }

	    // 2 - if north pole is below Y==0, ease it back up, about X-axis
	    if (obliqueOrientation.element[1][1] < 0) {
		double nZ = obliqueOrientation.element[2][1];
		double nY = obliqueOrientation.element[1][1];
		if (nZ > 0) angle = -Math.atan2(nY,nZ);
		else angle = Math.atan2(nY,-nZ);
		diffMat.setAxisAngle(xAxis, angle);
		obliqueOrientation = diffMat.mult(obliqueOrientation);
	    }

	    // 3 - rotate about north pole axis (left/right)
	    {
		double nX = m.element[0][0];
		double nZ = m.element[2][0];

		angle = Math.atan2(nZ, nX); // rotation about Y-axis
		axis.set(obliqueOrientation.element[0][1],
			 obliqueOrientation.element[1][1],
			 obliqueOrientation.element[2][1]); // North pole
		diffMat.setAxisAngle(axis, angle);
		obliqueOrientation = diffMat.mult(obliqueOrientation);
	    }

	    // 4 - rotate about X-axis (up/down)
	    {
		double nY = m.element[1][1];
		double nZ = m.element[2][1];
		angle = -Math.atan2(nZ, nY); // rotation about X-axis
		diffMat.setAxisAngle(xAxis, angle);
		obliqueOrientation = diffMat.mult(obliqueOrientation);

		// if north pole is below Y==0, ease it back up, about X-axis
		if (obliqueOrientation.element[1][1] < 0) {
		    nZ = obliqueOrientation.element[2][1];
		    nY = obliqueOrientation.element[1][1];
		    if (nZ > 0) angle = -Math.atan2(nY,nZ);
		    else angle = Math.atan2(nY,-nZ);
		    diffMat.setAxisAngle(xAxis, angle);
		    obliqueOrientation = diffMat.mult(obliqueOrientation);
		}
	    }
	}
	else {
	    obliqueOrientation = m.mult(obliqueOrientation);
	}
	inverseOrientation = obliqueOrientation.transpose();
	updateLocals();
    }

    // Set orientation to that specified.  Keep center point put, even if "North Up"
    void changeOrientation(Matrix3D m) {

	obliqueOrientation = m;
	if (northUp) {
	    if ((obliqueOrientation.element[0][1] > 0.001) ||
		(obliqueOrientation.element[0][1] < -0.001)) {
		double nX = obliqueOrientation.element[0][1];
		double nY = obliqueOrientation.element[1][1];
		angle = -Math.atan2(nX,nY);
		diffMat.setAxisAngle(zAxis, angle);
		obliqueOrientation = diffMat.mult(obliqueOrientation);
	    }
	}
	inverseOrientation = obliqueOrientation.transpose();
	updateLocals();
    }

    private void updateLocals() {
	i00 = inverseOrientation.element[0][0];
	i01 = inverseOrientation.element[0][1];
	i02 = inverseOrientation.element[0][2];
	i10 = inverseOrientation.element[1][0];
	i11 = inverseOrientation.element[1][1];
	i12 = inverseOrientation.element[1][2];
	i20 = inverseOrientation.element[2][0];
	i21 = inverseOrientation.element[2][1];
	i22 = inverseOrientation.element[2][2];

	r00 = obliqueOrientation.element[0][0];
	r01 = obliqueOrientation.element[0][1];
	r02 = obliqueOrientation.element[0][2];
	r10 = obliqueOrientation.element[1][0];
	r11 = obliqueOrientation.element[1][1];
	r12 = obliqueOrientation.element[1][2];
	r20 = obliqueOrientation.element[2][0];
	r21 = obliqueOrientation.element[2][1];
	r22 = obliqueOrientation.element[2][2];
    }

    // Put North at the top of the map
    // by rotating a certain amount about the Z-axis
    void setNorthUp(boolean state) {
	if (state) {
	    // What happens to the (0,1,0) Vector? (North Pole)
	    // The transformed X-coordinate should be zero
	    // The transformed Y-coordinate should be positive
	    double nX = obliqueOrientation.element[0][1];
	    double nY = obliqueOrientation.element[1][1];
	    angle = -Math.atan2(nX,nY);
	    diffMat.setAxisAngle(zAxis, angle);
	    updateOrientation(diffMat);
	    northUp = true;
	}
	else {
	    northUp = false;
	}
    }

    void setPixelRadius(double r) {
		pixelRadius = r;
		invPixelRadius = 1.0/pixelRadius;
		if (pixelRadius < 1) {
			pixelRadius = 1.0;
			invPixelRadius = 1.0;
		}
        resolution = pixelRadius / planetRadius;
    }

	void setResolution(double r) {
		setPixelRadius(r * planetRadius);
	}
	
	void setDetailLevel(double d) {
		detailLevel = d;
	}
	
	double getDetailLevel() {
		return detailLevel;
	}

    double getInvPixelRadius() {
		return 1.0/getPixelRadius();
    }

    double getPixelRadius() {
		return pixelRadius;
    }

    void setKmRadius(double r) {
		planetRadius = r;
    }

    double getKmRadius() {
		return planetRadius;
    }

    double getResolution() {
		return resolution;
    }

	// For nudging detail level
    double getAdjustedResolution() {
		return resolution * detailLevel;
    }
	
    Matrix3D getOrientation() {
	return obliqueOrientation;
    }

    Matrix3D getInverseOrientation() {
	return inverseOrientation;
    }
}
