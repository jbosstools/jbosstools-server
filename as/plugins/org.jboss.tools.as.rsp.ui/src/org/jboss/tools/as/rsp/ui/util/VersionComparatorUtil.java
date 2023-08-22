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

/**
 * Compare version strings.
 */
public class VersionComparatorUtil {

	public static boolean isGreaterThanOrEqualTo(String actual, String test) {
		return isGreaterThan(actual, test, true);
	}

	public static boolean isGreaterThan(String actual, String test) {
		return isGreaterThan(actual, test, false);
	}

	public static boolean isLessThanOrEqualTo(String actual, String test) {
		return isLessThan(actual, test, true);
	}

	public static boolean isLessThan(String actual, String test) {
		return isLessThan(actual, test, false);
	}

	private static boolean isGreaterThan(String actual, String test, boolean ifEqual) {
		if (test == null)
			return false;
		if (actual == null)
			return true;

		String[] splitActual = actual.split("[-\\.]");
		String[] splitTest = test.split("[-\\.]");

		// Find the number of segments actually available for comparison
		int comparableSegments = splitActual.length;
		if (splitTest.length < splitActual.length)
			comparableSegments = splitTest.length;

		for (int i = 0; i < comparableSegments; i++) {
			int actualSegment = -1;
			int testSegment = -1;
			try {
				actualSegment = Integer.parseInt(splitActual[i]);
			} catch (NumberFormatException nfe) {
			}
			try {
				testSegment = Integer.parseInt(splitTest[i]);
			} catch (NumberFormatException nfe) {
			}

			if (actualSegment == -1 || testSegment == -1) {
				// one of them is not integers, so we cant compare these segments.
				return ifEqual;
			}
			if (actualSegment != testSegment) {
				return actualSegment > testSegment;
			} // else if equal, continue
		}
		return ifEqual;
	}

	private static boolean isLessThan(String actual, String test, boolean ifEqual) {
		if (test == null)
			return false;
		if (actual == null)
			return true;

		String[] splitActual = actual.split("\\.");
		String[] splitTest = test.split("\\.");

		// Find the number of segments actually available for comparison
		int comparableSegments = splitActual.length;
		if (splitTest.length < splitActual.length)
			comparableSegments = splitActual.length;

		for (int i = 0; i < comparableSegments; i++) {
			int actualSegment = -1;
			int testSegment = -1;
			try {
				actualSegment = Integer.parseInt(splitActual[i]);
			} catch (NumberFormatException nfe) {
			}
			try {
				testSegment = Integer.parseInt(splitTest[i]);
			} catch (NumberFormatException nfe) {
			}

			if (actualSegment == -1 || testSegment == -1) {
				// one of them is not integers, so we cant compare these segments.
				return ifEqual;
			}
			if (actualSegment != testSegment) {
				return actualSegment < testSegment;
			} // else if equal, continue
		}
		return ifEqual;
	}
}
