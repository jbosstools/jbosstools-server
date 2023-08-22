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
package org.jboss.tools.as.rsp.ui.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.eclipse.swt.graphics.Image;
import org.jboss.tools.as.rsp.ui.RspUiActivator;
import org.jboss.tools.as.rsp.ui.model.IRsp;
import org.jboss.tools.as.rsp.ui.model.IRspCore;
import org.jboss.tools.as.rsp.ui.model.IRspStateController;
import org.jboss.tools.as.rsp.ui.model.IRspStateControllerProvider;
import org.jboss.tools.as.rsp.ui.model.IRspType;
import org.jboss.tools.as.rsp.ui.model.IServerIconProvider;
import org.jboss.tools.as.rsp.ui.model.impl.ReferenceRspControllerImpl;
import org.jboss.tools.as.rsp.ui.model.impl.RspTypeImpl;

/**
 * An abstract server connector for reference-implementation type RSPs of the
 * same type and structure as the redhat-server-connector or
 * community-server-connector
 */
public abstract class AbstractServerConnector {
	protected String name;
	protected String id;
	protected int minPort;
	protected int maxPort;
	protected String iconPath;
	protected String latestPropertiesUrl;
	protected String latestVersionKey;
	protected String latestUrlKey;

	protected AbstractServerConnector(String id, String name, int minPort, int maxPort, String iconPath,
			String latestPropertiesUrl, String latestVersionKey, String latestUrlKey) {
		this.id = id;
		this.name = name;
		this.minPort = minPort;
		this.maxPort = maxPort;
		this.iconPath = iconPath;
		this.latestPropertiesUrl = latestPropertiesUrl;
		this.latestVersionKey = latestVersionKey;
		this.latestUrlKey = latestUrlKey;
	}

	public IRsp getRsp(IRspCore core) {
		try {
			byte[] asBytes = downloadFile(latestPropertiesUrl);
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(asBytes));
			String version = props.getProperty(latestVersionKey);
			String url = props.getProperty(latestUrlKey);
			return getType(core).createRsp(version, url);
		} catch (IOException ioe) {
			return createFallbackRsp(core);
		}
	}

	// Subclasses can override with a default hard-coded 'latest' as a fallback
	protected IRsp createFallbackRsp(IRspCore core) {
		return getType(core).createRsp();
	};

	public static byte[] downloadFile(String url) throws IOException {
		URL url2 = new URL(url);
		URLConnection conn = url2.openConnection();
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		conn.connect();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		copy(conn.getInputStream(), baos);

		return baos.toByteArray();
	}

	protected Image loadIcon(String id) {
		return RspUiActivator.getDefault().getSharedImages().image(id);
	}

	protected IServerIconProvider createIconProvider() {
		return new IServerIconProvider() {
			@Override
			public Image getIcon() {
				return loadIcon(iconPath);
			}

			@Override
			public Image getIcon(String serverType) {
				return findIconForServerType(serverType);
			}
		};
	}

	protected IRspType getType(IRspCore core) {
		return new RspTypeImpl(core, id, name, createIconProvider(),
				createReferenceControllerProvider(minPort, maxPort));
	}

	protected abstract Image findIconForServerType(String serverType);

	protected IRspStateControllerProvider createReferenceControllerProvider(final int portMin, final int portMax) {
		return new IRspStateControllerProvider() {
			@Override
			public IRspStateController createController(IRspType rspServerType) {
				return new ReferenceRspControllerImpl(rspServerType, portMin, portMax);
			}
		};
	}

	final static int DEFAULT_BUFFER_SIZE = 8192;
	final static int EOF = -1;

	public static int copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		final long count = copyLarge(inputStream, outputStream);
		return count > Integer.MAX_VALUE ? EOF : (int) count;
	}

	public static long copyLarge(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
	}

	public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize)
			throws IOException {
		return copyLarge(inputStream, outputStream, byteArray(bufferSize));
	}

	public static byte[] byteArray() {
		return byteArray(DEFAULT_BUFFER_SIZE);
	}

	public static byte[] byteArray(final int size) {
		return new byte[size];
	}

	public static long copyLarge(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = inputStream.read(buffer))) {
			outputStream.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
