//
//  GeoCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//  A collection of smaller GeoObjects
//
// $Id$
// $Header$
// $Log$
// Revision 1.3  2005/03/02 01:51:19  cmbruns
// Renamed checkResolution() to usableResolution()
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;

public class GeoCollection extends GeoObject {
	Vector subObject;

	GeoCollection() {
		subObject = new Vector();
	}

	void addElement(GeoObject geoObject) {
		subObject.addElement(geoObject);
		if (subObject.size() == 1) setResolution(geoObject);
		else extendResolution(geoObject);
		boundingBox.addBoundingBox(geoObject.getBoundingBox());
	}
	
	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {		
		if (!usableResolution(genGlobe)) return;
		if (!boundingBox.overlaps(viewLens)) return;
		int i;
		for (i = 0; i < subObject.size(); ++i) {
			GeoObject geoObject = (GeoObject) subObject.elementAt(i);
			geoObject.paint(g, genGlobe, projection, viewLens);
		}
	}
}
