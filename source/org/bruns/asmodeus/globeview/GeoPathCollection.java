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
//  GeoPathCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.6  2005/03/28 01:06:01  cmbruns
// Use new MeasuredInputStream class so that load progress can be monitored
//
// Revision 1.5  2005/03/14 05:06:57  cmbruns
// Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
// Revision 1.4  2005/03/13 21:55:36  cmbruns
// Populate boundingLens from that of parent parameter file.
// Modified to dynamically load when needed, as is done for images.
// But only load during full image updates.
//
// Revision 1.3  2005/03/02 01:52:49  cmbruns
// Set resolution of individual paths
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;
import org.bruns.asmodeus.globeview.*;

public class GeoPathCollection extends GeoCollection 
{
	static double degreesToRadians = Math.PI / 180.0;
	Color color;
	URL url;
	boolean isLoaded = false;
	GeoCanvas canvas;
	
	GeoPathCollection(URL pathURL, GeoObject resolution, Color borderColor,
					  LensRegion lens, GeoCanvas geoCanvas) {
		canvas = geoCanvas;
		url = pathURL;
		setResolution(resolution);
		color = borderColor;
		boundingLens = lens;
		// loadData(); // TODO - only do this when needed
	}
	
	void loadData() {
		if (!canvas.fullUpdateNow) return;
		if (url == null) return;

		canvas.setWait("BUSY: Loading coast line file...");
		try {
			MeasuredInputStream urlStream = new MeasuredInputStream(url, canvas);
			InputStream uncompressedStream;
			GZIPInputStream gzipStream = new GZIPInputStream(urlStream);
			uncompressedStream = gzipStream;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(uncompressedStream));
			String inputLine;
			int segmentCount = 0;
			
			//	segment 1  rank 1  points 3
			//		31.411111 31.881667
			//		31.518611 31.369167
			//		31.427222 30.852222
			//		segment 2  rank 1  points 3
			//      ...				
			while ((inputLine = in.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(inputLine, "\t ", false);
				String keyWord = tokenizer.nextToken();
				if (!keyWord.equals("segment")) { // Error
					System.out.println("Error parsing boundary file. " + inputLine + "#" + keyWord);
					break;
				}
				segmentCount ++;
				
				tokenizer.nextToken(); // segment number
				tokenizer.nextToken(); // "rank"
				tokenizer.nextToken(); // rank number
				tokenizer.nextToken(); // "points"
				int pointCount = (new Integer(tokenizer.nextToken())).intValue(); // point count
				
				GeoPath path = new GeoPath();
				for (int i = 0; i < pointCount; i++) {
					inputLine = in.readLine();
					StringTokenizer tokenizer2 = new StringTokenizer(inputLine, "\t ", false);
					// Parse coordinate pair
					double latitude = (new Double(tokenizer2.nextToken())).doubleValue();
					double longitude = (new Double(tokenizer2.nextToken())).doubleValue();
					path.addPoint(longitude * degreesToRadians, latitude * degreesToRadians);
				}
				path.setResolution(this);
				path.color = color;
				addElement(path);
			}
			in.close();
			urlStream.close();
		} catch (IOException ex) {
			System.out.println("Problem reading paths URL: " + ex + " URL: " + url);
		}
		isLoaded = true;
		canvas.unsetWait();
	}

	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {		
		if (!usableResolution(genGlobe)) return;
		if (!overlaps(viewLens)) return;
		if (!isLoaded) loadData();
		if (!isLoaded) return;
		super.paint(g,genGlobe,projection,viewLens);
	}
}
