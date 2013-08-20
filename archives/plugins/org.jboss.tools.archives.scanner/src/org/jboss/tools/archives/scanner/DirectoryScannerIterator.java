/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.archives.scanner;

import java.util.ArrayList;

/**
 * This class is the default implementation for an {@link IDirectoryScannerIterator}
 * And can be used to traverse a directory scanner without forcing a full scan
 * of the underlying filesystem. 
 * 
 * It does this by only searching complete folders until results are found, 
 * and any additionally located folders are delayed to be scanned, but are
 * registered in a list, so that they can be scanned on the next call
 * for the iterator. 
 */
public class DirectoryScannerIterator<T> implements IDirectoryScannerIterator<T> {
	
	/**
	 * A list of matches
	 */
	protected ArrayList<DSPair> matches;
	
	/**
	 * The queue of folders still to be scanned
	 */
	protected ArrayList<DSPair> toScan;
	
	/**
	 * The scanner
	 */
	protected IterableDirectoryScanner<T> scanner;
	
	/**
	 * The pointer for where we are in the matches list. 
	 */
	protected int pointer;
	public DirectoryScannerIterator(IterableDirectoryScanner<T> scanner) {
		this.scanner = scanner;
		this.matches = new ArrayList<DSPair>();
		this.toScan = new ArrayList<DSPair>();
		pointer = 0;
	}
	
	/**
	 * Add the given file and path to the scan list
	 */
	public void addElementToScanList(T element, String vpath) {
		toScan.add(0, new DSPair(element, vpath));
	}
	
	/**
	 * A match has been discovered
	 */
	public void addMatch(T file, String vpath) {
		matches.add(pointer, new DSPair(file, vpath));
	}
	
	/**
	 * This call MAY be slow if the current list of matches
	 * is empty, but there are still folders in the queue. 
	 * It will search the remaining folders in the queue until a match 
	 * is found. 
	 */
	public boolean hasNext() {
		if( pointer <= matches.size() -1 ) return true;
		if( toScan.isEmpty()) return false;
		
		while( !toScan.isEmpty() && pointer == matches.size()) {
			DSPair pair = toScan.remove(0);
			scanner.scanDirectory(pair.element, pair.vpath);
		}
		
		boolean hasNext =  pointer <= matches.size() -1 ||
			(pointer == matches.size() && !toScan.isEmpty());
		if( !hasNext )
			scanner.cleanup();
		return hasNext;
	}

	/**
	 * Retrieve the next file.
	 */
	public T next() {
		if( pointer <= (matches.size()-1))
			return matches.get(pointer++).element;
		return null;
	}

	/**
	 * Unsupported remove
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * An internal class simply to keep a pair of data
	 */
	protected class DSPair {
		private T element;
		private String vpath;
		public DSPair(T element, String vpath) {
			this.element = element;
			this.vpath = vpath;
		}
	}
}