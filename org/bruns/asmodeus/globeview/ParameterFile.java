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
//  Revision 1.5  2005/03/28 01:49:43  cmbruns
//  Alphabetize the parameter names, to make this more self-documenting
//
//  Revision 1.4  2005/03/13 22:12:29  cmbruns
//  Added CROSSHAIR keyword.
//  Added SIZE keyword.
//  Changes to update parent canvas viewLens at the time certain parameters are changed.
//  New canvas parameter for GeoPathCollection constructor.
//
//  Revision 1.3  2005/03/11 00:17:14  cmbruns
//  Added SCALEBAR parameter
//  Corrected setLatLon call to send radians, not degrees
//  Changed order of lon and lat for MapBlitter call
//
//  Revision 1.2  2005/03/05 00:17:16  cmbruns
//  Added ability to load many other things in ParameterFiles;
//  Including other Parameter files, which can be dynamically loaded when needed, using the paint() routine.
//
//  Revision 1.1  2005/03/02 01:57:07  cmbruns
//  New Parameter file object now supports loading of COAST and SITES files
//
package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.awt.*;

public class ParameterFile extends GeoObject {

	double d2r = Math.PI/180.0;
	URL url;
	GeoCanvas canvas;
	boolean loadedData; // Check whether this parameter file has been parsed
	
	// Read file and apply information
	ParameterFile(URL parameterFileURL, GeoCanvas geoCanvas) throws IOException {
		url = parameterFileURL;
		canvas = geoCanvas;
		// loadData();
	}
	
