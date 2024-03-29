/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.ide.eclipse.as.reddeer.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.eclipse.ui.console.ConsoleView;

/**
 * Checks that the active console in console view does not contain an unexpected exception. 
 * Note that some exceptions are ignored - it is declared in {@link #EXPECTED_EXCEPTIONS_PATTERNS} constant. 
 * 
 * @author Lucia Jelinkova
 *
 */
public class ServerConsoleContainsNoExceptionMatcher extends TypeSafeMatcher<ConsoleView> {

	private static final String[] EXPECTED_EXCEPTIONS_PATTERNS = new String[]
			{".*Remote connection failed: java.io.IOException: Connection reset by peer.*",
			".*Remote connection failed: java.io.IOException: An established connection was aborted by the software in your host.*",
			".*XNIO001007: .*XNIO007007: Thread is terminating",
			".*java.io.FileNotFoundException:.*",
			".*expandExceptionLogs.*",
			".*UT005090: Unexpected failure: java.lang.IllegalStateException: UT000131: Buffer pool is closed.*", // https://issues.redhat.com/browse/JBIDE-28890
			".*XNIO001007: A channel event listener threw an exception: java.lang.IllegalStateException: UT000131: Buffer pool is closed.*"}; // https://issues.redhat.com/browse/JBIDE-28890

	private static final Logger log = Logger.getLogger(ServerConsoleContainsNoExceptionMatcher.class);

	private List<String> expectedExceptions = new ArrayList<String>();
	
	private List<String> unexpectedExceptions = new ArrayList<String>();
	
	private String actualText;

	@Override
	protected boolean matchesSafely(ConsoleView view) {
		actualText = view.getConsoleText();
		if (!actualText.contains("Exception")){
			return true;
		}
		
		findExceptionLines(actualText);
		return unexpectedExceptions.isEmpty();
	}

	private void findExceptionLines(String text) {
		unexpectedExceptions = new ArrayList<String>();
		expectedExceptions = new ArrayList<String>();

		Scanner scanner = new Scanner(text);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("Exception")){
				log.trace("Found line with exception: " + line);
				if (isExpected(line)){
					log.trace("Line will be ignored");
					expectedExceptions.add(line);
				} else {
					unexpectedExceptions.add(line);
				}
			}
		}
		scanner.close();
	}

	private boolean isExpected(String line) {
		for (String pattern : EXPECTED_EXCEPTIONS_PATTERNS){
			if(line.matches(pattern)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("console contains no unexpected exceptions '\n");
		description.appendText("Found exceptions are:\n");
		for (String exception : unexpectedExceptions){
			description.appendText("\t" + exception + "\n");
		}
		description.appendText("Found expected exceptions are:\n");
		for (String exception : expectedExceptions){
			description.appendText("\t" + exception + "\n");
		}
		description.appendText("Full console log:\n");
		description.appendText(actualText);
		description.appendText("'.");
	} 
}
