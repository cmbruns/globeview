//
// $Id$
// $Header$
// $Log$
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

public class Vector2D {
    double[] element = { 0, 0 };
    int flag = 0; // Used in map projections

    Vector2D() {
    }
    Vector2D(double x, double y) {
	set(x, y);
    }

    // Copy the contents of another vector into this one
    Vector2D copy(Vector2D v2) {
	if (v2 == null) return null;
	Vector2D v = this;
	if (this == null) {
	    v = new Vector2D();
	}
	v.element[0] = v2.element[0];
	v.element[1] = v2.element[1];
	v.flag = v2.flag;
	return v;
    }

    double getX() {return element[0];}
    double getY() {return element[1];}

    void set(double x, double y) {
	element[0] = x;
	element[1] = y;
    }

    // Dot (inner) Product
    double dot(Vector2D v2) {
	double answer = 0;
	answer += element[0] * v2.element[0];
	answer += element[1] * v2.element[1];
	return answer;
    }
    
    double length() {
	return Math.sqrt(this.dot(this));
    }
}
