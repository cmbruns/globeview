//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/09/30 16:42:56  cmbruns
// Make night side darker
// Create separate transform for antenna, terminator
// "set" method to copy Matrix3D in place
//
// Revision 1.3  2005/03/28 01:46:01  cmbruns
// Created copy() method for use in the copy() method of GenGlobe()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

public class Matrix3D {
    double[][] element = { { 1, 0, 0 },
			   { 0, 1, 0 },
			   { 0, 0, 1 }};
    Matrix3D() {
    }

    void set(
	     double e00, double e01, double e02,
	     double e10, double e11, double e12,
	     double e20, double e21, double e22
	     ) {
	element[0][0] = e00;
	element[0][1] = e01;
	element[0][2] = e02;
	element[1][0] = e10;
	element[1][1] = e11;
	element[1][2] = e12;
	element[2][0] = e20;
	element[2][1] = e21;
	element[2][2] = e22;

    }
	
	// Copy contents of another matrix, so we can change matriced passed by reference
    void set(Matrix3D m2) {
		element[0][0] = m2.element[0][0];
		element[0][1] = m2.element[0][1];
		element[0][2] = m2.element[0][2];
		element[1][0] = m2.element[1][0];
		element[1][1] = m2.element[1][1];
		element[1][2] = m2.element[1][2];
		element[2][0] = m2.element[2][0];
		element[2][1] = m2.element[2][1];
		element[2][2] = m2.element[2][2];
    }
	
	Matrix3D copy() {return this.mult(1.0);}
	
    Matrix3D mult(Matrix3D m2) {
	Matrix3D answer = new Matrix3D();
	int i, j, k;
	for (i = 0; i < 3; ++i) 
	    for (j = 0; j < 3; ++j) {
		answer.element[i][j] = 0;
		for (k = 0; k < 3; ++k) 
		    answer.element[i][j] +=
			element[i][k] * m2.element[k][j];
	    }
	return answer;
    }

    Matrix3D transpose() {
	Matrix3D answer = new Matrix3D();
	int i, j;
	for (i = 0; i < 3; ++i) 
	    for (j = 0; j < 3; ++j) {
		answer.element[i][j] = element[j][i];
	    }
	return answer;
    }

    Vector3D mult(Vector3D v) {
	Vector3D answer = new Vector3D();
	answer.set(
		   element[0][0] * v.element[0] +
		   element[0][1] * v.element[1] +
		   element[0][2] * v.element[2], 

		   element[1][0] * v.element[0] +
		   element[1][1] * v.element[1] +
		   element[1][2] * v.element[2], 

		   element[2][0] * v.element[0] +
		   element[2][1] * v.element[1] +
		   element[2][2] * v.element[2]);

	return answer;
    }

    Matrix3D mult(double r) {
	Matrix3D answer = new Matrix3D();
	// This kludge somehow fixes a bug in the Linux JVM...
	double trouble = element[0][0] * r;
	answer.set(
		   trouble, 
		   element[0][1] * r, 
		   element[0][2] * r, 
		   element[1][0] * r, 
		   element[1][1] * r, 
		   element[1][2] * r, 
		   element[2][0] * r, 
		   element[2][1] * r, 
		   element[2][2] * r
		   );

	return answer;
    }

    // return the matrix that rotates about "axis" by "angle"
    void setAxisAngle(Vector3D axis, double angle) {
	// make sure axis is OK
	double len = axis.length();

	Vector3D axis2 = axis;
	if (len < 0.0001) return;  // too small
	if ((len > 1.001) || (len < 0.999)) {
	    axis2.element[0] /= len;
	    axis2.element[1] /= len;
	    axis2.element[2] /= len;
	}

	// first put into quaterion representation
	double e0 = Math.cos(angle/2.0);
	double sinAngle2 = Math.sin(angle/2.0);
	double e1 = axis2.element[0] * sinAngle2;
	double e2 = axis2.element[1] * sinAngle2;
	double e3 = axis2.element[2] * sinAngle2;

	// now convert quaterion to matrix
	element[0][0] = e0*e0 + e1*e1 - e2*e2 - e3*e3;
	element[0][1] = 2.0 * (e1*e2 + e0*e3);
	element[0][2] = 2.0 * (e1*e3 - e0*e2);
	element[1][0] = 2.0 * (e1*e2 - e0*e3);
	element[1][1] = e0*e0 - e1*e1 + e2*e2 - e3*e3;
	element[1][2] = 2.0 * (e2*e3 + e0*e1);
	element[2][0] = 2.0 * (e1*e3 + e0*e2);
	element[2][1] = 2.0 * (e2*e3 - e0*e1);
	element[2][2] = e0*e0 - e1*e1 - e2*e2 + e3*e3;
    }
}
