package org.bruns.asmodeus.globeview;

public class ScreenPoint {
    int[] element = { 0, 0 };
    int flag = 0; // So we can remember seam-crossing

    ScreenPoint() {
    }
    ScreenPoint(int x, int y) {
	set(x, y);
    }

    int x() {return element[0];}
    int y() {return element[1];}

    void set(int x, int y) {
	element[0] = x;
	element[1] = y;
    }
}
