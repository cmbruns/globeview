//
//  AboutDialog.java
//  globeview
//
//  Created by Christopher Bruns on 1/8/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
// Dialog window when user selects "About Globeview"
// 
// $Id$
// $Header$
// $Log$
// Revision 1.5  2005/03/11 00:00:00  cmbruns
// Now descends from InfoDialog class
// New credit for city data
// Pointer to bugzilla site
//
// Revision 1.4  2005/03/04 23:54:30  cmbruns
// Added acknowledgements for image and coast data
//
// Revision 1.3  2005/03/01 02:06:03  cmbruns
// minor comment change
//
// Revision 1.2  2005/03/01 02:03:25  cmbruns
// Added cvs headers
//


package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.awt.*;

public class AboutDialog extends InfoDialog {
	AboutDialog(Frame frame) {
		super(frame, "About GlobeView", true); // modal dialog

		addLine("Globevew version 1.10 by Chris Bruns");
		addLine("Copyright \u00A9 2001-2005 all rights reserved");
		addLine("http://bruns.homeip.net/~bruns/globeview.html");
		addLine("cmbruns@comcast.net");
		addLine(" ");		
		addLine("Submit feature requests and bug reports to:");
		addLine("  http://bruns.homeip.net/bugzilla/index.cgi");
		addLine("");
		addLine("Satellite images from NASA courtesy of:");		
		addLine("  Reto St\u00F6ckli, NASA Earth Observatory");
		addLine("  rstockli@climate.gsfc.nasa.gov");
		addLine("  http://earthobservatory.nasa.gov/Newsroom/BlueMarble/");
		addLine(" ");		
		addLine("City data courtesy of:");		
		addLine("  Stefan Helders, World Gazetteer");
		addLine("  http://www.world-gazetteer.com/");
		addLine(" ");		
		addLine("Coast line data courtesy of:");		
		addLine("  United States Central Intelligence Agency (CIA)");
		addLine("  World Databank II");
		
		finalizeDialog();
	}
}
