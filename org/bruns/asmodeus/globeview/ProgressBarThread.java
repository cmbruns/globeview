//
//  ProgressBarThread.java
//  globeview
//
//  Created by Christopher Bruns on 3/14/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
package org.bruns.asmodeus.globeview;

import java.lang.*;
import org.bruns.asmodeus.globeview.*;

public class ProgressBarThread
extends Thread
{
    GeoCanvas canvas; // So we can manipulate image
	
    public ProgressBarThread(GeoCanvas geoCanvas) {
        super("ProgressBarThread");
		canvas = geoCanvas;
    }
	
    // Extend run method to perform action
    public void run() {
		while (true) {
			try {
				if (!canvas.keepProgressBar) return;
				sleep((long)250); // Wait 1/2 second
				if (!canvas.keepProgressBar) return;
				
				// System.out.println("Load " + canvas.loadProgress + ", Draw " + canvas.drawProgress);
				// Don't draw if everything is done
				if ((canvas.loadProgress >= 100) && (canvas.drawProgress >= 100))
				{
					canvas.progressBar.hide();
				}
				else {
					canvas.progressBar.show();
					canvas.progressBar.progressCanvas.forcePaint();
				}

            } catch (InterruptedException e) {
				if (!canvas.keepProgressBar) return;
			}
        }
    }
	
}
