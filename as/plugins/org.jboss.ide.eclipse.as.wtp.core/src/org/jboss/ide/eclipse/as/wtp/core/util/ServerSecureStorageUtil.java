/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.wst.server.core.IServerAttributes;

public class ServerSecureStorageUtil {
    /**
	 * @since 3.0
	 */
    public static String getFromSecureStorage(String baseKey, IServerAttributes server, String key) {
        try {
        	ISecurePreferences node = getNode(baseKey, server);
            String val = node.get(key, null);
            if (val == null) {
            	return null;
            }
            return new String(EncodingUtils.decodeBase64(val));
        } catch(IOException e) {
        	return null;
        } catch (StorageException e) {
        	return null;
		}
    }

    /**
	 * @since 3.0
	 */
    public static void storeInSecureStorage(String baseKey, IServerAttributes server, String key, String val ) throws StorageException, UnsupportedEncodingException {
        ISecurePreferences node = getNode(baseKey, server);
        if( val == null )
        	node.put(key, val, true);
        else
        	node.put(key, EncodingUtils.encodeBase64(val.getBytes()), true /* encrypt */); 
    }

    private static ISecurePreferences getNode(String baseKey, IServerAttributes server) 
    		throws UnsupportedEncodingException {
		String secureKey = new StringBuilder(baseKey)
			.append(server.getName())
			.append(Path.SEPARATOR).toString();

		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		String encoded = URLEncoder.encode(secureKey, "UTF-8"); //$NON-NLS-1$
		return root.node(encoded);
    }
}
