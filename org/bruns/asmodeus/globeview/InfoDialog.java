//
//  InfoDialog.java
//  globeview
//
//  Created by Christopher Bruns on 3/6/05.
//  Copyright 2005 __MyCompanyName__. All rights reserved.
//
//  InfoDialog - Parent class for help, about, and splash dialogs
// 
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/10 23:58:30  cmbruns
//  Parent class for splash, help, and about dialogs
//

package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class InfoDialog extends Dialog {

	Button okButton;
	Panel textPanel;
	
	InfoDialog(Frame frame, String title, boolean isModal) {
		super(frame, title, isModal); // modal dialog

		setUndecorated(true); // no close button
		setBackground(new Color(255, 255, 200));
		
		//Create OK button
		okButton = new Button("Dismiss");
		okButton.addActionListener ( new ActionListener() 
									 {
			public void actionPerformed( ActionEvent e ) {
				// Hide dialog
				setVisible(false);}									 });		
		
		// Create message to display
		textPanel = new Panel();
		textPanel.setLayout(new GridLayout(0,1,0,0)); // single column
	}
	
	void addLine(String line) {
		textPanel.add(new Label(line));
	}

	// Call at end of creation
	void finalizeDialog() {
		GridBagLayout gridBag = new GridBagLayout();		
		setLayout(gridBag);

		// The message should not fill, it should be centered within
		// this area, with
		// some extra padding.  The gridwidth of REMAINDER means this
		// is the only
		// thing on its row, and the gridheight of RELATIVE means
		// there should only
		// be one thing below it.
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.fill = GridBagConstraints.NONE;
		panelConstraints.anchor = GridBagConstraints.CENTER;
		panelConstraints.ipadx = 0;
		panelConstraints.ipady = 0;
		panelConstraints.weightx = 10.0;
		panelConstraints.weighty = 10.0;
		panelConstraints.gridwidth = GridBagConstraints.REMAINDER;
		panelConstraints.gridheight = GridBagConstraints.RELATIVE;
		panelConstraints.insets = new Insets(15,15,15,15);
		
		gridBag.setConstraints(textPanel, panelConstraints);
		add(textPanel);
		
		// The button has no padding, no weight, taked up minimal width, and
		// Is the last thing in its column.
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.fill = GridBagConstraints.NONE;
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		buttonConstraints.ipadx = 0;
		buttonConstraints.ipady = 0;
		buttonConstraints.weightx = 0.1;
		buttonConstraints.weighty = 0.1;
		buttonConstraints.gridwidth = 1;
		buttonConstraints.gridheight = GridBagConstraints.REMAINDER;
		panelConstraints.insets = new Insets(8,8,8,8);
		
		gridBag.setConstraints(okButton, buttonConstraints);
		add(okButton);

		pack();

		setResizable(false);
	}
	
	void showDialog() {
		// Center on screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int locX = screenSize.width/2 - getSize().width/2;
		int locY = screenSize.height/2 - getSize().height/2;
		if (locX < 0) locX = 0;
		if (locY < 0) locY = 0;
		setLocation(locX, locY);
		
		super.show();
		okButton.requestFocus();
	}
}
