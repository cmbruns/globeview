//
//  SiteLabelCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.3  2005/03/11 00:18:34  cmbruns
// Changed InputStream constructor call to read UTF-8 characters from labels file
//
// Revision 1.2  2005/03/01 02:13:13  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.bruns.asmodeus.globeview.*;

public class SiteLabelCollection extends GeoCollection {
	static double degreesToRadians = Math.PI / 180.0;

	SiteLabelCollection(URL siteURL) {
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(siteURL.openStream(),
												   "UTF-8"));		
			String inputLine;		
			while ((inputLine = in.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(inputLine, "\t");
				
				String siteName = tokenizer.nextToken();
				double latitude = degreesToRadians * (new Double(tokenizer.nextToken())).doubleValue();
				double longitude = degreesToRadians * (new Double(tokenizer.nextToken())).doubleValue();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				
				SiteLabel label = new SiteLabel(siteName, latitude, longitude);
				label.setMinResolution(minRes);
				label.setMaxResolution(maxRes);
				label.setMinFullResolution(minRes);
				label.setMaxFullResolution(maxRes);
				
				addElement(label);
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Problem reading sites URL: " + ex + " URL: " + siteURL);
		}
	}
}
