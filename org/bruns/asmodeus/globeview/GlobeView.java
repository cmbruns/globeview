//
// $Id$
// $Header$
// $Log$
// Revision 1.4  2005/03/05 00:05:09  cmbruns
// Changed signature to take only parameter file, not individual file URLS
//
// Created persistent variables for checkBoxMenuItems, so that changes from ParameterFiles can be reflected in the menu selections.
//
// Revision 1.3  2005/03/02 01:55:11  cmbruns
// Added loading of new ParameterFile
// Improved thrown error checking
// Converted projection parameter to lower case before checking
//
// Revision 1.2  2005/03/01 02:13:14  cmbruns
// added cvs headers
//
//

package org.bruns.asmodeus.globeview;

import java.awt.*;
import java.awt.event.*;
import org.bruns.asmodeus.globeview.*;
import java.net.*; // URL

public class GlobeView extends Frame 
    implements ActionListener, ItemListener
{
    GeoCanvas canvas;
    int canvasStartSize = 300;
    AboutDialog aboutDialog;

	// Supported projections
    static final int AZIMUTHALEQUALAREA   = 0;
    static final int AZIMUTHALEQUIDISTANT = 1;
    static final int EQUIRECTANGULAR      = 2;
    static final int GNOMONIC             = 3;
    static final int MERCATOR             = 4;
    static final int ORTHOGRAPHIC         = 5;
    static final int PERSPECTIVE          = 6;
    static final int SINUSOIDAL           = 7;
    static final int STEREOGRAPHIC        = 8;
    static final int totalProjections     = 9;
    static int currentProjection          = ORTHOGRAPHIC;

    CheckboxMenuItem[] projectionCheck = new CheckboxMenuItem[totalProjections];
    static String[] projectionDescription = new String[totalProjections];

	CheckboxMenuItem northUpButton;
	CheckboxMenuItem dayNightButton;
	CheckboxMenuItem graticulesButton;
	CheckboxMenuItem coastsButton;
	CheckboxMenuItem bearingButton;
	CheckboxMenuItem imagesButton;
	CheckboxMenuItem sitesButton;
    
    // Supported mouseActions
    static final int ROT_XY  = 0;
    static final int ZOOM    = 1;
    // static final int ROT_Z   = 2;
    // static final int ROT_XYZ = 3;
    static final int totalMouseActions = 2;
    static int currentMouseAction      = ROT_XY;
    CheckboxMenuItem[] mouseActionCheck = new CheckboxMenuItem[totalMouseActions];
    static String[] mouseActionDescription = new String[totalMouseActions];

    public static void main(String arg[]) {
	try {
		URL parameterURL = new URL(arg[0]);
	    GlobeView frame = new GlobeView(parameterURL);
	    frame.show();
	} catch (Exception exception) {
	    System.out.println(exception);
	    System.exit(0);
	}
    }

    GlobeView(URL parameterURL) {
		super("GlobeView by Chris Bruns");
		
		// Make window close when it should
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					System.exit(0); // Kill the program
				} catch (Exception exception) {
					hide(); // OK, just hide it then
				}
			}});
	
		projectionDescription[AZIMUTHALEQUALAREA] = "Azimuthal Equal Area";
		projectionDescription[AZIMUTHALEQUIDISTANT] = "Azimuthal Equidistant";
		projectionDescription[EQUIRECTANGULAR] = "Equirectangular";
		projectionDescription[GNOMONIC]        = "Gnomonic";
		projectionDescription[MERCATOR]        = "Mercator";
		projectionDescription[ORTHOGRAPHIC]    = "Orthographic";
		projectionDescription[PERSPECTIVE]     = "Perspective";
		projectionDescription[SINUSOIDAL]      = "Sinusoidal";
		projectionDescription[STEREOGRAPHIC]      = "Stereographic";
		
		mouseActionDescription[ROT_XY]  = "Rotate XY";
		// mouseActionDescription[ROT_Z]   = "Rotate Z";
		// mouseActionDescription[ROT_XYZ] = "Rotate XYZ";
		mouseActionDescription[ZOOM]    = "Zoom";
		
		// Menus
		MenuBar menuBar;
		Menu menu;
		MenuItem menuItem;
		CheckboxMenuItem checkboxMenuItem;
		
		menuBar = new MenuBar();
		setMenuBar(menuBar);
		
		menu = new Menu("GlobeView");
		menuBar.add(menu);
		
		northUpButton = new CheckboxMenuItem("North Up");
		northUpButton.setEnabled(true);
		northUpButton.setState(true);
		menu.add(northUpButton);
		northUpButton.addItemListener(this);
		
		dayNightButton = new CheckboxMenuItem("Day/Night");
		dayNightButton.setEnabled(true);
		dayNightButton.setState(true);
		menu.add(dayNightButton);
		dayNightButton.addItemListener(this);
		
		sitesButton = new CheckboxMenuItem("Place Names");
		sitesButton.setEnabled(true);
		sitesButton.setState(true);
		menu.add(sitesButton);
		sitesButton.addItemListener(this);
		
		imagesButton = new CheckboxMenuItem("Satellite Image");
		imagesButton.setEnabled(true);
		imagesButton.setState(true);
		menu.add(imagesButton);
		imagesButton.addItemListener(this);
		
		coastsButton = new CheckboxMenuItem("Coast Lines");
		coastsButton.setEnabled(true);
		coastsButton.setState(true);
		menu.add(coastsButton);
		coastsButton.addItemListener(this);
		
		graticulesButton = new CheckboxMenuItem("Graticule");
		graticulesButton.setEnabled(true);
		graticulesButton.setState(true);
		menu.add(graticulesButton);
		graticulesButton.addItemListener(this);
		
		bearingButton = new CheckboxMenuItem("Antenna Bearing");
		bearingButton.setEnabled(true);
		bearingButton.setState(false);
		menu.add(bearingButton);
		bearingButton.addItemListener(this);
		
		menuItem = new MenuItem("-"); // Separator
		menu.add(menuItem);
		
		menuItem = new MenuItem("Quit");
		menuItem.setEnabled(true);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		// menu = new Menu("Projection");
		// menuBar.add(menu);
		int i; 
		Menu projectionMenu = new Menu("Projection");
		menu = projectionMenu;
		menuBar.add(projectionMenu);
		// menu.add(projectionMenu);
		
		for (i = 0; i < totalProjections; ++i) {
			checkboxMenuItem = new CheckboxMenuItem(projectionDescription[i]);
			checkboxMenuItem.setEnabled(true);
			checkboxMenuItem.setState(i == currentProjection);
			projectionMenu.add(checkboxMenuItem);
			checkboxMenuItem.addItemListener(this);
			projectionCheck[i] = checkboxMenuItem;
		}

		menu = new Menu("MouseDrag");
		menuBar.add(menu);
		for (i = 0; i < totalMouseActions; ++i) {
			mouseActionCheck[i] = new CheckboxMenuItem(mouseActionDescription[i]);
			checkboxMenuItem = mouseActionCheck[i];
			checkboxMenuItem.setEnabled(true);
			checkboxMenuItem.setState(i == currentMouseAction);
			menu.add(checkboxMenuItem);
			checkboxMenuItem.addItemListener(this);
		}
		// mouseActionCheck[ROT_XYZ].setEnabled(false);
		
		menu = new Menu("Help");
		menuBar.setHelpMenu(menu);
		menuItem = new MenuItem("Help");
		menuItem.setEnabled(false);
		menu.add(menuItem);
		menuItem = new MenuItem("About GlobeView");
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		menu.add(menuItem);
		
		// Canvas, which actually implements most of the hard stuff
		canvas = new GeoCanvas(canvasStartSize, canvasStartSize, this);
		canvas.setMouseAction("mouseRotateXY");
		
		try {
			ParameterFile parameterFile = new ParameterFile(parameterURL, canvas);
			canvas.paramFiles.addElement(parameterFile);
		}
		catch (java.io.IOException e) {System.out.println("Parameter file failed, URL: " + parameterURL);}
		
		add(canvas);
		add("South", canvas.messageArea);
		canvas.nightUpdateThread.canvas = canvas;
		
		aboutDialog = new AboutDialog(this);
		
		// IE does not "pack" correctly, so try something reasonable
		setSize(canvasStartSize + 15, canvasStartSize + 65);
		pack();
    }

    // ***********************
    // *** Menu selections ***
    // ***********************
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand() == "Quit") {
	    try {
		System.exit(0); // Kill the program
	    } catch (Exception exception) {
		hide(); // At least hide it, anyway
	    }
	}
	if (e.getActionCommand() == "About GlobeView") {
	    // TODO - get about dialog working
	    aboutDialog.show();
	    // System.out.println("Tell me about globeview");
	}
    }

    public void updateProjectionMenu() {
	// enforce "radio button" behavior
	int j;
	for (j = 0; j < totalProjections; ++j)
	    projectionCheck[j].setState(j == currentProjection);
    }

    // Handle checkbox menu items
    public void itemStateChanged(ItemEvent e) {
	int i, j;

	if (e.getItem() == "North Up") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.setNorthUp(true);
			canvas.fullRepaint();
	    }
	    else {
			canvas.setNorthUp(false);
	    }
	    return;
	}

	if (e.getItem() == "Day/Night") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		canvas.dayNight = true;
		canvas.fullRepaint();
	    }
	    else {
		canvas.dayNight = false;
		canvas.fullRepaint();
	    }
	    return;
	}

	if (e.getItem() == "Graticule") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.drawGraticule = true;
			canvas.fullRepaint();
	    }
	    else {
			canvas.drawGraticule = false;
			canvas.fullRepaint();
	    }
	    return;
	}
	
	if (e.getItem() == "Place Names") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.drawLabels = true;
			canvas.fullRepaint();
	    }
	    else {
			canvas.drawLabels = false;
			canvas.fullRepaint();
	    }
	    return;
	}
	
	if (e.getItem() == "Coast Lines") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.drawCoastLines = true;
			canvas.fullRepaint();
	    }
	    else {
			canvas.drawCoastLines = false;
			canvas.fullRepaint();
	    }
	    return;
	}
	
	if (e.getItem() == "Satellite Image") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.drawSatelliteImage = true;
			canvas.fullRepaint();
	    }
	    else {
			canvas.drawSatelliteImage = false;
			canvas.fullRepaint();
	    }
	    return;
	}
	
	if (e.getItem() == "Antenna Bearing") {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
			canvas.drawBearing = true;
			canvas.fullRepaint();
	    }
	    else {
			canvas.drawBearing = false;
			canvas.fullRepaint();
	    }
	    return;
	}

	// Projections
	for (i = 0; i < totalProjections; ++i) {
	    if (e.getItem() == projectionDescription[i]) {
		currentProjection = i;

		if (e.getItem() == "Orthographic") {
		    // canvas.projection = canvas.orthographic;
		    canvas.projection = Projection.ORTHOGRAPHIC;
		}
		else if (e.getItem() == "Azimuthal Equal Area") {
		    canvas.projection = Projection.AZIMUTHALEQUALAREA;
		}
		else if (e.getItem() == "Azimuthal Equidistant") {
		    canvas.projection = Projection.AZIMUTHALEQUIDISTANT;
		}
		else if (e.getItem() == "Mercator") {
		    canvas.projection = Projection.MERCATOR;
		}
		else if (e.getItem() == "Perspective") {
		    canvas.projection = Projection.PERSPECTIVE;
		}
		else if (e.getItem() == "Equirectangular") {
		    canvas.projection = Projection.EQUIRECTANGULAR;
		}
		else if (e.getItem() == "Gnomonic") {
		    canvas.projection = Projection.GNOMONIC;
		}
		else if (e.getItem() == "Sinusoidal") {
		    canvas.projection = Projection.SINUSOIDAL;
		}
		else if (e.getItem() == "Stereographic") {
		    canvas.projection = Projection.STEREOGRAPHIC;
		}

		updateProjectionMenu();

		canvas.fullRepaint();
		return;
	    }
	}

	// Mouse Actions
	for (i = 0; i < totalMouseActions; ++i) {
	    if (e.getItem() == mouseActionDescription[i]) {
		currentMouseAction = i;

		if (e.getItem() == "Rotate XY")
		    canvas.setMouseAction("mouseRotateXY");
		else if (e.getItem() == "Rotate Z")
		    canvas.setMouseAction("mouseRotateZ");
		else if (e.getItem() == "Rotate XYZ")
		    canvas.setMouseAction("mouseRotateXYZ");
		else if (e.getItem() == "Zoom")
		    canvas.setMouseAction("mouseZoom");
		else if (e.getItem() == "Pan XY")
		    canvas.setMouseAction("mousePanXY");

		// enforce "radio button" behavior
		for (j = 0; j < totalMouseActions; ++j)
		    mouseActionCheck[j].setState(j == currentMouseAction);
		canvas.fastRepaint();
		return;
	    }
	}

    }

    // Try to make a new function to update canvas, menu, etc.
    public void setProjection(Projection p) {
        // 1) update currentProjection index variable
        if (p == Projection.AZIMUTHALEQUALAREA) currentProjection = AZIMUTHALEQUALAREA;
        else if (p == Projection.AZIMUTHALEQUIDISTANT) currentProjection = AZIMUTHALEQUIDISTANT;
        else if (p == Projection.EQUIRECTANGULAR) currentProjection = EQUIRECTANGULAR;
        else if (p == Projection.GNOMONIC) currentProjection = GNOMONIC;
        else if (p == Projection.MERCATOR) currentProjection = MERCATOR;
        else if (p == Projection.ORTHOGRAPHIC) currentProjection = ORTHOGRAPHIC;
        else if (p == Projection.PERSPECTIVE) currentProjection = PERSPECTIVE;
        else if (p == Projection.SINUSOIDAL) currentProjection = SINUSOIDAL;
        else if (p == Projection.STEREOGRAPHIC) currentProjection = STEREOGRAPHIC;
        else System.err.println("ERROR, Unknown projection");

        // 2) update canvas subobject
		canvas.projection = p;

        // 3) update projection menu
        int i;
        for (i = 0; i < totalProjections; ++i) {
            projectionCheck[i].setState(i == currentProjection);
        }
    }

}
