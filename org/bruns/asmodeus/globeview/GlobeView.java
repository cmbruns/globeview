//
// $Id$
// $Header$
// $Log$
// Revision 1.8  2005/03/28 01:12:06  cmbruns
// New menus for detail level, rivers, and political borders
// Menus rearranged under Nature, Cartography, and Politics
// Stereoscopic menu items placed under Perspective projection
// Modified Projection and MouseAction menus to use MyCheckBoxMenuItemGroup for radio button behavior
// Modified starting width and height of canvas
//
// Revision 1.7  2005/03/14 04:20:47  cmbruns
// Change catch of AccessControlExeception to placate Netscape 4.7, which does not understand java.security.AccessControlException, but prefers netscape.security.AccessControlException
//
// Revision 1.6  2005/03/13 21:58:47  cmbruns
// Added optional crosshair menu
// Added RotateZ option to mouse actions, but only when North is Up
// Replaced generic exception catches with specific ones.
// Rearranged and renamed some existing menus.
//
// Revision 1.5  2005/03/11 00:07:32  cmbruns
// Added Help dialog
// Added support for scale bar on canvas
// Window now centers on the users screen
//
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
import java.net.*; // URL
import java.util.*;
import org.bruns.asmodeus.globeview.*;

public class GlobeView extends Frame 
    implements ActionListener, ItemListener
{
    GeoCanvas canvas;
    int canvasStartWidth = 500;
    int canvasStartHeight = 280;
    AboutDialog aboutDialog;
    HelpDialog helpDialog;

	CheckboxMenuItem northUpButton;
	CheckboxMenuItem dayNightButton;
	CheckboxMenuItem graticulesButton;
	CheckboxMenuItem coastsButton;
	CheckboxMenuItem riversButton;
	CheckboxMenuItem bearingButton;
	CheckboxMenuItem imagesButton;
	CheckboxMenuItem sitesButton;
	CheckboxMenuItem bordersButton;
	CheckboxMenuItem scaleBarButton;
	CheckboxMenuItem crosshairButton;
    
	// Groups of menu items that must exhibit radio button behavior
	MyCheckboxMenuItemGroup detailGroup = new MyCheckboxMenuItemGroup();
	MyCheckboxMenuItemGroup projectionGroup = new MyCheckboxMenuItemGroup();
	MyCheckboxMenuItemGroup mouseActionGroup = new MyCheckboxMenuItemGroup();
	
	// Store mapping of map projections to menu item names
	Hashtable projectionNames = new Hashtable();
	Hashtable mouseActionNames = new Hashtable();
	
    public static void main(String arg[]) {
		try {
			URL parameterURL = new URL(arg[0]);
			GlobeView frame = new GlobeView(parameterURL);
			frame.show();
		} catch (MalformedURLException exception) {
			System.out.println(exception);
			System.exit(0);
		}
    }

    GlobeView(URL parameterURL) {
		super("GlobeView by Chris Bruns");
		
		// Make window close when it should
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {System.exit(0);} // Kill the program
				catch (Exception exception) {hide();} // OK, just hide it then
			}});

		// Menus
		MenuBar menuBar;
		Menu menu;
		MenuItem menuItem;
		CheckboxMenuItem checkboxMenuItem;
		
		menuBar = new MenuBar();
		setMenuBar(menuBar);
		
		menu = new Menu("GlobeView");
		menuBar.add(menu);
		
		Menu natureMenu = new Menu("Nature");
		menu.add(natureMenu);
		Menu cartographyMenu = new Menu("Cartography");
		menu.add(cartographyMenu);
		Menu politicsMenu = new Menu("Politics");
		menu.add(politicsMenu);

		northUpButton = new CheckboxMenuItem("Force North Up");
		northUpButton.setEnabled(true);
		northUpButton.setState(true);
		cartographyMenu.add(northUpButton);
		northUpButton.addItemListener(this);
		
		dayNightButton = new CheckboxMenuItem("Darken Night Side");
		dayNightButton.setEnabled(true);
		dayNightButton.setState(true);
		natureMenu.add(dayNightButton);
		dayNightButton.addItemListener(this);
		
		sitesButton = new CheckboxMenuItem("Show Place Names");
		sitesButton.setEnabled(true);
		sitesButton.setState(true);
		politicsMenu.add(sitesButton);
		sitesButton.addItemListener(this);
		
		bordersButton = new CheckboxMenuItem("Show Political Borders");
		bordersButton.setEnabled(true);
		bordersButton.setState(true);
		politicsMenu.add(bordersButton);
		bordersButton.addItemListener(this);
		
		imagesButton = new CheckboxMenuItem("Show Planet Image");
		imagesButton.setEnabled(true);
		imagesButton.setState(true);
		natureMenu.add(imagesButton);
		imagesButton.addItemListener(this);
		
		coastsButton = new CheckboxMenuItem("Show Coast Lines");
		coastsButton.setEnabled(true);
		coastsButton.setState(true);
		natureMenu.add(coastsButton);
		coastsButton.addItemListener(this);
		
		riversButton = new CheckboxMenuItem("Show Rivers");
		riversButton.setEnabled(true);
		riversButton.setState(true);
		natureMenu.add(riversButton);
		riversButton.addItemListener(this);
		
		graticulesButton = new CheckboxMenuItem("Show Graticule");
		graticulesButton.setEnabled(true);
		graticulesButton.setState(true);
		cartographyMenu.add(graticulesButton);
		graticulesButton.addItemListener(this);
		
		scaleBarButton = new CheckboxMenuItem("Show Scale Bar");
		scaleBarButton.setEnabled(true);
		scaleBarButton.setState(true);
		cartographyMenu.add(scaleBarButton);
		scaleBarButton.addItemListener(this);
		
		crosshairButton = new CheckboxMenuItem("Show Crosshair");
		crosshairButton.setEnabled(true);
		crosshairButton.setState(true);
		cartographyMenu.add(crosshairButton);
		crosshairButton.addItemListener(this);
		
		bearingButton = new CheckboxMenuItem("Show Antenna Bearing");
		bearingButton.setEnabled(true);
		bearingButton.setState(false);
		cartographyMenu.add(bearingButton);
		bearingButton.addItemListener(this);
		
		Menu dragMenu = new Menu("Mouse Drag Action");
		menu.add(dragMenu);
		addMouseActionItem(dragMenu, "Rotate XY", true);
		addMouseActionItem(dragMenu, "Zoom", true);
		boolean doIRotateZ = true;
		if (northUpButton.isEnabled()) doIRotateZ = false;
		addMouseActionItem(dragMenu, "Rotate Z", doIRotateZ);
		mouseActionGroup.getItem("Rotate XY").setState(true); // Set initial mouse to rot XY
		
		Menu detailMenu = new Menu("Detail Level");
		menu.add(detailMenu);
		
		CheckboxMenuItem lowDetailButton = new CheckboxMenuItem("Coarse Detail");
		lowDetailButton.setEnabled(true);
		lowDetailButton.setState(false);
		detailMenu.add(lowDetailButton);
		lowDetailButton.addItemListener(this);
		detailGroup.add(lowDetailButton);

		CheckboxMenuItem mediumDetailButton = new CheckboxMenuItem("Normal Detail");
		mediumDetailButton.setEnabled(true);
		mediumDetailButton.setState(true);
		detailMenu.add(mediumDetailButton);
		mediumDetailButton.addItemListener(this);
		detailGroup.add(mediumDetailButton);

		CheckboxMenuItem highDetailButton = new CheckboxMenuItem("Fine Detail");
		highDetailButton.setEnabled(true);
		highDetailButton.setState(false);
		detailMenu.add(highDetailButton);
		highDetailButton.addItemListener(this);
		detailGroup.add(highDetailButton);
		
		menuItem = new MenuItem("-"); // Separator
		menu.add(menuItem);
		
		menuItem = new MenuItem("Quit GlobeView");
		menuItem.setEnabled(true);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		int i; 
		Menu projectionMenu = new Menu("MapProjection");
		menuBar.add(projectionMenu);
		
		addProjectionMenuItem(projectionMenu, "Azimuthal Equal Area", true);
		addProjectionMenuItem(projectionMenu, "Azimuthal Equidistant", true);
		addProjectionMenuItem(projectionMenu, "Equirectangular", true);
		addProjectionMenuItem(projectionMenu, "Gnomonic", true);
		addProjectionMenuItem(projectionMenu, "Mercator", true);
		addProjectionMenuItem(projectionMenu, "Orthographic", true);

		Menu perspectiveMenu = new Menu("Perspective");
		projectionMenu.add(perspectiveMenu);

		addProjectionMenuItem(perspectiveMenu, "Normal Monoscopic Perspective", true);
		addProjectionMenuItem(perspectiveMenu, "Cross-Eye 3D", true);
		addProjectionMenuItem(perspectiveMenu, "Wall-Eye 3D", false);
		addProjectionMenuItem(perspectiveMenu, "Red/Blue 3D", false);
		addProjectionMenuItem(perspectiveMenu, "Interlaced 3D", false);		

		addProjectionMenuItem(projectionMenu, "Sinusoidal", true);
		addProjectionMenuItem(projectionMenu, "Stereographic", true);
				
		menu = new Menu("Help");
		menuBar.setHelpMenu(menu);
		menuItem = new MenuItem("GlobeView Help");
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		menu.add(menuItem);

		menuItem = new MenuItem("-"); // Separator
		menu.add(menuItem);
		
		menuItem = new MenuItem("About GlobeView");
		menuItem.addActionListener(this);
		menuItem.setEnabled(true);
		menu.add(menuItem);
		
		// Canvas, which actually implements most of the hard stuff
		canvas = new GeoCanvas(canvasStartWidth, canvasStartHeight, this);
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
		helpDialog = new HelpDialog(this);
		
		// IE does not "pack" correctly, so try something reasonable
		setSize(canvasStartWidth + 15, canvasStartHeight + 65);
		pack();
		
		// Center on screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int locX = screenSize.width/2 - getSize().width/2;
		int locY = screenSize.height/2 - getSize().height/2;
		if (locX < 0) locX = 0;
		if (locY < 0) locY = 0;
		setLocation(locX, locY);
    }

	void addProjectionMenuItem(Menu m, String projectionName, boolean enabled) {

		Projection p = Projection.getByName(projectionName);
		if (p == null) // There is no such projection
			throw new NoSuchElementException("No such projection " + projectionName);
		projectionNames.put(p, projectionName);
		
		CheckboxMenuItem checkboxMenuItem = new CheckboxMenuItem(projectionName);
		checkboxMenuItem.setEnabled(enabled);
		checkboxMenuItem.setState(false);
		m.add(checkboxMenuItem);
		checkboxMenuItem.addItemListener(this);
		projectionGroup.add(checkboxMenuItem);
	}
	
	void addMouseActionItem(Menu m, String mouseActionName, boolean enabled) {
		CheckboxMenuItem mouseActionCheck = new CheckboxMenuItem(mouseActionName);
		mouseActionCheck.setEnabled(enabled);
		mouseActionCheck.setState(false);
		m.add(mouseActionCheck);
		mouseActionCheck.addItemListener(this);
		mouseActionGroup.add(mouseActionCheck);
		mouseActionNames.put(mouseActionName, "No value");
	}
	
    // ***********************
    // *** Menu selections ***
    // ***********************
    public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Quit GlobeView") {
			try {
				System.exit(0); // Kill the program
			// } catch (java.security.AccessControlException exception) {
			} catch (Exception exception) {
				hide(); // At least hide it, anyway
			}
		}
		if (e.getActionCommand() == "About GlobeView") {
			aboutDialog.showDialog();
		}
		if (e.getActionCommand() == "GlobeView Help") {
			helpDialog.showDialog();
		}
    }

	void setNorthUp(boolean status) {
		northUpButton.setState(status);
		CheckboxMenuItem rotateZMenu = mouseActionGroup.getItem("Rotate Z");
		if (status) {
			rotateZMenu.setEnabled(false);
			if (rotateZMenu.getState()) {
				mouseActionGroup.set("Rotate XY");
				canvas.setMouseAction("mouseRotateXY");
			}
		}
		else {
			rotateZMenu.setEnabled(true);
		}
	}
	
    // Handle checkbox menu items
    public void itemStateChanged(ItemEvent e) {
		int i, j;
		String itemName = (String) e.getItem();
		
		if (itemName.equals("Force North Up")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				canvas.setNorthUp(true);
				setNorthUp(true);
				canvas.fullRepaint();
			}
			else {
				canvas.setNorthUp(false);
				setNorthUp(false);
			}
			return;
		}
		
		if (itemName.equals("Darken Night Side")) {
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

		if (itemName.equals("Show Graticule")) {
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
		
		if (itemName.equals("Show Place Names")) {
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
		
		if (itemName.equals("Show Coast Lines")) {
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
	
		if (itemName.equals("Show Rivers")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				canvas.drawRivers = true;
				canvas.fullRepaint();
			}
			else {
				canvas.drawRivers = false;
				canvas.fullRepaint();
			}
			return;
		}
		
		if (itemName.equals("Show Planet Image")) {
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
		
		if (itemName.equals("Show Crosshair")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				canvas.drawCrosshair = true;
				canvas.fullRepaint();
			}
			else {
				canvas.drawCrosshair = false;
				canvas.fullRepaint();
			}
			return;
		}
		
		if (itemName.equals("Show Scale Bar")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				canvas.drawScaleBar = true;
				canvas.fullRepaint();
			}
			else {
				canvas.drawScaleBar = false;
				canvas.fullRepaint();
			}
			return;
		}
	
		if (itemName.equals("Show Antenna Bearing")) {
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
		
		if (itemName.equals("Coarse Detail")) {
			detailGroup.set(itemName);
			canvas.genGlobe.setDetailLevel(0.5);
			canvas.fullRepaint();
			return;
		}
		if (itemName.equals("Normal Detail")) {
			detailGroup.set(itemName);
			canvas.genGlobe.setDetailLevel(1.0);
			canvas.fullRepaint();
			return;
		}
		if (itemName.equals("Fine Detail")) {
			detailGroup.set(itemName);
			canvas.genGlobe.setDetailLevel(2.0);
			canvas.fullRepaint();
			return;
		}
		
		// Projections
		Projection p = Projection.getByName(itemName);
		if (p != null) {
			if (canvas.projection != p) { // Projection actually changed
				canvas.setProjection(p);
				projectionGroup.set(itemName);
				canvas.fullRepaint();
			}
			return;
		}		

		// Mouse Actions
		if (mouseActionNames.get(itemName) != null) {

			if (itemName.equals("Rotate XY"))
				canvas.setMouseAction("mouseRotateXY");
			else if (itemName.equals("Rotate Z"))
				canvas.setMouseAction("mouseRotateZ");
			else if (itemName.equals("Rotate XYZ"))
				canvas.setMouseAction("mouseRotateXYZ");
			else if (itemName.equals("Zoom"))
				canvas.setMouseAction("mouseZoom");
			else if (itemName.equals("Pan XY"))
				canvas.setMouseAction("mousePanXY");

			mouseActionGroup.set(itemName);
			canvas.fastRepaint();
			return;
		}
    }
	
	void setProjection(Projection p) {
		if (p == null) return;
		String projectionName = (String)projectionNames.get(p);
		if (projectionName == null)
			throw new NoSuchElementException("Projection name not found for " +
											 p.getName());
		projectionGroup.set(projectionName);
		canvas.projection = p;
	}

	public void stop() { // What to do when parent applet gets stop()
		hide();
		canvas.stopBitmapThread();
		canvas.stopNightUpdateThread();
		canvas.stopProgressBarThread();
	}
	public void start() { // What to do when parent applet gets stop()
		canvas.startNightUpdateThread();
		canvas.startProgressBarThread();
		show();
	}
}
