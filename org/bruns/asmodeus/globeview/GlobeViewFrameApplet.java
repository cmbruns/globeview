//
// $Id$
// $Header$
// $Log$
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
		URL mapURL = null;
		URL siteURL = null;
		URL borderURL = null;

		String parameterString = getParameter("parameters");
		if (parameterString != null) {
			try {parameterURL = new URL(parameterString);
			} catch (java.net.MalformedURLException ex) {
				System.out.println("Problem with URL for parameters. " + 
								   ex +
								   "  URL: " + parameterURL);
				parameterURL = null;
			}
		}
		
		try {
			frame = new GlobeView(parameterURL);

		    // Check for projection option
                    // TODO - this is causing some kind of permission error
                    // frame.setProjection(Projection.AZIMUTHALEQUIDISTANT);
  		    String projectionName = getParameter("projection");
			if (projectionName != null) {
				projectionName = projectionName.toLowerCase(); // Convert to lower case
				if (projectionName.equals("azimuthal equidistant"))
					frame.setProjection(Projection.AZIMUTHALEQUIDISTANT);
				else if (projectionName.equals("orthographic")) 
					frame.setProjection(Projection.ORTHOGRAPHIC);
				else if (projectionName.equals("azimuthal equal area")) 
					frame.setProjection(Projection.AZIMUTHALEQUALAREA);
				else if (projectionName.equals("mercator")) 
					frame.setProjection(Projection.MERCATOR);
				else if (projectionName.equals("perspective")) 
					frame.setProjection(Projection.PERSPECTIVE);
				else if (projectionName.equals("equirectangular")) 
					frame.setProjection(Projection.EQUIRECTANGULAR);
				else if (projectionName.equals("gnomonic")) 
					frame.setProjection(Projection.GNOMONIC);
				else if (projectionName.equals("sinusoidal")) 
					frame.setProjection(Projection.SINUSOIDAL);
				else if (projectionName.equals("stereographic"))
					frame.setProjection(Projection.STEREOGRAPHIC);
			}
			else 
				if ((frame != null) && (frame.canvas != null) && (frame.canvas.projection != null))
					frame.setProjection(frame.canvas.projection);
			//
		} catch (Exception exception) {
		    System.out.println(e);
		    setCursor(defaultCursor);
		    button.setCursor(defaultCursor);
		    button.setLabel("Globeview failed");
		    System.exit(1);
		}

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
	if (button == null) {
	    button = new Button("Launch GlobeView");
	    button.setActionCommand("Launch GlobeView");
	    button.addActionListener(this);
	    add(button);
	}
    }

    public void stop() {
		if (frame != null) frame.hide();
		frame = null;
    }

    // Returns a Color based on 'colorName' which must be one
    // of the predefined colors in java.awt.Color.
    // Returns null if colorName is not valid.
    public Color stringToColor(String colorName) {
        try {
            // Find the field and value of colorName
            Field field = Class.forName("java.awt.Color").getField(colorName);
            return (Color)field.get(null);
        } catch (Exception e) {
            return null;
        }
    }


}
