//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/03/28 01:48:25  cmbruns
// Pass canvas as constructor argument
// Extend interval from 30 seconds to 60 seconds
//
// Revision 1.3  2005/03/14 04:25:24  cmbruns
// Permit stopping of nightUpdateThread by unsetting canvas.keepNightUpdate variable.
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.lang.*;
import org.bruns.asmodeus.globeview.*;

// NightUpdateThread.java
// March 16, 2003  Chris Bruns
// Intended to update display when a period of inactivity has elapsed

public class NightUpdateThread
extends Thread
{
    GeoCanvas canvas; // So we can manipulate image
	
    public NightUpdateThread(GeoCanvas geoCanvas) {
        super("NightUpdateThread");
		canvas = geoCanvas;
    }
	
    // Extend run method to perform action
    public void run() {
		while (true) {
			try {
				sleep((long)60000); // Wait 60 seconds
				if (canvas != null) {
					if (!canvas.keepNightUpdate) return;
					if (canvas.dayNight) {
						canvas.fullUpdateNow = true;
						canvas.repaint();
					}
				}
            } catch (InterruptedException e) {
				if (!canvas.keepNightUpdate) return;
			}
        }
    }
	
}


