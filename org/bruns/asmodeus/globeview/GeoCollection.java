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
//  GeoCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  A collection of smaller GeoObjects
//
// $Id$
// $Header$
// $Log$
// Revision 1.6  2005/03/14 05:06:57  cmbruns
// Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
// Revision 1.5  2005/03/11 00:03:58  cmbruns
// added elementCount() method
//
// Revision 1.4  2005/03/04 23:58:15  cmbruns
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

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;

public class GeoCollection extends GeoObject {
	Vector subObject;

	GeoCollection() {
		subObject = new Vector();
	}

	int elementCount() {
		return subObject.size();
	}
	
	void addElement(GeoObject geoObject) {
		subObject.addElement(geoObject);
		if (subObject.size() == 1) setResolution(geoObject);
		else extendResolution(geoObject);
		addBoundingBox(geoObject);
		addBoundingLens(geoObject);
	}
	
	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {		
		if (!usableResolution(genGlobe)) return;
		if (!overlaps(viewLens)) return;
		int i;
		for (i = 0; i < subObject.size(); ++i) {
			GeoObject geoObject = (GeoObject) subObject.elementAt(i);
			geoObject.paint(g, genGlobe, projection, viewLens);
		}
	}
}
