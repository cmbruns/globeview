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
//  Revision 1.3  2005/03/27 21:40:00  cmbruns
//  Added handling of drawNothing variable TODO is this used?
//  Set progress bar to 100 percent when bombing out
//  update two image sources when drawing stereoscopic
//
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
		canvas.drawNothing = true;
		if (!canvas.images.keepDrawing) {
			canvas.unsetWait();
			canvas.drawProgress = 100;
			canvas.drawNothing = false;
			return;
		}
		
		try {
			canvas.setWait("BUSY: DRAWING IMAGE..."); // Could be slow
			canvas.paintBitmap(canvas.offScreenGraphics, graphics, canvas.genGlobe, canvas.projection, viewLens);
			if (!canvas.images.keepDrawing) {
				canvas.unsetWait();
				canvas.drawProgress = 100;
				canvas.drawNothing = false;
				return;
			}

			canvas.fullUpdateNow = false;

			if (canvas.drawStereoscopic) {
				canvas.offScreenSource.newPixels();			
				canvas.leftEyeSource.newPixels();			
				canvas.updateOffScreen(viewLens);				
			}
			else {
				canvas.offScreenSource.newPixels();			
				canvas.updateOffScreen(viewLens);
			}

			canvas.updateOnScreen(graphics);

			canvas.unsetWait();
			canvas.drawProgress = 100;
		}
		catch (OutOfMemoryError e) {
			canvas.respondToOutOfMemory(e);
			canvas.drawProgress = 100;
			canvas.drawNothing = false;
			throw e;
		}
		canvas.drawNothing = false;
		return;
	}
}
