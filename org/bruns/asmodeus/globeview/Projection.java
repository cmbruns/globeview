//
// $Id$
// $Header$
// $Log$
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

// Projection.java
// March 22, 2001  Chris Bruns
// Map Projections
// Removed from subclass of GeoCanvas
// No scaling, just project unit sphere to plane


// Base class
abstract class Projection {
    abstract Vector2D vec3DTo2D(double x, double y, double z, Vector2D v);
    
    abstract Vector3D vec2DTo3D(double x, double y, Vector3D v);

    abstract int getBackgroundType(); // Circle? InfinitePlane? Vertical Stripe? Sinusoid? Rectangle?
    abstract double getBackgroundParameter();
	abstract double getMinimumZ(); // Least z coordinate that might be drawn

	// Shapes that the whole map can have in different projections
    static int BKGD_CIRCLE    = 1; // orthographic, azimuthals, perspective
    static int BKGD_INFPLANE  = 2; // Gnomonic, stereographic
    static int BKGD_VSTRIPE   = 3; // Mercator
    static int BKGD_SINUSOID  = 4; // Sinusoidal
    static int BKGD_RECTANGLE = 5; // Equirectangular

    static AzimuthalEqualAreaProjection   AZIMUTHALEQUALAREA   = new AzimuthalEqualAreaProjection();
    static AzimuthalEquidistantProjection AZIMUTHALEQUIDISTANT = new AzimuthalEquidistantProjection();
    static EquirectangularProjection      EQUIRECTANGULAR      = new EquirectangularProjection();
    static GnomonicProjection             GNOMONIC             = new GnomonicProjection();
    static MercatorProjection             MERCATOR             = new MercatorProjection();
    static OrthographicProjection         ORTHOGRAPHIC         = new OrthographicProjection();
    static PerspectiveProjection          PERSPECTIVE          = new PerspectiveProjection();
    static SinusoidalProjection           SINUSOIDAL           = new SinusoidalProjection();
    static StereographicProjection        STEREOGRAPHIC        = new StereographicProjection();
}
    

    // **************************** //
    // *** Azimuthal Equal Area *** //
    // **************************** //
    class AzimuthalEqualAreaProjection extends Projection {
	double z;

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < -0.99) return null;

	    double denominator = Math.sqrt((1.0 + z) / 2.0);

	    v.element[0] = x/denominator;
	    v.element[1] = y/denominator;
	    
	    return v;
	}

	// Faster version that takes normalized x,y
	// and sends it to premade vector
	Vector3D vec2DTo3D(double x, double y, Vector3D v) {

	    double rho = Math.sqrt(x*x + y*y);
	    if (rho > 2.0)
		return null;

	    z = Math.cos(2.0 * Math.asin(rho/2.0));
	    double denominator = Math.sqrt((1.0 + z) / 2.0);

	    v.element[0] = x * denominator;
	    v.element[1] = y * denominator;
	    v.element[2] = z;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_CIRCLE;
	}

	double getBackgroundParameter() {return 2.0;}
    }

  
    // ***************************** //
    // *** Azimuthal Equidistant *** //
    // ***************************** //
    class AzimuthalEquidistantProjection extends Projection {
	double z;

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < -0.99) return null;

	    double denominator = 1.0;
	    double rho = Math.acos(z);
	    if (rho == 0) denominator = 1.0;
	    else denominator = Math.sin(rho)/rho;
	    if (denominator == 0) denominator = 1.0;

	    v.element[0] = x/denominator;
	    v.element[1] = y/denominator;
	    
	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {

	    double rho = Math.sqrt(x*x + y*y);
	    if (rho > Math.PI)
		return null;

	    z = Math.cos(rho);
	    double denominator = Math.sin(rho)/rho;

	    v.element[0] = x * denominator;
	    v.element[1] = y * denominator;
	    v.element[2] = z;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_CIRCLE;
	}

	double getBackgroundParameter() {return Math.PI;}
    }

  
    // *********************** //
    // *** Equirectangular *** //
    // *********************** //
    class EquirectangularProjection extends Projection {
	Vector2D corner = new Vector2D(0,0);

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Don't cross the "seam" in the back of the planet
	    int parity = 0;
	    if (z < 0) {
		if (x < 0) parity = -1;
		else parity = 1;
	    }
	    v.flag = parity;

	    double lambda = Math.atan2(x, z); // longitude
	    double phi = Math.asin(y); // latitude

	    v.element[0] = lambda;
	    v.element[1] = phi;
	    
	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {
	    // Check that we are in active area
	    if ((Math.PI/2.0) < y) return null;
	    if (-(Math.PI/2.0) > y) return null;

	    double latcoeff = Math.cos(y);

	    if (Math.PI < x) return null;
	    if (-Math.PI > x) return null;

	    double x3 = Math.sin(x) * latcoeff;
	    double y3 = Math.sin(y);
	    double z3 = Math.cos(x) * latcoeff;
	    
	    v.element[0] = x3;
	    v.element[1] = y3;
	    v.element[2] = z3;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_RECTANGLE;
	}

	double getBackgroundParameter() {return Math.PI;} // Half - Width

    }


    // **************** //
    // *** Gnomonic *** //
    // **************** //
    class GnomonicProjection extends Projection {

	double getMinimumZ() { // Least z coordinate that might be drawn
		return 0.0;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z <= 0) return null;

	    v.element[0] = x/z;
	    v.element[1] = y/z;
	    
	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {
	    v.element[2] = Math.sqrt(1 / (1 + x*x + y*y));
	    v.element[0] = x * v.element[2];
	    v.element[1] = y * v.element[2];

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_INFPLANE;
	}

	double getBackgroundParameter() {return 0;}
    }


    // **************** //
    // *** Mercator *** //
    // **************** //
    class MercatorProjection extends Projection {

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Don't cross the "seam" in the back of the planet
	    int parity = 0;
	    if (z < 0) {
		if (x < 0) parity = -1;
		else parity = 1;
	    }
	    v.flag = parity;

	    double lambda = Math.atan2(x, z); // longitude
	    double phi = Math.asin(y); // latitude

	    v.element[0] = lambda;
	    v.element[1] =  Math.log(Math.tan(Math.PI/4 + phi/2.0));

	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {

	    double sy = (Math.atan(Math.exp(y)) - Math.PI/4.0) * 2.0;
	    double latcoeff = Math.cos(sy);

	    if (Math.PI < x) return null;
	    if (-Math.PI > x) return null;

	    double x3 = Math.sin(x) * latcoeff;
	    double y3 = Math.sin(sy);
	    double z3 = Math.cos(x) * latcoeff;
	    
	    v.element[0] = x3;
	    v.element[1] = y3;
	    v.element[2] = z3;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_VSTRIPE;
	}

	double getBackgroundParameter() {return Math.PI;} // Half Width
    }


    // ******************** //
    // *** Orthographic *** //
    // ******************** //
    class OrthographicProjection extends Projection {
	double z;

	double getMinimumZ() { // Least z coordinate that might be drawn
		return 0.0;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z < 0) return null;

	    v.element[0] = x;
	    v.element[1] = y;

	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {
	    // Check that we are in active area
	    if (1.0 < x) return null;
	    if (-1.0 > x) return null;
	    if (1.0 < y) return null;
	    if (-1.0 > y) return null;
	    z = (1 - x*x - y*y);
	    if (z <= 0) return null; // Clipping
	    z = Math.sqrt(z);

	    v.element[0] = x;
	    v.element[1] = y;
	    v.element[2] = z;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_CIRCLE;
	}

	double getBackgroundParameter() {return 1.0;}
    }


    // ******************* //
    // *** Perspective *** //
    // ******************* //
    class PerspectiveProjection extends Projection {
	double viewerDistance = 9; // radians between eye and screen
	double screenRadius = 1; // Radius of image of projected globe
	double zclip = 0; // Don't draw any points below here

	double getMinimumZ() { // Least z coordinate that might be drawn
		checkRadius();
		return zclip;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z < 0) return null;
	    checkRadius();
	    if (z < zclip) return null;

	    double vd = viewerDistance;
	    double foreshortening = vd / (vd + 1.0 - z);
	    v.element[0] = x * foreshortening;
	    v.element[1] = y * foreshortening;

	    return v;
	}

	void checkRadius() {
	    // If we don't already know the radius, we need to update the screenRadius
		// Only need to do this once
		if (zclip > 0) return;

	    double v = viewerDistance;
	    screenRadius = v / Math.sqrt(v*v + 2*v);
	    zclip = 1 / (v + 1);
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D vec) {
	    // Check that we are in active area
	    if (1.0 < x) return null;
	    if (-1.0 > x) return null;
	    if (1.0 < y) return null;
	    if (-1.0 > y) return null;

	    // Check that we are in active area
	    double sr = screenRadius;
	    double r02 = x*x + y*y;
	    if (r02 > (sr*sr)) return null;

	    double vd = viewerDistance;
      
	    double z = (r02*vd + r02 + vd * Math.sqrt(vd*vd - vd*(vd + 2)*r02))
		/ (vd*vd + r02);
	    double foreshortening = (vd + 1 - z) / vd;

	    vec.element[0] = foreshortening * x;
	    vec.element[1] = foreshortening * y;
	    vec.element[2] = z;

	    return vec;
	}

	int getBackgroundType() {
		checkRadius();
	    return Projection.BKGD_CIRCLE;
	}

	double getBackgroundParameter() {
	    double v = viewerDistance;
	    return v / Math.sqrt(v*v + 2*v);
	}
    }


    // ****************** //
    // *** Sinusoidal *** //
    // ****************** //
    class SinusoidalProjection extends Projection {
	Vector2D corner = new Vector2D(0,0);

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Don't cross the "seam" in the back of the planet
	    int parity = 0;
	    if (z < 0) {
		if (x < 0) parity = -1;
		else parity = 1;
	    }
	    v.flag = parity;

	    double lambda = Math.atan2(x, z); // longitude
	    double phi = Math.asin(y); // latitude
	    double sinusThing = Math.sqrt(1.0 - y * y);

	    v.element[0] = lambda * sinusThing;
	    v.element[1] = phi;

	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {
	    if ((Math.PI/2.0) < y) return null;
	    if (-(Math.PI/2.0) > y) return null;

	    double latcoeff = Math.cos(y);
	    double sx = x/latcoeff;
	    // Check that we are in active area
	    if (Math.PI < sx) return null;
	    if (-Math.PI > sx) return null;

	    double x3 = Math.sin(sx) * latcoeff;
	    double y3 = Math.sin(y);
	    double z3 = Math.cos(sx) * latcoeff;
	    
	    v.element[0] = x3;
	    v.element[1] = y3;
	    v.element[2] = z3;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_SINUSOID;
	}

	double getBackgroundParameter() {return Math.PI;} // Half-Width
    }


    // ********************* //
    // *** Stereographic *** //
    // ********************* //
    class StereographicProjection extends Projection {
	double z;

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < -0.99) return null;

	    double denominator = (1.0 + z) / 2.0;

	    v.element[0] = x/denominator;
	    v.element[1] = y/denominator;

	    return v;
	}

	Vector3D vec2DTo3D(double x, double y, Vector3D v) {

	    double rho2 = x*x + y*y;

	    z = (4.0 - rho2) / (4.0 + rho2);
	    double denominator = (1.0 + z) / 2.0;

	    v.element[0] = x * denominator;
	    v.element[1] = y * denominator;
	    v.element[2] = z;

	    return v;
	}

	int getBackgroundType() {
	    return Projection.BKGD_INFPLANE;
	}

	double getBackgroundParameter() {return 0.0;}
    }

  
