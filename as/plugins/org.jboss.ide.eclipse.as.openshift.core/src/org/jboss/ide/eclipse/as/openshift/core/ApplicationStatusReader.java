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

/**
 * @author André Dietisheim
 */
public class ApplicationStatusReader extends Reader {

	private IOpenshiftService service;
	private Application application;
	private StringReader serviceResponseReader;

	public ApplicationStatusReader(Application application, IOpenshiftService service) {
		this.application = application;
		this.service = service;
	}

	protected String requestStatus() throws IOException {
		try {
			return service.getStatus(application.getName(), application.getCartridge());
		} catch (OpenshiftException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int charactersRead = -1;
		for (;;) {
			charactersRead = getServiceResponseReader().read(cbuf, off, len);
			if (charactersRead != -1) {
				return charactersRead;
			}
		}
	}

	private Reader getServiceResponseReader() throws IOException {
		if (serviceResponseReader == null) {
			this.serviceResponseReader = new StringReader(requestStatus());
		}
		return serviceResponseReader;

	}

	@Override
	public void close() throws IOException {
		if (serviceResponseReader != null) {
			serviceResponseReader.close();
		}
	}
}
