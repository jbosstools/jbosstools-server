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

import org.jboss.ide.eclipse.as.openshift.internal.core.OpenshiftCoreActivator;
import org.jboss.ide.eclipse.as.openshift.internal.core.utils.Base64Encoder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * @author André Dietisheim
 */
public class SSHKey {

	/**
	 * the length of the key that is created when using #create. ssh-keygen uses
	 * a default of 2048
	 * 
	 * @see #create(String, String, String)
	 * @see http://en.wikipedia.org/wiki/Ssh-keygen
	 */
	private static final int KEYLENGTH = 2048;

	private KeyPair keyPair;
	private String privateKeyPath;
	private String publicKeyPath;

	private SSHKey(KeyPair keyPair, String privateKeyPath, String publicKeyPath) throws OpenshiftException {
		this.keyPair = keyPair;
		this.privateKeyPath = privateKeyPath;
		this.publicKeyPath = publicKeyPath;
	}

	/**
	 * Creates private and public ssh-rsa keys and stores them to the given
	 * paths. The key is created while using the given pass phrase.
	 * 
	 * @param passPhrase
	 *            the pass phrase to set to the new key
	 * @param privateKeyPath
	 *            the path where the new private key gets stored
	 * @param publicKeyPath
	 *            the path where the new public key gets stored
	 * @return
	 * @throws OpenshiftException
	 *             if the key could not be created
	 */
	public static SSHKey create(String passPhrase, String privateKeyPath, String publicKeyPath)
			throws OpenshiftException {
		try {
			KeyPair keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA, KEYLENGTH);
			keyPair.setPassphrase(passPhrase);
			keyPair.writePublicKey(publicKeyPath, "created by " + OpenshiftCoreActivator.PLUGIN_ID);
			keyPair.writePrivateKey(privateKeyPath);
			return new SSHKey(keyPair, privateKeyPath, publicKeyPath);
		} catch (Exception e) {
			throw new OpenshiftException(e, "Could not create new rsa key", e);
		}
	}

	/**
	 * Loads existing private and public ssh key from the given paths.
	 * 
	 * @param privateKeyPath
	 *            the path to the private key
	 * @param publicKeyPath
	 *            the path to the public key
	 * @return
	 * @throws OpenshiftException
	 */
	public static SSHKey load(String privateKeyPath, String publicKeyPath)
			throws OpenshiftException {
		try {
			KeyPair keyPair = KeyPair.load(new JSch(), privateKeyPath, publicKeyPath);
			return new SSHKey(keyPair, privateKeyPath, publicKeyPath);
		} catch (JSchException e) {
			throw new OpenshiftException(e, "Could not create new rsa key");
		}
	}

	/**
	 * Returns the content of the public key (key content without ssh-rsa
	 * identifier nor comment) of the ssh key
	 * 
	 * @return the content of the public key (without signature, without
	 *         comment)
	 * @throws OpenshiftException
	 */
	public String getPublicKeyContent() throws OpenshiftException {
		return new String(Base64Encoder.encode(keyPair.getPublicKeyBlob()));
	}

	protected String getPublicKeyPath() {
		return publicKeyPath;
	}

	protected String getPrivateKeyPath() {
		return privateKeyPath;
	}

}
