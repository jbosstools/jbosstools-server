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
import org.jboss.ide.eclipse.as.openshift.core.internal.InternalUser;

/**
 * @author André Dietisheim
 */
public class ApplicationLogReader extends Reader {

	private static final Pattern LOG_REGEX = Pattern.compile("Tail of .+$", Pattern.MULTILINE);

	private static final long STATUS_REQUEST_DELAY = 4 * 1024;

	private IOpenshiftService service;
	private Reader logReader;
	private Application application;
	private InternalUser user;
	private String currentStatus;

	public ApplicationLogReader(Application application, InternalUser user, IOpenshiftService service) {
		this.application = application;
		this.user = user;
		this.service = service;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int charactersRead = -1;
		try {
			while (charactersRead == -1) {
				if (logReader == null) {
					this.logReader = createLogReader(requestStatus());
				}
				charactersRead = logReader.read(cbuf, off, len);
				if (charactersRead == -1) {
					this.logReader = null;
				}
				System.err.println("charactersRead = " + charactersRead);
			}
			return charactersRead;
		} catch (OpenshiftException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			return -1;
		}
	}

	private Reader createLogReader(String status) throws InterruptedException, IOException {
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

	protected String requestStatus() throws InterruptedException, OpenshiftException {
		String status = null;
		while (status == null) {
			status = service.getStatus(application.getName(), application.getCartridge(), user);
			if (isSameStatus(currentStatus, status)) {
				Thread.sleep(STATUS_REQUEST_DELAY);
				status = null;
				continue;
			}
		}
		this.currentStatus = status;
		return status;
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
