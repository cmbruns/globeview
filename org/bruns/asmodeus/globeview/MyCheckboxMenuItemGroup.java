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
//  MyCheckBoxMenuItemGroup.java
//  globeview
//
//  Created by Christopher Bruns on 3/16/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  Group checkBoxMenuItems to get Radio Button behavior
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/28 01:47:00  cmbruns
//  New class for enforcing radio button behavior on CheckboxMenuItems
//

package org.bruns.asmodeus.globeview;

import java.util.*;
import java.awt.*;
import org.bruns.asmodeus.globeview.*;

public class MyCheckboxMenuItemGroup {
	private Vector items = new Vector();
	
	public MyCheckboxMenuItemGroup() {}

	public void add(CheckboxMenuItem c) {
		items.addElement(c);
	}
	
    public void set(String itemName) throws NoSuchElementException {

		// First make sure the item is in the group
		boolean foundItem = false;
ITEMS:    for (int i = 0; i < items.size(); i++) {
	        CheckboxMenuItem item = (CheckboxMenuItem) items.elementAt(i);
	        String itemLabel = item.getLabel();
			if (itemLabel.equals(itemName)) {
				foundItem = true;
				break ITEMS;
			}
		}
		if (!foundItem) throw new NoSuchElementException("No CheckboxMenuItem called " + itemName);
				
		// enforce "radio button" behavior
		for (int i = 0; i < items.size(); i++) {
	    CheckboxMenuItem item = (CheckboxMenuItem) items.elementAt(i);
	        String itemLabel = item.getLabel();
			if (itemLabel.equals(itemName)) item.setState(true);
			else item.setState(false);
		}
    }
	
	CheckboxMenuItem getItem(String itemName) {
		for (int i = 0; i < items.size(); i++) {
			CheckboxMenuItem item = (CheckboxMenuItem) items.elementAt(i);
			String itemLabel = item.getLabel();
			if (itemLabel.equals(itemName))
				return item;
		}
		return null;
	}
}
