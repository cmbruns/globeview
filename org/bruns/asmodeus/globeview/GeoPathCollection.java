//
//  GeoPathCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
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
	
	GeoPathCollection(URL pathURL, GeoObject resolution, Color borderColor) {

		setResolution(resolution);
		color = borderColor;
		if (pathURL == null) return;

		try {
			InputStream urlStream = pathURL.openStream();
			InputStream uncompressedStream;
			try {
				// Try to open it as a gzipped stream
				GZIPInputStream gzipStream = new GZIPInputStream(urlStream);
				uncompressedStream = gzipStream;
			} catch (Exception exception) {
				// OK, just use the stream non-gzipped
				uncompressedStream = urlStream;
			}
			
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
				path.setResolution(resolution);
				path.color = color;
				addElement(path);
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Problem reading paths URL: " + ex + " URL: " + pathURL);
		}
	}
}
