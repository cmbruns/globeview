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
// $Id$
// $Header$
// $Log$
// Revision 1.8  2005/03/28 01:13:19  cmbruns
// Removed support for projection parameter
// Added frame.start() to start() method
//
// Revision 1.7  2005/03/14 04:22:48  cmbruns
// Changed so that parameterURL can be relative to the enclosing document
// Changed applet start(), stop(), init(), and destroy() routines to be a bit nicer, and to stop all Canvas threads when appropriate
//
// Revision 1.6  2005/03/13 22:02:20  cmbruns
// Replaced generic exception catches with specific ones.
//
// Revision 1.5  2005/03/11 00:08:22  cmbruns
// New splash dialog appears when globeview is started
//
// Revision 1.4  2005/03/05 00:07:26  cmbruns
// Changed signature to take only parameter file, not individual file URLS
//
// Uncommented block for failed GlobeView load.
//
// Revision 1.3  2005/03/02 01:55:11  cmbruns
// Added loading of new ParameterFile
// Improved thrown error checking
// Converted projection parameter to lower case before checking
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import org.bruns.asmodeus.globeview.*;
import java.net.*; // URL
import java.lang.reflect.*; // Field

public class GlobeViewFrameApplet extends Applet 
    implements ActionListener
{
    GlobeView frame;
    Button button;

    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    Cursor crosshairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
    Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

    public void init() {
		super.init();
		
        // Avoid default gray behind launch button
        Color buttonBackgroundColor = stringToColor(getParameter("button_background"));
        if (buttonBackgroundColor != null) setBackground(buttonBackgroundColor);
        else setBackground(Color.white);

		button = new Button("Launch GlobeView");
		button.setActionCommand("Launch GlobeView");
		button.addActionListener(this);
		add(button);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand() == "Launch GlobeView") {
	    // frame.pack();  // Doesn't help IE get the initial size
	    if (frame == null) {

		// Shut down the button
		button.setLabel("Loading Data...");
		setCursor(waitCursor);
		button.setCursor(waitCursor);
		button.removeActionListener(this);

		// Create a whole new Application
		URL parameterURL = null;

		String parameterString = getParameter("parameters");
		if (parameterString != null) {
			try {parameterURL = new URL(getDocumentBase(), parameterString);
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for parameters. " + 
								   ex +
								   "  URL: " + parameterURL);
				parameterURL = null;
			}
		}
		
		frame = new GlobeView(parameterURL);

		if ((frame != null) && (frame.canvas != null) && (frame.canvas.projection != null))
			frame.setProjection(frame.canvas.projection);

		// Re-activate the button
		button.addActionListener(this);
		setCursor(defaultCursor);
		button.setCursor(defaultCursor);
		button.setLabel("Launch GlobeView");
		
	    }
	    frame.show();
		
		// Splash screen
		InfoDialog splashDialog = new InfoDialog(frame, "GlobeView", true);
		splashDialog.okButton.setLabel("OK");
		splashDialog.addLine("GlobeView applet copyright \u00A9 2001-2005");
		splashDialog.addLine("Christopher M. Bruns PhD");
		splashDialog.addLine("All rights reserved");
		splashDialog.addLine("");
		splashDialog.addLine("http://bruns.homeip.net/~bruns/globeview.html");
		splashDialog.addLine("cmbruns@comcast.net");
		splashDialog.addLine("");
		splashDialog.addLine("See HELP menu to get started");
		splashDialog.addLine("See Help menu->About for credits");
		splashDialog.addLine("");
		splashDialog.addLine("Submit feature requests and bug reports to:");
		splashDialog.addLine("  http://bruns.homeip.net/bugzilla/index.cgi");
		splashDialog.finalizeDialog();
		splashDialog.showDialog();
		
	}
    }

    public void start() {
		super.start();
		
		if (frame != null) frame.start();
		if (button == null) {
			button = new Button("Launch GlobeView");
			button.setActionCommand("Launch GlobeView");
			button.addActionListener(this);
			add(button);
		}
    }

    public void stop() {
		if (frame != null) frame.stop();
		super.stop();
    }
	
	public void destroy() {
		stop();
		button = null;
		frame = null;
		super.destroy();
	}

    // Returns a Color based on 'colorName' which must be one
    // of the predefined colors in java.awt.Color.
    // Returns null if colorName is not valid.
    public Color stringToColor(String colorName) {
        try {
            // Find the field and value of colorName
            Field field = Class.forName("java.awt.Color").getField(colorName);
            return (Color)field.get(null);
        } 
		catch (java.lang.NoSuchFieldException e) {return null;}
		catch (java.lang.ClassNotFoundException e) {return null;}
		catch (java.lang.IllegalAccessException e) {return null;}
		catch (java.lang.NullPointerException e) {return null;}
    }


}
