package org.bruns.asmodeus.globeview;

import java.applet.*;
import java.awt.*;
import org.bruns.asmodeus.globeview.*;

public class GlobeViewApplet extends Applet
{
    GeoCanvas canvas;
    // functions specific to Applets
    public void init() {
	canvas = new GeoCanvas(300,300);
	canvas.nightUpdateThread.canvas = canvas;

	add(canvas);
    }
}
