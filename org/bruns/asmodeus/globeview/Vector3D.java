//
// $Id$
// $Header$
// $Log$
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
}
