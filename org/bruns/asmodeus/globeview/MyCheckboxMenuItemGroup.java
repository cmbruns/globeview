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
