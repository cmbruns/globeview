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
