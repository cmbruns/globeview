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
