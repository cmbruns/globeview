//
//  MeasuredInputStream.java
//  globeview
//
//  Created by Christopher Bruns on 3/14/05.
//  Copyright 2005 Christopher Bruns. All rights reserved.
//
//  Extend input stream to count how many bytes it has read
//
//  $Id$
//  $Header$
//  $Log$
//  Revision 1.1  2005/03/28 01:46:27  cmbruns
//  New class for monitoring the progress of file loading
//

package org.bruns.asmodeus.globeview;

import java.io.*;
import java.net.*;
import org.bruns.asmodeus.globeview.*;

public class MeasuredInputStream extends InputStream {
	long bytesRead = 0;
	long totalBytes = -1;
	long startTime = -1;
	InputStream internalInputStream;
	GeoCanvas canvas; // So we can store the progress
	
	MeasuredInputStream(InputStream is, GeoCanvas geoCanvas) {
		canvas = geoCanvas;
		internalInputStream = is;
		totalBytes = -1;
	}
	
	MeasuredInputStream(InputStream is, long size, GeoCanvas geoCanvas) {
		canvas = geoCanvas;
		internalInputStream = is;
		totalBytes = size;
	}

	MeasuredInputStream(URL url, GeoCanvas geoCanvas) throws IOException {
		canvas = geoCanvas;
		URLConnection connection = url.openConnection(); 
		totalBytes = connection.getContentLength(); 
		internalInputStream = connection.getInputStream(); 
	}

	// Overloaded method is the trick to this class
	public int read(byte[] b, int off, int len)
		throws IOException {
			if (startTime == -1)
				startTime = System.currentTimeMillis();
			
			int read = internalInputStream.read(b,off,len);
			bytesRead += read;
			
			canvas.loadProgress = getPercentProgress();
			
			return read;
		}
	
	public int read() throws IOException { 
		if (startTime == -1) 
			startTime = System.currentTimeMillis(); 		
		int b = internalInputStream.read(); 		
		if (b != -1) 
			bytesRead++; 		

		canvas.loadProgress = getPercentProgress();
		
		return b; 
	} 
	
	// Thin wrappers
	public int available() throws IOException {return internalInputStream.available();}
	public void close() throws IOException {
		internalInputStream.close();
		canvas.loadProgress = 100;
	} 
	public void mark(int readlimit) {internalInputStream.mark(readlimit);} 	
	public boolean markSupported() {return internalInputStream.markSupported();} 

	public void reset() throws IOException { 
		startTime = -1; 
		bytesRead = 0; 		
		internalInputStream.reset(); 
	} 
	
	public long skip(long n) throws 
		IOException { 
			long skipped = internalInputStream.skip(n); 
			bytesRead += skipped; 			
			
			canvas.loadProgress = getPercentProgress();
			
			return skipped; 
		} 
	
	double getPercentProgress() {
		if (getTotalBytes() <= 0) return -1;
		return (100.0*getBytesRead()/getTotalBytes());
	}
	
	long getBytesRead() {
		return bytesRead;
	}
	long getTotalBytes() {
		return totalBytes;
	}
}
