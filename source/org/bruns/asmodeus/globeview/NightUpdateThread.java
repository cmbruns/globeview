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


