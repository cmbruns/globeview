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
