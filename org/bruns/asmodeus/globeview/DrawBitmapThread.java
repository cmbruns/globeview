//
//  DrawBitmapThread.java
//  globeview
//
//  Created by Christopher Bruns on 3/4/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.2  2005/03/13 21:41:44  cmbruns
//  run() method now catches OutOfMemoryError
//
//  Revision 1.1  2005/03/04 23:55:52  cmbruns
//  Created DrawBitmapThread object to draw slow satellite and day/night updates.  This way the draw can be interrupted by the main thread when the user gets bored/frustrated/confused.
//
//
//  Thread to draw the bitmap in globeview without locking everything up

package org.bruns.asmodeus.globeview;

import java.lang.*;
import java.awt.*;
import org.bruns.asmodeus.globeview.*;

public class DrawBitmapThread 
extends Thread
{
	GeoCanvas canvas;
	Graphics graphics;
	LensRegion viewLens;

    public DrawBitmapThread(String str, GeoCanvas gc, Graphics g, LensRegion l) {
        super(str);
		canvas = gc;
		graphics = g;
		viewLens = l;
    }
	
    // Extend run method to perform action
    public void run() {
		if (!canvas.images.keepDrawing) return;
		
		try {
			canvas.setWait("BUSY: DRAWING IMAGE..."); // Could be slow
			canvas.paintBitmap(canvas.offScreenGraphics, graphics, canvas.genGlobe, canvas.projection, viewLens);
			if (!canvas.images.keepDrawing) {
				canvas.unsetWait();
				return;
			}

			canvas.fullUpdateNow = false;
			canvas.offScreenSource.newPixels();
			canvas.offScreenGraphics.drawImage(canvas.memOffScreenImage, 0, 0, null);		
			canvas.paintForeground(canvas.offScreenGraphics, viewLens);
			graphics.drawImage(canvas.offScreenImage, 0, 0, canvas);
			canvas.unsetWait();
		}
		catch (OutOfMemoryError e) {
			canvas.respondToOutOfMemory(e);
			return;
		}
	}
}
