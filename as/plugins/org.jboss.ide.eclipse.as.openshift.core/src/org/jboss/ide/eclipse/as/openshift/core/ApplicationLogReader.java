/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.openshift.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: make sure it behaves correctly on subsequent requests that get app
 * status on the server: The server would either repeat the whole log or just
 * respond with the diff.
 * 
 * 
 * @author André Dietisheim
 */
public class ApplicationLogReader extends Reader {

	private static final Pattern LOG_REGEX = Pattern.compile("Tail of .+$", Pattern.MULTILINE);

	private IOpenshiftService service;
	private StringReader logReader;
	private Application application;
	private int logIndex = 0;

	public ApplicationLogReader(Application application, IOpenshiftService service) {
		this.application = application;
		this.service = service;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int charactersRead = -1;
		if (logReader == null) {
			this.logReader = createLogReader(requestStatus());
		}
		charactersRead = logReader.read(cbuf, off, len);
		if (charactersRead != -1) {
			logIndex += charactersRead;
			return charactersRead;
		}
		this.logReader = null;
		return -1;
	}

	protected StringReader createLogReader(String status) throws IOException {
		String log = getLog(status);
		return new StringReader(log);
	}

	private String getLog(String status) throws IOException {
		Matcher matcher = LOG_REGEX.matcher(status);
		if (matcher.find()) {
			logIndex = matcher.end() + 1;
		}
		return status.substring(logIndex);
	}

	protected String requestStatus() throws IOException {
		try {
			return service.getStatus(application.getName(), application.getCartridge());
		} catch (OpenshiftException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (logReader != null) {
			logReader.close();
		}
	}
}
