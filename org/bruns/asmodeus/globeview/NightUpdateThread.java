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
	
    public NightUpdateThread(String str) {
        super(str);
    }
	
    // Extend run method to perform action
    public void run() {
		while (true) {
			try {
				sleep((long)30000); // Wait 30 seconds
				if (canvas != null) {
					if (canvas.dayNight) {
						canvas.fullUpdateNow = true;
						canvas.repaint();
					}
				}
            } catch (InterruptedException e) {}
        }
    }
	
}


