//
//  ParameterFile.java
//  globeview
//
//  Created by Christopher Bruns on 3/1/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  File directing globeview to data files, and other options
// 
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/02 01:57:07  cmbruns
//  New Parameter file object now supports loading of COAST and SITES files
//
package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;

public class ParameterFile {

	// Read file and apply information
	ParameterFile(URL parameterFileURL, GeoCanvas canvas) throws IOException {
		// Open file
		BufferedReader in;
		// try {
			InputStream uncompressedStream;
			InputStream urlStream = parameterFileURL.openStream();
			uncompressedStream = urlStream;
			in = new BufferedReader(new InputStreamReader(uncompressedStream));
		// }
		String inputLine;
		// Read one line at a time
PARAMETER_LINE: 
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(inputLine, "\t ", false);
			String keyWord = "Hey, this isn't a recognized keyword";
			try { keyWord = tokenizer.nextToken(); }
			catch (java.util.NoSuchElementException e) { // Line must be empty?
				continue PARAMETER_LINE;
			}
			
			// TODO - put lots of cool stuff here
			// Read in a coastline file
			if (keyWord.equals("COAST")) {
				// Load a coastline file
				String coastFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				URL coastURL;
				coastURL = new URL(parameterFileURL, coastFileName);
				GeoPathCollection coast = new GeoPathCollection(coastURL, resolution, canvas.borderColor);
				canvas.borders.addElement(coast);
			}

			else if (keyWord.equals("SITES")) {
				// Load a city/sites file
				String sitesFileName = tokenizer.nextToken();
				URL sitesURL;
				sitesURL = new URL(parameterFileURL, sitesFileName);
				SiteLabelCollection sites = new SiteLabelCollection(sitesURL);
				canvas.siteLabels.addElement(sites);
			}
			
			else System.out.println(inputLine + "#" + keyWord); // Unrecognized lines
		}
		in.close();
	}
}
