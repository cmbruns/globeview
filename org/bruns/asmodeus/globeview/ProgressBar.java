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
//  ProgressBar.java
//  globeview
//
//  Created by Christopher Bruns on 3/14/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  Growing bar to show progress of download and drawing
//
//  -------------
//  Loading... 59%
//  |######     |
//
//  Drawing... 20%
//  |##         |
//  -------------
//  
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/28 01:50:35  cmbruns
//  New class to display the progress of slow file loading and image drawing activities
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import org.bruns.asmodeus.globeview.*;

public class ProgressBar extends Frame {

	GeoCanvas canvas;
	ProgressCanvas progressCanvas;
	
	ProgressBar (GeoCanvas geoCanvas) {
		super("GlobeView Progress");
		canvas = geoCanvas;
		
		progressCanvas = new ProgressCanvas(this);
		add(progressCanvas);

		try{setUndecorated(true);}
		catch(NoSuchMethodError e) {}
		try{setFocusableWindowState(false);}
		catch(NoSuchMethodError e) {}

		setResizable(false);
		pack();
	}
	
	class ProgressCanvas extends Canvas {

		Image progressImage = null;
		Graphics graphics;
		
		int width = 120;
		int height = 60;
		
		int barLeft = 10;
		int barWidth = width - (2*barLeft);
		int barTop = 12;
		int barHeight = 10;
		int barInterval = height/2;
		
		Color bgColor = new Color(200, 200, 160);
		Color bgHilightColor = new Color(250, 250, 200);
		Color bgLolightColor = new Color(150, 150, 120);
		
		Color barColor = new Color(160, 160, 200);
		Color barHilightColor = new Color(200, 200, 250);
		Color barLolightColor = new Color(120, 120, 150);
		
		Color barBgColor = new Color(100,100,100);
		
		Color textColor = new Color(0,0,0);
		Font font = new Font("Arial", Font.PLAIN, 10);
		
		ProgressBar progressBar;
				
		ProgressCanvas(ProgressBar parent) {
			super();
			progressBar = parent;
			setSize(width, height);
		}
		
		public void update(Graphics g) {
			paint(g);
		}
	
		void checkImage() {
			if (progressImage == null) {
				progressImage = createImage(width, height);
				graphics = progressImage.getGraphics();
			}
		}

		public void forcePaint() {
			paint(getGraphics());
		}
		
		public void paint(Graphics g) {
			try {
				Point p = canvas.getLocationOnScreen();
				progressBar.setLocation(p.x + 5, p.y + 5);
			} catch (IllegalComponentStateException e) {
				// Canvas is not displayed
				progressBar.hide();
				return;
			}
									
			double loadProgress = canvas.loadProgress;
			double drawProgress = canvas.drawProgress;
			
			checkImage();
			// Background
			graphics.setColor(bgColor);
			graphics.fillRect(0,0,width,height);
			
			// Background Border
			graphics.setColor(bgHilightColor);
			graphics.drawLine(0,0,width-1,0);
			graphics.drawLine(0,0,0,height-1);
			
			graphics.setColor(bgLolightColor);
			graphics.drawLine(0,height-1,width-1,height-1);
			graphics.drawLine(width-1,0,width-1,height-1);

			// Bar background
			graphics.setColor(barBgColor);
			graphics.fillRect(barLeft,barTop,barWidth+1,barHeight+1);
			graphics.fillRect(barLeft,barTop+barInterval,barWidth+1,barHeight+1);
			
			// Bar outer border
			graphics.setColor(bgLolightColor);
			graphics.drawLine(barLeft-1, barTop-1, barLeft-1,barTop+barHeight+1);
			graphics.drawLine(barLeft-1, barTop-1, barLeft+barWidth+1,barTop-1);
			graphics.drawLine(barLeft-1, barTop+barInterval-1, barLeft-1,barTop+barInterval+barHeight+1);
			graphics.drawLine(barLeft-1, barTop+barInterval-1, barLeft+barWidth+1,barTop+barInterval-1);
			
			graphics.setColor(bgHilightColor);
			graphics.drawLine(barLeft+barWidth+1, barTop-1, barLeft+barWidth+1,barTop+barHeight+1);
			graphics.drawLine(barLeft-1, barTop+barHeight+1, barLeft+barWidth+1,barTop+barHeight+1);
			graphics.drawLine(barLeft+barWidth+1, barTop+barInterval-1, barLeft+barWidth+1,barTop+barInterval+barHeight+1);
			graphics.drawLine(barLeft-1, barTop+barInterval+barHeight+1, barLeft+barWidth+1,barTop+barInterval+barHeight+1);

			// Labels
			graphics.setFont(font);
			graphics.setColor(textColor);
			graphics.drawString("Loading... "+(int)Math.round(loadProgress)+"%", 2, 8);
			graphics.drawString("Drawing... "+(int)Math.round(drawProgress)+"%", 2, 38);
			
			int loadPixels = (int)(loadProgress/100 * barWidth);
			int drawPixels = (int)(drawProgress/100 * barWidth);
			
			// The bars themselves
			graphics.setColor(barColor);
			graphics.fillRect(barLeft,barTop,loadPixels+1,barHeight+1);
			graphics.fillRect(barLeft,barTop+barInterval,drawPixels+1,barHeight+1);
			
			// Bar inner border
			graphics.setColor(barHilightColor);
			graphics.drawLine(barLeft, barTop, barLeft,barTop+barHeight);
			graphics.drawLine(barLeft, barTop, barLeft+loadPixels,barTop);
			graphics.drawLine(barLeft, barTop+barInterval, barLeft,barTop+barInterval+barHeight);
			graphics.drawLine(barLeft, barTop+barInterval, barLeft+drawPixels,barTop+barInterval);
			
			graphics.setColor(barLolightColor);
			graphics.drawLine(barLeft+loadPixels, barTop, barLeft+loadPixels,barTop+barHeight);
			graphics.drawLine(barLeft, barTop+barHeight, barLeft+loadPixels,barTop+barHeight);
			graphics.drawLine(barLeft+drawPixels, barTop+barInterval, barLeft+drawPixels,barTop+barInterval+barHeight);
			graphics.drawLine(barLeft, barTop+barInterval+barHeight, barLeft+drawPixels,barTop+barInterval+barHeight);
			
			g.drawImage(progressImage, 0, 0, this);
		}
	}
}
