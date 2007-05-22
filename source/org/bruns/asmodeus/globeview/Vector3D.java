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
// Revision 1.5  2005/03/13 22:14:31  cmbruns
// Added minus(v) method
// Added getLongitude() and getLatitude() methods
//
// Revision 1.4  2005/03/11 00:19:31  cmbruns
// Added toString() method
//
// Revision 1.3  2005/03/05 00:21:05  cmbruns
// Added Vector3D(longitude, latitude) constructor
//
// Added .unit() routine
//
// Added .mult(double) routine
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

public class Vector3D {
    double[] element = { 0, 0, 0 };

    Vector3D() {
    }

    Vector3D(double x, double y, double z) {
		set(x, y, z);
    }

	Vector3D(double longitude, double latitude) {
	    double latcoeff = Math.cos(latitude);
		set(Math.sin(longitude) * latcoeff,
			Math.sin(latitude),
			Math.cos(longitude) * latcoeff);
	}
	
    // Copy the contents of another vector into this one
    Vector3D copy(Vector3D v2) {
	if (v2 == null) return null;
	Vector3D v = this;
	if (this == null) {
	    v = new Vector3D();
	}
	v.element[0] = v2.element[0];
	v.element[1] = v2.element[1];
	v.element[2] = v2.element[2];
	return v;
    }

    void set(double x, double y, double z) {
	element[0] = x;
	element[1] = y;
	element[2] = z;
    }

    void setX(double x) {element[0] = x;}
    void setY(double y) {element[1] = y;}
    void setZ(double z) {element[2] = z;}
	
    double getX() {return element[0];}
    double getY() {return element[1];}
    double getZ() {return element[2];}

    double x() {return element[0];}
    double y() {return element[1];}
    double z() {return element[2];}

    // Subtract one vector from another
    Vector3D minus(Vector3D v2) {
		Vector3D answer = new Vector3D();
		Vector3D v = this;
		answer.setX(v.x() - v2.x());
		answer.setY(v.y() - v2.y());
		answer.setZ(v.z() - v2.z());
		return answer;
	}
	
    // scale
    Vector3D mult(double r) {
		return new Vector3D(x()*r, y()*r, z()*r);
    }
    
    // Cross Product
    Vector3D cross(Vector3D v2) {
	Vector3D answer = new Vector3D();
	answer.element[0] = y() * v2.z() -
	    z() * v2.y();
	answer.element[1] = z() * v2.x() -
	    x() * v2.z();
	answer.element[2] = x() * v2.y() -
	    y() * v2.x();
	return answer;
    }
    
    // Dot (inner) Product
    double dot(Vector3D v2) {
		double answer = x() * v2.x();
		answer += y() * v2.y();
		answer += z() * v2.z();
		return answer;
    }
    
    double length() {
	return Math.sqrt(this.dot(this));
    }

	// Normalize to unit length
	Vector3D unit() {
		double len = length();
		return new Vector3D(x()/len, y()/len, z()/len);
	}
	
	public String toString() {
		return "("+x()+", "+y()+", "+z()+")";
	}

	// Assume unit vector, return longitude on surface of sphere
	double getLongitude() {
		return Math.atan2(x(), z());
	}
	// Assume unit vector, return latitude on surface of sphere
	double getLatitude() {
		return Math.asin(y());
	}
}
