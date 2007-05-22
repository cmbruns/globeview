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
