package org.bruns.asmodeus.globeview;

import org.bruns.asmodeus.globeview.*;

public class TestLabel
{
    public static void main(String arg[]) {
	SiteLabel label = new SiteLabel("Calgary", 51.1, -114.017);
	System.out.println(label.getLabel());
    }

    // Constructor
    TestLabel() {
    }
}
