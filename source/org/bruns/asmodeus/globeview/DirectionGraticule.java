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
// Revision 1.3  2005/03/13 21:40:42  cmbruns
// Added maxDistance argument to constructor, for creating higher resolution bearing lines.
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;

// Compass directions and distances
public class DirectionGraticule
extends GeoCollection
{
	
	DirectionGraticule(double distanceIncrement, double angleIncrement, GenGlobe genGlobe, double maxDistance) {
		// Firefox JVM doesn't understand Color.GRAY
		Color graticuleColor = new Color(128, 128, 128); // Is this the same as GRAY?
		Color northGraticuleColor = new Color(255, 255, 255); // Is this the same as WHITE?

		// First set up distance circles, then angle semicircles
		double planetRadius = genGlobe.getPlanetRadius();
		double ringDistance; // angular distance on a unit sphere, in radians
		for (ringDistance = distanceIncrement/planetRadius; 
			ringDistance < maxDistance/planetRadius; 
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
