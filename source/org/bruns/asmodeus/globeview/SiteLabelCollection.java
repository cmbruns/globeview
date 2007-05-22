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
//  SiteLabelCollection.java
//  globeview
//
//  Created by Christopher Bruns on 2/21/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//
// $Id$
// $Header$
// $Log$
// Revision 1.6  2005/03/14 05:06:58  cmbruns
// Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
// Revision 1.5  2005/03/14 04:26:47  cmbruns
// Catch IllegalArgumentException and let Java 1.1 screw up the UTF-8 city labels
//
// Revision 1.4  2005/03/13 22:13:34  cmbruns
// Replace generic exception catches with specific ones.
//
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
			BufferedReader in;
			InputStream is = siteURL.openStream();
			InputStreamReader isr;
			try {isr = new InputStreamReader(is,"UTF-8");} 
			catch (UnsupportedEncodingException e) {
				// City names will be ugly if UTF-8 is not available...
				isr = new InputStreamReader(is);		
			}
			catch (IllegalArgumentException e) {
				// City names will be ugly if UTF-8 is not available...
				isr = new InputStreamReader(is);		
			}
			in = new BufferedReader(isr);
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
		} catch (IOException ex) {
			System.out.println("Problem reading sites URL: " + ex + " URL: " + siteURL);
		}
	}
}
