package org.bruns.asmodeus.globeview3;

// Hierarchical region of space for geometric hashing
// Each cublet contains 125 sub-cublets
// i.e. each edge is divided into 5 pieces

public class Cubelet {
    private final static int cubeletsPerSide = 5;

    private Bounds3D bounds;
    private bool dataLoaded = false;
    private Hashtable subCubelets;

    boolean contains(Vector3D v);
    void render() {
	// 1) return if not on sphere (maybe such cubelets should never exist?
	// 2) return if not shown in current projection (by plane cut-off)
	// 3) return if not on screen (by plane cut-off)
	// 4) render contents
	// 5) recursively call render() routine on sub-cubelets
    }
}

