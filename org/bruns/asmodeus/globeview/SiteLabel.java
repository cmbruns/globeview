//
// $Id$
// $Header$
// $Log$
// Revision 1.5  2005/03/28 01:55:36  cmbruns
// replace getResolution() with getAdjustedResolution() so that alpha blending is consistent with the detail level of usableResolution()
//
// Revision 1.4  2005/03/04 23:59:56  cmbruns
// made overlap comparison to be more generic, using both LensRegions and BoundingBoxes
//
// Revision 1.3  2005/03/02 01:51:19  cmbruns
// Renamed checkResolution() to usableResolution()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.util.*;
import org.bruns.asmodeus.globeview.*;

// Class to deliver colors from a bitmap image to the GlobeView program
// Create March 19, 2000

public class SiteLabel extends GeoObject
{
	static Vector defaultLabelColors;
	static int colorResolution = 10;
	
    // Colors of different transparency for labels
    Vector labelColors;

    String label; // What actually appears on the map there
    GeoPosition position = new GeoPosition();

    // Constructor
    SiteLabel(String l, double lon, double lat, Vector colors) {
		checkLabelColors();
		setLabel(l);
		position.set(lon, lat);
        labelColors = colors;
		addBoxPoint(position.getSpherePoint());
    }

    // Constructor
    SiteLabel(String l, double lon, double lat) {
		checkLabelColors();
		setLabel(l);
		position.set(lon, lat);
        labelColors = defaultLabelColors;
		addBoxPoint(position.getSpherePoint());
    }
	
	void checkLabelColors() {
		if (defaultLabelColors == null) {
			defaultLabelColors = fadeColors(new Color(100, 255, 255));
		}
	}
    
	// accessors
    String getLabel() {return label;}
    GeoPosition getPosition() {return position;}
    Vector3D getVector3D() {return position.getSpherePoint();}
    double getLatitude() {return position.getLatitude();}
    double getLongitude() {return position.getLongitude();}

    // modifiers
    void setLabel(String s) {label = s;}

    // Paint one city label
    Vector3D tempV3 = new Vector3D();
    Vector2D tempV2 = new Vector2D();
    public void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {

		if (!usableResolution(genGlobe)) return;
		if (!overlaps(viewLens)) return;

        double resolution = genGlobe.getAdjustedResolution(); // Pixels per kilometer

        tempV3 = tempV3.copy(getVector3D());
        tempV3 = genGlobe.rotate(tempV3);
        Vector2D v2 = projection.vec3DTo2D(tempV3.x(), tempV3.y(), tempV3.z(), tempV2);
        if (v2 == null) return; // Not in current view

        // Set transparency
        int colorResolution = labelColors.size() - 1;
        Color color = (Color) labelColors.elementAt(colorResolution);
        if (getMinFullResolution() > resolution) {
            double alpha = (resolution - getMinResolution()) /
                (getMinFullResolution() - getMinResolution());
            if (alpha < 0) alpha = 0.0;
            if (alpha > 1) alpha = 1.0;
            if (alpha <= 0) return;
            color = (Color) labelColors.elementAt((int)(alpha * colorResolution));
        }
        else if (getMaxFullResolution() < resolution) {
            double alpha = (resolution - getMaxResolution()) /
                (getMinFullResolution() - getMaxResolution());
            if (alpha < 0) alpha = 0.0;
            if (alpha > 1) alpha = 1.0;
            color = new Color(
                              color.getRed(),
                              color.getGreen(),
                              color.getBlue(),
                              (int) (alpha * 255));
        }
        g.setColor(color);

        int sx = genGlobe.screenX(v2.getX());
        int sy = genGlobe.screenY(v2.getY());

        // FIXME - these sizes and offset should be adjustable
        g.fillRect(sx - 1, sy - 1, 3, 3);
        g.drawString(getLabel(), sx + 5, sy + 5);
    }
	
    // Produce a vector of colors with decreasing transparency (to avoid "Pop-ups")
    // FIXME - Java 1.1 colors don't have ALPHA
    static Vector fadeColors(Color color) {
		Vector answer = new Vector();
		int i;
		for (i = 0; i <= colorResolution; ++i) {
			double alpha = (double)i / (double)colorResolution;
			Color shadedColor;
			
			// Java 1.1 does not support alpha component of Colors
			try {
				shadedColor = new Color(color.getRed(),
										color.getGreen(),
										color.getBlue(),
										(int) (alpha * 255));
			}
			catch (NoSuchMethodError exception) {
				// System.err.println(exception);
				shadedColor = new Color(color.getRed(),
										color.getGreen(),
										color.getBlue());
			}
			answer.addElement(shadedColor);
			
		}
		return answer;
    }
}