	void loadData(LensRegion viewLens) throws IOException {
		loadedData = true;
		// Open file
		BufferedReader in;
		// try {
			InputStream uncompressedStream;
			InputStream urlStream = url.openStream();
			uncompressedStream = urlStream;
			in = new BufferedReader(new InputStreamReader(uncompressedStream));
		// }
		String inputLine;
		// Read one line at a time
PARAMETER_LINE: 
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer tokenizer = new StringTokenizer(inputLine, "\t ", false);
			String keyWord = "Hey, this isn't a recognized keyword!";
			try {keyWord = tokenizer.nextToken();}
			catch (java.util.NoSuchElementException e) { // Line must be empty?
				continue PARAMETER_LINE;
			}
			keyWord = keyWord.toUpperCase();
			
			// Branch on record keyword
			if (false) {} // So I won't keep copy/pasting the if without the else

			else if (keyWord.equals("BEARING"))  {canvas.setBearing(true);}
			else if (keyWord.equals("!BEARING")) {canvas.setBearing(false);}
			
			else if (keyWord.equals("BORDERS"))  {canvas.setBorders(true);}
			else if (keyWord.equals("!BORDERS")) {canvas.setBorders(false);}
			
			else if (keyWord.equals("COASTLINES"))  {canvas.setCoastLines(true);}
			else if (keyWord.equals("!COASTLINES")) {canvas.setCoastLines(false);}
			
			else if (keyWord.equals("CROSSHAIR"))  {canvas.setCrosshair(true);}
			else if (keyWord.equals("!CROSSHAIR")) {canvas.setCrosshair(false);}
			
			else if (keyWord.equals("DAYNIGHT"))  {canvas.setDayNight(true);}
			else if (keyWord.equals("!DAYNIGHT")) {canvas.setDayNight(false);}

			else if (keyWord.equals("GRATICULES"))  {canvas.setGraticules(true);}
			else if (keyWord.equals("!GRATICULES")) {canvas.setGraticules(false);}
			
			else if (keyWord.equals("NORTHUP"))  {canvas.setNorthUp(true);}
			else if (keyWord.equals("!NORTHUP")) {canvas.setNorthUp(false);}
			
			else if (keyWord.equals("RIVERS"))  {canvas.setSatellites(true);}
			else if (keyWord.equals("!RIVERS")) {canvas.setSatellites(false);}
			
			else if (keyWord.equals("SATELLITES"))  {canvas.setSatellites(true);}
			else if (keyWord.equals("!SATELLITES")) {canvas.setSatellites(false);}
			
			else if (keyWord.equals("SCALEBAR"))  {canvas.setScaleBar(true);}
			else if (keyWord.equals("!SCALEBAR")) {canvas.setScaleBar(false);}
			
			else if (keyWord.equals("SITELABELS"))  {canvas.setSiteLabels(true);}
			else if (keyWord.equals("!SITELABELS")) {canvas.setSiteLabels(false);}
			
			// Read in a political boundary file
			else if (keyWord.equals("BORDER")) {
				// Load a borderline file
				String borderFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				URL borderURL;
				borderURL = new URL(url, borderFileName);
				GeoPathCollection border = 
					new GeoPathCollection(borderURL, resolution, canvas.borderColor,
										  boundingLens, canvas); // Use bounds of param file
				canvas.borders.addElement(border);
			}
			
			// Center on position
			else if (keyWord.equals("CENTER")) {
				double longitude = (new Double(tokenizer.nextToken())).doubleValue();
				double latitude = (new Double(tokenizer.nextToken())).doubleValue();
				canvas.centerOnPosition(d2r*longitude, d2r*latitude);
				viewLens.changeValues(canvas.getViewLens());
			}
			
			// Read in a coastline file
			else if (keyWord.equals("COAST")) {
				// Load a coastline file
				String coastFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				URL coastURL;
				coastURL = new URL(url, coastFileName);
				GeoPathCollection coast = 
					new GeoPathCollection(coastURL, resolution, canvas.coastColor,
										  boundingLens, canvas); // Use bounds of param file
				canvas.coasts.addElement(coast);
			}
			
			// TODO - set longitude/latitude limits on graticule
			else if (keyWord.equals("GRATI")) { // graticule
												// Create a graticule
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				double interval = (new Double(tokenizer.nextToken())).doubleValue();
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				LatitudeGraticule graticule = new LatitudeGraticule(interval * d2r, 
																	resolution);
				canvas.graticule.addElement(graticule);
			}
			
			else if (keyWord.equals("IMAGE")) {
				// Load a satellite bitmap file
				String imageFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				double minLon = (new Double(tokenizer.nextToken())).doubleValue();
				double maxLon = (new Double(tokenizer.nextToken())).doubleValue();
				double minLat = (new Double(tokenizer.nextToken())).doubleValue();
				double maxLat = (new Double(tokenizer.nextToken())).doubleValue();
				// TODO handle other image projections, such as orthographic
				String projection = tokenizer.nextToken();
				URL imageURL;
				imageURL = new URL(url, imageFileName);
				MapBlitter mapBlitter = MapBlitter.readMap(imageURL, canvas,
														   minLon*d2r, maxLon*d2r,
														   minLat*d2r, maxLat*d2r);
				if (mapBlitter != null) {
					GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
					mapBlitter.setResolution(resolution);
					canvas.images.addElement(mapBlitter);
				}
			}
			
			// Consider loading another parameter file
			else if (keyWord.equals("PARAM")) {
				String paramFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				double minLon = (new Double(tokenizer.nextToken())).doubleValue();
				double maxLon = (new Double(tokenizer.nextToken())).doubleValue();
				double minLat = (new Double(tokenizer.nextToken())).doubleValue();
				double maxLat = (new Double(tokenizer.nextToken())).doubleValue();

				URL paramURL;
				paramURL = new URL(url, paramFileName);
				ParameterFile param = new ParameterFile(paramURL, canvas);
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				
				if (param != null) {
					param.setResolution(resolution);
					param.setLonLatRange(d2r*minLon, d2r*maxLon, d2r*minLat, d2r*maxLat);
					canvas.paramFiles.addElement(param);
				}
			}

			else if (keyWord.equals("PROJECTION")) {
				String projectionName = tokenizer.nextToken();
				Projection projection = Projection.getByName(projectionName);
				if (projection != null) {
					canvas.setProjection(projection);
				}
				viewLens.changeValues(canvas.getViewLens());
			}
			
			// Read in a coastline file
			else if (keyWord.equals("RIVER")) {
				// Load a riverline file
				String riverFileName = tokenizer.nextToken();
				double minRes = (new Double(tokenizer.nextToken())).doubleValue();
				double maxRes = (new Double(tokenizer.nextToken())).doubleValue();
				GeoObject resolution = new GeoObject(minRes, minRes, maxRes, maxRes);
				URL riverURL;
				riverURL = new URL(url, riverFileName);
				GeoPathCollection river = 
					new GeoPathCollection(riverURL, resolution, canvas.riverColor,
										  boundingLens, canvas); // Use bounds of param file
				canvas.rivers.addElement(river);
			}
			
			// Set zoom level
			else if (keyWord.equals("SCALE")) {
				double scale = (new Double(tokenizer.nextToken())).doubleValue();
				canvas.genGlobe.setResolution(scale); // pixels per kilometer
				viewLens.changeValues(canvas.getViewLens());
			}
			
			// Set projection
			else if (keyWord.equals("SITES")) {
				// Load a city/sites file
				String sitesFileName = tokenizer.nextToken();
				URL sitesURL;
				sitesURL = new URL(url, sitesFileName);
				SiteLabelCollection sites = new SiteLabelCollection(sitesURL);
				canvas.siteLabels.addElement(sites);
			}

			// Set window size
			else if (keyWord.equals("SIZE")) {
				int width = (new Integer(tokenizer.nextToken())).intValue();
				int height = (new Integer(tokenizer.nextToken())).intValue();
				canvas.globeViewFrame.setSize(width, height); // pixels per kilometer
				viewLens.changeValues(canvas.getViewLens());
			}
			
			else if (keyWord.equals("#")) {} // comment
			
			else System.out.println(inputLine + "#" + keyWord); // Unrecognized lines
		}
		in.close();
	}

	// Use the paint action to decide whether we need to load data
	void paint(Graphics g, GenGlobe genGlobe, Projection projection, LensRegion viewLens) {
		if (loadedData) return;

		if (false && (boundingLens != null) && (viewLens != null)) {
			System.out.println(" viewLens: "+ 
							   viewLens.getUnitVector().x()+", "+viewLens.getUnitVector().y()+", "+viewLens.getUnitVector().z()+
							   " # "+viewLens.getAngleRadius()*180/Math.PI);
			System.out.println(" parameterFile: "+ 
							   boundingLens.getUnitVector().x()+", "+boundingLens.getUnitVector().y()+", "+boundingLens.getUnitVector().z()+
							   " # "+boundingLens.getAngleRadius()*180/Math.PI);
		}
		
		if (!usableResolution(genGlobe)) return;
		if (!overlaps(viewLens)) return;
		// If we get this far, it's time to load the data from this parameter file
		canvas.setWait("BUSY: LOADING DATA...");
		try {loadData(viewLens);}
		catch (IOException e) {
			System.out.println("Error loading parameter file: " + e);
		}
		canvas.unsetWait();
	}	
}
