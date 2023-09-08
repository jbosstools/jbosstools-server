/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util;

import java.util.Comparator;

/**
 * Compare two strings for the purposes of sorting servers strings that have
 * letters and numeric (ie version) strings mixed in the middle.
 *
 * This is required to ensure that wildfly-7.0.0-Final is marked as earlier than
 * Wildfly-10.0.0-Final. A normal string comparison would mark wildfly10 as
 * lower because of the presence of the 1.
 */
public class AlphanumComparator implements Comparator<String> {
	public AlphanumComparator() {
	}

	@Override
	public int compare(String s1, String s2) {
		return staticCompare(s1, s2);
	}

	/**
	 * Length of string is passed in for improved efficiency (only need to calculate
	 * it once)
	 **/
	private static final String getChunk(String s, int slength, int marker) {
		StringBuilder chunk = new StringBuilder();
		char c = s.charAt(marker);
		chunk.append(c);
		marker++;
		if (Character.isDigit(c)) {
			while (marker < slength) {
				c = s.charAt(marker);
				if (!Character.isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		} else {
			while (marker < slength) {
				c = s.charAt(marker);
				if (Character.isDigit(c))
					break;
				chunk.append(c);
				marker++;
			}
		}
		return chunk.toString();
	}

	public static int staticCompare(String s1, String s2) {
		if ((s1 == null) || (s2 == null)) {
			return 0;
		}

		int thisMarker = 0;
		int thatMarker = 0;
		int s1Length = s1.length();
		int s2Length = s2.length();

		while (thisMarker < s1Length && thatMarker < s2Length) {
			String thisChunk = getChunk(s1, s1Length, thisMarker);
			thisMarker += thisChunk.length();

			String thatChunk = getChunk(s2, s2Length, thatMarker);
			thatMarker += thatChunk.length();

			// If both chunks contain numeric characters, sort them numerically
			int result = 0;
			if (Character.isDigit(thisChunk.charAt(0)) && Character.isDigit(thatChunk.charAt(0))) {
				// Simple chunk comparison by length.
				long thisLong = Long.parseLong(thisChunk);
				long thatLong = Long.parseLong(thatChunk);
				if (thisLong != thatLong) {
					// If they're different numbers, fine
					return thisLong - thatLong > 0 ? 1 : -1;
				} else {
					// They're basically the same number, but could have
					// rogue zeroes there.
					if (thisChunk.length() == thatChunk.length())
						result = 0; // same number, same size, equal
					else
						return thatChunk.length() - thisChunk.length();
				}
			} else {
				result = thisChunk.compareTo(thatChunk);
			}

			if (result != 0)
				return result;
		}

		return s1Length - s2Length;
	}

}