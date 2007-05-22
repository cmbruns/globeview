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
//  HelpDialog.java
//  globeview
//
//  Created by Christopher Bruns on 3/4/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.3  2005/03/14 05:06:57  cmbruns
//  Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
//  Revision 1.2  2005/03/13 22:03:31  cmbruns
//  Added suggestion to find your home town.
//  Wrapped call to Java 1.4 setUndecorated() method in catch NoSuchMethodException
//
//  Revision 1.1  2005/03/11 00:08:58  cmbruns
//  New help dialog when user clicks Help->Globeview Help
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.awt.*;

public class HelpDialog extends InfoDialog {
	HelpDialog(Frame frame) {
		super(frame, "GlobeView Help", false); // not modal dialog
		
		addLine("Using GlobeView:");
		addLine("  CLICK the mouse to center on something");
		addLine("  DRAG the mouse to pan up/down and left/right");
		addLine("  SHIFT-DRAG (or far-click) to zoom in and out");
		addLine(" ");		
		addLine("Zoom out until you can see most of the earth.");
		addLine("Then experiment with the different options in the PROJECTION menu");
		addLine("Next, zoom in and find your home town");
		addLine(" ");		
		addLine("For more information:");
		addLine("  READ http://bruns.homeip.net/~bruns/globeview.html");
		addLine("  CONTACT Chris Bruns cmbruns@comcast.net");
		
		try {setUndecorated(false);}  // So we can drag it around
		catch (java.lang.NoSuchMethodError e) {} // Only works in Java 1.4

		finalizeDialog();
	}
}
