package org.bruns.asmodeus.globeview3;

// A Boundary contains an area, and therefore must follow the edge of a Cubelet if
// cut.
// This is in contrast to a Path, which can end at a Cubelet boundary
// County lines, for example, should produce both Boundaries and Paths

public class Boundary {
    private Boundary decimate(double resolution) {
	// First pass, remove all points separated by less than resolution
	//   keep first and last point -- merge them is that is all there is
        //   if total extent is less than resolution/2, return empty/null
	// Second pass
	//  all points must still be separated by no more than 5 * resolution
	//  remove points whose removal results in interpolation error less than resolution
	//  start with lowest error points first
	//   for each point
	//     store error upon removal
	//     store separation upon removal
	//     upon removal, update values for adjacent points
	//     remove only the best candidate within a window 10 points up and downstream
	
    }
}
