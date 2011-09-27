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

import org.jboss.ide.eclipse.as.openshift.core.internal.Application;
import org.jboss.ide.eclipse.as.openshift.core.internal.IOpenshiftService;
import org.jboss.ide.eclipse.as.openshift.core.internal.User;

/**
 * @author André Dietisheim
 */
public class ApplicationLogReader extends Reader {

	private static final Pattern LOG_REGEX = Pattern.compile("Tail of .+$", Pattern.MULTILINE);

	private static final long STATUS_REQUEST_DELAY = 4 * 1024;

	private IOpenshiftService service;
	private StringReader logReader;
	private Application application;
	private User user;
	private String currentStatus;

	public ApplicationLogReader(Application application, User user, IOpenshiftService service) {
		this.application = application;
		this.user = user;
		this.service = service;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int charactersRead = -1;
		try {
			do {
				charactersRead = readStatus(cbuf, off, len);
			} while (charactersRead == -1);
		} catch (InterruptedException e) {
			// do nothing
		}
		return charactersRead;
	}

	protected int readStatus(char[] cbuf, int off, int len) throws IOException,
			InterruptedException {
		int charactersRead = -1;
		if (logReader == null) {
			String status = requestStatus();
			if (status == null) {
				Thread.sleep(STATUS_REQUEST_DELAY);
				return -1;
			}
			this.currentStatus = status;
			this.logReader = createLogReader(status);
		}

		charactersRead = logReader.read(cbuf, off, len);
		if (charactersRead == -1) {
			this.logReader = null;
			return -1;
		}
		return charactersRead;
	}

	protected StringReader createLogReader(String status) throws IOException {
		String log = getLog(status);
		return new StringReader(log);
	}

	private String getLog(String status) throws IOException {
		Matcher matcher = LOG_REGEX.matcher(status);
		int logStart = 0;
		if (matcher.find()) {
			logStart = matcher.end() + 1;
		}
		return status.substring(logStart);
	}

	protected String requestStatus() throws IOException {
		try {
			String status = service.getStatus(application.getName(), application.getCartridge(), user);
			if (isSameStatus(status, currentStatus)) {
				status = null;
			}
			return status;

		} catch (OpenshiftException e) {
			throw new IOException(e);
		}
	}

	private boolean isSameStatus(String thisStatus, String otherStatus) {
		return otherStatus != null
				&& otherStatus.equals(thisStatus);
	}

	@Override
	public void close() throws IOException {
		if (logReader != null) {
			logReader.close();
		}
	}
}
