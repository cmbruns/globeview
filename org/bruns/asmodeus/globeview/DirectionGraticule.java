package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;

// Compass directions and distances
public class DirectionGraticule
extends GeoCollection
{
	
	DirectionGraticule(double distanceIncrement, double angleIncrement, GenGlobe genGlobe) {
		// Firefox JVM doesn't understand Color.GRAY
		Color graticuleColor = new Color(128, 128, 128); // Is this the same as GRAY?
		Color northGraticuleColor = new Color(255, 255, 255); // Is this the same as WHITE?

		// First set up distance circles, then angle semicircles
		double planetRadius = genGlobe.getPlanetRadius();
		double ringDistance; // angular distance on a unit sphere, in radians
		for (ringDistance = distanceIncrement/planetRadius; 
			ringDistance < Math.PI; 
			ringDistance += distanceIncrement/planetRadius) 
		{
			// Distance rings in kilometers
			GeoPath path = new GeoPath();
			path.setColor(graticuleColor);
			path.rotateNorthOnly = true; // draw this one graticule with respect to centered position
			
			double distanceRadians = ringDistance;
			double cartesianZ = Math.cos(distanceRadians);
			double radius3D = Math.sin(distanceRadians);
			for (double arcStep = 0; arcStep <= (2 * Math.PI); arcStep += 0.15) {
				double cartesianX = Math.cos(arcStep) * radius3D;
				double cartesianY = Math.sin(arcStep) * radius3D;
				path.addPoint(cartesianX, cartesianY, cartesianZ);
			}
			// connect back to initial point to close the circle
			if (path.point.size() > 0) {
				path.addPoint((GeoPosition)path.point.elementAt(0));
				addElement(path);
			}
		}
		
		double compassDirection; // In radians
		double degreesToRadians = Math.PI / 180.0;
		for (compassDirection = 0;
			 compassDirection <= 359.5 * degreesToRadians;
			 compassDirection += angleIncrement * degreesToRadians) 
		{
			GeoPath path = new GeoPath();
			path.setColor(graticuleColor);
			if (compassDirection == 0) path.setColor(northGraticuleColor);
			path.rotateNorthOnly = true; // draw this one graticule with respect to centered position

			double arcStep;
			// don't crowd the distance lines at the center
			double minArc = 0.5 * distanceIncrement / planetRadius;
			for (arcStep = minArc; arcStep < Math.PI; arcStep += 0.15) {
				double cartesianZ = Math.cos(arcStep);
				double pointRadius = Math.sin(arcStep);
				double cartesianY = pointRadius * Math.cos(compassDirection);
				double cartesianX = pointRadius * Math.sin(compassDirection);
				path.addPoint(cartesianX, cartesianY, cartesianZ);
			}

			if (path.point.size() > 0) {
				// path.addPoint(0.0, 0.0, -1.0); // connect to opposite side of the planet
				addElement(path);
			}
		}
	}

}
