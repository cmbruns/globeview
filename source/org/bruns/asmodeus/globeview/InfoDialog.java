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
//  InfoDialog.java
//  globeview
//
//  Created by Christopher Bruns on 3/6/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  InfoDialog - Parent class for help, about, and splash dialogs
// 
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.3  2005/03/14 05:06:58  cmbruns
//  Changed autocreated copyright text from __MyCompanyName__ to Christopher Bruns
//
//  Revision 1.2  2005/03/13 21:38:15  cmbruns
//  Wrapped java 1.4 method setUndecorated to catch NoSuchMethodException
//
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

		try {setUndecorated(true);} // no close button, Java 1.4 only
		catch (java.lang.NoSuchMethodError e) {}

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
