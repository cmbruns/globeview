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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class AboutDialog extends Dialog {
	AboutDialog(Frame frame) {
		super(frame, "About GlobeView", true); // modal dialog
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		
		//Create OK button
		Button okButton = new Button( "OK" );
		okButton.addActionListener ( new ActionListener() 
									 {
			public void actionPerformed( ActionEvent e )
		{
				// Hide dialog
				setVisible(false);
		}
									 });
		
		
		// Create message to display
		Panel textPanel = new Panel();
		textPanel.setLayout(new GridLayout(0,1,0,0)); // single column
		textPanel.add(new Label("Globevew version 1.10 by Chris Bruns"));
		textPanel.add(new Label("Copyright 2001-2005 all rights reserved"));
		textPanel.add(new Label("http://bruns.homeip.net/~bruns/globeview.html"));
		textPanel.add(new Label("cmbruns@comcast.net"));
		textPanel.add(new Label(" "));		
		textPanel.add(new Label("Satellite images from NASA courtesy of:"));		
		textPanel.add(new Label("  Reto Stoeckli, NASA Earth Observatory"));
		textPanel.add(new Label("  rstockli@climate.gsfc.nasa.gov"));
		textPanel.add(new Label("  http://earthobservatory.nasa.gov/Newsroom/BlueMarble/"));
		textPanel.add(new Label(" "));		
		textPanel.add(new Label("Coast line data courtesy of:"));		
		textPanel.add(new Label("  United States Central Intelligence Agency (CIA)"));
		textPanel.add(new Label("  World Databank II"));
		
		// TODO - find a way to print out build time
		// SimpleDateFormat dateFormat = new SimpleDateFormat();
		// String dateString = dateFormat.format(new Date());
		// textPanel.add(new Label("Build: " + dateString));
		
		setLayout( gridbag );
		
		// The message should not fill, it should be centered within
		// this area, with
		// some extra padding.  The gridwidth of REMAINDER means this
		// is the only
		// thing on its row, and the gridheight of RELATIVE means
		// there should only
		// be one thing below it.
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.ipadx = 20;
		constraints.ipady = 20;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = GridBagConstraints.RELATIVE;
		
		gridbag.setConstraints(textPanel, constraints);
		add(textPanel);
		
		// The button has no padding, no weight, taked up minimal width, and
		// Is the last thing in its column.
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
		constraints.gridwidth = 1;
		constraints.gridheight = GridBagConstraints.REMAINDER;
		
		gridbag.setConstraints( okButton, constraints );
		
		add( okButton );
		
		pack();
		setResizable(false);
	}
}
