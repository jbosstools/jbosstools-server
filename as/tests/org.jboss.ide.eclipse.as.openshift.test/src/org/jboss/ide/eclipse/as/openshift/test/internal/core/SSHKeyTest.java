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
package org.jboss.ide.eclipse.as.openshift.test.internal.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jboss.ide.eclipse.as.openshift.core.SSHKey;
import org.junit.Test;

public class SSHKeyTest {

	private static final String PASSPHRASE = "12345";

	@Test
	public void canCreatePublicKey() throws Exception {
		String publicKeyPath = createTempFile().getAbsolutePath();
		String privateKeyPath = createTempFile().getAbsolutePath();
		SSHKey sshKey = SSHKey.create(PASSPHRASE, privateKeyPath, publicKeyPath);
		String publicKey = sshKey.getPublicKeyContent();

		assertNotNull(publicKey);
		assertTrue(!publicKey.contains("ssh-rsa")); // no identifier
		assertTrue(!publicKey.contains(" ")); // no comment
	}

	@Test
	public void canLoadPublicKey() throws Exception {
		String publicKeyPath = createTempFile().getAbsolutePath();
		String privateKeyPath = createTempFile().getAbsolutePath();
		SSHKey.create(PASSPHRASE, privateKeyPath, publicKeyPath);

		SSHKey sshKey = SSHKey.load(privateKeyPath, publicKeyPath);
		String publicKey = sshKey.getPublicKeyContent();

		assertNotNull(publicKey);
		assertTrue(!publicKey.contains("ssh-rsa")); // no identifier
		assertTrue(!publicKey.contains(" ")); // no comment
	}

	private File createTempFile() throws IOException {
		return File.createTempFile(String.valueOf(System.currentTimeMillis()), null);
	}
}
