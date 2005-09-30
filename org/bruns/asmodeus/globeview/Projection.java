//
// $Id$
// $Header$
// $Log$
// Revision 1.5  2005/09/30 16:42:56  cmbruns
// Make night side darker
// Create separate transform for antenna, terminator
// "set" method to copy Matrix3D in place
//
// Revision 1.4  2005/03/28 01:53:13  cmbruns
// Created four separate static instances of Perspective projection for 3D stereoscopic modes
// Enhanced getByName() method to work for Orthographic and some of the stereoscopic modes
//
// Revision 1.3  2005/03/05 00:19:28  cmbruns
// Added getName() function to each projection, but didn't really use it yet.
//
// Added static getByName() function to Projection class
//
// use getMinimumZ() routine in checking clipping
//
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
	
	abstract String getName();

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
    static PerspectiveProjection          WALLEYE3D       = new PerspectiveProjection();
    static PerspectiveProjection          CROSSEYE3D       = new PerspectiveProjection();
    static PerspectiveProjection          REDBLUE3D       = new PerspectiveProjection();
    static PerspectiveProjection          INTERLACED3D       = new PerspectiveProjection();

	static Projection getByName(String projectionName) {
		String lowerName = projectionName.toLowerCase();

		if (lowerName.equals("azimuthal equal area")) return AZIMUTHALEQUALAREA;
		if (lowerName.equals("azimuthal_equal_area")) return AZIMUTHALEQUALAREA;
		if (lowerName.equals("azimuthalequalarea")) return AZIMUTHALEQUALAREA;

		if (lowerName.equals("azimuthal equidistant")) return AZIMUTHALEQUIDISTANT;		
		if (lowerName.equals("azimuthal_equidistant")) return AZIMUTHALEQUIDISTANT;		
		if (lowerName.equals("azimuthalequidistant")) return AZIMUTHALEQUIDISTANT;		

		if (lowerName.equals("equirectangular")) return EQUIRECTANGULAR;
		if (lowerName.equals("plate caree")) return EQUIRECTANGULAR;
		if (lowerName.equals("plate_caree")) return EQUIRECTANGULAR;
		if (lowerName.equals("platecaree")) return EQUIRECTANGULAR;

		if (lowerName.equals("gnomonic")) return GNOMONIC;

		if (lowerName.equals("mercator")) return MERCATOR;

		if (lowerName.equals("perspective")) return PERSPECTIVE;
		if (lowerName.equals("normal monoscopic perspective")) return PERSPECTIVE;
		
		if (lowerName.equals("orthographic")) return ORTHOGRAPHIC;

		if (lowerName.equals("sinusoidal")) return SINUSOIDAL;

		if (lowerName.equals("stereographic")) return STEREOGRAPHIC;

		if (lowerName.equals("cross-eye 3d")) return CROSSEYE3D;
		if (lowerName.equals("stereoscopic")) return CROSSEYE3D;
		if (lowerName.equals("stereoscopic3d")) return CROSSEYE3D;
		if (lowerName.equals("stereoscopic 3d")) return CROSSEYE3D;
		if (lowerName.equals("stereoscopic_3d")) return CROSSEYE3D;
		
		if (lowerName.equals("wall-eye 3d")) return WALLEYE3D;
		
		if (lowerName.equals("red/blue 3d")) return REDBLUE3D;
		
		if (lowerName.equals("interlaced 3d")) return INTERLACED3D;
		
		return null; // if all else fails
	}
}
    
// **************************** //
// *** Azimuthal Equal Area *** //
// **************************** //
class AzimuthalEqualAreaProjection extends Projection {
	double z;

	String getName() {return "Azimuthal Equal Area";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < getMinimumZ()) return null;

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

	String getName() {return "Azimuthal Equidistant";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < getMinimumZ()) return null;

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

	String getName() {return "Equirectangular";}

	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < getMinimumZ()) return null;

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

	    if (Math.PI < x) return null;
	    if (-Math.PI > x) return null;
		
	    double latcoeff = Math.cos(y);

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

	String getName() {return "Gnomonic";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return 0.001;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z <= getMinimumZ()) return null;

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

		String getName() {return "Mercator";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z <= getMinimumZ()) return null;

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

		String getName() {return "Orthographic";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return 0.000;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z < getMinimumZ()) return null;

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
	// TODO - Adjust viewerDistance and screenRadius with zoom level
	// TODO - Make new projections for left and right views
	
	double viewerDistance = 9; // radians between eye and screen
	double screenRadius = 1; // Radius of image of projected globe
	double zclip = 0; // Don't draw any points below here

	String getName() {return "Perspective";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		checkRadius();
		return zclip;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z < 0) return null;
	    if (z < getMinimumZ()) return null;

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

	String getName() {return "Sinusoidal";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping
	    if (z < getMinimumZ()) return null;

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

	String getName() {return "Stereographic";}
	
	double getMinimumZ() { // Least z coordinate that might be drawn
		return -0.99;
	}
	
	Vector2D vec3DTo2D(double x, double y, double z, Vector2D v) {
	    // Clipping - stay away from the edges
	    if (z < getMinimumZ()) return null;

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

  
