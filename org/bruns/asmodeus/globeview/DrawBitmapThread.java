//
//  DrawBitmapThread.java
//  globeview
//
//  Created by Christopher Bruns on 3/4/05.
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
